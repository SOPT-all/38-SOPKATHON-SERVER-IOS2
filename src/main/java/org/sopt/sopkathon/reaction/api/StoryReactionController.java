package org.sopt.sopkathon.reaction.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.sopt.sopkathon.global.response.ApiResponse;
import org.sopt.sopkathon.reaction.application.StoryReactionService;
import org.sopt.sopkathon.reaction.dto.StoryReactionRequest;
import org.sopt.sopkathon.reaction.dto.StoryReactionResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.sopt.sopkathon.global.response.ApiResponse.success;

@Tag(name = "Story Reaction", description = "스토리 반응 API")
@Validated
@RestController
@RequestMapping("/api/v1/stories/{storyId}/reactions")
public class StoryReactionController {

    private final StoryReactionService storyReactionService;

    public StoryReactionController(StoryReactionService storyReactionService) {
        this.storyReactionService = storyReactionService;
    }

    @Operation(summary = "스토리 반응 처리", description = "반응 추가, 취소, 전환을 하나의 API에서 처리합니다.")
    @PostMapping
    public ApiResponse<StoryReactionResponse> react(
            @PathVariable @Positive(message = "storyId는 양수여야 합니다.") Long storyId,
            @Valid @RequestBody StoryReactionRequest request
    ) {
        return success(storyReactionService.react(storyId, request));
    }
}
