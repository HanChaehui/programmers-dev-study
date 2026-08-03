package com.example.spring.boardtoken.domain.repository;

import com.example.spring.boardtoken.domain.entity.Board;
import com.example.spring.boardtoken.dto.BoardAuthorStatsResponseDto;
import com.example.spring.boardtoken.dto.BoardListItemResponseDto;
import com.example.spring.boardtoken.dto.BoardSearchRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BoardRepositoryCustom {

    Page<BoardListItemResponseDto> searchBoards(BoardSearchRequestDto condition, Pageable pageable);

    Optional<Board> findWithComments(Long id);

    List<BoardAuthorStatsResponseDto> countBoardsByAuthor(long minCount);
}