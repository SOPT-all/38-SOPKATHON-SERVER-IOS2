package org.sopt.sopkathon.story.api;

import static org.sopt.sopkathon.global.response.ApiResponse.created;
import static org.sopt.sopkathon.global.response.ApiResponse.success;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.sopt.sopkathon.global.error.GlobalErrorResponse;
import org.sopt.sopkathon.story.application.StoryService;
import org.sopt.sopkathon.story.dto.StoryCreateRequest;
import org.sopt.sopkathon.story.dto.StoryCreateResponse;
import org.sopt.sopkathon.story.dto.StoryDetailResponse;
import org.sopt.sopkathon.story.dto.StoryListResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Story", description = "장소별 스토리 조회/작성 API")
@Validated
@RestController
@RequestMapping("/api/v1")
public class StoryController {

    private final StoryService storyService;

    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    @Operation(summary = "장소별 스토리 목록 조회", description = "특정 장소에 등록된 스토리를 최신순 또는 인기순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스토리 목록 조회 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = GlobalErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "장소가 존재하지 않는 경우",
                    content = @Content(schema = @Schema(implementation = GlobalErrorResponse.class))
            )
    })
    @GetMapping("/spots/stories")
    public org.sopt.sopkathon.global.response.ApiResponse<StoryListResponse> getStories(
            @Positive(message = "요청 유저 ID는 양수여야 합니다.") @RequestParam Long userId,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        return success(storyService.getStories(userId, sort));
    }

    @Operation(summary = "스토리 상세 조회", description = "스토리 본문, 반응별 개수, 요청 유저의 반응을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스토리 상세 조회 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = GlobalErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "스토리가 존재하지 않는 경우",
                    content = @Content(schema = @Schema(implementation = GlobalErrorResponse.class))
            )
    })
    @GetMapping("/stories/{storyId}")
    public org.sopt.sopkathon.global.response.ApiResponse<StoryDetailResponse> getStory(
            @Positive(message = "스토리 ID는 양수여야 합니다.") @PathVariable Long storyId,
            @Positive(message = "요청 유저 ID는 양수여야 합니다.") @RequestParam Long userId
    ) {
        return success(storyService.getStory(storyId, userId));
    }

    @Operation(summary = "스토리 작성", description = "특정 장소에 스토리를 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "스토리 작성 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = GlobalErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "장소가 존재하지 않는 경우",
                    content = @Content(schema = @Schema(implementation = GlobalErrorResponse.class))
            )
    })
    @PostMapping("/spots/stories")
    public ResponseEntity<org.sopt.sopkathon.global.response.ApiResponse<StoryCreateResponse>> createStory(
            @Valid @RequestBody StoryCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(created(storyService.createStory(request)));
    }
}
