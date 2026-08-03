package com.example.spring.boardtoken.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BoardDeleteRequestDto {
    @Schema(description = "함께 삭제할 첨부파일 경로(없으면 비움)", example = "3f2a1b_이력서.pdf")
    private String filePath;
}