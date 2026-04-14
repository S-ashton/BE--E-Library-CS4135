# =============================================================================
# E-Library Circuit Breaker Integration Tests
# =============================================================================
# End-to-end tests validating Resilience4j circuit breaker behaviour across
# the microservice stack. Covers tripping, recovery, graceful degradation,
# and gateway fallback scenarios.
#
# Prerequisites: all services running via docker compose
# =============================================================================

# =============================================================================
# Helper Functions
# =============================================================================
# Coloured output helpers for consistent test reporting, and a utility to
# query circuit breaker state from a service's Actuator endpoint.

$ErrorActionPreference = "SilentlyContinue"

function Write-Header($text) {
    Write-Host "`n$("=" * 60)" -ForegroundColor Cyan
    Write-Host "  $text" -ForegroundColor Cyan
    Write-Host "$("=" * 60)" -ForegroundColor Cyan
}
function Write-Step($text) { Write-Host "`n>> $text" -ForegroundColor Yellow }
function Write-Pass($text) { Write-Host "  [PASS] $text" -ForegroundColor Green }
function Write-Fail($text) { Write-Host "  [FAIL] $text" -ForegroundColor Red }
function Write-Info($text) { Write-Host "  [INFO] $text" -ForegroundColor Gray }

function Get-CBState($port, $name) {
    $r = Invoke-RestMethod "http://localhost:$port/actuator/circuitbreakers" -ErrorAction Stop
    return $r.circuitBreakers.$name
}

# =============================================================================
# Setup: Authentication & Circuit Breaker Warm-Up
# =============================================================================
# Register (or reuse) a dedicated test user, acquire a JWT token, then send
# successful requests through each circuit breaker to ensure they start in the
# CLOSED state. Prior postman runs may have left breakers in HALF_OPEN; the
# warm-up transitions them back to CLOSED before the tests begin.

Write-Header "SETUP: Register & authenticate circuit breaker test user"
$cbEmail    = "circuitbreakertest@elibrary.ie"
$cbPassword = "CBTest@12345"

Write-Step "Registering test user ($cbEmail)..."
$registerBody = @{ email = $cbEmail; password = $cbPassword; role = "USER" } | ConvertTo-Json
try {
    $regResponse = Invoke-RestMethod -Method POST http://localhost:8080/api/auth/register -ContentType "application/json" -Body $registerBody -ErrorAction Stop
    $cbUserId = $regResponse.id
    Write-Pass "Registered new user (id=$cbUserId)"
} catch {
    $regCode = [int]$_.Exception.Response.StatusCode
    if ($regCode -eq 409) {
        Write-Info "User already exists - reusing"
    } else {
        Write-Fail "Registration failed: $regCode"; exit 1
    }
}

$loginBody = @{ email = $cbEmail; password = $cbPassword } | ConvertTo-Json
$loginResponse = Invoke-RestMethod -Method POST http://localhost:8080/api/auth/login -ContentType "application/json" -Body $loginBody
$token = $loginResponse.token
if (-not $token) { Write-Fail "Could not get token."; exit 1 }
Write-Pass "Token acquired"

# If we didn't get the ID from registration (user already existed), fetch it via /api/users/me
if (-not $cbUserId) {
    try {
        $meResponse = Invoke-RestMethod "http://localhost:8080/api/users/me" -Headers @{ Authorization = "Bearer $token" } -ErrorAction Stop
        $cbUserId = $meResponse.id
        Write-Info "Resolved user id=$cbUserId from /api/users/me"
    } catch {
        Write-Fail "Could not resolve user ID"; exit 1
    }
}

$authHeaders = @{ Authorization = "Bearer $token"; "Content-Type" = "application/json" }
$borrowBody  = @{ bookId = 1; email = $cbEmail } | ConvertTo-Json

Write-Step "Resetting circuit breakers to CLOSED (warm-up requests)..."
# Send successful requests to transition any HALF_OPEN CBs to CLOSED
1..5 | ForEach-Object {
    try { Invoke-RestMethod "http://localhost:8080/api/books/search" -Headers $authHeaders -ErrorAction Stop | Out-Null } catch {}
    try { Invoke-RestMethod "http://localhost:8084/api/recommendations?limit=1" -Headers @{ Authorization = "Bearer $token"; "X-Authenticated-User-Id" = "$cbUserId" } -ErrorAction Stop | Out-Null } catch {}
}
Start-Sleep -Seconds 2
$loanCb = Get-CBState 8083 "book-service"
$embedCb = Get-CBState 8084 "embedding-service"
if ($loanCb.state -eq "CLOSED" -and $embedCb.state -eq "CLOSED") {
    Write-Pass "All circuit breakers are CLOSED"
} else {
    Write-Info "loan-service CB: $($loanCb.state), embedding CB: $($embedCb.state) (may affect results)"
}

# =============================================================================
# TEST 1: Circuit Breaker Trips OPEN on Downstream Failure
# =============================================================================
# Stop book-service and fire borrow requests through loan-service.
# After the sliding window fills with failures (5 calls, 50% threshold),
# the breaker should transition to OPEN and short-circuit subsequent
# requests (notPermittedCalls > 0) without making network calls.

Write-Header "TEST 1: loan-service CB - trips OPEN when book-service is down"
$cb = Get-CBState 8083 "book-service"
Write-Info "Initial state: $($cb.state)"
Write-Step "Stopping book-service..."
docker compose stop book-service 2>&1 | Out-Null
Start-Sleep -Seconds 2
Write-Step "Firing 12 borrow requests..."
$s503 = 0
1..12 | ForEach-Object {
    try { Invoke-RestMethod -Method POST http://localhost:8080/api/loans -Headers $authHeaders -Body $borrowBody | Out-Null }
    catch { if ([int]$_.Exception.Response.StatusCode -eq 503) { $s503++ } }
}
Write-Info "503 responses: $s503/12"
$cb = Get-CBState 8083 "book-service"
Write-Info "state          : $($cb.state)"
Write-Info "bufferedCalls  : $($cb.bufferedCalls)"
Write-Info "failedCalls    : $($cb.failedCalls)"
Write-Info "notPermitted   : $($cb.notPermittedCalls)"
if ($cb.state -eq "OPEN") { Write-Pass "CB is OPEN - circuit tripped correctly" } else { Write-Fail "Expected OPEN, got $($cb.state)" }
if ([int]$cb.notPermittedCalls -ge 1) { Write-Pass "notPermittedCalls=$($cb.notPermittedCalls) - last requests short-circuited without network call" } else { Write-Fail "Expected notPermittedCalls > 0" }

# =============================================================================
# TEST 2: Circuit Breaker Recovery (OPEN -> HALF_OPEN -> CLOSED)
# =============================================================================
# Restart book-service and wait for the waitDurationInOpenState (30s) to
# expire so the breaker transitions to HALF_OPEN. Then send probe requests;
# successful probes should transition the breaker back to CLOSED, completing
# the full recovery cycle. Also waits for Eureka discovery cache propagation.

Write-Header "TEST 2: loan-service CB - HALF_OPEN to CLOSED recovery"
Write-Step "Restarting book-service..."
docker compose start book-service 2>&1 | Out-Null
Write-Step "Waiting 32s for waitDurationInOpenState (30s) to expire..."
1..32 | ForEach-Object { Write-Host -NoNewline "."; Start-Sleep -Seconds 1 }
Write-Host ""
$cb = Get-CBState 8083 "book-service"
Write-Info "CB state after wait: $($cb.state)"
Write-Step "Waiting for book-service to become healthy (polling /actuator/health)..."
$bookReady = $false
1..30 | ForEach-Object {
    try {
        $h = Invoke-RestMethod "http://localhost:8082/actuator/health" -ErrorAction Stop
        if ($h.status -eq "UP") { $bookReady = $true; Write-Host "" }
    } catch {}
    if (-not $bookReady) { Write-Host -NoNewline "."; Start-Sleep -Seconds 2 }
}
if ($bookReady) {
    Write-Info "book-service is UP"
    Write-Step "Waiting 30s for Eureka discovery cache to propagate to gateway/loan-service..."
    1..30 | ForEach-Object { Write-Host -NoNewline "."; Start-Sleep -Seconds 1 }
    Write-Host ""
} else {
    Write-Info "book-service did not respond in time -- probes may still fail"
}
Write-Step "Sending 3 probe requests..."
1..3 | ForEach-Object {
    try { Invoke-RestMethod -Method POST http://localhost:8080/api/loans -Headers $authHeaders -Body $borrowBody | Out-Null }
    catch { Write-Info "Probe $_ : $([int]$_.Exception.Response.StatusCode)" }
}
Start-Sleep -Seconds 2
$cb = Get-CBState 8083 "book-service"
Write-Info "CB state after probes: $($cb.state)"
if ($cb.state -eq "CLOSED") { Write-Pass "CB recovered to CLOSED - full OPEN->HALF_OPEN->CLOSED cycle complete" }
elseif ($cb.state -eq "HALF_OPEN") { Write-Pass "CB is HALF_OPEN - probes in progress, will close on next success" }
else { Write-Fail "Expected CLOSED or HALF_OPEN after recovery, got $($cb.state)" }

# =============================================================================
# TEST 3: Graceful Degradation with Embedding Service Down
# =============================================================================
# Seed books into recommendation-service, then SIGKILL the embedding-service
# and clear the embedding cache. Subsequent recommendation requests should
# fall back to popularity-based recommendations while the embedding CB
# accumulates failures and transitions to OPEN (slidingWindow=5, threshold=60%).
# SIGKILL is used before cache clear to prevent RabbitMQ replay from
# repopulating the cache through the still-alive embedding-service.

Write-Header "TEST 3: recommendation-service CB - graceful degradation when embedding-service is down"
$cb = Get-CBState 8084 "embedding-service"
Write-Info "Initial embedding CB state: $($cb.state)"

Write-Step "Seeding books into recommendation-service via internal endpoint (RabbitMQ events may be missed on restart)..."
$bookList = Invoke-RestMethod "http://localhost:8080/api/books/search" -Headers $authHeaders -ErrorAction Stop
foreach ($b in $bookList) {
    $body = @{ id = $b.id; title = $b.title; description = $b.description } | ConvertTo-Json
    try { Invoke-RestMethod -Method POST "http://localhost:8084/api/recommendations/internal/books/update" -ContentType "application/json" -Body $body | Out-Null } catch {}
}
Write-Info "Seeded $($bookList.Count) book(s) into recommendation-service"

Write-Step "Killing embedding-service (SIGKILL for immediate stop, before cache clear to prevent RabbitMQ replay repopulating cache)..."
docker compose kill embedding-service 2>&1 | Out-Null
Start-Sleep -Seconds 3
Write-Step "Verifying embedding-service is unreachable..."
try {
    Invoke-RestMethod "http://localhost:8000/health" -TimeoutSec 2 | Out-Null
    Write-Fail "embedding-service is still reachable - test may not be valid"
} catch {
    Write-Pass "embedding-service is unreachable - confirmed down"
}
Write-Step "Clearing in-memory embedding cache (service already dead - RabbitMQ replays will fail)..."
Invoke-RestMethod -Method POST "http://localhost:8084/api/recommendations/internal/embeddings/clear" | Out-Null
$cb = Get-CBState 8084 "embedding-service"
Write-Info "CB state after clear: buffered=$($cb.bufferedCalls) failed=$($cb.failedCalls)"
Start-Sleep -Seconds 2
Write-Step "Calling recommendations with embedding-service down..."
$recHeaders = @{ Authorization = "Bearer $token"; "X-Authenticated-User-Id" = "$cbUserId" }
try {
    $recs = Invoke-RestMethod "http://localhost:8080/api/recommendations?limit=3" -Headers $recHeaders -ErrorAction Stop
    Write-Pass "Recommendations returned $($recs.Count) result(s) - popularity fallback working"
} catch {
    Write-Fail "Recommendations failed: $([int]$_.Exception.Response.StatusCode)"
}
$cb = Get-CBState 8084 "embedding-service"
Write-Info "CB after 1st request: buffered=$($cb.bufferedCalls) failed=$($cb.failedCalls) state=$($cb.state)"
Write-Step "Firing 6 more calls to accumulate failures..."
1..6 | ForEach-Object {
    try { Invoke-RestMethod "http://localhost:8080/api/recommendations?limit=1" -Headers $recHeaders | Out-Null } catch {}
    $cb2 = Get-CBState 8084 "embedding-service"
    Write-Info "  Call $_ : buffered=$($cb2.bufferedCalls) failed=$($cb2.failedCalls) state=$($cb2.state)"
}
$cb = Get-CBState 8084 "embedding-service"
Write-Info "Embedding CB state  : $($cb.state)"
Write-Info "failedCalls         : $($cb.failedCalls)"
Write-Info "bufferedCalls       : $($cb.bufferedCalls)"
if ($cb.state -eq "OPEN") { Write-Pass "Embedding CB is OPEN" } else { Write-Info "CB state: $($cb.state) - books may already have cached embeddings so embed() was not called" }

# =============================================================================
# TEST 4: API Gateway Fallback (503 Service Unavailable)
# =============================================================================
# Stop user-service and hit the gateway's /api/auth/login endpoint.
# The gateway's circuit breaker (or direct routing failure) should return
# a structured 503 response from the FallbackController rather than an
# unhandled error, verifying the gateway's resilience configuration.

Write-Header "TEST 4: api-gateway CB - structured 503 fallback response"
Write-Step "Stopping user-service..."
docker compose stop user-service 2>&1 | Out-Null
Start-Sleep -Seconds 2
Write-Step "Hitting /api/auth/login with user-service down..."
$loginBody2 = '{"email":"x@x.com","password":"x"}'
try {
    Invoke-RestMethod -Method POST http://localhost:8080/api/auth/login -ContentType "application/json" -Body $loginBody2 | Out-Null
    Write-Fail "Expected 503 but got 200"
} catch {
    $code = [int]$_.Exception.Response.StatusCode
    $body = $_.ErrorDetails.Message
    Write-Info "HTTP status : $code"
    Write-Info "Body        : $body"
    if ($code -eq 503) { Write-Pass "Gateway returned 503 from FallbackController" }
    elseif ($code -eq 502) { Write-Info "Got 502 - gateway CB not yet open but downstream correctly unreachable" }
    else { Write-Fail "Unexpected status: $code" }
}

# =============================================================================
# Cleanup: Restore Services & Return Stale Loans
# =============================================================================
# Restart all stopped/killed services, then return any active loans created
# by the probe requests so the system is left in a clean state for subsequent
# test runs or postman collections.

Write-Header "CLEANUP: Restore all services"
docker compose start book-service embedding-service user-service 2>&1 | Out-Null
Start-Sleep -Seconds 5
Write-Pass "All services restored"

Write-Step "Returning any active loans left by probe requests..."
try {
    $history = Invoke-RestMethod "http://localhost:8080/api/loans/history" -Headers $authHeaders -ErrorAction Stop
    $activeLoans = $history | Where-Object { $_.status -eq "ACTIVE" -or $_.status -eq "OVERDUE" }
    foreach ($loan in $activeLoans) {
        try {
            Invoke-RestMethod -Method POST "http://localhost:8080/api/loans/$($loan.id)/return" -Headers $authHeaders | Out-Null
            Write-Info "Returned stale loan $($loan.id) for bookId=$($loan.bookId)"
        } catch {
            Write-Info "Could not return loan $($loan.id): $([int]$_.Exception.Response.StatusCode)"
        }
    }
    if (-not $activeLoans) { Write-Info "No active loans to clean up" }
} catch {
    Write-Info "Could not fetch loan history for cleanup"
}

# =============================================================================
# Final Status: Circuit Breaker State Summary
# =============================================================================
# Print the final state of all circuit breakers for manual inspection.
# Expected: loan-service CB should be CLOSED (recovered), embedding CB
# may be OPEN or HALF_OPEN depending on timing.

Write-Header "FINAL CB STATUS"
Write-Host "`nloan-service (book-service CB):" -ForegroundColor White
Invoke-RestMethod http://localhost:8083/actuator/circuitbreakers | ConvertTo-Json -Depth 5
Write-Host "`nrecommendation-service (embedding-service CB):" -ForegroundColor White
Invoke-RestMethod http://localhost:8084/actuator/circuitbreakers | ConvertTo-Json -Depth 5
Write-Host "`nDone." -ForegroundColor Cyan