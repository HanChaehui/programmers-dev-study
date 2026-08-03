package com.example.spring.boardtoken.service;

import com.example.spring.boardtoken.domain.entity.Board;
import com.example.spring.boardtoken.domain.repository.BoardRepository;
import com.example.spring.boardtoken.dto.*;
import com.example.spring.boardtoken.exception.BoardNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final FileService fileService;

    public List<Board> getBoardList(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
        return boardRepository.findAll(pageable).getContent();
    }

    public int getTotalBoards() {
        return (int) boardRepository.count();
    }

    @Transactional
    public void saveBoard( String userId, String title, String content, MultipartFile file ) {

        String filePath = fileService.storeFile(file);

        boardRepository.save(
                Board.builder()
                        .userId(userId)
                        .title(title)
                        .content(content)
                        .filePath(filePath)
                        .created(LocalDateTime.now())
                        .build()
        );
    }

    public Board getBoardDetail(long id) {
        return boardRepository.findById(id)
                .orElseThrow( () -> new BoardNotFoundException("[BOARD] 게시글을 찾을 수 없습니다. id : " + id));
    }

    @Transactional
    public void updateBoard(long id, BoardUpdateRequestDto dto) {
        Board board = boardRepository.findById(id)
                .orElseThrow(
                        () -> new BoardNotFoundException("[BOARD] 수정할 게시글을 찾을 수 없습니다. id : " + id)
                );

        String filePath = board.getFilePath();
        if ( dto.isFileFlag() ) { // 파일 변경이 있었을 경우
            fileService.deleteFile(filePath); // 기존 파일 삭제
            filePath = fileService.storeFile(dto.getFile()); // 새 파일 저장
        }

        board.update( dto.getTitle(), dto.getContent(), filePath );
    }

    @Transactional
    public void deleteBoard(long id, BoardDeleteRequestDto dto) {

        if ( !boardRepository.existsById(id) ) {
            throw new BoardNotFoundException("[BOARD] 삭제할 게시글을 찾을 수 없습니다. id : " + id);
        }

        boardRepository.deleteById(id);
        fileService.deleteFile(dto.getFilePath());
    }

    public Page<BoardListItemResponseDto> searchBoards(BoardSearchRequestDto dto, Pageable pageable) {
        return boardRepository.searchBoards(dto, pageable);
    }

    public Board getBoardWithComments(Long id) {
        return boardRepository.findWithComments(id)
                .orElseThrow(
                        () -> new BoardNotFoundException("게시글을 찾을 수 없습니다. id = " + id)
                );
    }

    public List<BoardAuthorStatsResponseDto> getAuthorStats(long minCount) {
        return boardRepository.countBoardsByAuthor(minCount);
    }

}