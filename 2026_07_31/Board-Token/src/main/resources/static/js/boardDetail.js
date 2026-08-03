let loginUser = null;

$(document).ready(async () => {
    loginUser = await loadAuthenticatedUser();

    if (loginUser == null) {
        return;
    }

    loadBoardDetail();
});

let editArticle = () => {
    const boardId = $('#hiddenId').val();

    window.location.href = '/update/' + boardId;
};

let deleteArticle = () => {
    const boardId = $('#hiddenId').val();

    $.ajax({
        type: 'DELETE',
        url: '/api/boards/' + boardId,
        success: () => {
            alert('게시글이 성공적으로 삭제되었습니다.');
            window.location.href = '/';
        },
        error: (error) => {
            console.error('오류 발생:', error);

            if (error.status === 403) {
                alert('작성자만 게시글을 삭제할 수 있습니다.');
                return;
            }

            alert('게시글 삭제 중 오류가 발생했습니다.');
        }
    });
};

let loadBoardDetail = () => {
    const boardId = $('#hiddenId').val();

    $.ajax({
        type: 'GET',
        url: '/api/boards/' + boardId + '/with-comments',
        success: (response) => {
            $('#title').text(response.title);
            $('#content').text(response.content);
            $('#userId').text(response.userId);
            $('#created').text(response.created);

            if (loginUser.userId !== response.userId) {
                $('#editBtn').prop('disabled', true);
                $('#deleteBtn').prop('disabled', true);
            }

            renderFile(response.filePath);
            renderComments(response.comments);
        },
        error: (error) => {
            console.error('오류 발생:', error);
            alert('상세 데이터를 불러오는데 오류가 발생했습니다.');
        }
    });
};

let renderFile = (filePath) => {
    const $fileList = $('#fileList');

    $fileList.empty();

    if (filePath == null || filePath.length === 0) {
        $fileList.append(
            '<li>첨부된 파일이 없습니다.</li>'
        );

        return;
    }

    const normalizedPath = filePath.replace(/\\/g, '/');

    const fileName = normalizedPath.substring(
        normalizedPath.lastIndexOf('/') + 1
    );

    $fileList.append(`
        <li>
            <a href="/api/boards/file/download/${fileName}">
                ${fileName}
            </a>
        </li>
    `);
};

let renderComments = (comments) => {
    const $commentList = $('#commentList');

    $commentList.empty();

    $('#commentCount').text(
        comments && comments.length > 0
            ? comments.length
            : ''
    );

    if (comments == null || comments.length === 0) {
        $commentList.append(`
            <li class="no-comment">
                아직 댓글이 없습니다. 첫 댓글을 남겨보세요!
            </li>
        `);

        return;
    }

    comments.forEach((comment) => {
        $commentList.append(`
            <li class="comment-item">
                <div class="comment-meta">
                    <strong>${comment.userId}</strong>
                    <span class="comment-date">
                        ${comment.created}
                    </span>
                </div>

                <p class="comment-content">
                    ${comment.content}
                </p>
            </li>
        `);
    });
};

let submitComment = () => {
    const boardId = $('#hiddenId').val();
    const content = $('#commentContent').val();

    if (content == null || content.trim() === '') {
        alert('댓글 내용을 입력해주세요.');
        return;
    }

    $.ajax({
        type: 'POST',
        url: '/api/boards/' + boardId + '/comments',
        contentType: 'application/json',
        data: JSON.stringify({
            content: content
        }),
        success: () => {
            $('#commentContent').val('');
            loadBoardDetail();
        },
        error: (error) => {
            console.error('오류 발생:', error);
            alert('댓글 등록 중 오류가 발생했습니다.');
        }
    });
};