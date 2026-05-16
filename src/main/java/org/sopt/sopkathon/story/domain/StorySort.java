package org.sopt.sopkathon.story.domain;

import java.util.Arrays;
import org.sopt.sopkathon.global.error.BusinessException;
import org.sopt.sopkathon.global.error.CommonErrorCode;

public enum StorySort {
    LATEST("latest"),
    POPULAR("popular");

    private final String value;

    StorySort(String value) {
        this.value = value;
    }

    public static StorySort from(String value) {
        return Arrays.stream(values())
                .filter(sort -> sort.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        CommonErrorCode.COMMON_BAD_REQUEST,
                        "지원하지 않는 정렬 기준입니다."
                ));
    }
}
