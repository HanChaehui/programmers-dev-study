package com.example.spring.boardtoken.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class BoardUpdateRequestDto {
    @Schema(description = "수정할 제목", example = "제목을 고쳤습니다")
    private String title;
    @Schema(description = "수정할 내용", example = "내용도 고쳤습니다")
    private String content;
    @Schema(type = "string", format = "binary", description = "새로 올릴 파일 (교체할 때만). fileFlag=true 이면서 비어 있으면 기존 파일 제거로 처리")
    private MultipartFile file; // 새로 올린 파일(교체할 때만 값이 있음)
    @Schema(description = "첨부파일을 건드렸는지 여부(true=교체 또는 제거, false=기존 유지)", example = "false")
    private boolean fileFlag; // 첨부파일을 변경했는지 여부

}