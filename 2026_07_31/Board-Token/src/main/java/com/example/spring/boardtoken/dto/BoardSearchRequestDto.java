package com.example.spring.boardtoken.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class BoardSearchRequestDto {

    @Schema(description = "제목 키워드 (부분 일치 검색)", example = "스프링")
    private String title;
    @Schema(description = "작성자 아이디 (정확히 일치)", example = "hong")
    private String userId;
    @Schema(description = "작성일 시작 (이 날짜 00:00부터)", example = "2026-01-01")
    private LocalDate from;
    @Schema(description = "작성일 끝 (이 날짜 23:59:59까지)", example = "2026-06-30")
    private LocalDate to;
}
