
$(document).ready(() => {

    $('#signin').click(() => {

        let formData = {
            userId: $('#user_id').val(),
            password: $('#password').val()
        }

        $.ajax({
            type: 'POST',
            url: '/users/login', // UsernamePasswordAuthenticationFilter가 가로채는 URL
            data: formData, // JSON.stringify 하지 않은 객체 → jQuery가 userId=...&password=... 형태로 변환해 전송
            // 요청 본문의 형식(Content-Type 헤더). jQuery 기본값이라 생략해도 되지만 학습용으로 명시.
            // JSON이 아니라 form-urlencoded로 보내야 UsernamePasswordAuthenticationFilter가 "파라미터"로 읽을 수 있다.
            // (signUp.js는 application/json — 받는 쪽이 필터가 아니라 @RequestBody 컨트롤러라서 가능한 것)
            contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
            // 응답을 해석할 형식. 성공/실패 핸들러가 내려주는 JSON을 파싱해 객체로 받는다 (요청과 무관)
            dataType: 'json',
            success: function(response) {
                // CustomAuthenticationSuccessHandler가 내려준 JSON
                alert(response.message);
                window.location.href = response.url;
            },
            error: function(xhr) {
                // CustomAuthenticationFailureHandler가 내려준 JSON (401)
                let response = xhr.responseJSON;
                alert(response && response.message ? response.message : '로그인 중 오류가 발생했습니다.');
            }
        });

    });

});