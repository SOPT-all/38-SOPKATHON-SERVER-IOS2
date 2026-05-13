package org.sopt.sopkathon.global.error;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopt.sopkathon.global.web.TraceIdFilter;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<GlobalErrorResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = exception.errorCode();
        log(errorCode, exception, request);
        GlobalErrorResponse response = GlobalErrorResponse.of(
                errorCode,
                exception.getMessage(),
                request.getRequestURI(),
                traceId(request)
        );
        return ResponseEntity.status(errorCode.httpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.COMMON_VALIDATION_FAILED;
        List<FieldErrorResponse> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldErrorResponse)
                .toList();

        log(errorCode, exception, request);
        GlobalErrorResponse response = GlobalErrorResponse.of(
                errorCode,
                errorCode.message(),
                request.getRequestURI(),
                traceId(request),
                errors
        );
        return ResponseEntity.status(errorCode.httpStatus()).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GlobalErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return handleKnownException(ErrorCode.COMMON_INVALID_JSON, exception, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<GlobalErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        return handleKnownException(ErrorCode.COMMON_METHOD_NOT_ALLOWED, exception, request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<GlobalErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        return handleKnownException(ErrorCode.COMMON_NOT_FOUND, exception, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalErrorResponse> handleException(Exception exception, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.COMMON_INTERNAL_SERVER_ERROR;
        log(errorCode, exception, request);
        GlobalErrorResponse response = GlobalErrorResponse.of(
                errorCode,
                errorCode.message(),
                request.getRequestURI(),
                traceId(request)
        );
        return ResponseEntity.status(errorCode.httpStatus()).body(response);
    }

    private ResponseEntity<GlobalErrorResponse> handleKnownException(
            ErrorCode errorCode,
            Exception exception,
            HttpServletRequest request
    ) {
        log(errorCode, exception, request);
        GlobalErrorResponse response = GlobalErrorResponse.of(errorCode, request.getRequestURI(), traceId(request));
        return ResponseEntity.status(errorCode.httpStatus()).body(response);
    }

    private FieldErrorResponse toFieldErrorResponse(FieldError fieldError) {
        return FieldErrorResponse.of(
                fieldError.getField(),
                fieldError.getRejectedValue(),
                fieldError.getDefaultMessage()
        );
    }

    private String traceId(HttpServletRequest request) {
        Object traceId = request.getAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE);
        return traceId == null ? null : String.valueOf(traceId);
    }

    private void log(ErrorCode errorCode, Exception exception, HttpServletRequest request) {
        String message = "Handled exception. code={}, path={}, traceId={}";
        String traceId = traceId(request);
        switch (errorCode.logLevel()) {
            case INFO -> log.info(message, errorCode.code(), request.getRequestURI(), traceId);
            case WARN -> log.warn(message, errorCode.code(), request.getRequestURI(), traceId);
            case ERROR -> log.error(message, errorCode.code(), request.getRequestURI(), traceId, exception);
        }
    }
}
