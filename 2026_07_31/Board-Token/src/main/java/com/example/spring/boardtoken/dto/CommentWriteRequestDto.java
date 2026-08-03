package com.example.spring.boardtoken.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommentWriteRequestDto {

    @Schema(description = "댓글 작성자 아이디", example = "hong")
    private String userId;

    @Schema(description = "댓글 내용", example = "좋은 글 감사합니다.")
    private String content;

}