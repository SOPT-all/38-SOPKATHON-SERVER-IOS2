package org.sopt.sopkathon.comment.error;

import org.sopt.sopkathon.global.error.ErrorCode;
import org.sopt.sopkathon.global.error.ErrorLogLevel;
import org.springframework.http.HttpStatus;

public enum CommentErrorCode implements ErrorCode {
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다.", ErrorLogLevel.WARN);

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    private final ErrorLogLevel logLevel;

    CommentErrorCode(HttpStatus httpStatus, String code, String message, ErrorLogLevel logLevel) {
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
