package com.example.spring.boardtoken.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BoardWithCommentsResponseDto {

    @Schema(description = "제목", example = "첫 번째 게시글입니다")
    private String title;

    @Schema(description = "본문 내용", example = "안녕하세요, 게시판 첫 글입니다.")
    private String content;

    @Schema(description = "작성자 아이디", example = "hong")
    private String userId;

    @Schema(description = "작성 일시", example = "2026-06-01 09:12")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime created;

    @Schema(description = "첨부파일 경로(없으면 null)")
    private String filePath;

    @Schema(description = "이 게시글에 달린 댓글 목록")
    private List<CommentResponseDto> comments;
}