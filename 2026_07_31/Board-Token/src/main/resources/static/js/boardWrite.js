let selectedFile = null;

$(document).ready(async () => {
    const user = await loadAuthenticatedUser();

    if (user == null) {
        return;
    }

    $('#userId').val(
        `${user.userName} (${user.userId})`
    );

    bindSaveEvent();
    bindFileChangeEvent();
});

let bindSaveEvent = () => {
    $('#submitBtn').on('click', (event) => {
        event.preventDefault();

        const formData = new FormData(
            $('#writeForm')[0]
        );

        $.ajax({
            type: 'POST',
            url: '/api/boards',
            data: formData,
            processData: false,
            contentType: false,
            success: () => {
                alert('게시글이 성공적으로 등록되었습니다.');
                window.location.href = '/';
            },
            error: (error) => {
                console.error('오류 발생:', error);
                alert('게시글 등록 중 오류가 발생하였습니다.');
            }
        });
    });
};

let bindFileChangeEvent = () => {
    $('#file').on('change', (event) => {
        selectedFile = event.target.files[0] ?? null;
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
        updateFileList();
    });
};