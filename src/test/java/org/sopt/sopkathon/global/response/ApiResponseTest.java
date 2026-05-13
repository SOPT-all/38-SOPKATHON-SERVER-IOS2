package org.sopt.sopkathon.global.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @Test
    @DisplayName("success(data)는 성공 여부, 표준 코드, 기본 메시지, 데이터를 담는다")
    void successWithData() {
        ApiResponse<String> response = ApiResponse.success("ok");

        assertThat(response.success()).isTrue();
        assertThat(response.code()).isEqualTo("SUCCESS_200");
        assertThat(response.message()).isEqualTo("요청이 성공했습니다.");
        assertThat(response.data()).isEqualTo("ok");
    }

    @Test
    @DisplayName("created(data)는 생성 성공 코드와 데이터를 담는다")
    void createdWithData() {
        ApiResponse<Long> response = ApiResponse.created(1L);

        assertThat(response.success()).isTrue();
        assertThat(response.code()).isEqualTo("SUCCESS_201");
        assertThat(response.message()).isEqualTo("리소스가 생성되었습니다.");
        assertThat(response.data()).isEqualTo(1L);
    }

    @Test
    @DisplayName("noContent()는 삭제/수정 성공처럼 응답 데이터가 없을 때 사용할 수 있다")
    void noContent() {
        ApiResponse<Void> response = ApiResponse.noContent();

        assertThat(response.success()).isTrue();
        assertThat(response.code()).isEqualTo("SUCCESS_204");
        assertThat(response.message()).isEqualTo("요청이 성공했고 응답 데이터가 없습니다.");
        assertThat(response.data()).isNull();
    }
}
