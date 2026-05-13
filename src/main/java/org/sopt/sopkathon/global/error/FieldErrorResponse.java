package org.sopt.sopkathon.global.error;

public record FieldErrorResponse(
        String field,
        String rejectedValue,
        String message
) {

    public static FieldErrorResponse of(String field, Object rejectedValue, String message) {
        return new FieldErrorResponse(field, rejectedValue == null ? null : String.valueOf(rejectedValue), message);
    }
}
