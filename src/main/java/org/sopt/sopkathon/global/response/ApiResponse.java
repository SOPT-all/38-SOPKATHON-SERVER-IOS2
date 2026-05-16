package org.sopt.sopkathon.global.response;

public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data
) {

    private static final String SUCCESS_CODE = "SUCCESS_200";
    private static final String CREATED_CODE = "SUCCESS_201";
    private static final String NO_CONTENT_CODE = "SUCCESS_204";

    // 일반 조회/수정 성공에 사용한다. 클라이언트는 success/code/message/data 모양을 항상 기대한다.
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, SUCCESS_CODE, "요청이 성공했습니다.", data);
    }

    // 생성 API는 HTTP 201과 함께 이 factory를 사용한다.
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, CREATED_CODE, "리소스가 생성되었습니다.", data);
    }

    // HTTP 204 자체가 아니라 data가 없는 성공 body다. 실제 204가 필요하면 ResponseEntity.noContent()를 쓴다.
    public static ApiResponse<Void> noContent() {
        return new ApiResponse<>(true, NO_CONTENT_CODE, "요청이 성공했고 응답 데이터가 없습니다.", null);
    }
}
