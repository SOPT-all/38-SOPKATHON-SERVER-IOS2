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

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, SUCCESS_CODE, "요청이 성공했습니다.", data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, CREATED_CODE, "리소스가 생성되었습니다.", data);
    }

    public static ApiResponse<Void> noContent() {
        return new ApiResponse<>(true, NO_CONTENT_CODE, "요청이 성공했고 응답 데이터가 없습니다.", null);
    }
}
