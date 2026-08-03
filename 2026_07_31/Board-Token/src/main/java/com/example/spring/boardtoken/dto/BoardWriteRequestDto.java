package com.example.spring.boardtoken.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class BoardWriteRequestDto {

    @Schema(
            description = "게시글 제목",
            example = "첫 글입니다."
    )
    private String title;

    @Schema(
            description = "게시글 내용",
            example = "안녕하세요. 반갑습니다."
    )
    private String content;

    @Schema(
            description = "첨부파일 (선택)",
            type = "string",
            format = "binary"
    )
    private MultipartFile file;
}