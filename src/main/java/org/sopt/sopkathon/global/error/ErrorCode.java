package org.sopt.sopkathon.global.error;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    HttpStatus httpStatus();

    String code();

    String message();

    ErrorLogLevel logLevel();
}
