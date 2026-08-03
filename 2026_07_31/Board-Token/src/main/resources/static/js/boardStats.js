
$(document).ready(() => {
    checkSession();
    loadStats(); // 처음엔 전체(1편 이상) 작성자를 보여준다

    // 적용 버튼 - 입력한 최소 게시글 수로 다시 조회한다
    $('#applyBtn').on('click', () => loadStats());

    // 숫자 입력 중 Enter 키로도 적용되게 한다
    $('#minCount').on('keydown', (e) => {
        if (e.key === 'Enter') loadStats();
    });
});

// 로그인(세션) 확인 - 로그인 정보가 없으면 로그인 페이지로 보낸다
let checkSession = () => {
    let hUserId = $('#hiddenUserId').val();

    if (hUserId == null || hUserId === '')
        window.location.href = "/members/login";
}

// 통계 데이터를 로드하는 함수 - minCount 는 서버 쿼리의 having 조건이 된다
let loadStats = () => {
    // 입력값이 비었거나 1 미만이면 1로 보정한다 (음수/0 을 보내는 실수 방지)
    let minCount = parseInt($('#minCount').val());
    if (isNaN(minCount) || minCount < 1) {
        minCount = 1;
        $('#minCount').val(1);
    }

    $.ajax({
        type: 'GET',
        url: '/api/boards/stats/authors',
        data: {
            minCount: minCount
        },
        success: (response) => {
            // 응답은 배열이다: [{ userId, userName, boardCount }, ...] (게시글 수 내림차순 정렬됨)
            renderStats(response);
        },
        error: (error) => {
            console.error('오류 발생:', error);
            alert('통계 데이터를 불러오는데 오류가 발생했습니다.');
        }
    });
}

// 통계 목록을 테이블에 그린다
let renderStats = (stats) => {
    const $content = $('#statsContent');
    $content.empty(); // 기존 내용 비우기

    if (stats == null || stats.length <= 0) {
        // 조건에 맞는 작성자가 없는 경우 메시지 출력
        $content.append(
            `<tr>
                <td colspan="3" style="text-align: center;">조건에 맞는 작성자가 없습니다.</td>
            </tr>`
        );
        return;
    }

    stats.forEach((item, index) => {
        // 서버가 게시글 수 내림차순으로 정렬해 주므로, 배열 순서(index)가 곧 순위다
        const rank = index + 1;

        // 작성자 표시: 이름(아이디). 매칭 회원이 없으면(leftJoin) 이름이 null 이라 아이디만 보여준다
        const author = item.userName ? `${item.userName} (${item.userId})` : item.userId;

        // 1~3위는 배지로 강조한다
        const rankBadge = rank <= 3
            ? `<span class="rank-badge rank-${rank}">${rank}</span>`
            : rank;

        $content.append(
            `
            <tr>
                <td>${rankBadge}</td>
                <td>${author}</td>
                <td><span class="board-count">${item.boardCount}</span></td>
            </tr>
            `
        );
    });
}