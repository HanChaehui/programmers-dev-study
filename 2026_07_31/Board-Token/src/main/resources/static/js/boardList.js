
$(document).ready(() => {
    checkSession();
    loadBoard(1); // 처음엔 1페이지를 보여준다

    // [추가 - QueryDSL] 검색 버튼 - 입력한 조건으로 1페이지부터 다시 조회한다
    //   (검색 조건이 바뀌면 결과 전체가 달라지므로, 보던 페이지가 아니라 1페이지부터 보여주는 게 자연스럽다)
    $('#searchBtn').on('click', () => loadBoard(1));

    // [추가 - QueryDSL] 초기화 버튼 - 조건을 비우고 전체 목록(1페이지)으로 돌아간다
    $('#searchResetBtn').on('click', () => {
        $('#searchTitle').val('');
        $('#searchUserId').val('');
        $('#searchFrom').val('');
        $('#searchTo').val('');
        loadBoard(1);
    });

    // [추가 - QueryDSL] 검색어 입력 중 Enter 키로도 검색되게 한다 (작은 편의 기능)
    $('#searchTitle, #searchUserId').on('keydown', (e) => {
        if (e.key === 'Enter') loadBoard(1);
    });
});

const PAGE_SIZE = 10; // 한 페이지에 보여줄 게시글 수

// 로그인(세션) 확인 - 로그인 정보가 없으면 로그인 페이지로 보낸다
let checkSession = () => {
    let hUserId = $('#hiddenUserId').val();

    if (hUserId == null || hUserId === '')
        window.location.href = "/members/login";
}

// 검색 폼의 입력값을 모아 "값이 있는 것만" 객체로 만든다
//   - 빈 문자열을 그대로 보내면 서버에서 빈 값 처리로 헷갈릴 수 있으니, 애초에 값이 있는 키만 담는다
//   - 이 객체가 $.ajax 의 data 에 합쳐져 ?title=..&userId=..&from=..&to=.. 쿼리 파라미터가 된다
//     (서버의 BoardSearchRequestDto 필드 이름과 키가 같아야 @ModelAttribute 로 바인딩된다)
let getSearchCondition = () => {
    const condition = {};
    const title = $('#searchTitle').val();
    const userId = $('#searchUserId').val();
    const from = $('#searchFrom').val();   // <input type="date"> 는 "2026-06-01" 형식 문자열을 준다
    const to = $('#searchTo').val();       //  → 서버의 LocalDate 필드에 그대로 바인딩되는 형식이다

    if (title) condition.title = title;
    if (userId) condition.userId = userId;
    if (from) condition.from = from;
    if (to) condition.to = to;
    return condition;
}

// 특정 페이지의 게시글 + 하단 페이지 번호를 로드하는 함수
let loadBoard = (page) => {
    $.ajax({
        type: 'GET',
        // [변경 - QueryDSL] 호출 API 교체
        //   기존: url: '/api/boards'          (단순 페이징 목록 - Board 필드만)
        //   변경: url: '/api/boards/search'   (동적 검색 + 작성자 이름(member 조인) + 댓글 수(comment 집계))
        url: '/api/boards/search',
        data: {
            page: page,
            size: PAGE_SIZE,
            // 검색 조건을 페이징 파라미터와 함께 펼쳐 보낸다
            ...getSearchCondition()
        },
        success: (response) => {
            // 응답 구조가 달라졌다
            //   기존: response.boards       (BoardListResponseDto 의 필드 이름)
            //   변경: response.content      (스프링 Page 객체의 표준 필드 이름 - 목록은 항상 content 에 담긴다)
            //   totalPages 는 Page 에도 같은 이름으로 있어서 그대로 쓴다
            renderBoards(response.content);                // 게시글 목록 그리기
            renderPagination(page, response.totalPages);   // 하단 1,2,3... 페이지 번호 그리기
        },
        error: function (error) {
            console.error('오류 발생:', error);
            alert('게시판 데이터를 불러오는데 오류가 발생했습니다.');
        }
    });
}

// 게시글 목록을 테이블에 그린다
let renderBoards = (boards) => {
    const $content = $('#boardContent');
    $content.empty(); // 기존 게시글 내용 비우기

    if (boards == null || boards.length <= 0) {
        // 게시글이 없는 경우 메시지 출력
        $content.append(
            `<tr>
                <td colspan="5" style="text-align: center;">글이 존재하지 않습니다.</td>
            </tr>`
        );
        // ↑ [변경 - QueryDSL] colspan 4 → 5 (댓글 수 컬럼이 늘어서)
        return;
    }

    boards.forEach((item) => {
        // [변경 - QueryDSL] 한 행의 구성이 달라졌다
        //   기존: 번호 / 제목 / 작성자(item.userId 아이디만) / 작성일           ← 4칸
        //   변경: 번호 / 제목 / 작성자(이름(아이디)) / 댓글 수 / 작성일          ← 5칸
        //     - item.userName     : member 조인으로 온 "이름" (예: 홍길동)
        //     - item.commentCount : comment 집계로 온 "댓글 수"
        //     - userName 이 null 일 수 있어(매칭 회원 없음 - leftJoin) 그땐 아이디만 보여준다
        const author = item.userName ? `${item.userName} (${item.userId})` : item.userId;
        const commentBadge = item.commentCount > 0
            ? `<span class="comment-count">${item.commentCount}</span>`
            : '-';

        $content.append(
            `
            <tr>
                <td>${item.id}</td>
                <td><a href="/detail?id=${item.id}">${item.title}</a></td>
                <td>${author}</td>
                <td>${commentBadge}</td>
                <td>${item.created}</td>
            </tr>
            `
        );
    });
}

// 하단에 페이지 번호(1,2,3...)를 그린다
// currentPage: 지금 보고 있는 페이지, totalPages: 전체 페이지 수(서버가 내려줌)
let renderPagination = (currentPage, totalPages) => {
    const $pagination = $('#pagination');
    $pagination.empty(); // 기존 번호 버튼 비우기

    // 1번부터 전체 페이지 수까지 버튼을 만든다
    for (let p = 1; p <= totalPages; p++) {
        const $btn = $(`<button class="btn page-btn">${p}</button>`);

        if (p === currentPage) {
            // 현재 페이지는 강조하고 클릭할 수 없게 막는다
            $btn.addClass('active');
            $btn.prop('disabled', true);
        }

        // 버튼을 누르면 그 페이지를 로드한다
        $btn.on('click', () => loadBoard(p));

        $pagination.append($btn);
    }
}