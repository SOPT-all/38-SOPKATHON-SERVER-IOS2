package org.sopt.sopkathon.global.error;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sopt.sopkathon.global.web.TraceIdFilter;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 서비스/도메인에서 의도적으로 던지는 실패다.
    // 새 기능의 예상 가능한 실패는 RuntimeException 대신 BusinessException으로 던진다.
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

    // @RequestBody DTO의 @NotBlank, @Email 같은 검증 실패를 필드별 errors로 변환한다.
    // 회원가입/로그인 폼 오류처럼 프론트가 입력칸 옆에 표시해야 하는 실패가 여기로 온다.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<FieldErrorResponse> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldErrorResponse)
                .toList();

        return handleValidationException(exception, request, errors);
    }

    // 필수 query parameter가 빠진 경우다. 예: /api/v1/items?keyword=... 에서 keyword 누락.
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<GlobalErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        List<FieldErrorResponse> errors = List.of(FieldErrorResponse.of(
                exception.getParameterName(),
                null,
                "필수 요청 파라미터입니다."
        ));
        return handleValidationException(exception, request, errors);
    }

    // path variable 또는 query parameter의 타입이 맞지 않는 경우다. 예: Long id 자리에 "abc" 전달.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<GlobalErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        List<FieldErrorResponse> errors = List.of(FieldErrorResponse.of(
                exception.getName(),
                exception.getValue(),
                "요청 값 타입이 올바르지 않습니다."
        ));
        return handleValidationException(exception, request, errors);
    }

    // @RequestParam @Min(1)처럼 Controller 메서드 파라미터에 직접 붙은 제약 조건 실패다.
    // Spring이 파라미터별 검증 결과를 따로 주기 때문에 공통 FieldErrorResponse로 풀어준다.
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<GlobalErrorResponse> handleHandlerMethodValidationException(
            HandlerMethodValidationException exception,
            HttpServletRequest request
    ) {
        List<FieldErrorResponse> errors = new ArrayList<>();
        for (ParameterValidationResult result : exception.getParameterValidationResults()) {
            String field = parameterName(result.getMethodParameter());
            Object rejectedValue = result.getArgument();
            for (MessageSourceResolvable error : result.getResolvableErrors()) {
                errors.add(FieldErrorResponse.of(field, rejectedValue, error.getDefaultMessage()));
            }
        }
        return handleValidationException(exception, request, errors);
    }

    // JSON 문법이 깨졌거나 body 타입이 맞지 않는 경우다.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GlobalErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return handleKnownException(CommonErrorCode.COMMON_INVALID_JSON, exception, request);
    }

    // 지원하지 않는 HTTP 메서드다. 예: GET 전용 API에 POST 요청.
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<GlobalErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        return handleKnownException(CommonErrorCode.COMMON_METHOD_NOT_ALLOWED, exception, request);
    }

    // 매핑되는 API가 없는 경우다. 프론트가 경로를 잘못 붙였을 때 공통 404 JSON으로 내려준다.
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<GlobalErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        return handleKnownException(CommonErrorCode.COMMON_NOT_FOUND, exception, request);
    }

    // 마지막 안전망이다. 예상 못 한 예외의 상세 메시지는 클라이언트에 노출하지 않는다.
    // 실제 원인은 traceId와 함께 서버 로그에서 확인한다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalErrorResponse> handleException(Exception exception, HttpServletRequest request) {
        ErrorCode errorCode = CommonErrorCode.COMMON_INTERNAL_SERVER_ERROR;
        log(errorCode, exception, request);
        GlobalErrorResponse response = GlobalErrorResponse.of(
                errorCode,
                errorCode.message(),
                request.getRequestURI(),
                traceId(request)
        );
        return ResponseEntity.status(errorCode.httpStatus()).body(response);
    }

    // 400/404/405처럼 필드 errors가 필요 없는 Spring MVC 공통 예외를 처리한다.
    private ResponseEntity<GlobalErrorResponse> handleKnownException(
            ErrorCode errorCode,
            Exception exception,
            HttpServletRequest request
    ) {
        log(errorCode, exception, request);
        GlobalErrorResponse response = GlobalErrorResponse.of(errorCode, request.getRequestURI(), traceId(request));
        return ResponseEntity.status(errorCode.httpStatus()).body(response);
    }

    // 모든 검증 실패를 같은 code/message/status로 맞춘다.
    // 프론트는 body/query/path 중 어디가 틀렸는지와 상관없이 errors 배열만 보면 된다.
    private ResponseEntity<GlobalErrorResponse> handleValidationException(
            Exception exception,
            HttpServletRequest request,
            List<FieldErrorResponse> errors
    ) {
        ErrorCode errorCode = CommonErrorCode.COMMON_VALIDATION_FAILED;
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

    // DTO 필드 검증 실패를 클라이언트가 바로 표시할 수 있는 field/message/rejectedValue로 줄인다.
    private FieldErrorResponse toFieldErrorResponse(FieldError fieldError) {
        return FieldErrorResponse.of(
                fieldError.getField(),
                fieldError.getRejectedValue(),
                fieldError.getDefaultMessage()
        );
    }

    // Controller 파라미터 이름은 @RequestParam("name"), @PathVariable("id") 값을 우선 사용한다.
    // 어노테이션 값이 비어 있으면 Java 파라미터 이름으로 fallback한다.
    private String parameterName(MethodParameter methodParameter) {
        RequestParam requestParam = methodParameter.getParameterAnnotation(RequestParam.class);
        if (requestParam != null) {
            return annotationName(requestParam.name(), requestParam.value(), methodParameter);
        }

        PathVariable pathVariable = methodParameter.getParameterAnnotation(PathVariable.class);
        if (pathVariable != null) {
            return annotationName(pathVariable.name(), pathVariable.value(), methodParameter);
        }

        String parameterName = methodParameter.getParameterName();
        return parameterName == null ? "parameter" : parameterName;
    }

    // @RequestParam은 name/value 둘 다 alias라서 둘 중 실제로 적힌 값을 사용한다.
    private String annotationName(String name, String value, MethodParameter methodParameter) {
        if (!name.isBlank()) {
            return name;
        }
        if (!value.isBlank()) {
            return value;
        }
        String parameterName = methodParameter.getParameterName();
        return parameterName == null ? "parameter" : parameterName;
    }

    // TraceIdFilter가 만든 요청 추적 ID다. 에러 응답과 서버 로그를 연결하는 데 쓴다.
    private String traceId(HttpServletRequest request) {
        Object traceId = request.getAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE);
        return traceId == null ? null : String.valueOf(traceId);
    }

    // ErrorCode별 로그 레벨을 따른다. 500만 stack trace를 남기고, 검증 실패는 로그를 가볍게 유지한다.
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
