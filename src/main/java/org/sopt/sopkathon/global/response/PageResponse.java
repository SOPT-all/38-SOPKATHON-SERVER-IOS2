package org.sopt.sopkathon.global.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponse<T>(
        List<T> content,
        PageInfo page
) {

    public static <T> PageResponse<T> from(Page<T> page) {
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
