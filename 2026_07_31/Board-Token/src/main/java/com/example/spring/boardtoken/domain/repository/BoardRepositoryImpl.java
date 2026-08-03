package com.example.spring.boardtoken.domain.repository;

import com.example.spring.boardtoken.domain.entity.Board;
import com.example.spring.boardtoken.domain.entity.QBoard;
import com.example.spring.boardtoken.domain.entity.QComment;
import com.example.spring.boardtoken.domain.entity.QUser;
import com.example.spring.boardtoken.dto.BoardAuthorStatsResponseDto;
import com.example.spring.boardtoken.dto.BoardListItemResponseDto;
import com.example.spring.boardtoken.dto.BoardSearchRequestDto;
import com.example.spring.boardtoken.dto.QBoardAuthorStatsResponseDto;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QBoard board = QBoard.board;
    private static final QComment comment = QComment.comment;
    private static final QUser user = QUser.user;

    @Override
    public Page<BoardListItemResponseDto> searchBoards(BoardSearchRequestDto condition, Pageable pageable) {

        List<BoardListItemResponseDto> content = queryFactory.select(
                        Projections.constructor(
                                BoardListItemResponseDto.class,
                                board.id,
                                board.title,
                                board.userId,
                                user.userName,
                                commentCountOf(board), // 서브쿼리
                                board.created
                        )
                )
                .from(board)
                .leftJoin(user).on(board.userId.eq(user.userId))
                .where(
                        titleContains(condition.getTitle()),
                        userIdEquals(condition.getUserId()),
                        createdGoe(condition.getFrom()),
                        createdLoe(condition.getTo())
                )
                .orderBy(board.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(board.count())
                .from(board)
                .where(
                        titleContains(condition.getTitle()),
                        userIdEquals(condition.getUserId()),
                        createdGoe(condition.getFrom()),
                        createdLoe(condition.getTo())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Optional<Board> findWithComments(Long id) {
        Board result = queryFactory
                .selectFrom(board)
                .leftJoin(board.comments, comment).fetchJoin()
                .where(board.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<BoardAuthorStatsResponseDto> countBoardsByAuthor(long minCount) {
        return queryFactory
                .select(new QBoardAuthorStatsResponseDto(
                        board.userId,
                        user.userName,
                        board.count()
                ))
                .from(board)
                .leftJoin(user).on(board.userId.eq(user.userId))
                .groupBy(board.userId, user.userName)
                .having(board.count().goe(minCount))
                .orderBy(board.count().desc())
                .fetch();
    }

    // 제목 부분 일치 (Like %title%). 빈 값이면 조건 없음(null)
    private BooleanExpression titleContains(String title) {
        return (title == null || title.isBlank()) ? null : board.title.contains(title);
    }

    // 작성자 아이디 정확히 일치. 빈 값이면 조건 없음(null)
    private BooleanExpression userIdEquals(String userId) {
        return (userId == null || userId.isBlank()) ? null : board.userId.eq(userId);
    }

    private BooleanExpression createdGoe(LocalDate from) {
        return from == null ? null : board.created.goe(from.atStartOfDay());
    }

    private BooleanExpression createdLoe(LocalDate to) {
        return to == null ? null : board.created.loe(to.atTime(LocalTime.MAX));
    }

    private Expression<Long> commentCountOf(QBoard board) {
        return JPAExpressions
                .select(comment.count())
                .from(comment)
                .where(comment.board.id.eq(board.id));
    }

}