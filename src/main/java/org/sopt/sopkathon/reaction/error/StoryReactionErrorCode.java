package org.sopt.sopkathon.reaction.error;

import org.sopt.sopkathon.global.error.ErrorCode;
import org.sopt.sopkathon.global.error.ErrorLogLevel;
import org.springframework.http.HttpStatus;

public enum StoryReactionErrorCode implements ErrorCode {
    STORY_REACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "STORY_REACTION_NOT_FOUND", "반응을 찾을 수 없습니다.", ErrorLogLevel.WARN),
    STORY_REACTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "STORY_REACTION_ALREADY_EXISTS", "이미 반응한 썰입니다.", ErrorLogLevel.WARN);

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    private final ErrorLogLevel logLevel;

    StoryReactionErrorCode(HttpStatus httpStatus, String code, String message, ErrorLogLevel logLevel) {
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
