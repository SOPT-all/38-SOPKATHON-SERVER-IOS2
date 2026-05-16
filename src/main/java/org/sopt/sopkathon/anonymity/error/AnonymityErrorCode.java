package org.sopt.sopkathon.anonymity.error;

import org.sopt.sopkathon.global.error.ErrorCode;
import org.sopt.sopkathon.global.error.ErrorLogLevel;
import org.springframework.http.HttpStatus;

public enum AnonymityErrorCode implements ErrorCode {
    ANONYMITY_NOT_FOUND(HttpStatus.NOT_FOUND, "ANONYMITY_NOT_FOUND", "익명 댓글 작성자를 찾을 수 없습니다.", ErrorLogLevel.WARN),
    ANONYMITY_ALREADY_EXISTS(HttpStatus.CONFLICT, "ANONYMITY_ALREADY_EXISTS", "이미 익명 번호가 발급된 사용자입니다.", ErrorLogLevel.WARN);

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    private final ErrorLogLevel logLevel;

    AnonymityErrorCode(HttpStatus httpStatus, String code, String message, ErrorLogLevel logLevel) {
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
