// 모든 ajax 요청에 access token을 Authorization 헤더로 실어 보낸다
// (beforeSend가 요청 때마다 localStorage를 읽으므로 토큰 갱신 후 재설정할 필요 없음)
let setupAjax = () => {
    $.ajaxSetup({
        beforeSend: (xhr) => {
            let token = localStorage.getItem("accessToken");
            if (token) {
                xhr.setRequestHeader('Authorization', 'Bearer ' + token);
            }
        }
    });
}

// refresh 쿠키로 access/refresh 토큰 재발급
// 성공: 새 access token은 localStorage에 저장, 새 refresh token은 서버가 기존 쿠키에 덮어씌움
// 실패(쿠키 없음/만료): reject → 호출한 쪽에서 로그인 페이지로 보낸다
let refreshTokens = () => {
    return new Promise((resolve, reject) => {
        $.ajax({
            type: 'POST',
            url: '/api/tokens/refresh',
            contentType: 'application/json; charset=utf-8',
            dataType: 'json',
            xhrFields: {
                withCredentials: true // refresh token 쿠키를 포함해서 요청
            },
            success: (response) => {
                localStorage.setItem('accessToken', response.accessToken);
                resolve(response);
            },
            error: (xhr) => reject(xhr)
        });
    });
}

// 로그인한 사용자 정보 조회 (인증 필요 API)
let getUserInfo = () => {
    return new Promise((resolve, reject) => {
        $.ajax({
            type: 'GET',
            url: '/api/users/info',
            dataType: 'json',
            success: (response) => resolve(response),
            error: (xhr) => reject(xhr)
        });
    });
}

let redirectToLogin = () => {
    alert('로그인이 필요합니다. 다시 로그인해주세요.');
    localStorage.removeItem('accessToken');
    window.location.href = '/users/login';
}