
$(document).ready(() => {

    $('#signin').click(() => {

        let formData = {
            userId: $('#user_id').val(),
            password: $('#password').val()
        }

        $.ajax({
            type: 'POST',
            url: '/api/users/login', // UserApiController.signIn (JSON API)
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(formData),
            dataType: 'json',
            success: function(response) {
                // access token은 localStorage에 저장, refresh token은 서버가 HttpOnly 쿠키로 심어준다
                localStorage.setItem('accessToken', response.accessToken);
                alert(response.message);
                window.location.href = response.url;
            },
            error: function(xhr) {
                let response = xhr.responseJSON;
                alert(response && response.message ? response.message : '로그인 중 오류가 발생했습니다.');
            }
        });

    });

});