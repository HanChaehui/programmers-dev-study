let selectedFile = null; // 파일은 1개만 선택 가능

$(document).ready(() => {
    checkSession();
    loadBoardDetail();
    updated();
    fileChanged();
    $('#hiddenFileFlag').val(false);
});

let updated = () => {
    $('#submitBtn').on('click', (event) => {
        event.preventDefault();

        let hId = $('#hiddenId').val(); // 수정할 글의 id (URL 경로에 넣는다)
        let formData = new FormData($('#writeForm')[0]);

        $.ajax({
            type: 'PUT',
            url: '/api/boards/' + hId, // 수정 대상 글을 경로로 식별한다 (PUT /api/boards/{id})
            data: formData,
            processData: false,
            contentType: false,
            success: function(response) {
                // 성공 시 실행될 콜백 함수
                alert('게시글이 성공적으로 수정되었습니다!')
                // 성공 후 다른 페이지로 이동하거나 처리할 코드 작성 가능
                window.location.href = '/';
            },
            error: function(error) {
                // 실패 시 실행될 콜백 함수
                console.error('오류 발생:', error);
                alert('게시글 수정 중 오류가 발생하였습니다.');
            }
        });

    });
}

let fileChanged = () => {
    // 파일 선택 시 이벤트
    $('#file').on('change', function(e) {
        const file = e.target.files[0]; // 첫 번째 파일만 선택

        selectedFile = file; // 선택된 파일을 변수에 저장
        $('#hiddenFileFlag').val(true);
        updateFileList(); // 파일 목록 업데이트
    });
}

// 파일 목록 업데이트 함수 (파일 하나만)
let updateFileList = () => {
    $('#fileList').empty(); // 기존 목록 비우기

    if (selectedFile) {
        $('#fileList').append(`
                    <li>
                        ${selectedFile.name} <button type="button" class="remove-btn">X</button>
                    </li>
                `);

        // X 버튼 클릭 시 파일 제거
        $('.remove-btn').on('click', function () {
            selectedFile = null; // 선택된 파일 제거
            $('#file').val(''); // 파일 input 초기화
            $('#hiddenFileFlag').val(true);
            updateFileList(); // 파일 목록 갱신
        });
    }
}

let checkSession = () => {
    let hUserId = $('#hiddenUserId').val();

    if (hUserId == null || hUserId === '')
        window.location.href = "/members/login";
}

let loadBoardDetail = () => {

    let hId = $('#hiddenId').val();
    $.ajax({
        type: 'GET',
        url: '/api/boards/' + hId,
        success: (response) => {
            $('#title').val(response.title);
            $('#content').val(response.content); // textarea 값은 .val() 로 채운다
            $('#userId').val(response.userId);

            // 파일 목록이 있는 경우, 파일 다운로드 링크 추가
            if (response.filePath && response.filePath.length > 0) {
                let filePath = response.filePath;
                $('#hiddenFilePath').val(filePath)
                // 경로 구분자를 '/'로 통일한 뒤 마지막 '/' 뒤(파일명)를 자른다 (OS별 구분자 차이 대응)
                let normalized = filePath.replace(/\\/g, '/');
                let fileName = normalized.substring(normalized.lastIndexOf('/') + 1);
                let fileElement = ` 
                            <li>
                                ${fileName} <button type="button" class="remove-btn">X</button>
                            </li>`
                ;
                $('#fileList').append(fileElement);

                // X 버튼 클릭 시 파일 제거
                $('.remove-btn').on('click', function () {
                    selectedFile = null; // 선택된 파일 제거
                    $('#hiddenFileFlag').val(true);
                    $('#file').val(''); // 파일 input 초기화
                    updateFileList(); // 파일 목록 갱신
                });
            } else {
                $('#fileList').append('<li>첨부된 파일이 없습니다.</li>');
            }

        },
        error: function (error) {
            console.error('오류 발생:', error);
            alert('상세 데이터를 불러오는데 오류가 발생했습니다.');
        }
    });
}