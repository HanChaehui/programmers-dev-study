package com.example.spring.boardtoken.service;

import com.example.spring.boardtoken.domain.entity.Board;
import com.example.spring.boardtoken.domain.repository.BoardRepository;
import com.example.spring.boardtoken.dto.BoardAuthorStatsResponseDto;
import com.example.spring.boardtoken.dto.BoardListItemResponseDto;
import com.example.spring.boardtoken.dto.BoardSearchRequestDto;
import com.example.spring.boardtoken.dto.BoardUpdateRequestDto;
import com.example.spring.boardtoken.exception.BoardNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
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
        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by("id").descending()
        );

        return boardRepository.findAll(pageable).getContent();
    }

    public int getTotalBoards() {
        return (int) boardRepository.count();
    }

    @Transactional
    public void saveBoard(
            String userId,
            String title,
            String content,
            MultipartFile file
    ) {
        String filePath = fileService.storeFile(file);

        Board board = Board.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .filePath(filePath)
                .created(LocalDateTime.now())
                .build();

        boardRepository.save(board);
    }

    public Board getBoardDetail(long id) {
        return boardRepository.findById(id)
                .orElseThrow(
                        () -> new BoardNotFoundException(
                                "[BOARD] 게시글을 찾을 수 없습니다. id : " + id
                        )
                );
    }

    @Transactional
    public void updateBoard(
            long id,
            String loginUserId,
            BoardUpdateRequestDto dto
    ) {
        Board board = boardRepository.findById(id)
                .orElseThrow(
                        () -> new BoardNotFoundException(
                                "[BOARD] 수정할 게시글을 찾을 수 없습니다. id : " + id
                        )
                );

        validateOwner(board, loginUserId);

        String filePath = board.getFilePath();

        if (dto.isFileFlag()) {
            String newFilePath = fileService.storeFile(dto.getFile());

            fileService.deleteFile(filePath);

            filePath = newFilePath;
        }

        board.update(
                dto.getTitle(),
                dto.getContent(),
                filePath
        );
    }

    @Transactional
    public void deleteBoard(
            long id,
            String loginUserId
    ) {
        Board board = boardRepository.findById(id)
                .orElseThrow(
                        () -> new BoardNotFoundException(
                                "[BOARD] 삭제할 게시글을 찾을 수 없습니다. id : " + id
                        )
                );

        validateOwner(board, loginUserId);

        String filePath = board.getFilePath();

        boardRepository.delete(board);

        fileService.deleteFile(filePath);
    }

    public Page<BoardListItemResponseDto> searchBoards(
            BoardSearchRequestDto dto,
            Pageable pageable
    ) {
        return boardRepository.searchBoards(dto, pageable);
    }

    public Board getBoardWithComments(Long id) {
        return boardRepository.findWithComments(id)
                .orElseThrow(
                        () -> new BoardNotFoundException(
                                "게시글을 찾을 수 없습니다. id = " + id
                        )
                );
    }

    public List<BoardAuthorStatsResponseDto> getAuthorStats(
            long minCount
    ) {
        return boardRepository.countBoardsByAuthor(minCount);
    }

    private void validateOwner(
            Board board,
            String loginUserId
    ) {
        if (!board.getUserId().equals(loginUserId)) {
            throw new AccessDeniedException(
                    "작성자만 게시글을 수정하거나 삭제할 수 있습니다."
            );
        }
    }
}