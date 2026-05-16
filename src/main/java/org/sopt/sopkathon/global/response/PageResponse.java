package org.sopt.sopkathon.global.response;

import java.util.List;
import org.springframework.data.domain.Page;

// 목록 API에서 Spring Page를 그대로 노출하지 않기 위한 공통 응답 DTO다.
// page.number는 Spring Pageable 기준이라 0부터 시작한다.
public record PageResponse<T>(
        List<T> content,
        PageInfo page
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        // Spring Page를 그대로 응답하면 내부 필드가 너무 많이 노출되어 클라이언트 계약이 흔들린다.
        return new PageResponse<>(
                page.getContent(),
                new PageInfo(
                        page.getNumber(),
                        page.getSize(),
                        page.getNumberOfElements(),
                        page.getTotalElements(),
                        page.getTotalPages(),
                        page.isFirst(),
                        page.isLast()
                )
        );
    }

    public record PageInfo(
            int number,
            int size,
            int numberOfElements,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last
    ) {
    }
}
