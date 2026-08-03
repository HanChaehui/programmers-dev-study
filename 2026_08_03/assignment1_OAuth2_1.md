spring-boot/security/oauth2

#### kakao developers 절차

1. kakao developers 접속 → 로그인
2. 앱 → 앱 생성 (icon 함께 등록), 도메인 등록 X
3. 비즈 앱 전환
앱 → 앱 설정 → 앱 → 일반 → 개인 개발자 or 사업자 정보 등록 (개인 사업자 번호 필수) 비즈앱 전환 (가입 시 이메일을 받아오기 위해 필수) → 카카오 비즈니스 인증 진행
4. client-id (REST API 키), client-secret (카카오 로그인 코드)
앱 → 앱 설정 → 앱 → 플랫폼 키 → REST API 키
5. 카카오 로그인 리다이렉트 URI (저장 필수)
앱 → 앱 설정 → 앱 → 플랫폼 키 → REST API 키
http://localhost:8080/login/oauth2/code/kakao
6. 카카오 로그인 활성화
앱 → 제품 설정 → 카카오 로그인 → 일반 → 활성화 시키기
7. 동의항목
앱 → 제품 설정 → 카카오 로그인 → 동의항목 → 닉네임, 카카오계정 설정 [필수 동의]

## OAuth2 (Open Authorization 2.0)

OAuth2는 "비밀번호를 넘겨주지 않고 권한을 위임"하기 위한 표준 프레임워크

#### OAuth2 사용 방법:

사용자가 "카카오에게 직접" 허락 받고 우리 서비스는 그 허락의 증표(access token)만 받는다.
→ 비밀번호는 원래 주인(카카오)만 알고, 위임 범위와 회수가 가능해진다.

#### 역할(role)

1) Resource Owner : 자원의 주인 = 사용자(카카오 계정의 주인)

2) Client : 자원을 쓰고 싶은 제3자 앱 = 우리 서비스

3) Authorization Server : 허락(인가)을 발급하는 서버 = kauth.kakao.com

4) Resource Server : 실제 자원(프로필 등)을 보관할 서버 = kapi.kakao.com

#### 인가 코드 방식 - 표준 흐름

1) Client가 사용자를 Authorization Server의 인가 페이지로 리다이렉트
(client_id, redirect_uri, scope, state를 쿼리로 실어 보낸다.)

2) 사용자가 "카카오 화면에서" 로그인하고 권한 제공에 동의

3) Authorization Server가 redirect_uri로 "인가 코드"를 돌려준다. (브라우저 공유)

4) Client 서버가 code + client_secret으로 토큰 엔드포인트에 직접 요청(브라우저 안 거침)

5) Authorization Server가 access token 발급

6) Client가 그 토큰으로 Resource Server에서 사용자 정보 조회

#### Spring Security에서의 동작 흐름 - oauth2Login()

위 표준 흐름을 필터 두 개가 나눠서 대신 처리한다.

1) OAuth2AuthorizationRequestRedirectFilter
→ /oauth2/authorization/{registrationId} 요청을 가로채 인가 페이지로 리다이렉트

2) OAuth2LoginAuthenticationFilter
→ /login/oauth2/code/{registrationId}로 돌아온 code를 받아 
"state 검증 → 토큰 교환 → 사용자 정보 조회까지 수행"

3) 조회된 사용자 정보를 OAuth2UserService.loadUser()에 넘긴다.
→ 여기서 "제공자의 회원"을 "우리 DB의 회원"으로 연결(없으면 가입)하는 것이 개발자의 몫

4) 반환된 OAuth2User로 Authentication을 만들어 SecurityContext에 저장 → 로그인 완료

5) 마지막으로 SuccessHandler 호출 → 로그인 후처리(JWT 발급)도 개발자의 몫

정리하면 개발자가 구현하는 것은 파이프라인의 양 끝 훅(hook)두 개뿐이다.

- OAuth2UserService : 제공자 응답 → 우리 회원 매핑
- SuccessHandler : 로그인 성공 → 후처리 (토큰 발급,리다이렉트 등)

나머지는(리다이렉트, state,코드-토큰 교환,정보 조회)는 전부 프레임워크가 처리하고, 제공자별 차이(엔드포인트 URL, scope등)는 코드가 아닌 설정 파일의 registration, provider항목으로 흡수된다. 그래서 네이버 같은 새 공급자를 추가해도 Java 코드는 거의 안 바뀐다.

#### CustomOAuth2User

CustomUserDetails의OAuth2 버전이다.

- 자체 로그인 경로 : 시큐리티가 요구하는 표준 = UserDetails → CustomUserDetails가 User를 감싼다.
- 소셜 로그인 경로 : 시큐리티가 요구하는 표준 = OAuth2User → 이 클래스가 User를 감싼다.

즉, 이 클래스도 "우리 도메인(User)과 시큐리티 사이의 어댑터"이고, 어떤 경로로 로그인하든 principal에서 우리 User 엔티티를 꺼낼 수 있게 만드는 장치이다.

#### record란? (Java 16+)

필드 나열만 하면 나머지 상용구를 컴파일러가 다써주는, 불변 데이터 전용 클래스이다.
필드선언, 생성자, getter, equals/hashCode/toString까지 수십 줄을 대신 해준다.
record인 이유: 상태는 응답 맵 하나뿐인 불변 값 객체이고, 컴포넌트 이름을 attributes로 지으면 인터페이스 attributes()가 자동 구현된다.