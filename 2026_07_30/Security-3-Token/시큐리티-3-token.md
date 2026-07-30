# JWT 토큰으로 로그인·인증·인가 만들기 (Access/Refresh + Stateless)

> 폼 로그인이 "우리 화면 + **세션(stateful)**"이었다면, 토큰 방식은 **서명된 JWT를 클라이언트가 들고 다니는 stateless** 방식이에요. 서버는 아무것도 기억하지 않아요 — 대신 요청마다 실려 오는 토큰의 **서명**만 검증해요.
> 로그인 상태의 주인이 또 이동해요: Basic은 브라우저, 폼 로그인은 서버(세션), 이번엔 **토큰 그 자체**예요.
> 코드보다 중요한 건 흐름이에요: **로그인 성공의 결과가 "세션 저장"이 아니라 "토큰 발급"이고, 이후 요청은 필터가 토큰에서 인증 상태를 복원한다.**
>
> 💡 각 Step의 **힌트는 접혀 있어요.** 먼저 스스로 만들어 보고 막히면 펼치세요.

<details>
<summary>✅ 최종 완성 코드 보기 (먼저 직접 만들어 본 뒤 펼쳐서 비교하세요)</summary>

> Step 6까지 모두 반영한 **완성본**이에요. `User`/`Role`/`UserRepository`/`CustomUserDetails`/`UserDetailService`/회원가입(`UserService.signUp` 등)은 **2번 과제(form-login)와 동일**해서 생략해요 — 그대로 가져오세요.

**`build.gradle`** (의존성 — jjwt 3종이 추가됐어요)
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'   // 부트 3.x는 spring-boot-starter-web

    implementation 'io.jsonwebtoken:jjwt-api:0.13.0'  // JWT API
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.13.0'    // JWT 구현체
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.13.0' // JSON 처리를 위한 Jackson 모듈

    compileOnly 'org.projectlombok:lombok'
    runtimeOnly 'com.mysql:mysql-connector-j'
    annotationProcessor 'org.projectlombok:lombok'
}
```

**`application.yaml`** — 토큰 정책은 코드가 아니라 설정 한 곳에서
```yaml
spring:
  application:
    name: token
  datasource:
    url: jdbc:mysql://localhost:3306/java_basic?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: 1234

jwt:
  issuer: test@naver.com
  secret_key: <openssl rand -base64 64 로 생성한 값>   # HS512는 64바이트 이상!
  access-token-validity: 2h    # "2h" 같은 문자열이 Duration으로 자동 바인딩돼요
  refresh-token-validity: 7d
```

**`config/jwt/JwtProperties.java`**
```java
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String issuer;
    private String secretKey;               // relaxed binding: secret_key → secretKey
    private Duration accessTokenValidity;
    private Duration refreshTokenValidity;
}
```

**`config/jwt/TokenStatus.java`**
```java
public enum TokenStatus {
    VALID,      // 유효한 토큰
    EXPIRED,    // 만료된 토큰
    INVALID     // 서명 불일치, 형식 오류 등
}
```

**`config/jwt/TokenProvider.java`** — 토큰의 생성·검증·해석 전담
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenProvider {

    private static final String CLAIM_ID = "id";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_NAME = "name";

    private final JwtProperties jwtProperties;

    private SecretKey secretKey;
    private JwtParser jwtParser;

    @PostConstruct
    private void init() {
        // 키와 파서는 불변 → 요청마다 만들지 않고 한 번만 생성해 재사용
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtProperties.getSecretKey()));
        this.jwtParser = Jwts.parser().verifyWith(secretKey).build();
    }

    public String generateToken(User user, Duration expiredAt) {
        Date now = new Date();
        return makeToken(user, new Date(now.getTime() + expiredAt.toMillis()));
    }

    private String makeToken(User user, Date expire) {
        Date now = new Date();
        return Jwts.builder()
                .header().type("JWT").and()
                .issuer(jwtProperties.getIssuer())  // 등록된 클레임 iss
                .issuedAt(now)                      // iat
                .expiration(expire)                 // exp
                .subject(user.getUserId())          // sub
                .claim(CLAIM_ID, user.getId())      // 비공개 클레임
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_NAME, user.getName())
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    public TokenStatus validateToken(String token) {
        try {
            jwtParser.parseSignedClaims(token);
            return TokenStatus.VALID;
        } catch (ExpiredJwtException e) {
            log.warn("Token is expired");
            return TokenStatus.EXPIRED;
        } catch (Exception e) {
            log.warn("Token is not valid");
            return TokenStatus.INVALID;
        }
    }

    // 클레임 → 도메인 User 복원 (DB 조회 없이 토큰만으로!)
    public User getTokenDetails(String token) {
        Claims claims = getClaims(token);
        return User.builder()
                .id(claims.get(CLAIM_ID, Long.class))
                .userId(claims.getSubject())
                .name(claims.get(CLAIM_NAME, String.class))
                .role(Role.valueOf(claims.get(CLAIM_ROLE, String.class)))
                .build();
    }

    // 복원된 User → 시큐리티가 이해하는 Authentication
    public Authentication getAuthentication(User user, String token) {
        // principal 자리에 CustomUserDetails를 넣으면
        // 컨트롤러에서 @AuthenticationPrincipal로 도메인 User까지 바로 꺼낼 수 있다
        CustomUserDetails principal = CustomUserDetails.builder().user(user).build();
        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }

    private Claims getClaims(String token) {
        return jwtParser.parseSignedClaims(token).getPayload();
    }
}
```

**`config/filter/TokenAuthenticationFilter.java`** — 요청마다 토큰에서 인증 상태 복원
```java
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        // 토큰이 없으면 인증 없이 통과 (보호된 경로면 뒤의 AuthorizationFilter가 거부)
        if (token != null) {
            TokenStatus status = tokenProvider.validateToken(token);

            if (status == TokenStatus.VALID) {
                User user = tokenProvider.getTokenDetails(token);   // 파싱은 여기 한 번
                Authentication authentication = tokenProvider.getAuthentication(user, token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else if (status == TokenStatus.EXPIRED) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;   // 만료는 401로 즉시 응답 → 프론트가 refresh를 시도한다
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

**`config/SecurityConfig.java`** — 전체 조립
```java
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)   // @PreAuthorize 활성화
public class SecurityConfig {

    private final TokenAuthenticationFilter tokenAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 인증을 Authorization 헤더(자동 전송 안 됨)로 하므로 CSRF가 성립하지 않는다
                .csrf(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)      // 로그아웃은 /api/users/logout에서 직접
                .formLogin(AbstractHttpConfigurer::disable)   // 2번 과제 방식 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)   // 1번 과제 방식 비활성화
                // JWT 사용 → 세션을 만들지도, 사용하지도 않는다 (무상태)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/users/login", "/users/join",
                                "/",       // 페이지(HTML)는 공개, 데이터(API)는 보호
                                "/admin",  // 관리자 "페이지"도 공개 — 실제 차단은 API의 @PreAuthorize가
                                "/api/users/join", "/api/users/login",
                                "/api/users/logout",   // 만료 토큰으로도 로그아웃은 가능해야
                                "/api/tokens/refresh", // access 만료 상태에서 호출하므로 필수
                                "/css/**", "/js/**",
                                "/access-denied",
                                "/error"   // 막으면 무한 리다이렉트 루프!
                        ).permitAll()
                        .anyRequest().authenticated())
                // 인가 판단(체인 끝) 전에 SecurityContext를 채워둬야 하므로 앞에 끼운다
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(accessDeniedHandler())           // 403: 누군지 알지만 자격 없음
                        .authenticationEntryPoint(authenticationEntryPoint()) // 401: 누군지 모름
                );

        return http.build();
    }

    // 권한 계층: ADMIN은 USER의 권한을 포함 (요청 인가 + @PreAuthorize에 자동 반영)
    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("USER")
                .build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 로그인 검증 진입점 — form-login에선 필터가 호출했지만, 이번엔 UserService가 직접 호출
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, e) -> {
            // API 요청(ajax)은 리다이렉트를 따라가 HTML을 받아버리므로 상태 코드 + JSON으로
            if (request.getRequestURI().startsWith("/api/")) {
                sendErrorJson(response, HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다.");
            } else {
                response.sendRedirect("/access-denied");
            }
        };
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, e) -> {
            if (request.getRequestURI().startsWith("/api/")) {
                sendErrorJson(response, HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다.");
            } else {
                response.sendRedirect("/access-denied");
            }
        };
    }

    private void sendErrorJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\": " + status + ", \"message\": \"" + message + "\"}");
    }
}
```

**`service/TokenService.java`** — 토큰 쌍 발급 + 재발급
```java
@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenProvider tokenProvider;
    private final JwtProperties jwtProperties;

    public record TokenPair(String accessToken, String refreshToken) {}

    // 수명 정책은 application.yaml 한 곳에서만 결정된다
    public TokenPair issueTokens(User user) {
        String accessToken = tokenProvider.generateToken(user, jwtProperties.getAccessTokenValidity());
        String refreshToken = tokenProvider.generateToken(user, jwtProperties.getRefreshTokenValidity());
        return new TokenPair(accessToken, refreshToken);
    }

    public RefreshTokenResponseDto refreshToken(Cookie[] cookies) {
        String refreshToken = getRefreshTokenFromCookies(cookies);

        if (refreshToken != null && tokenProvider.validateToken(refreshToken) == TokenStatus.VALID) {
            User user = tokenProvider.getTokenDetails(refreshToken);
            TokenPair tokens = issueTokens(user);   // refresh도 새로 발급 = rotation
            return RefreshTokenResponseDto.builder()
                    .validated(true)
                    .accessToken(tokens.accessToken())
                    .refreshToken(tokens.refreshToken())
                    .build();
        }
        return RefreshTokenResponseDto.builder().validated(false).build();
    }

    private String getRefreshTokenFromCookies(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(CookieUtil.REFRESH_TOKEN_COOKIE)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
```

**`service/UserService.java`** — 로그인 = 검증 + 토큰 발급 (signUp은 2번 과제와 동일)
```java
public SignInResponseDto signIn(String username, String password) {
    // form-login에서 필터가 하던 검증을 직접 호출한다:
    // authenticate() → DaoAuthenticationProvider
    //   → UserDetailService.loadUserByUsername() → passwordEncoder.matches()
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password));

    User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();

    // 인증 성공의 결과가 "세션 저장"이 아니라 "토큰 발급" — form-login과의 결정적 차이
    TokenService.TokenPair tokens = tokenService.issueTokens(user);

    return SignInResponseDto.builder()
            .isLoggedIn(true).message("로그인 성공").url("/")
            .accessToken(tokens.accessToken())
            .refreshToken(tokens.refreshToken())
            .userId(user.getUserId()).userName(user.getName())
            .build();
}
```

**`util/CookieUtil.java`** (핵심 부분)
```java
public class CookieUtil {

    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true); // JavaScript로 접근 불가 → XSS가 못 훔친다
        cookie.setSecure(false);  // 로컬(HTTP) 개발용. 운영(HTTPS)에선 반드시 true
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return;
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                cookie.setMaxAge(0);
                cookie.setValue("");
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        }
    }
}
```

**`controller/UserApiController.java`**
```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserApiController {

    private final UserService userService;
    private final JwtProperties jwtProperties;

    @PostMapping("/join")
    public SignUpResponseDto signUp(@RequestBody SignUpRequestDto requestDto) {
        userService.signUp(requestDto);
        return SignUpResponseDto.builder().url("/users/login").build();
    }

    @PostMapping("/login")
    public SignInResponseDto signIn(@RequestBody SignInRequestDto dto, HttpServletResponse response) {
        SignInResponseDto result = userService.signIn(dto.getUserId(), dto.getPassword());

        // refresh token은 HttpOnly 쿠키로만 전달 (쿠키 수명 = 토큰 수명)
        CookieUtil.addCookie(response, CookieUtil.REFRESH_TOKEN_COOKIE, result.getRefreshToken(),
                (int) jwtProperties.getRefreshTokenValidity().toSeconds());
        result.setRefreshToken(null);   // body에서는 제거 (@JsonInclude(NON_NULL)로 응답에서 사라짐)

        return result;
    }

    @PostMapping("/logout")
    public LogoutResponseDto logout(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request, response, CookieUtil.REFRESH_TOKEN_COOKIE);
        return LogoutResponseDto.builder().message("로그아웃 되었습니다.").url("/users/login").build();
    }

    @GetMapping("/info")
    public UserInfoResponseDto getUserInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // 필터가 SecurityContext에 넣어둔 principal이 그대로 주입된다
        User user = userDetails.getUser();
        return UserInfoResponseDto.builder()
                .id(user.getId()).userId(user.getUserId())
                .userName(user.getName()).role(user.getRole())
                .build();
    }

    @PreAuthorize("hasRole('USER')")    // 내부적으로 "ROLE_USER" 권한을 찾는다
    @GetMapping("/user")
    public AuthorityResponseDto authority() {
        return AuthorityResponseDto.builder().message("일반 사용자만 볼 수 있는 권한입니다.").build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public AuthorityResponseDto authorityAdmin() {
        return AuthorityResponseDto.builder().message("관리자만 볼 수 있는 권한입니다.").build();
    }
}
```

**`controller/TokenApiController.java`**
```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tokens")
public class TokenApiController {

    private final TokenService tokenService;
    private final JwtProperties jwtProperties;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        RefreshTokenResponseDto dto = tokenService.refreshToken(request.getCookies());

        if (dto.isValidated()) {
            // 새 refresh token으로 기존 쿠키를 덮어씌운다 (rotation)
            CookieUtil.addCookie(response, CookieUtil.REFRESH_TOKEN_COOKIE, dto.getRefreshToken(),
                    (int) jwtProperties.getRefreshTokenValidity().toSeconds());
            dto.setRefreshToken(null);
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDto(HttpStatus.UNAUTHORIZED.value(), "리프레시 토큰이 만료되었습니다."));
    }
}
```

**`dto`** — 새로 추가된 것만
```java
@Getter @Setter @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)   // null 필드(쿠키로만 주는 refreshToken)는 응답에서 제외
public class SignInResponseDto {
    private boolean isLoggedIn;
    private String url;
    private String userName;
    private String userId;
    private String message;
    private String accessToken;
    private String refreshToken;
}

@Getter @Setter @Builder
public class RefreshTokenResponseDto {
    private boolean validated;
    private String accessToken;
    private String refreshToken;
}

@Getter @Builder
public class UserInfoResponseDto {
    private long id;
    private String userId;
    private String userName;
    private Role role;
}

@Getter @Builder
public class AuthorityResponseDto {
    private String message;
}

@Getter @Builder
public class LogoutResponseDto {
    private String message;
    private String url;
}
```

**`static/js/common.js`** — 토큰 공통 로직 (Promise 기반)
```javascript
// 모든 ajax 요청에 access token을 Authorization 헤더로 실어 보낸다
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

// refresh 쿠키로 토큰 재발급. 성공: 새 access는 localStorage에 (새 refresh는 서버가 쿠키에 덮어씀)
let refreshTokens = () => {
    return new Promise((resolve, reject) => {
        $.ajax({
            type: 'POST',
            url: '/api/tokens/refresh',
            dataType: 'json',
            xhrFields: { withCredentials: true },   // 쿠키 포함
            success: (response) => {
                localStorage.setItem('accessToken', response.accessToken);
                resolve(response);
            },
            error: (xhr) => reject(xhr)
        });
    });
}

let getUserInfo = () => {
    return new Promise((resolve, reject) => {
        $.ajax({
            type: 'GET', url: '/api/users/info', dataType: 'json',
            success: resolve, error: reject
        });
    });
}

let redirectToLogin = () => {
    alert('로그인이 필요합니다. 다시 로그인해주세요.');
    localStorage.removeItem('accessToken');
    window.location.href = '/users/login';
}
```

**`static/js/hello.js`** (핵심 부분) — 토큰 체크 흐름
```javascript
// 1) access token 있으면 조회 → 2) 없으면 refresh로 재발급 후 조회
// 3) 조회 실패(만료 401)면 재발급 후 한 번 재시도 → 4) refresh도 안 되면 로그인으로
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
```

**`static/js/signIn.js`** (핵심 부분) — ★ 이번엔 로그인도 JSON으로!
```javascript
$.ajax({
    type: 'POST',
    url: '/api/users/login',                  // 필터가 아니라 "우리 컨트롤러"가 받는다
    contentType: 'application/json; charset=utf-8',
    data: JSON.stringify(formData),           // form-login과 반대 — @RequestBody가 받으니 JSON
    dataType: 'json',
    success: function(response) {
        localStorage.setItem('accessToken', response.accessToken);  // ★ 토큰 저장
        alert(response.message);
        window.location.href = response.url;
    },
    ...
});
```

**동작 확인 (curl)**
```bash
# 회원가입 (2번 과제와 동일)
curl -i -H "Content-Type: application/json" \
     -d '{"userId":"kim","password":"1234","userName":"김개발","role":"ROLE_USER"}' \
     http://localhost:8080/api/users/join

# 로그인 — 이번엔 JSON! (form-login은 form-urlencoded였죠)
curl -i -c cookie.txt -H "Content-Type: application/json" \
     -d '{"userId":"kim","password":"1234"}' \
     http://localhost:8080/api/users/login
# → 200 {"accessToken":"eyJ..."} + Set-Cookie: refreshToken=... (HttpOnly)

# 인증 요청 — Authorization 헤더에 Bearer 토큰
curl -i -H "Authorization: Bearer <accessToken>" http://localhost:8080/api/users/info
# → 200 {"id":1,"userId":"kim",...}   (토큰 없이 부르면 401 JSON)

# 토큰 재발급 — 쿠키의 refresh token으로
curl -i -b cookie.txt -X POST http://localhost:8080/api/tokens/refresh
# → 200 {"validated":true,"accessToken":"eyJ..."} + 새 Set-Cookie: refreshToken=...
```

</details>

---

## 0. 먼저 알아둘 점

- 완성하면 이렇게 동작해요: **로그인하면 access token(2시간)은 응답 body로, refresh token(7일)은 HttpOnly 쿠키로 발급 → 이후 API 요청마다 `Authorization: Bearer` 헤더로 인증 → access가 만료되면 refresh로 재발급 → 권한(ROLE_USER/ROLE_ADMIN)에 따라 API 접근이 갈려요.**
- **2번 과제(form-login) 프로젝트를 기반으로 시작해요.** `User`, `Role`, `UserRepository`, `CustomUserDetails`, `UserDetailService`, 회원가입 흐름은 그대로 재사용해요. 성공/실패 핸들러와 `formLogin` 설정은 **버려요** — 이번엔 필터가 아니라 우리가 직접 로그인을 처리하니까요.
- **DB는 회원 정보에만 써요.** 토큰은 어디에도 저장하지 않아요(stateless). "서버가 기억하지 않는데 어떻게 신뢰하지?"가 이번 과제의 핵심 질문이에요 — 답은 **서명**이에요.
- ⚠️ **이 과제 최대의 함정**: 시크릿 키가 짧으면 서버가 뜨자마자 `WeakKeyException`으로 죽어요. HS512는 **64바이트 이상**이 필요해요 → `openssl rand -base64 64`로 생성하세요.
- ⚠️ 2번 과제와 반대로, **이번엔 로그인 요청을 JSON으로** 보내요. 로그인을 가로채는 필터가 없고 우리 컨트롤러(`@RequestBody`)가 받으니까요. "왜 반대인지" 설명할 수 있으면 두 과제를 다 이해한 거예요.
- 브라우저의 **페이지 이동은 `Authorization` 헤더를 실을 수 없어요.** 그래서 페이지(HTML)는 공개하고, 실질적 보호는 데이터 API가 담당하는 구조가 돼요 (핵심 개념 (5)).

---

## 1. 무엇을 만드나요?

| 주소 | 하는 일 | 누가 처리? | 접근 |
|------|------|------|------|
| `POST /api/users/join` | 회원가입 (role 선택 포함) | 컨트롤러 + 서비스 | 개방 |
| `POST /api/users/login` | 로그인 → **토큰 발급** | **우리 컨트롤러** (필터 아님!) | 개방 |
| `GET /api/users/info` | 내 정보 조회 | 컨트롤러 (`@AuthenticationPrincipal`) | **토큰 필요** |
| `POST /api/tokens/refresh` | access/refresh 재발급 | 컨트롤러 (쿠키의 refresh 사용) | 개방(필수!) |
| `POST /api/users/logout` | refresh 쿠키 삭제 | 컨트롤러 | 개방 |
| `GET /api/users/user` | 권한 테스트 | `@PreAuthorize("hasRole('USER')")` | ROLE_USER↑ |
| `GET /api/users/admin` | 권한 테스트 | `@PreAuthorize("hasRole('ADMIN')")` | ROLE_ADMIN |
| `GET /`, `/admin`, `/users/login`, `/users/join` | 화면 | 컨트롤러 | 개방 (껍데기) |

**흐름 예시**: 로그인 → access는 localStorage, refresh는 HttpOnly 쿠키 → 메인에서 `/api/users/info`로 "OO님" 표시 → 2시간 뒤 access 만료 → 401 → 프론트가 조용히 `/api/tokens/refresh` → 새 토큰으로 재시도 → 사용자는 아무것도 못 느낌.

---

## 2. 학습 목표

| 개념 | 어디서 배우나 |
|------|------|
| JWT 구조(Header.Payload.Signature)와 클레임, 서명의 의미 | Step 1 |
| jjwt로 토큰 생성/검증/해석 (`Jwts.builder`, `parser`) | Step 1~2 |
| 로그인 = `AuthenticationManager` 직접 호출 + 토큰 발급 | Step 3 |
| access/refresh 이원화와 저장 위치(localStorage vs HttpOnly 쿠키) | Step 3, 5 |
| `OncePerRequestFilter`로 SecurityContext 복원, principal | Step 4 |
| STATELESS 설정과 401/403 처리(API는 JSON, 페이지는 리다이렉트) | Step 4 |
| refresh rotation과 stateless의 트레이드오프 | Step 5 |
| `@PreAuthorize` 메서드 인가 + `RoleHierarchy` | Step 6 |

---

## 3. 핵심 개념

### (1) 세 가지 인증 방식 — 로그인 상태의 주인이 이동한다
| 구분 | HTTP Basic | 폼 로그인 | 토큰 (이번) |
|------|------|------|------|
| 자격증명 전달 | 매 요청 ID/PW | 최초 1회 (파라미터) | 최초 1회 (JSON) |
| 이후 요청의 증명 | ID/PW 재전송 | JSESSIONID 쿠키 | **JWT (Bearer 헤더)** |
| 서버가 기억하는 것 | 없음 | **세션** | **없음 (stateless)** |
| 로그인 상태의 주인 | 브라우저 | 서버(세션) | **토큰 자체** |
| 서버 재시작하면 | 유지 | 풀림 | **유지** (서명 키가 같다면) |
| 로그아웃 | 불가 | 가능(세션 무효화) | **애매함** — 토큰은 만료까지 유효 |
| 수평 확장(서버 여러 대) | 쉬움 | 세션 공유 필요 | **쉬움** — 이게 JWT를 쓰는 큰 이유 |

### (2) JWT 구조 — `xxxxx.yyyyy.zzzzz`
```
Header.Payload.Signature
  │      │        └─ HMACSHA512( base64Url(header)+"."+base64Url(payload), secretKey )
  │      └─ 클레임(claim)들: sub, iss, exp, iat + 커스텀(id, role, name)
  └─ 메타 정보: { "alg": "HS512", "typ": "JWT" }
```
- **클레임** = payload에 담긴 정보 한 조각(key-value). 토큰이 "이 사용자는 kim이다, 10시에 만료된다"라고 **주장(claim)** 하는 거예요.
- ⚠️ payload는 **암호화가 아니라 인코딩**이에요. 누구나 디코딩해 볼 수 있어요 → **비밀번호 등 민감 정보 금지.** jwt.io에 토큰을 붙여넣어 직접 확인해 보세요.
- 서명 키는 서버만 알아요. 그래서 **"서명이 유효하다" = "이 서버가 발급했고 위조되지 않았다"** — 이게 세션 없이 사용자를 신뢰하는 근거예요. payload를 조작하면(role → ADMIN) 서명이 안 맞아 즉시 들켜요. JWT는 "내용을 숨기는" 게 아니라 **"변조를 막는"** 기술이에요.

### (3) 두 개의 파이프라인 — 로그인과 요청 인증
```
[로그인]  POST /api/users/login (JSON)
  → 우리 컨트롤러 → UserService.signIn()
  → AuthenticationManager.authenticate()      ← ★ form-login에선 필터가 하던 호출을 직접
       ├─ UserDetailService.loadUserByUsername()   (2번 과제 그대로!)
       └─ PasswordEncoder.matches()
  → 성공: TokenService.issueTokens() → access는 body로, refresh는 HttpOnly 쿠키로

[이후 요청]  GET /api/users/info + Authorization: Bearer <token>
  → TokenAuthenticationFilter                 ← ★ 우리가 구현
       ├─ 서명·만료 검증 (DB 조회 없음!)
       └─ 클레임 → User 복원 → SecurityContext에 등록
  → AuthorizationFilter가 인가 판단 → 컨트롤러 도달
```
검증부(`UserDetailService` + BCrypt)는 2번 과제와 **완전히 같아요.** 바뀐 건 "성공의 결과"(세션 저장 → 토큰 발급)와 "이후 요청의 증명"(세션 복원 → 토큰 검증)이에요.

### (4) access/refresh 이원화 — 왜 토큰이 두 개인가
- **access token (2h, localStorage)**: 매 요청에 실려 다녀 노출 위험이 커요 → 탈취돼도 피해 시간을 제한하려고 **짧게**.
- **refresh token (7d, HttpOnly 쿠키)**: 재발급 API에서만 쓰여요. HttpOnly라 **JS가 못 읽어요** → XSS가 훔칠 수 없어요. 짧은 access 때문에 매번 재로그인하는 불편을 이게 흡수해요.
- 보안 관점 정리: JWT를 헤더로 보내니 **CSRF는 성립 안 함**(쿠키 자동 전송을 악용하는 공격이므로). 대신 localStorage는 **XSS에 노출**돼요 — 그래서 더 귀한 refresh는 HttpOnly 쿠키에 두는 거예요. "어떤 공격을 막으려고 어디에 두는가"를 설명할 수 있어야 해요.

### (5) stateless의 빛과 그림자
- 빛: 서버 확장이 쉬워요. 서버 재시작에도 로그인이 유지돼요(2번 과제와 정반대 — 직접 실험!).
- 그림자 ①: **진짜 로그아웃이 안 돼요.** 쿠키/localStorage를 지워도 이미 발급된 토큰은 만료까지 유효해요. 강제 무효화하려면 서버 측 저장소(블랙리스트)가 필요한데, 그럼 stateless가 아니게 되죠. 트레이드오프예요.
- 그림자 ②: **페이지를 서버에서 못 막아요.** 주소창 입력·링크 클릭은 Bearer 헤더를 실을 수 없어서, `/admin` 페이지를 `hasRole`로 잠그면 관리자조차 못 들어가요. 그래서 **HTML은 빈 껍데기(공개), 인가는 데이터 API(`@PreAuthorize`)가 담당** — 페이지를 열 수 있다는 건 아무 권한도 증명하지 않아요.

```
[기억법] 로그인은 JSON(우리 컨트롤러가 받음) / 서명이 곧 신뢰(서버는 기억 안 함) / access는 짧게 refresh는 쿠키에 / 페이지는 껍데기, 인가는 API
```

---

## 4. 파일 구조 & 준비물

| 파일 | 역할 | |
|------|------|------|
| `User`, `Role`, `UserRepository`, `CustomUserDetails`, `UserDetailService`, 회원가입 일체 | 2번 과제 **재사용** | ♻️ |
| `JwtProperties` | yaml의 issuer/키/수명 바인딩 | 신규 |
| `TokenProvider`, `TokenStatus` | 토큰 생성·검증·해석 | 신규 |
| `TokenAuthenticationFilter` | 요청마다 토큰 → SecurityContext 복원 | 신규 |
| `SecurityConfig` | STATELESS + 필터 등록 + 401/403 + RoleHierarchy | 재작성 |
| `TokenService`, `TokenApiController` | 토큰 쌍 발급 / refresh 재발급 | 신규 |
| `UserService.signIn`, `UserApiController` | 로그인·로그아웃·정보·권한 API | 확장 |
| `CookieUtil` | HttpOnly 쿠키 추가/삭제 | 신규 |
| `common.js`, `hello.js`, `admin.js`, `signIn.js` | 토큰 저장·자동 첨부·재발급 흐름 | 신규/수정 |

**의존성** — 2번 과제 것에 **jjwt 3종**(`jjwt-api`, `jjwt-impl`, `jjwt-jackson`, 버전 `0.13.0`)을 추가해요.
⚠️ mvnrepository에서 `spring-security-jwt`가 검색되는데 **쓰면 안 돼요** — 2018년이 마지막 릴리스인 EOL 프로젝트예요. 라이브러리를 고를 땐 마지막 릴리스 날짜를 꼭 확인하세요.

**시크릿 키 생성** — 터미널에서:
```bash
openssl rand -base64 64
```

---

## 5. Step by Step

### Step 0. 프로젝트 준비 — form-login에서 출발

**할 일**
1. 2번 과제 프로젝트를 복사해요. `User`/`Role`/`UserRepository`/`CustomUserDetails`/`UserDetailService`/회원가입 흐름은 그대로 둬요.
2. **지워요**: `CustomAuthenticationSuccessHandler`, `CustomAuthenticationFailureHandler`, `SecurityConfig`의 `formLogin`/`logout` 설정. (세션에 저장하던 코드는 이번 방식과 안 맞아요)
3. `build.gradle`에 jjwt 3종을 추가하고 Gradle을 리로드해요.
4. `application.yaml`에 `jwt.issuer`, `jwt.secret_key`(openssl로 생성), `jwt.access-token-validity: 2h`, `jwt.refresh-token-validity: 7d`를 추가하고, `JwtProperties`로 바인딩해요.

<details>
<summary>💡 힌트 보기</summary>

- `@ConfigurationProperties(prefix = "jwt")` + `@Component` + Lombok `@Getter @Setter`. **relaxed binding** 덕분에 yaml의 `secret_key`도, `secret-key`도 `secretKey` 필드에 들어와요.
- 수명 필드를 `Duration` 타입으로 선언하면 `"2h"`, `"7d"` 같은 문자열이 자동 변환돼요. `Duration.ofHours(2)`를 코드 여기저기 하드코딩하지 않기 위한 준비예요.
- ⚠️ jjwt 버전을 임의로 올려 적으면(예: 존재하지 않는 0.13.5) **의존성 다운로드가 실패**하고, IDE가 `Cannot resolve symbol 'Jwts'`를 뿜어요. "코드 문제인 줄 알았는데 의존성 문제"인 대표 사례 — 버전은 Maven Central에서 확인하세요.
- ⚠️ 구버전(0.11.x) 예제 코드를 복사하면 `parserBuilder()`, `setSigningKey()` 등이 deprecated예요. 0.12+ 대응표:

| 0.11.x (옛 예제) | 0.12+ (이번 과제) |
|------|------|
| `Jwts.parserBuilder()` | `Jwts.parser()` |
| `.setSigningKey(key)` | `.verifyWith(key)` |
| `.parseClaimsJws(token)` | `.parseSignedClaims(token)` |
| `.getBody()` | `.getPayload()` |
| `.setSubject()` / `.setExpiration()` … | `.subject()` / `.expiration()` … |
| `.signWith(key, SignatureAlgorithm.HS512)` | `.signWith(key, Jwts.SIG.HS512)` |

</details>

**확인**: 컴파일되고 앱이 뜨면 준비 완료예요. (아직 로그인은 안 돼요 — 정상!)

---

### Step 1. TokenProvider ① — 토큰 만들기

**목표**: `User`를 받아 **서명된 JWT 문자열**을 만들어요. 클레임에 뭘 담을지, 뭘 담으면 안 되는지가 포인트예요.

**할 일**
1. `TokenProvider`에 `@PostConstruct init()`으로 `SecretKey`를 **한 번만** 만들어 필드에 캐싱해요 (`Keys.hmacShaKeyFor(Base64.getDecoder().decode(...))`).
2. `makeToken(User, Date)`: `Jwts.builder()`로 헤더(typ) + 등록 클레임(iss/iat/exp/sub) + 커스텀 클레임(id/role/name)을 채우고 `signWith(key, Jwts.SIG.HS512)` → `compact()`.
3. `generateToken(User, Duration)`: 현재 시각 + 수명으로 만료일을 계산해 `makeToken`을 호출해요.
4. 클레임 키(`"id"`, `"role"`, `"name"`)는 상수로 뽑아요 — 넣는 곳과 꺼내는 곳(Step 2)이 달라서, 문자열이 흩어지면 오타를 런타임에야 발견해요.

<details>
<summary>💡 힌트 보기</summary>

```java
private String makeToken(User user, Date expire) {
    Date now = new Date();
    return Jwts.builder()
            .header().type("JWT").and()
            .issuer(jwtProperties.getIssuer())
            .issuedAt(now)
            .expiration(expire)
            .subject(user.getUserId())
            .claim(CLAIM_ID, user.getId())
            .claim(CLAIM_ROLE, user.getRole().name())   // enum은 .name()으로 명시적으로
            .claim(CLAIM_NAME, user.getName())
            .signWith(secretKey, Jwts.SIG.HS512)
            .compact();     // ← build()가 아니라 compact()!
}
```

- 빌더의 마지막은 `build()`가 아니라 **`compact()`** — "세 조각을 압축해 문자열로 만든다"는 뜻이에요.
- role은 enum 객체 대신 `.name()` 문자열로 넣어요. 꺼낼 때 `Role.valueOf()`와 정확히 대칭이 되고, 직렬화 설정에 의존하지 않아요.
- **비밀번호는 절대 클레임에 넣지 마세요.** payload는 누구나 디코딩해 볼 수 있어요.
- 키가 64바이트 미만이면 `WeakKeyException` — 서버가 **뜨자마자** 죽어요. 요청이 와서야 터지는 것보다 낫죠(이게 `@PostConstruct`에서 키를 만드는 이유 중 하나예요).

</details>

**확인**: 테스트 코드나 임시 코드로 토큰을 하나 찍어서 **jwt.io에 붙여넣어 보세요.** payload의 클레임이 그대로 보이면(= 인코딩일 뿐임을 눈으로 확인) + "Invalid Signature"가 아니라 시크릿을 넣었을 때 서명이 검증되면 성공이에요.

---

### Step 2. TokenProvider ② — 검증하고 해석하기

**목표**: 토큰을 받아 **상태를 판정**하고(`VALID`/`EXPIRED`/`INVALID`), 클레임에서 **User를 복원**하고, 시큐리티용 **Authentication**을 만들어요.

**할 일**
1. `TokenStatus` enum을 만들어요. (1/2/3 같은 매직 넘버로 하면 호출부가 못 읽어요 — enum이면 `status == TokenStatus.EXPIRED`처럼 의미가 드러나고, switch 누락도 IDE가 잡아줘요)
2. `init()`에서 `JwtParser`도 캐싱해요: `Jwts.parser().verifyWith(secretKey).build()` — 파서는 스레드 세이프해서 재사용 가능해요.
3. `validateToken(String)`: `parseSignedClaims()`를 try-catch — 성공이면 `VALID`, `ExpiredJwtException`이면 `EXPIRED`, 그 외 예외는 `INVALID`.
4. `getTokenDetails(String)`: 클레임 → `User.builder()`로 복원. **DB 조회가 없다는 것**에 주목하세요.
5. `getAuthentication(User, String)`: principal 자리에 `CustomUserDetails`를 넣은 `UsernamePasswordAuthenticationToken`을 반환해요.

<details>
<summary>💡 힌트 보기</summary>

```java
public Authentication getAuthentication(User user, String token) {
    CustomUserDetails principal = CustomUserDetails.builder().user(user).build();
    return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
}
```

- **principal이란?** `Authentication`은 세 조각이에요 — principal("인증된 주체가 **누구**인가"), credentials(증명 수단), authorities(권한 목록). 생성자 인자 순서가 정확히 이 순서예요.
- principal에 `CustomUserDetails`를 넣는 이유: 나중에 컨트롤러에서 `@AuthenticationPrincipal CustomUserDetails`로 **주입받아** `getUser()`로 도메인 User까지 바로 접근할 수 있어요(Step 4에서 사용).
- 검증(`validateToken`)과 해석(`getTokenDetails`)을 나누면 파싱이 두 번이지만, "상태 확인 → 정보 추출"이라는 두 단계가 읽기 쉬워요. 학습 단계에선 가독성이 우선이에요.
- `getTokenDetails`가 DB를 안 거치는 게 stateless의 실체예요: **필요한 정보를 토큰에 다 담아뒀으니까** 서버는 조회 없이 서명만 믿으면 돼요. (뒤집으면: 토큰 발급 후 DB에서 role을 바꿔도, 기존 토큰엔 옛 role이 살아 있어요 — 만료까지. 이것도 트레이드오프!)

</details>

**확인**: 컴파일되면 통과예요. 셋의 역할을 한 문장씩으로 말해 보세요 — 검증(진짜인가), 해석(누구인가), 변환(시큐리티 규격으로).

---

### Step 3. 로그인 = 검증 + 토큰 발급

**목표**: form-login에서 **필터가 하던 일을 우리가 직접** 해요. `AuthenticationManager`로 아이디/비밀번호를 검증하고, 성공하면 세션 대신 **토큰 두 개**를 발급해요.

**할 일**
1. `SecurityConfig` 뼈대: csrf/formLogin/httpBasic/logout 전부 disable, **`SessionCreationPolicy.STATELESS`**, permitAll 목록(`/api/users/join`, `/api/users/login`, `/css/**`, `/js/**`, `/error` 등) + `anyRequest().authenticated()`. `AuthenticationManager`와 `BCryptPasswordEncoder` 빈도 등록해요.
2. `TokenService.issueTokens(User)`: access(설정의 2h)와 refresh(7d) 한 쌍을 발급해 record `TokenPair`로 반환해요.
3. `UserService.signIn(username, password)`: `authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(...))` → principal에서 `User`를 꺼내 → `issueTokens()` → DTO로 반환해요.
4. `UserApiController`의 `POST /api/users/login`: **access token은 응답 body로, refresh token은 HttpOnly 쿠키로** 내려요(`CookieUtil.addCookie`, 쿠키 maxAge = refresh 수명). body의 refreshToken 필드는 null로 지워요.
5. `signIn.js`: `/api/users/login`에 **JSON**으로 보내고, 응답의 `accessToken`을 `localStorage`에 저장해요.

<details>
<summary>💡 힌트 보기</summary>

```java
// UserService — form-login에서 필터가 하던 호출을 직접
Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(username, password));
User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
TokenService.TokenPair tokens = tokenService.issueTokens(user);
```

- `authenticate()` 내부는 2번 과제와 같아요: `DaoAuthenticationProvider` → 우리 `UserDetailService` → `matches()`. 실패하면 `BadCredentialsException`이 던져져요. **UserDetailsService 빈은 정확히 하나**여야 자동 연결돼요 — 실수로 두 개 만들면 어느 쪽도 연결 안 되고 로그인이 전부 실패해요.
- `SecurityContextHolder.setAuthentication(...)`을 로그인에서 호출할 필요 없어요 — STATELESS라 이 요청이 끝나면 어차피 사라져요. 인증 컨텍스트는 **다음 요청부터 필터가** 채워요(Step 4).
- 쿠키: `setHttpOnly(true)`(XSS 방어), `setPath("/")`, 로컬 개발은 `setSecure(false)`(Safari는 localhost HTTP에서 Secure 쿠키를 버려요). **쿠키 수명과 토큰 수명을 같은 설정값에서** 가져와야 불일치가 안 생겨요.
- 응답에서 refreshToken을 빼는 법: `dto.setRefreshToken(null)` + DTO에 `@JsonInclude(NON_NULL)` — body엔 access만, refresh는 쿠키로만.
- ⭐ 2번 과제와 비교: 그땐 로그인이 form-urlencoded(필터는 파라미터만 읽음), 이번엔 JSON(우리 컨트롤러의 `@RequestBody`가 받음). **"누가 요청을 받느냐"가 형식을 결정**해요.

</details>

**확인**: curl로 로그인해서 ① 200 + body에 `accessToken`, ② `Set-Cookie: refreshToken=...; HttpOnly`, ③ body에 refreshToken이 **없음** — 셋 다 확인되면 통과예요.

---

### Step 4. 인증 필터 — 토큰에서 인증 상태를 복원한다

**목표**: 이번 과제의 심장이에요. 요청마다 `Authorization: Bearer` 헤더의 토큰을 검증해서 `SecurityContext`를 채우는 필터를 만들고, 보호된 API(`/api/users/info`)로 확인해요.

**할 일**
1. `TokenAuthenticationFilter extends OncePerRequestFilter`: 헤더에서 토큰 추출(`resolveToken`) → 없으면 그냥 통과 → `VALID`면 `getTokenDetails` + `getAuthentication`으로 `SecurityContextHolder`에 등록 → `EXPIRED`면 **401로 즉시 응답 후 return**(프론트가 refresh를 시도할 신호).
2. `SecurityConfig`에 `.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)`로 등록해요.
3. 401/403 처리: `authenticationEntryPoint`(미인증)와 `accessDeniedHandler`(권한 부족)를 등록하되, **`/api/**` 요청엔 JSON + 상태 코드**, 페이지 요청엔 리다이렉트로 분기해요.
4. `GET /api/users/info`: `@AuthenticationPrincipal CustomUserDetails`로 유저를 주입받아 정보를 반환해요.
5. 메인 화면(`hello.js`)에서 `setupAjax()`(모든 ajax에 Bearer 자동 첨부) + `/api/users/info` 호출로 "OO님" 표시해요.

<details>
<summary>💡 힌트 보기</summary>

- 토큰이 없을 때 401을 직접 주지 말고 **그냥 통과**시키세요. permitAll 경로일 수도 있으니까요. 보호된 경로라면 체인 끝의 `AuthorizationFilter`가 알아서 거부하고 entryPoint(401)로 보내줘요. 역할 분리: **필터는 "복원"만, 거부는 "인가"가.**
- 필터 위치: 인가 판단은 체인 **맨 끝**에서 일어나요. 그 전에 SecurityContext를 채워둬야 "인증된 요청"으로 취급돼요.
- 401 vs 403 구분: **401 = 누군지 모름**(토큰 없음/무효) → entryPoint, **403 = 누군지 알지만 자격 없음** → accessDeniedHandler. 면접 단골이에요.
- API에 리다이렉트로 응답하면 ajax가 302를 따라가 **HTML을 받아버려요**(그리고 파싱 에러). 그래서 `/api/**`는 JSON으로 분기하는 거예요.
- `@AuthenticationPrincipal`은 `SecurityContext`의 Authentication에서 **principal을 꺼내 파라미터로 주입**해주는 어노테이션이에요. Step 2에서 principal에 `CustomUserDetails`를 넣어둔 게 여기서 빛을 봐요 — request attribute로 넘기는 우회 없이 표준 방식으로 끝나요.

</details>

**확인**: ① Bearer 토큰과 함께 `/api/users/info` → 200 + 내 정보, ② 토큰 없이 → 401 JSON, ③ 브라우저 메인에서 이름이 표시되면 통과예요. ⭐ 추가 실험: **서버를 재시작하고** 새로고침해 보세요. 로그인이 유지돼요! (2번 과제와 정반대 — 왜인지 설명해 보세요)

---

### Step 5. Refresh 재발급 + 로그아웃 — 만료를 부드럽게 넘기기

**목표**: access(2h)가 만료됐을 때 사용자 몰래 재발급하는 흐름을 완성해요. 그리고 "stateless에서 로그아웃이란 무엇인가"를 생각해 봐요.

**할 일**
1. `TokenService.refreshToken(Cookie[])`: 쿠키에서 refresh를 꺼내 검증 → `VALID`면 `issueTokens()`로 **한 쌍을 새로** 발급(rotation) → DTO 반환. 아니면 `validated=false`.
2. `TokenApiController`의 `POST /api/tokens/refresh`: 성공 시 새 refresh를 **기존 쿠키에 덮어쓰고**(같은 이름·같은 path면 브라우저가 자동 교체) access를 body로. 실패 시 **401 + `ErrorResponseDto`**(다른 에러 응답과 형태 통일).
3. ⚠️ `/api/tokens/refresh`를 **permitAll에 추가**해요. 이걸 부르는 시점은 access가 만료된 상태 — 인증 필요 경로면 갱신이 영원히 불가능해요.
4. `common.js`의 `refreshTokens()`(Promise)와 `hello.js`의 흐름: access 없으면 refresh 시도 → 조회 401이면 refresh 후 **한 번 재시도** → refresh도 실패면 로그인 페이지로.
5. 로그아웃: `POST /api/users/logout`에서 refresh 쿠키 삭제 + 프론트에서 localStorage의 access 삭제. 이것도 permitAll(만료된 토큰으로도 로그아웃은 돼야죠).

<details>
<summary>💡 힌트 보기</summary>

- 재발급 응답도 로그인과 같은 원칙: **access는 body, refresh는 쿠키.** 프론트는 `localStorage.setItem('accessToken', ...)`만 갱신하면 돼요 (`$.ajaxSetup`의 beforeSend가 매번 localStorage를 읽으니 재설정 불필요).
- rotation의 한계: 새 refresh를 발급해도 **이전 refresh는 만료까지 유효**해요. DB 없이 가는 이 과제에선 무효화가 불가능해요 — 실무에선 refresh를 DB/Redis에 저장해 "사용된 토큰 폐기"까지 해요. **"왜 이 예제에선 못 하는가"를 설명할 수 있으면** 이 Step의 목표 달성이에요.
- 로그아웃도 마찬가지: 쿠키와 localStorage를 지웠으니 정상 클라이언트는 더 못 쓰지만, **이미 발급된 access는 만료(2h)까지 살아 있어요.** "stateless 로그아웃 = 클라이언트가 잊어버리기"예요.
- 이 시점 최종 프론트 흐름:
```
페이지 진입 → access 있음? ─예→ /api/users/info ─200→ 렌더링
                 │아니오                └─401→ refresh → 재시도 ─실패→ 로그인으로
                 └→ refresh 시도 ─성공→ 조회   ─실패→ 로그인으로
```

</details>

**확인**: 개발자도구 → Application에서 `accessToken`을 지우고 새로고침 → **refresh 쿠키만으로 조용히 복구**되면 통과예요. 쿠키까지 지우면 로그인 페이지로 가야 해요. 로그아웃 → 새로고침 → 로그인 페이지 확인까지.

---

### Step 6. 인가 — 역할에 따라 API를 잠근다

**목표**: `@PreAuthorize`로 메서드 단위 인가를 적용하고, `RoleHierarchy`로 "ADMIN은 USER 권한도 포함"을 선언해요. 회원가입에서 role을 선택하게 해서 두 계정으로 실험해요.

**할 일**
1. 회원가입 화면에 role `<select>`(ROLE_USER/ROLE_ADMIN)를 추가하고, DTO에 `Role role` 필드를 받아 저장해요.
2. `SecurityConfig`에 `@EnableMethodSecurity(prePostEnabled = true)`를 붙여요.
3. 권한 테스트 API 두 개: `GET /api/users/user`에 `@PreAuthorize("hasRole('USER')")`, `GET /api/users/admin`에 `@PreAuthorize("hasRole('ADMIN')")`.
4. `RoleHierarchy` 빈: `RoleHierarchyImpl.withDefaultRolePrefix().role("ADMIN").implies("USER").build()`.
5. 메인 화면에 버튼 두 개(각 API 호출, 성공/403 메시지 표시)와, 관리자 페이지(`/admin`) — **페이지는 permitAll**, 진입 즉시 admin API를 호출해서 403이면 쫓아내는 구조로.

<details>
<summary>💡 힌트 보기</summary>

- `hasRole('USER')`는 내부적으로 `ROLE_` 접두사를 붙여 **`ROLE_USER` 권한**을 찾아요. `CustomUserDetails.getAuthorities()`가 `user.getRole().name()`(= `ROLE_USER`)을 반환하니 그대로 매칭돼요. 접두사를 이중으로 붙이는 실수(`hasRole('ROLE_USER')`) 조심!
- `RoleHierarchy`는 **빈으로 등록만 하면** URL 인가와 `@PreAuthorize` 양쪽에 자동 반영돼요(Spring Security 6.3+). 계층이 없으면 ADMIN이 `hasRole('USER')` API에서 403을 받아요 — 계층 등록 전후를 비교 실험해 보세요.
- `/admin` **페이지**에 `hasRole`을 걸면 안 되는 이유(핵심 개념 (5)): 페이지 이동엔 Bearer 헤더가 없어요 → 관리자도 차단돼요. 껍데기는 공개하고 인가는 API가. "UI에서 링크를 숨기는 건 편의일 뿐, 보안은 항상 서버가"까지 세트로 기억하세요.
- DTO에서 `.role(role != null ? role : Role.ROLE_USER)`로 방어하세요 — `@Builder.Default`는 빌더 메서드를 **아예 호출하지 않을 때만** 동작하고, `.role(null)`을 넘기면 null이 그대로 들어가요.

</details>

**확인**:
| 계정 | `/api/users/user` | `/api/users/admin` | `/admin` 페이지 |
|------|------|------|------|
| ROLE_USER | ✅ 200 | ❌ 403 JSON | 진입 즉시 쫓겨남 |
| ROLE_ADMIN | ✅ 200 (계층 덕분!) | ✅ 200 | ✅ 표시 |

이 표대로 동작하면 완성이에요. 🎉

---

## ⚠️ 자주 나는 오류 (막히면 여기부터)

| 증상 | 원인 / 해결 |
|------|------|
| IDE가 `Cannot resolve symbol 'Jwts'` | jjwt **버전이 존재하지 않아** 다운로드 실패(예: 0.13.5). Maven Central에서 실제 버전 확인 → Gradle 리로드. |
| `setSigningKey`, `parserBuilder` 등이 deprecated | 0.11.x API예요. Step 0 힌트의 0.12+ 대응표로 교체. |
| 서버가 뜨자마자 `WeakKeyException` | 시크릿 키가 짧아요. HS512는 64바이트 이상 → `openssl rand -base64 64`. |
| `Can't configure requestMatchers after anyRequest` | `authorizeHttpRequests`를 **두 번** 선언했거나 `anyRequest()` 뒤에 `requestMatchers`를 추가했어요. 블록 하나로 합치고 `anyRequest()`는 맨 마지막 한 번만. |
| `/access-denied`가 무한 반복 호출됨 (리다이렉트 루프) | `/error`가 잠겨 있어요. 404 등 모든 에러는 `/error`로 **포워딩**되는데, 이게 인증 필요면 "에러→401→리다이렉트→에러→…" 루프. `/error`를 permitAll에. |
| 아이디/비밀번호가 정확한데 로그인이 항상 실패 | ① `UserDetailsService` 빈이 **2개** → 하나로. ② 회원가입 때 `encode()` 누락(2번 과제와 동일한 함정). |
| access 만료 후 재발급이 안 되고 로그인으로 튕김 | `/api/tokens/refresh`가 permitAll이 아니에요. 만료 상태에서 부르는 API라 **반드시 개방**. |
| 로그인은 되는데 쿠키가 안 심김 (특히 Safari) | `setSecure(true)` 상태로 HTTP 접속 중이에요. 로컬 개발은 `false`, 운영(HTTPS)은 `true`. |
| 관리자인데 `/admin` 페이지에서 쫓겨남 / 페이지에 `hasRole` 걸었더니 아무도 못 들어감 | 페이지 이동은 Bearer 헤더를 못 실어요. 페이지는 permitAll + 인가는 API의 `@PreAuthorize`로 (핵심 개념 (5)). |
| ADMIN 계정이 USER용 API에서 403 | `RoleHierarchy` 빈이 없어요. ADMIN은 `ROLE_USER` 권한을 "자동으로" 갖지 않아요 — 계층을 선언해야. |
| ajax 응답이 HTML(로그인/에러 페이지)로 옴 | 401/403을 리다이렉트로 처리 중이에요. `/api/**` 요청은 상태 코드 + JSON으로 분기. |
| 재발급 직후 다음 요청이 또 401 | 프론트가 새 accessToken을 localStorage에 저장 안 했거나, 갱신 성공 후 원래 요청을 **재시도하지 않는** 구조예요. |
| 토큰은 유효한데 role 변경이 반영 안 됨 | 버그가 아니라 **stateless의 본질**이에요. 토큰엔 발급 시점의 클레임이 박제돼요 — 만료 후 재발급 때 반영돼요. |
| 이름에 `<script>alert(1)</script>`를 넣고 가입했더니 화면에서 **스크립트가 실행됨** | **XSS**예요. 입력값을 이스케이프 없이 렌더링하는 코드가 원인 — jQuery는 `.html()` 대신 **`.text()`**, Thymeleaf는 `th:utext` 대신 **`th:text`**. 토큰 방식에서 XSS는 치명적이에요: 주입된 스크립트가 `localStorage.getItem('accessToken')`으로 **토큰을 통째로 훔칠 수 있어요.** refresh token이 무사한 이유(HttpOnly 쿠키 = JS 접근 불가)와 묶어서 기억하세요. |

---

## 6. 학습 체크

- [ ] JWT 세 부분(Header/Payload/Signature)의 역할과, payload가 암호화가 아닌 이유를 설명할 수 있다
- [ ] 클레임이 무엇인지, 등록된 클레임(sub/iss/exp/iat)과 비공개 클레임을 구분할 수 있다
- [ ] "서명이 유효하다"가 왜 신뢰의 근거가 되는지(서버만 아는 키) 설명할 수 있다
- [ ] 로그인 요청이 2번 과제(form-urlencoded)와 달리 왜 JSON인지 안다 (받는 주체가 필터 → 컨트롤러)
- [ ] access/refresh를 나누는 이유와 각각의 저장 위치·수명 선택 근거를 말할 수 있다
- [ ] XSS와 CSRF 중 localStorage가 취약한 것, HttpOnly 쿠키가 막는 것을 구분할 수 있다
- [ ] 필터는 "복원"만 하고 거부는 "인가"가 한다는 역할 분리를 안다 (토큰 없으면 그냥 통과시키는 이유)
- [ ] 401(미인증)과 403(권한 부족)의 차이와 각각의 처리 지점을 안다
- [ ] principal이 무엇인지, `@AuthenticationPrincipal`이 어떻게 동작하는지 안다
- [ ] 서버 재시작 실험에서 세 방식(Basic/form/token)의 로그인 유지가 왜 다른지 설명할 수 있다
- [ ] stateless에서 "진짜 로그아웃"과 "즉시 권한 회수"가 왜 안 되는지, 실무에선 어떻게 보완하는지 안다
- [ ] 페이지 인가가 불가능한 이유와 "껍데기는 공개, 인가는 API" 패턴을 설명할 수 있다

## 7. 최종 완성 체크리스트

- [ ] 로그인 성공 시 access token(body) + refresh token(HttpOnly 쿠키)이 발급된다
- [ ] 응답 body에 refreshToken이 노출되지 않는다
- [ ] Bearer 토큰으로 `/api/users/info`가 조회되고, 토큰 없이는 401 JSON이 온다
- [ ] jwt.io에서 payload가 열리는 것과, 조작 시 서명 검증이 실패하는 것을 확인했다
- [ ] localStorage의 access를 지워도 refresh 쿠키만으로 조용히 복구된다 (재발급 시 쿠키도 새로 덮임)
- [ ] refresh까지 없거나 만료면 로그인 페이지로 안내된다
- [ ] 로그아웃하면 쿠키·localStorage가 정리된다 (+ 기존 access가 만료까지 유효한 이유를 설명할 수 있다)
- [ ] ROLE_USER/ROLE_ADMIN 두 계정으로 Step 6의 확인 표대로 동작한다
- [ ] 서버를 재시작해도 로그인이 유지된다 — 그리고 그 이유를 2번 과제와 대조해 설명할 수 있다