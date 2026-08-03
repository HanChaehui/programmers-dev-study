package com.example.spring.boardtoken.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BoardListItemResponseDto {

    @Schema(description = "게시글 id", example = "1")
    private Long id;

    @Schema(description = "제목", example = "첫 번째 게시글입니다.")
    private String title;

    @Schema(description = "작성자 아이디", example = "hong")
    private String userId;

    @Schema(description = "작성자 이름", example = "홍길동")
    private String userName;

    @Schema(description = "댓글 수", example = "3")
    private Long commentCount;

    @Schema(description = "작성 일시", example = "2026-06-01 09:17")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime created;

}