$(document).ready(async () => {
    const user = await loadAuthenticatedUser();

    if (user == null) {
        return;
    }

    loadStats();

    $('#applyBtn').on('click', () => {
        loadStats();
    });

    $('#minCount').on('keydown', (event) => {
        if (event.key === 'Enter') {
            loadStats();
        }
    });
});

let loadStats = () => {
    let minCount = parseInt(
        $('#minCount').val()
    );

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
            renderStats(response);
        },
        error: (error) => {
            console.error('오류 발생:', error);
            alert('통계 데이터를 불러오는데 오류가 발생했습니다.');
        }
    });
};

let renderStats = (stats) => {
    const $content = $('#statsContent');

    $content.empty();

    if (stats == null || stats.length === 0) {
        $content.append(`
            <tr>
                <td colspan="3" style="text-align: center;">
                    조건에 맞는 작성자가 없습니다.
                </td>
            </tr>
        `);

        return;
    }

    stats.forEach((item, index) => {
        const rank = index + 1;

        const author = item.userName
            ? `${item.userName} (${item.userId})`
            : item.userId;

        const rankBadge = rank <= 3
            ? `<span class="rank-badge rank-${rank}">${rank}</span>`
            : rank;

        $content.append(`
            <tr>
                <td>${rankBadge}</td>
                <td>${author}</td>
                <td>
                    <span class="board-count">
                        ${item.boardCount}
                    </span>
                </td>
            </tr>
        `);
    });
};