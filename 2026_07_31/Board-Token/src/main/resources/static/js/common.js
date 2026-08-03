const TOKEN_EXCLUDED_URLS = [
    '/api/users/login',
    '/api/users/join',
    '/api/users/logout',
    '/api/tokens/refresh'
];

// AJAX 요청에 access token을 자동으로 추가
let setupAjax = () => {
    $.ajaxSetup({
        beforeSend: (xhr, settings) => {
            const isExcluded = TOKEN_EXCLUDED_URLS.some(
                url => settings.url.startsWith(url)
            );

            if (isExcluded) {
                return;
            }

            const accessToken = localStorage.getItem('accessToken');

            if (accessToken) {
                xhr.setRequestHeader(
                    'Authorization',
                    'Bearer ' + accessToken
                );
            }
        }
    });
};

// refresh token 쿠키를 사용해 토큰 재발급
let refreshTokens = () => {
    return new Promise((resolve, reject) => {
        $.ajax({
            type: 'POST',
            url: '/api/tokens/refresh',
            contentType: 'application/json; charset=utf-8',
            dataType: 'json',
            xhrFields: {
                withCredentials: true
            },
            success: (response) => {
                localStorage.setItem(
                    'accessToken',
                    response.accessToken
                );

                resolve(response);
            },
            error: (xhr) => {
                reject(xhr);
            }
        });
    });
};

// 현재 로그인 사용자 정보 조회
let getUserInfo = () => {
    return new Promise((resolve, reject) => {
        $.ajax({
            type: 'GET',
            url: '/api/users/info',
            dataType: 'json',
            success: (response) => {
                resolve(response);
            },
            error: (xhr) => {
                reject(xhr);
            }
        });
    });
};

// 페이지 진입 시 로그인 상태 확인
let loadAuthenticatedUser = async () => {
    setupAjax();

    if (!localStorage.getItem('accessToken')) {
        try {
            await refreshTokens();
        } catch (error) {
            redirectToLogin();
            return null;
        }
    }

    try {
        return await getUserInfo();
    } catch (error) {
        try {
            await refreshTokens();
            return await getUserInfo();
        } catch (refreshError) {
            redirectToLogin();
            return null;
        }
    }
};

// 로그아웃
let logout = () => {
    $.ajax({
        type: 'POST',
        url: '/api/users/logout',
        dataType: 'json',
        xhrFields: {
            withCredentials: true
        },
        success: (response) => {
            localStorage.removeItem('accessToken');
            alert(response.message);
            window.location.href = response.url;
        },
        error: () => {
            localStorage.removeItem('accessToken');
            window.location.href = '/users/login';
        }
    });
};

// 로그인 페이지로 이동
let redirectToLogin = () => {
    alert('로그인이 필요합니다. 다시 로그인해주세요.');
    localStorage.removeItem('accessToken');
    window.location.href = '/users/login';
};