package org.sopt.sopkathon.global.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class PageResponseTest {

    @Test
    @DisplayName("Page 객체를 클라이언트 친화적인 페이징 응답으로 변환한다")
    void fromPage() {
        Page<String> page = new PageImpl<>(
                List.of("a", "b"),
                PageRequest.of(1, 2),
                5
        );

        PageResponse<String> response = PageResponse.from(page);

        assertThat(response.content()).containsExactly("a", "b");
        assertThat(response.page().number()).isEqualTo(1);
        assertThat(response.page().size()).isEqualTo(2);
        assertThat(response.page().numberOfElements()).isEqualTo(2);
        assertThat(response.page().totalElements()).isEqualTo(5);
        assertThat(response.page().totalPages()).isEqualTo(3);
        assertThat(response.page().first()).isFalse();
        assertThat(response.page().last()).isFalse();
    }
}
