package org.sopt.sopkathon.global.error;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorCodeArchitectureTest {

    @Test
    @DisplayName("ErrorCode는 도메인별 에러 enum이 구현할 공통 계약이다")
    void errorCodeIsSharedContract() {
        assertThat(ErrorCode.class).isInterface();
    }
}
