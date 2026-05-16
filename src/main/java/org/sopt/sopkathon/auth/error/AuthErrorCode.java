package org.sopt.sopkathon.auth.error;

import org.sopt.sopkathon.global.error.ErrorCode;
import org.sopt.sopkathon.global.error.ErrorLogLevel;
import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements ErrorCode {
    AUTH_INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "AUTH_INVALID_CREDENTIALS",
            "이메일 또는 비밀번호가 올바르지 않습니다.",
            ErrorLogLevel.WARN
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    private final ErrorLogLevel logLevel;

    AuthErrorCode(HttpStatus httpStatus, String code, String message, ErrorLogLevel logLevel) {
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
