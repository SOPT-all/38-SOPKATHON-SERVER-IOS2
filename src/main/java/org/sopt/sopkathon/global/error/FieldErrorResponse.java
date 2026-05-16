package org.sopt.sopkathon.global.error;

import java.util.Locale;

public record FieldErrorResponse(
        String field,
        String rejectedValue,
        String message
) {

    private static final String MASKED_REJECTED_VALUE = "[MASKED]";
    private static final String[] SENSITIVE_FIELD_KEYWORDS = {
            "password",
            "token",
            "secret",
            "credential",
            "authorization"
    };

    public static FieldErrorResponse of(String field, Object rejectedValue, String message) {
        String displayRejectedValue = rejectedValue == null ? null : String.valueOf(rejectedValue);
        if (isSensitiveField(field) && displayRejectedValue != null) {
            displayRejectedValue = MASKED_REJECTED_VALUE;
        }
        return new FieldErrorResponse(field, displayRejectedValue, message);
    }

    private static boolean isSensitiveField(String field) {
        if (field == null) {
            return false;
        }
        String lowerCaseField = field.toLowerCase(Locale.ROOT);
        for (String keyword : SENSITIVE_FIELD_KEYWORDS) {
            if (lowerCaseField.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
