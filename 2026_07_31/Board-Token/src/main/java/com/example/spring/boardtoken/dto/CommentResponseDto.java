package com.example.spring.boardtoken.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponseDto {

    @Schema(description = "댓글 아이디", example = "1")
    private Long id;

    @Schema(description = "작성자 아이디", example = "hong")
    private String userId;

    @Schema(description = "댓글 내용", example = "좋은 글 감사합니다.")
    private String content;

    @Schema(description = "작성 일시", example = "2026-06-01 09:12")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime created;

}