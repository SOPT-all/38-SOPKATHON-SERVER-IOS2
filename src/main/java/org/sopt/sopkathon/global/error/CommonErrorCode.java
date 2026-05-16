package org.sopt.sopkathon.global.error;

import org.springframework.http.HttpStatus;

public enum CommonErrorCode implements ErrorCode {
    COMMON_BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다.", ErrorLogLevel.WARN),
    COMMON_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "COMMON_VALIDATION_FAILED", "요청 값 검증에 실패했습니다.", ErrorLogLevel.WARN),
    COMMON_INVALID_JSON(HttpStatus.BAD_REQUEST, "COMMON_INVALID_JSON", "요청 본문을 읽을 수 없습니다.", ErrorLogLevel.WARN),
    COMMON_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404", "요청한 리소스를 찾을 수 없습니다.", ErrorLogLevel.INFO),
    COMMON_METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_405", "지원하지 않는 HTTP 메서드입니다.", ErrorLogLevel.WARN),
    COMMON_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류가 발생했습니다.", ErrorLogLevel.ERROR);

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    private final ErrorLogLevel logLevel;

    CommonErrorCode(HttpStatus httpStatus, String code, String message, ErrorLogLevel logLevel) {
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
