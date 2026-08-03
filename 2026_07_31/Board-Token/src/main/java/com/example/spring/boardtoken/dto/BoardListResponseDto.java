package com.example.spring.boardtoken.dto;

import com.example.spring.boardtoken.domain.entity.Board;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BoardListResponseDto {
    private List<Board> boards;
    private boolean last;
    private int totalPages;
}
