package com.example.spring.boardtoken.service;

import com.example.spring.boardtoken.domain.entity.Board;
import com.example.spring.boardtoken.domain.entity.Comment;
import com.example.spring.boardtoken.domain.repository.BoardRepository;
import com.example.spring.boardtoken.domain.repository.CommentRepository;
import com.example.spring.boardtoken.dto.CommentWriteRequestDto;
import com.example.spring.boardtoken.exception.BoardNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void addComment(Long boardId, CommentWriteRequestDto dto) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("게시글을 찾을 수 없습니다. id = " + boardId));

        Comment comment = Comment.builder()
                .content(dto.getContent())
                .userId(dto.getUserId())
                .board(board)
                .created(LocalDateTime.now())
                .build();

        commentRepository.save(comment);
        log.info("댓글 등록 : commentId = {}, boardId = {}, userId = {}", comment.getId(), boardId, dto.getUserId());
    }

}