package org.sopt.sopkathon.global.security;

import org.sopt.sopkathon.global.error.ErrorCode;
import org.sopt.sopkathon.global.error.ErrorLogLevel;
import org.springframework.http.HttpStatus;

public enum SecurityErrorCode implements ErrorCode {
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_401", "인증이 필요합니다.", ErrorLogLevel.WARN),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_403", "접근 권한이 없습니다.", ErrorLogLevel.WARN),
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다.", ErrorLogLevel.WARN);

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    private final ErrorLogLevel logLevel;

    SecurityErrorCode(HttpStatus httpStatus, String code, String message, ErrorLogLevel logLevel) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
        this.logLevel = logLevel;
    }

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public ErrorLogLevel logLevel() {
        return logLevel;
    }
}
