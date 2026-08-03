package com.example.spring.boardtoken.controller;


import com.example.spring.boardtoken.config.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.spring.boardtoken.domain.entity.Board;
import com.example.spring.boardtoken.dto.*;
import com.example.spring.boardtoken.mapper.BoardMapper;
import com.example.spring.boardtoken.service.BoardService;
import com.example.spring.boardtoken.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Tag( name = "게시글 API", description = "게시글 목록/상세 조회, 작성, 수정, 삭제, 첨부파일 다운로드" )
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
public class BoardApiController {

    private final BoardService boardService;
    private final FileService fileService;
    private final BoardMapper boardMapper;

    @Operation(
            summary = "게시글 목록 조회",
            description = "페이지 단위로 게시글 목록을 조회한다. 목록(boards)과 마지막 페이지 여부(last), 전체 페이지 수(totalPages)를 함께 돌려준다."
    )
    @GetMapping
    public BoardListResponseDto getBoardList(
            @Parameter( description = "조회할 페이지 번호 (1부터 시작)", example = "1" )
            @RequestParam(defaultValue = "1") int page,
            @Parameter( description = "한 페이지에 담을 게시글 수", example = "10" )
            @RequestParam(defaultValue = "10") int size
    ) {
        // 게시글 목록
        List<Board> boards = boardService.getBoardList(page, size);

        // 전체 게시글 수 가져오기
        int totalBoards = boardService.getTotalBoards();

        // 전체 페이지 수 계산
        int totalPages = (int) Math.ceil((double) totalBoards / size);

        // 마지막 페이 여부
        boolean last = page >= totalPages;

        return BoardListResponseDto.builder()
                .boards(boards)
                .last(last)
                .totalPages(totalPages)
                .build();
    }

    @Operation(
            summary = "게시글 작성",
            description = "로그인 사용자가 제목, 내용, 첨부파일을 입력하여 게시글을 작성한다."
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void saveBoard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute BoardWriteRequestDto dto
    ) {
        boardService.saveBoard(
                userDetails.getUsername(),
                dto.getTitle(),
                dto.getContent(),
                dto.getFile()
        );
    }

    @Operation( summary = "게시글 상세 조회", description = "id로 게시글 한 건의 상세 내용을 조회한다." )
    @ApiResponses({
            @ApiResponse( responseCode = "200", description = "게시글 상세 조회 성공" ),
            @ApiResponse( responseCode = "404", description = "게시글 상세 조회 실패 - 없음",
                    content = @Content( schema = @Schema(implementation = ErrorResponseDto.class) )
            )
    })
    @GetMapping("/{id}")
    public BoardDetailResponseDto getBoardDetail(
            @Parameter( description = "조회할 게시글 id", example = "1" )
            @PathVariable long id
    ) {
        Board boardDetail = boardService.getBoardDetail(id);
        return BoardDetailResponseDto.builder()
                .title(boardDetail.getTitle())
                .content(boardDetail.getContent())
                .filePath(boardDetail.getFilePath())
                .created(boardDetail.getCreated())
                .userId(boardDetail.getUserId())
                .build();
    }

    @Operation(summary = "첨부파일 다운로드",
            description = "저장된 파일 이름으로 첨부파일을 내려받는다. Content-Disposition: attachment 로 브라우저가 다운로드하게 한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "파일 다운로드",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "해당 이름의 파일이 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/file/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "서버에 저장된 파일 이름(UUID 포함)", example = "3f2a1b_이력서.pdf")
            @PathVariable String fileName
    ) {
        Resource resource = fileService.downloadFile(fileName);

        String encodedFileName = URLEncoder.encode(resource.getFilename(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }

    @Operation(
            summary = "게시글 수정",
            description = "로그인한 작성자 본인의 게시글만 수정할 수 있다."
    )
    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public void updateBoard(
            @Parameter(
                    description = "수정할 게시글 ID",
                    example = "1"
            )
            @PathVariable long id,

            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @ModelAttribute
            BoardUpdateRequestDto dto
    ) {
        boardService.updateBoard(
                id,
                userDetails.getUsername(),
                dto
        );
    }

    @Operation(
            summary = "게시글 삭제",
            description = "로그인한 작성자 본인의 게시글만 삭제할 수 있다."
    )
    @DeleteMapping("/{id}")
    public void deleteBoard(
            @Parameter(
                    description = "삭제할 게시글 ID",
                    example = "1"
            )
            @PathVariable long id,

            @AuthenticationPrincipal
            CustomUserDetails userDetails
    ) {
        boardService.deleteBoard(
                id,
                userDetails.getUsername()
        );
    }

    @Operation(
            summary = "게시글 검색(QueryDSL)",
            description = "제목/작성자/작성기간으로 동적 검색한다. 작성자 이름(member)과 댓글 수(comment)를 함께 내려준다."
    )
    @GetMapping("/search")
    public Page<BoardListItemResponseDto> searchBoards(
            @ModelAttribute BoardSearchRequestDto dto,
            @Parameter( description = "조회할 페이지 번호 (1부터 시작)", example = "1" )
            @RequestParam(defaultValue = "1") int page,
            @Parameter( description = "한 페이지에 담을 게시글 수", example = "10" )
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return boardService.searchBoards(dto, pageable);
    }

    @Operation(summary = "게시글 상세 + 댓글",
            description = "게시글 한 건과 그에 달린 댓글 목록을 fetch join 으로 한 번에 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 id 의 게시글이 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @GetMapping("/{id}/with-comments")
    public BoardWithCommentsResponseDto getBoardWithComments(
            @Parameter(description = "조회할 게시글 id", example = "1")
            @PathVariable long id
    ) {
        Board board = boardService.getBoardWithComments(id);
        return boardMapper.toBoardWithCommentsResponseDto(board);
    }

    @Operation(summary = "작성자별 게시글 수 통계",
    description = "작성별로 게시글 수를 집계하고(group by), minCount 편 이상 쓴 작성자만(having) 많이 쓴 순으로 내려준다.")
    @GetMapping("/stats/authors")
    public List<BoardAuthorStatsResponseDto> getAuthors(
            @Parameter(description = "최소 게시글 수 (이 값 이상 쓴 작성자만)", example = "1")
            @RequestParam(defaultValue = "1") long minCount
    ) {
        return boardService.getAuthorStats(minCount);
    }

}