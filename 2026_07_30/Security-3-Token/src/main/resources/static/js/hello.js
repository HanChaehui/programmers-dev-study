$(document).ready(() => {
    setupAjax();
    loadUserInfo();

    $('#logout').click(() => logout());
    $('#call-user').click(() => callAuthorityApi('/api/users/user'));
    $('#call-admin').click(() => callAuthorityApi('/api/users/admin'));
});

// 권한 테스트 API 호출. 권한이 없으면 서버가 403 + {status, message} JSON을 내려준다
let callAuthorityApi = (url) => {
    $.ajax({
        type: 'GET',
        url: url,
        dataType: 'json',
        success: (response) => {
            $('#authority-result').text(response.message).css('color', 'green');
        },
        error: (xhr) => {
            let response = xhr.responseJSON;
            let message = response && response.message ? response.message : '요청에 실패했습니다.';
            $('#authority-result').text(message).css('color', 'red');
        }
    });
}

// 서버가 refresh token 쿠키를 지우고, 클라이언트는 access token을 지운다
let logout = () => {
    $.ajax({
        type: 'POST',
        url: '/api/users/logout',
        dataType: 'json',
        xhrFields: {
            withCredentials: true // refresh token 쿠키를 포함해야 서버가 지울 수 있다
        },
        success: (response) => {
            localStorage.removeItem('accessToken');
            alert(response.message);
            window.location.href = response.url;
        },
        error: () => {
            // 서버 호출이 실패해도 로컬 토큰은 지우고 로그인 페이지로 보낸다
            localStorage.removeItem('accessToken');
            window.location.href = '/users/login';
        }
    });
}

// [토큰 체크 흐름]
// 1) access token이 있으면 그걸로 사용자 정보 조회
// 2) access token이 없으면 → refresh 쿠키로 재발급 시도 후 조회
// 3) 조회가 401 등으로 실패하면(access 만료) → 재발급 한 번 시도 후 재조회
// 4) refresh까지 없거나 만료면 → 로그인 페이지로
let loadUserInfo = async () => {
    try {
        if (localStorage.getItem('accessToken') == null) {
            await refreshTokens();
        }
        renderUserInfo(await getUserInfo());
    } catch (e) {
        try {
            await refreshTokens();
            renderUserInfo(await getUserInfo());
        } catch (e2) {
            redirectToLogin();
        }
    }
}

let renderUserInfo = (user) => {
    $('#user-name').text(user.userName);
    $('#user-id').text(user.userId);
    $('#user-role').text(user.role);

    // 관리자에게만 관리자 페이지 링크 노출
    // (숨겨도 URL 직접 입력으로 들어갈 수는 있지만, 그 경우 admin.js가 API 403을 받고 쫓아낸다
    //  — UI 숨김은 편의일 뿐, 실제 보안은 항상 서버(@PreAuthorize)가 담당한다)
    if (user.role === 'ROLE_ADMIN') {
        $('#admin-link').show();
    }
}