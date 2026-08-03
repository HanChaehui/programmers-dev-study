package com.example.spring.boardtoken.controller;

import com.example.spring.boardtoken.config.security.CustomUserDetails;
import com.example.spring.boardtoken.dto.CommentWriteRequestDto;
import com.example.spring.boardtoken.service.CommentService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "댓글 API",
        description = "게시글 댓글 관련 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards/{boardId}/comments")
public class CommentApiController {

    private final CommentService commentService;

    @PostMapping
    public void addComment(
            @Parameter(
                    description = "댓글을 작성할 게시글 ID",
                    example = "1"
            )
            @PathVariable long boardId,

            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @RequestBody
            CommentWriteRequestDto dto
    ) {
        commentService.addComment(
                boardId,
                userDetails.getUsername(),
                dto
        );
    }
}