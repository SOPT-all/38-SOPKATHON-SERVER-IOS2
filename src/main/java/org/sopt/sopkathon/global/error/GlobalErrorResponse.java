package org.sopt.sopkathon.global.error;

import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;

public record GlobalErrorResponse(
        boolean success,
        int status,
        String code,
        String message,
        String path,
        String traceId,
        Instant timestamp,
        List<FieldErrorResponse> errors
) {

    public static GlobalErrorResponse of(ErrorCode errorCode, String path, String traceId) {
        return of(errorCode, errorCode.message(), path, traceId, List.of());
    }

    public static GlobalErrorResponse of(ErrorCode errorCode, String message, String path, String traceId) {
        return of(errorCode, message, path, traceId, List.of());
    }

    public static GlobalErrorResponse of(
            ErrorCode errorCode,
            String message,
            String path,
            String traceId,
            List<FieldErrorResponse> errors
    ) {
        HttpStatus status = errorCode.httpStatus();
        return new GlobalErrorResponse(
                false,
                status.value(),
                errorCode.code(),
                message,
                path,
                traceId,
                Instant.now(),
                errors == null ? List.of() : List.copyOf(errors)
        );
    }
}
