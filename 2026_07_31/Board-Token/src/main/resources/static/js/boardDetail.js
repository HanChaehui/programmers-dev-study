
$(document).ready(() => {
    checkSession();
    loadBoardDetail();
});

let editArticle = () => {
    let resourceId = $('#hiddenId').val();
    window.location.href = "/update/" + resourceId;
}

let deleteArticle = () => {

    let resourceId = $('#hiddenId').val();
    let filePath = $('#hiddenFilePath').val();

    $.ajax({
        type: 'DELETE',
        url: '/api/boards/' + resourceId, // 실제 서버 API URL 및 삭제할 리소스 ID
        data: JSON.stringify({ filePath: filePath }), // filePath를 JSON으로 서버에 전송
        contentType: 'application/json', // JSON 형식으로 전송
        success: (response) => {
            alert('리소스가 성공적으로 삭제되었습니다.');
            window.location.href = '/'; // 성공 후 목록 페이지로 이동
        },
        error: (error) => {
            alert('리소스 삭제 중 오류가 발생했습니다.');
            console.error('Error:', error);
        }
    });
}

let checkSession = () => {
    let hUserId = $('#hiddenUserId').val();

    if (hUserId == null || hUserId === '')
        window.location.href = "/members/login";
}

let loadBoardDetail = () => {

    let hId = $('#hiddenId').val();
    let hUserId = $('#hiddenUserId').val();
    $.ajax({
        type: 'GET',
        // 호출 API 교체
        //   기존: url: '/api/boards/' + hId                    (게시글만 - BoardDetailResponseDto)
        //   변경: url: '/api/boards/' + hId + '/with-comments' (게시글 + 댓글을 fetch join 으로 한 번에)
        //   - 게시글 필드(title/content/userId/created/filePath)는 이름이 같아 기존 코드가 그대로 동작하고,
        //     응답에 comments 배열이 추가로 담겨 온다
        url: '/api/boards/' + hId + '/with-comments',
        success: (response) => {
            $('#title').text(response.title);
            $('#content').text(response.content);
            $('#userId').text(response.userId);
            $('#created').text(response.created);

            if (hUserId != response.userId) {
                $('#editBtn').prop('disabled', true);
                $('#deleteBtn').prop('disabled', true);
            }

            // 다시 불러도(댓글 등록 후 재호출) 목록이 중복으로 쌓이지 않도록 먼저 비운다
            //   (기존에는 최초 1번만 호출돼 append 만으로 충분했지만, 이제는 재호출이 생겨 초기화가 필요하다)
            $('#fileList').empty();

            // 파일 목록이 있는 경우, 파일 다운로드 링크 추가
            if (response.filePath && response.filePath.length > 0) {
                let filePath = response.filePath;
                $('#hiddenFilePath').val(filePath)
                // 저장된 전체 경로에서 파일명만 추출한다
                // 경로 구분자는 OS마다 다르므로(mac/리눅스: '/', 윈도우: '\') 백슬래시를 '/'로 통일한 뒤 마지막 '/' 뒤를 자른다
                let normalized = filePath.replace(/\\/g, '/');
                let fileName = normalized.substring(normalized.lastIndexOf('/') + 1);
                let fileElement = `
                            <li>
                                <a href="/api/boards/file/download/${fileName}">${fileName}</a> <!-- 다운로드 링크 -->
                            </li>`;
                $('#fileList').append(fileElement);
            } else {
                $('#fileList').append('<li>첨부된 파일이 없습니다.</li>');
            }

            // 응답에 함께 온 댓글 목록을 그린다
            renderComments(response.comments);

        },
        error: function (error) {
            console.error('오류 발생:', error);
            alert('상세 데이터를 불러오는데 오류가 발생했습니다.');
        }
    });
}

// 댓글 목록을 그린다 - /with-comments 응답의 comments 배열을 받는다
let renderComments = (comments) => {
    const $list = $('#commentList');
    $list.empty(); // 재호출 시 중복 방지

    // 댓글 개수 표시 (없으면 숫자 대신 비움)
    $('#commentCount').text(comments && comments.length > 0 ? comments.length : '');

    if (comments == null || comments.length <= 0) {
        $list.append('<li class="no-comment">아직 댓글이 없습니다. 첫 댓글을 남겨보세요!</li>');
        return;
    }

    comments.forEach((c) => {
        // 한 댓글 = 작성자 + 작성일(위) / 내용(아래)
        $list.append(
            `
            <li class="comment-item">
                <div class="comment-meta">
                    <strong>${c.userId}</strong>
                    <span class="comment-date">${c.created}</span>
                </div>
                <p class="comment-content">${c.content}</p>
            </li>
            `
        );
    });
}

// 댓글 등록 - POST /api/boards/{boardId}/comments
//   - 작성자(userId)는 입력받지 않고 로그인 세션 값(hiddenUserId)을 쓴다
//   - 성공하면 입력칸을 비우고 상세를 다시 불러 댓글 목록(과 목록 화면의 댓글 수 집계 대상)을 갱신한다
let submitComment = () => {
    let hId = $('#hiddenId').val();
    let hUserId = $('#hiddenUserId').val();
    let content = $('#commentContent').val();

    // 빈 댓글 방지 - trim 으로 공백만 친 경우도 걸러낸다
    if (content == null || content.trim() === '') {
        alert('댓글 내용을 입력해주세요.');
        return;
    }

    $.ajax({
        type: 'POST',
        url: '/api/boards/' + hId + '/comments',
        contentType: 'application/json',                              // JSON 본문 (@RequestBody 로 받는다)
        data: JSON.stringify({ userId: hUserId, content: content }),  // CommentWriteRequestDto 필드와 키가 같아야 한다
        success: () => {
            $('#commentContent').val('');   // 입력칸 비우기
            loadBoardDetail();              // 방금 단 댓글이 보이도록 다시 조회
        },
        error: (error) => {
            console.error('오류 발생:', error);
            alert('댓글 등록 중 오류가 발생했습니다.');
        }
    });
}