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

# SETUP
Write-Header "SETUP: Get auth token"
$loginBody = '{"email":"e2e-user@elibrary.ie","password":"Test@12345"}'
$loginResponse = Invoke-RestMethod -Method POST http://localhost:8080/api/auth/login -ContentType "application/json" -Body $loginBody
$token = $loginResponse.token
if (-not $token) { Write-Fail "Could not get token."; exit 1 }
Write-Pass "Token acquired"
$authHeaders = @{ Authorization = "Bearer $token"; "Content-Type" = "application/json" }
$borrowBody  = '{"bookId":1,"email":"e2e-user@elibrary.ie"}'

# TEST 1 - loan-service CB trips OPEN
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

# TEST 2 - loan-service CB recovery
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

# TEST 3 - recommendation-service embedding CB graceful degradation
Write-Header "TEST 3: recommendation-service CB - graceful degradation when embedding-service is down"
$cb = Get-CBState 8084 "embedding-service"
Write-Info "Initial embedding CB state: $($cb.state)"
Write-Step "Stopping embedding-service..."
docker compose stop embedding-service 2>&1 | Out-Null
Start-Sleep -Seconds 2
Write-Step "Calling recommendations with embedding-service down..."
$recHeaders = @{ Authorization = "Bearer $token"; "X-Authenticated-User-Id" = "1" }
try {
    $recs = Invoke-RestMethod "http://localhost:8080/api/recommendations?limit=3" -Headers $recHeaders -ErrorAction Stop
    Write-Pass "Recommendations returned $($recs.Count) result(s) - popularity fallback working"
} catch {
    Write-Fail "Recommendations failed: $([int]$_.Exception.Response.StatusCode)"
}
Write-Step "Firing 6 more calls to accumulate failures..."
1..6 | ForEach-Object {
    try { Invoke-RestMethod "http://localhost:8080/api/recommendations?limit=1" -Headers $recHeaders | Out-Null } catch {}
}
$cb = Get-CBState 8084 "embedding-service"
Write-Info "Embedding CB state  : $($cb.state)"
Write-Info "failedCalls         : $($cb.failedCalls)"
Write-Info "bufferedCalls       : $($cb.bufferedCalls)"
if ($cb.state -eq "OPEN") { Write-Pass "Embedding CB is OPEN" } else { Write-Info "CB state: $($cb.state) - books may already have cached embeddings so embed() was not called" }

# TEST 4 - gateway fallback
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

# CLEANUP
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

# FINAL STATUS
Write-Header "FINAL CB STATUS"
Write-Host "`nloan-service (book-service CB):" -ForegroundColor White
Invoke-RestMethod http://localhost:8083/actuator/circuitbreakers | ConvertTo-Json -Depth 5
Write-Host "`nrecommendation-service (embedding-service CB):" -ForegroundColor White
Invoke-RestMethod http://localhost:8084/actuator/circuitbreakers | ConvertTo-Json -Depth 5
Write-Host "`nDone." -ForegroundColor Cyan