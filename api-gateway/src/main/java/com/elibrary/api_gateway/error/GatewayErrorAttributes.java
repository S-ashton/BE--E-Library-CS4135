package com.elibrary.api_gateway.error;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

@Component
public class GatewayErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable error = getError(request);
        HttpStatus status = resolveStatus(error);

        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("timestamp", Instant.now().toString());
        attributes.put("status", status.value());
        attributes.put("error", status.getReasonPhrase());
        attributes.put("message", resolveMessage(status));
        attributes.put("path", request.path());
        attributes.put("requestId", request.exchange().getRequest().getId());
        return attributes;
    }

    private HttpStatus resolveStatus(Throwable error) {
        if (hasCause(error, TimeoutException.class) || hasCause(error, ReadTimeoutException.class)) {
            return HttpStatus.GATEWAY_TIMEOUT;
        }

        if (hasCause(error, ConnectException.class)
                || hasCause(error, ConnectTimeoutException.class)
                || hasCause(error, UnknownHostException.class)) {
            return HttpStatus.BAD_GATEWAY;
        }

        if (error instanceof NoResourceFoundException) {
            return HttpStatus.NOT_FOUND;
        }

        if (error instanceof ResponseStatusException responseStatusException) {
            HttpStatusCode statusCode = responseStatusException.getStatusCode();
            HttpStatus status = HttpStatus.resolve(statusCode.value());
            if (status != null) {
                return status;
            }
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveMessage(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> "Route not found";
            case BAD_GATEWAY -> "Downstream service unavailable";
            case GATEWAY_TIMEOUT -> "Downstream service timed out";
            default -> "Gateway request failed";
        };
    }

    private boolean hasCause(Throwable error, Class<? extends Throwable> type) {
        Throwable current = error;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
