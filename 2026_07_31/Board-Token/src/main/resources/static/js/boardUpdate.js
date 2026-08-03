let selectedFile = null;
let loginUser = null;

$(document).ready(async () => {
    loginUser = await loadAuthenticatedUser();

    if (loginUser == null) {
        return;
    }

    $('#hiddenFileFlag').val(false);

    bindUpdateEvent();
    bindFileChangeEvent();
    loadBoardDetail();
});

let bindUpdateEvent = () => {
    $('#submitBtn').on('click', (event) => {
        event.preventDefault();

        const boardId = $('#hiddenId').val();

        const formData = new FormData(
            $('#writeForm')[0]
        );

        $.ajax({
            type: 'PUT',
            url: '/api/boards/' + boardId,
            data: formData,
            processData: false,
            contentType: false,
            success: () => {
                alert('게시글이 성공적으로 수정되었습니다.');
                window.location.href = '/detail?id=' + boardId;
            },
            error: (error) => {
                console.error('오류 발생:', error);
                alert('게시글 수정 중 오류가 발생하였습니다.');
            }
        });
    });
};

let bindFileChangeEvent = () => {
    $('#file').on('change', (event) => {
        selectedFile = event.target.files[0] ?? null;

        $('#hiddenFileFlag').val(true);

        updateFileList();
    });
};

let updateFileList = () => {
    const $fileList = $('#fileList');

    $fileList.empty();

    if (selectedFile == null) {
        return;
    }

    $fileList.append(`
        <li>
            ${selectedFile.name}
            <button type="button" class="remove-btn">
                X
            </button>
        </li>
    `);

    $('.remove-btn').on('click', () => {
        selectedFile = null;

        $('#file').val('');
        $('#hiddenFileFlag').val(true);

        updateFileList();
    });
};

let loadBoardDetail = () => {
    const boardId = $('#hiddenId').val();

    $.ajax({
        type: 'GET',
        url: '/api/boards/' + boardId,
        success: (response) => {
            if (loginUser.userId !== response.userId) {
                alert('작성자만 게시글을 수정할 수 있습니다.');
                window.location.href = '/detail?id=' + boardId;
                return;
            }

            $('#title').val(response.title);
            $('#content').val(response.content);
            $('#userId').val(response.userId);

            renderCurrentFile(response.filePath);
        },
        error: (error) => {
            console.error('오류 발생:', error);
            alert('상세 데이터를 불러오는데 오류가 발생했습니다.');
        }
    });
};

let renderCurrentFile = (filePath) => {
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
            ${fileName}
            <button type="button" class="remove-btn">
                X
            </button>
        </li>
    `);

    $('.remove-btn').on('click', () => {
        selectedFile = null;

        $('#file').val('');
        $('#hiddenFileFlag').val(true);

        updateFileList();
    });
};