package org.sopt.sopkathon.story.api;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.sopkathon.global.error.GlobalExceptionHandler;
import org.sopt.sopkathon.global.web.TraceIdFilter;
import org.sopt.sopkathon.reaction.domain.ReactionType;
import org.sopt.sopkathon.story.application.StoryService;
import org.sopt.sopkathon.story.domain.StoryType;
import org.sopt.sopkathon.story.dto.StoryCreateRequest;
import org.sopt.sopkathon.story.dto.StoryCreateResponse;
import org.sopt.sopkathon.story.dto.StoryDetailResponse;
import org.sopt.sopkathon.story.dto.StoryListResponse;
import org.sopt.sopkathon.story.dto.StorySummaryResponse;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class StoryControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private StoryService storyService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        storyService = mock(StoryService.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new StoryController(storyService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .addFilters(new TraceIdFilter())
                .build();
    }

    @Test
    @DisplayName("장소별 스토리 목록 API는 공통 성공 응답으로 목록을 반환한다")
    void getStories() throws Exception {
        StoryListResponse response = new StoryListResponse(List.of(
                new StorySummaryResponse(
                        1L,
                        "성수동 카페거리",
                        "한강밤",
                        "한강에서 밤새고...",
                        StoryType.MEMORY,
                        ReactionType.LIKE,
                        3L
                )
        ));
        given(storyService.getStories(1L, "latest")).willReturn(response);

        mockMvc.perform(get("/api/v1/spots/stories")
                        .param("userId", "1")
                        .param("sort", "latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS_200"))
                .andExpect(jsonPath("$.data.stories", hasSize(1)))
                .andExpect(jsonPath("$.data.stories[0].storyId").value(1))
                .andExpect(jsonPath("$.data.stories[0].myReactionType").value("LIKE"));
    }

    @Test
    @DisplayName("스토리 상세 API는 반응 수와 요청 유저 반응을 반환한다")
    void getStory() throws Exception {
        StoryDetailResponse response = new StoryDetailResponse(
                1L,
                "성수동 카페거리",
                1L,
                "핀고",
                "한강밤",
                "스토리 본문입니다.",
                StoryType.MEMORY,
                Map.of(
                        ReactionType.LIKE, 12L,
                        ReactionType.EMPATHY, 3L,
                        ReactionType.SURPRISE, 0L,
                        ReactionType.SAD, 1L
                ),
                ReactionType.LIKE,
                3L,
                Instant.parse("2026-05-17T12:00:00Z")
        );
        given(storyService.getStory(1L, 1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/stories/{storyId}", 1L)
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS_200"))
                .andExpect(jsonPath("$.data.storyId").value(1))
                .andExpect(jsonPath("$.data.reactionCounts.LIKE").value(12))
                .andExpect(jsonPath("$.data.myReactionType").value("LIKE"));
    }

    @Test
    @DisplayName("스토리 작성 API는 생성 상태와 생성된 스토리 ID를 반환한다")
    void createStory() throws Exception {
        given(storyService.createStory(any(StoryCreateRequest.class))).willReturn(new StoryCreateResponse(1L));

        StoryCreateRequest request = new StoryCreateRequest(
                1L,
                "한강밤",
                "스토리 본문입니다.",
                StoryType.MEMORY
        );

        mockMvc.perform(post("/api/v1/spots/stories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS_201"))
                .andExpect(jsonPath("$.data.storyId").value(1));
    }

    @Test
    @DisplayName("스토리 작성 API는 제목이 5글자를 넘으면 검증 실패를 반환한다")
    void createStoryValidationFailed() throws Exception {
        StoryCreateRequest request = new StoryCreateRequest(
                1L,
                "여섯글자제목",
                "스토리 본문입니다.",
                StoryType.MEMORY
        );

        mockMvc.perform(post("/api/v1/spots/stories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field").value("title"));
    }
}
