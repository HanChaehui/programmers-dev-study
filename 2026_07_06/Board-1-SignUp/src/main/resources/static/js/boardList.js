
$(document).ready(() => {
    checkSession();
    loadBoard(1); // 처음엔 1페이지를 보여준다
});

const PAGE_SIZE = 10; // 한 페이지에 보여줄 게시글 수

// 로그인(세션) 확인 - 로그인 정보가 없으면 로그인 페이지로 보낸다
let checkSession = () => {
    let hUserId = $('#hiddenUserId').val();

    if (hUserId == null || hUserId === '')
        window.location.href = "/members/login";
}

// 특정 페이지의 게시글 + 하단 페이지 번호를 로드하는 함수
let loadBoard = (page) => {
    $.ajax({
        type: 'GET',
        url: '/api/boards',
        data: {
            page: page,
            size: PAGE_SIZE
        },
        success: (response) => {
            renderBoards(response.boards);                 // 게시글 목록 그리기
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
                <td colspan="4" style="text-align: center;">글이 존재하지 않습니다.</td>
            </tr>`
        );
        return;
    }

    boards.forEach((item) => {
        $content.append(
            `
            <tr>
                <td>${item.id}</td>
                <td><a href="/detail?id=${item.id}">${item.title}</a></td>
                <td>${item.userId}</td>
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