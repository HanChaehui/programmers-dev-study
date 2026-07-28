# 폼 로그인으로 회원가입·로그인·로그아웃 만들기 (DB + BCrypt + 세션)

> HTTP Basic이 "브라우저 팝업 + 매 요청 헤더(stateless)"였다면, 폼 로그인은 **우리가 만든 로그인 화면 + 세션(stateful)**이에요. 사람이 쓰는 일반적인 웹 서비스의 표준 방식이죠.
> 이번엔 유저도 진짜예요 — 인메모리가 아니라 **DB에 회원가입**하고, 비밀번호는 **BCrypt 해시**로 저장하고, 로그인하면 **세션**이 생기고, **로그아웃도 진짜로** 돼요(Basic에선 안 되던 그것!).
> 코드보다 중요한 건 흐름이에요: **필터가 로그인 요청을 가로채고, 우리는 "유저를 찾는 방법"과 "성공/실패 후 처리"만 끼워 넣는다.**
>
> 💡 각 Step의 **힌트는 접혀 있어요.** 먼저 스스로 만들어 보고 막히면 펼치세요.

<details>
<summary>✅ 최종 완성 코드 보기 (먼저 직접 만들어 본 뒤 펼쳐서 비교하세요)</summary>

> Step 5까지 모두 반영한 **완성본**이에요. 패키지는 `config`, `config.security`, `controller`, `service`, `domain`, `dto`, `exception`으로 나눴어요. Lombok 설정이 되어 있어야 해요.

**`build.gradle`** (의존성)
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'   // 부트 3.x는 spring-boot-starter-web
    compileOnly 'org.projectlombok:lombok'
    runtimeOnly 'com.mysql:mysql-connector-j'
    annotationProcessor 'org.projectlombok:lombok'
}
```

**`application.yaml`**
```yaml
spring:
  application:
    name: form-login
  datasource:
    url: jdbc:mysql://localhost:3306/java_basic?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: 1234
```

**`user` 테이블** (MySQL에서 미리 실행)
```sql
CREATE TABLE user (
      id BIGINT NOT NULL AUTO_INCREMENT,
      name VARCHAR(20),
      email VARCHAR(50),
      user_id VARCHAR(50),
      password VARCHAR(100),                -- BCrypt 해시는 60자 → 넉넉하게
      role ENUM('ROLE_USER', 'ROLE_ADMIN') DEFAULT 'ROLE_USER',
      PRIMARY KEY (id)
);
```

**`domain/entity/User.java`**
```java
@Entity
@Table(name = "user")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String name;

    @Column(length = 50)
    private String email;

    @Column(length = 50)
    private String userId;

    @Column(length = 100)
    private String password;      // 평문이 아니라 BCrypt 해시가 저장된다

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.ROLE_USER;
}
```

**`domain/entity/Role.java`**
```java
public enum Role {
    ROLE_USER,
    ROLE_ADMIN
}
```

**`domain/repository/UserRepository.java`**
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);
    boolean existsByUserId(String userId);
}
```

**`dto` 패키지** — 요청/응답 그릇
```java
@Getter
public class SignUpRequestDto {
    private String userId;
    private String password;
    private String userName;

    public User toUser(String encodedPassword) {
        return User.builder()
                .userId(userId)
                .password(encodedPassword)   // 해시된 비밀번호를 받아 엔티티를 만든다
                .name(userName)
                .build();
    }
}

@Getter @AllArgsConstructor
public class SignUpResponseDto {
    private String url;
}

@Getter @Builder
public class SignInResponseDto {
    private boolean isLoggedIn;
    private String url;
    private String userName;
    private String userId;
    private String message;
}

@Getter @AllArgsConstructor
public class ErrorResponseDto {
    private int status;
    private String message;
}
```

**`exception` 패키지** — 중복 아이디 409 처리
```java
public class DuplicateUserIdException extends RuntimeException {
    public DuplicateUserIdException(String message) {
        super(message);
    }
}

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DuplicateUserIdException.class)
    public ResponseEntity<ErrorResponseDto> duplicateUserIdException(DuplicateUserIdException e) {
        log.warn("409 응답 : {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDto(HttpStatus.CONFLICT.value(), e.getMessage()));
    }
}
```

**`service/UserService.java`** — 회원가입 (시큐리티 밖, 일반 MVC)
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(SignUpRequestDto request) {
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new DuplicateUserIdException("이미 사용 중인 아이디입니다.");
        }
        // 로그인 때 대조할 BCrypt 해시가 여기서 만들어진다
        User user = request.toUser(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }
}
```

**`config/security/CustomUserDetails.java`** — 우리 User를 시큐리티 규격으로 포장
```java
@Getter
@Builder
public class CustomUserDetails implements UserDetails {
    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    public String getPassword() { return user.getPassword(); }   // DB의 해시

    @Override
    public String getUsername() { return user.getUserId(); }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}
```

**`service/UserDetailService.java`** — "이 아이디의 유저를 이렇게 찾아라"
```java
@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserId(username)
                .orElseThrow(() -> new UsernameNotFoundException(username + " not found"));

        return CustomUserDetails.builder().user(user).build();
    }
}
```

**`config/security/CustomAuthenticationSuccessHandler.java`** — 성공: 세션 저장 + 200 JSON
```java
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;   // 부트 4.x는 tools.jackson.databind, 3.x는 com.fasterxml.jackson.databind

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // 화면(home.html)에서 쓸 정보를 세션에 저장
        HttpSession session = request.getSession();
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("userName", user.getName());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json; charset=utf-8");

        SignInResponseDto dto = SignInResponseDto.builder()
                .isLoggedIn(true)
                .message("로그인 성공")
                .url("/")
                .userId(user.getUserId())
                .userName(user.getName())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(dto));
    }
}
```

**`config/security/CustomAuthenticationFailureHandler.java`** — 실패: 401 JSON
```java
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=utf-8");

        SignInResponseDto dto = SignInResponseDto.builder()
                .isLoggedIn(false)
                .message("로그인 실패\n다시 로그인해주세요.")
                .url("/users/login")
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(dto));
    }
}
```

**`config/SecurityConfig.java`** — 전체 조립
```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 학습용으로 비활성화. AJAX에 토큰을 실어야 하는 부담을 덜기 위함 (핵심 개념 (5) 참고)
                .csrf(AbstractHttpConfigurer::disable)

                // 인가 규칙: 위에서부터 순서대로 매칭 → 구체적인 것 먼저, anyRequest()는 마지막
                .authorizeHttpRequests(auth -> auth
                        // 회원가입(계정이 아직 없는 사람용)과 정적 리소스(안 열면 로그인 화면이 깨짐)는 개방
                        .requestMatchers("/users/join", "/api/users/join", "/css/**", "/js/**").permitAll()
                        .anyRequest().authenticated())

                // 폼 로그인: UsernamePasswordAuthenticationFilter 활성화 + 커스터마이징
                .formLogin(form -> form
                        .loginPage("/users/login")            // GET: 우리가 만든 로그인 화면
                        .loginProcessingUrl("/users/login")   // POST: 필터가 가로챈다 (컨트롤러 불필요!)
                        .usernameParameter("userId")          // 폼이 보내는 파라미터 이름과 맞춘다 (기본값 username)
                        .passwordParameter("password")
                        .successHandler(customAuthenticationSuccessHandler)   // 리다이렉트 대신 JSON
                        .failureHandler(customAuthenticationFailureHandler)   // 401 + JSON
                        .permitAll())                          // 로그인하려는 사람은 당연히 미인증 상태

                // 로그아웃: LogoutFilter가 처리 (역시 컨트롤러 불필요)
                .logout(logout -> logout
                        .logoutUrl("/users/logout")
                        .logoutSuccessUrl("/users/login")
                        .invalidateHttpSession(true)          // 서버 쪽: 세션 무효화
                        .deleteCookies("JSESSIONID")          // 브라우저 쪽: 죽은 세션 ID 쿠키 정리
                        .permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();   // 이번엔 진짜 인코더!
    }
}
```

**`controller`** — 화면 3개 + 회원가입 API
```java
@Controller
public class HomeController {
    @GetMapping("/")
    public String home() { return "home"; }
}

@Controller
@RequestMapping("/users")
public class UserController {
    @GetMapping("/join")
    public String join() { return "sign-up"; }

    @GetMapping("/login")
    public String login() { return "login"; }
}

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApiController {
    private final UserService userService;

    @PostMapping("/join")
    public SignUpResponseDto signUp(@RequestBody SignUpRequestDto request) {
        userService.signUp(request);
        return new SignUpResponseDto("/users/login");
    }
}
```

**`templates/login.html`** (핵심 부분) — `sign-up.html`도 같은 구조(아이디/비밀번호/이름 + `signUp.js`)
```html
<html xmlns:th="http://www.thymeleaf.org">
<head>
  <script src="https://code.jquery.com/jquery-3.7.1.js" crossorigin="anonymous"></script>
  <script th:src="@{/js/signIn.js}"></script>
</head>
<body>
  <h2>로그인</h2>
  <input type="text" id="user_id" placeholder="아이디를 입력하세요" required>
  <input type="password" id="password" placeholder="비밀번호를 입력하세요" required>
  <input id="signin" type="submit" value="로그인">
  <a href="/users/join">회원가입</a>
</body>
</html>
```

**`templates/home.html`** (핵심 부분)
```html
<h2 th:text="|${session.userName}님 환영합니다|">환영합니다</h2>
<a href="/users/logout">로그아웃</a>
```

**`static/js/signIn.js`** — ★ 로그인은 JSON이 아니라 form-urlencoded로!
```javascript
$(document).ready(() => {
    $('#signin').click(() => {
        let formData = {
            userId: $('#user_id').val(),
            password: $('#password').val()
        }

        $.ajax({
            type: 'POST',
            url: '/users/login',   // UsernamePasswordAuthenticationFilter가 가로채는 URL
            data: formData,        // JSON.stringify 금지! 필터는 요청 "파라미터"만 읽는다
            dataType: 'json',
            success: function(response) {          // 성공 핸들러의 200 JSON
                alert(response.message);
                window.location.href = response.url;
            },
            error: function(xhr) {                 // 실패 핸들러의 401 JSON
                let response = xhr.responseJSON;
                alert(response && response.message ? response.message : '로그인 중 오류가 발생했습니다.');
            }
        });
    });
});
```

**`static/js/signUp.js`** — 회원가입은 일반 API라서 JSON으로 보낸다 (로그인과 비교!)
```javascript
$(document).ready(() => {
    $('#signup').click(() => {
        let formData = {
            userId: $('#user_id').val(),
            password: $('#password').val(),
            userName: $('#user_name').val()
        }

        $.ajax({
            type: 'POST',
            url: '/api/users/join',
            data: JSON.stringify(formData),                  // 이쪽은 JSON (우리 컨트롤러의 @RequestBody가 받음)
            contentType: 'application/json; charset=utf-8',
            dataType: 'json',
            success: function(response) {
                alert('회원가입이 성공했습니다.\n로그인해주세요.');
                window.location.href = response.url;
            },
            error: function(error) {
                alert('회원가입 중 오류가 발생했습니다.');
            }
        });
    });
});
```

**동작 확인**
```bash
# 회원가입 (JSON)
curl -i -H "Content-Type: application/json" \
     -d '{"userId":"kim","password":"1234","userName":"김개발"}' \
     http://localhost:8080/api/users/join
# → 200 {"url":"/users/login"}   (같은 아이디로 또 하면 409)

# 로그인 (form-urlencoded — 필터가 가로챈다)
curl -i -d "userId=kim&password=1234" http://localhost:8080/users/login
# → 200 {"isLoggedIn":true, ...}  + Set-Cookie: JSESSIONID=...
```

</details>

---

## 0. 먼저 알아둘 점

- 완성하면 이렇게 동작해요: **회원가입 → DB에 BCrypt 해시로 저장 → 로그인하면 세션 생성(JSESSIONID 쿠키) → 홈에서 "OO님 환영합니다" → 로그아웃하면 진짜로 풀림.** 미인증 상태로 홈에 접근하면 로그인 페이지로 리다이렉트돼요.
- **MySQL이 필요해요.** `java_basic` 데이터베이스에 `user` 테이블을 먼저 만들어요(완성 코드의 SQL 참고). BCrypt 해시는 60자라서 `password` 컬럼을 `VARCHAR(100)`으로 넉넉히 잡아요.
- ⚠️ **이 과제 최대의 함정**: 로그인 요청은 **JSON으로 보내면 안 돼요.** `UsernamePasswordAuthenticationFilter`는 요청 **파라미터**(form-urlencoded)만 읽어요. JSON 본문은 쳐다보지도 않아서, 아이디/비밀번호를 정확히 입력해도 **항상 실패**해요.
- ⚠️ 폼의 파라미터 이름이 기본값(`username`)이 아니면 — 우리는 `userId`를 써요 — `usernameParameter("userId")`로 **반드시 맞춰야** 해요. 안 맞으면 필터가 값을 못 찾아 항상 실패해요.
- 로그인 처리(POST /users/login)와 로그아웃엔 **컨트롤러를 만들지 않아요.** 요청이 DispatcherServlet에 도달하기 **전에** 필터가 가로채요. "내가 안 만든 URL이 동작한다"가 어색하면 정상이에요 — 그게 이번 학습 포인트예요.
- 학습 집중을 위해 CSRF는 **꺼요.** 왜 끄는지, 실서비스에선 어때야 하는지는 핵심 개념 (5)에서 다뤄요.

---

## 1. 무엇을 만드나요?

| 주소 | 하는 일 | 누가 처리? | 접근 |
|------|------|------|------|
| `GET /users/join` | 회원가입 화면 | 컨트롤러 | 개방 |
| `POST /api/users/join` | 회원가입 API (JSON) | 컨트롤러 + 서비스 | 개방 |
| `GET /users/login` | 로그인 화면 | 컨트롤러 | 개방 |
| `POST /users/login` | 로그인 처리 | **필터** (컨트롤러 없음!) | 개방 |
| `GET /` | 홈 — "OO님 환영합니다" | 컨트롤러 | **로그인 필요** |
| `GET /users/logout` | 로그아웃 | **필터** (컨트롤러 없음!) | 개방 |

**흐름 예시**: 로그인 안 하고 `/` 접근 → `/users/login`으로 리다이렉트 → 로그인 성공 → `alert("로그인 성공")` → 홈에서 `김개발님 환영합니다` → 로그아웃 → 다시 로그인 페이지.

---

## 2. 학습 목표

| 개념 | 어디서 배우나 |
|------|------|
| 회원가입 시 BCrypt로 해싱해 저장 (`encode`) | Step 1 |
| `UserDetailsService`/`UserDetails`로 DB 유저를 시큐리티에 연결 | Step 2 |
| `formLogin` 설정: loginPage / loginProcessingUrl / usernameParameter | Step 3 |
| 인가 규칙(`permitAll` vs `authenticated`)과 순서 | Step 3 |
| 성공/실패 핸들러 커스터마이징 (AJAX 대응 JSON 응답) | Step 4 |
| 세션 기반 인증 유지(JSESSIONID)와 로그아웃 | Step 5 |

---

## 3. 핵심 개념

### (1) HTTP Basic vs 폼 로그인 — 뭐가 달라졌나
| 구분 | HTTP Basic (지난 과제) | 폼 로그인 (이번) |
|------|------|------|
| 로그인 화면 | 브라우저 기본 팝업 | **우리가 만든 HTML** |
| 자격증명 전달 | 매 요청 `Authorization` 헤더 | **최초 1회만** 요청 본문(파라미터) |
| 상태 | stateless (서버가 기억 안 함) | **stateful** — 세션 + JSESSIONID 쿠키 |
| 로그인 유지의 주인 | 브라우저(자격증명 캐싱) | **서버**(세션) |
| 로그아웃 | 사실상 불가 | **가능** (세션 무효화) |

### (2) 인증 흐름 — 필터가 가로채고, 우리는 두 조각만 끼운다
```
POST /users/login (userId=kim&password=1234)
  → UsernamePasswordAuthenticationFilter  (loginProcessingUrl 가로챔, 미인증 토큰 생성)
  → AuthenticationManager (ProviderManager)
  → DaoAuthenticationProvider
       ├─ UserDetailsService.loadUserByUsername()  ← ★ 우리가 구현 (DB 조회)
       └─ PasswordEncoder.matches(입력값, DB 해시)  ← ★ 우리가 빈 등록 (BCrypt)
  → 성공: SecurityContext에 저장 → HttpSession에 보관 → JSESSIONID 쿠키 발급
         → AuthenticationSuccessHandler  ← ★ 우리가 교체 (JSON 응답)
  → 실패: AuthenticationFailureHandler   ← ★ 우리가 교체 (401 JSON)
```
전체 파이프라인은 시큐리티가 굴리고, 우리는 **★ 지점만** 구현해요. HTTP Basic 때와 검증부(3~4행)는 완전히 같아요 — 입구(팝업→폼)와 출구(무상태→세션)만 바뀐 거예요.

### (3) BCrypt — 비밀번호는 절대 평문으로 저장하지 않는다
- 회원가입: `encode("1234")` → `$2a$10$N9qo8uLO...` (60자 해시, **같은 비밀번호도 매번 다른 해시**)
- 로그인: `matches("1234", 저장된해시)` → 해시를 복호화하는 게 아니라, **다시 계산해서 맞는지만** 확인
- 그래서 DB가 유출돼도 원래 비밀번호를 바로 알 수 없어요. 회원가입 때 `encode`를 빼먹으면(평문 저장) 로그인이 **항상 실패**해요 — matches가 "이건 BCrypt 해시가 아닌데?" 하니까요.

### (4) 이후 요청은 세션으로 — stateful
로그인 성공 후에는 아이디/비밀번호를 다시 보내지 않아요. 브라우저가 `JSESSIONID` 쿠키를 자동 전송하고, 서버가 그걸로 세션을 찾아 `SecurityContext`를 복원해요(`SecurityContextHolderFilter`). **로그인 상태의 주인이 서버**라서, 세션을 무효화하면(로그아웃) 진짜로 풀려요. 서버를 재시작해도 풀려요 — Basic과 정반대죠.

### (5) CSRF — 왜 끄고 시작하나
CSRF는 "브라우저가 쿠키를 **자동으로** 실어 보내는" 성질을 악용해, 로그인된 사용자의 브라우저로 의도하지 않은 요청(송금, 비밀번호 변경…)을 보내게 하는 공격이에요. 시큐리티는 기본으로 **세션마다 토큰을 발급**해 POST 요청마다 검사해요(막히면 403). 켜두면 우리 AJAX마다 토큰을 실어야 해서, **인증 흐름 학습에 집중하려고 이번엔 꺼요.** 실서비스(세션 방식)라면 켜는 게 원칙이에요. 다음 수업 예고: JWT를 `Authorization` 헤더로 보내는 방식은 "쿠키 자동 전송"이 없어서 CSRF가 원천적으로 성립하지 않아요 — 대신 XSS라는 다른 위협이 부각돼요.

```
[기억법] 로그인은 form-urlencoded(필터는 파라미터만) / 비번은 encode로 저장·matches로 대조 / 세션이 주인이라 로그아웃 가능
```

---

## 4. 파일 구조 & 준비물

| 파일 | 역할 |
|------|------|
| `User`, `Role`, `UserRepository` | 유저 엔티티 + JPA 조회 |
| `UserService`, `UserApiController`, DTO들 | 회원가입 (중복 검사 + BCrypt 저장) |
| `DuplicateUserIdException`, `GlobalExceptionHandler` | 중복 아이디 → 409 |
| `CustomUserDetails`, `UserDetailService` | DB 유저 ↔ 시큐리티 연결 다리 |
| `SecurityConfig` | 인가 규칙 + formLogin + logout 조립 |
| `CustomAuthenticationSuccessHandler` / `FailureHandler` | 성공(세션+200 JSON) / 실패(401 JSON) |
| `HomeController`, `UserController` | 화면 라우팅 |
| `login.html`, `sign-up.html`, `home.html` + `signIn.js`, `signUp.js` | 화면 + AJAX |

**의존성** — Spring Initializr에서 **Spring Web, Spring Security, Spring Data JPA, Thymeleaf, MySQL Driver, Lombok**을 추가해요.

**DB 준비** — MySQL에 `java_basic` 데이터베이스와 `user` 테이블(완성 코드의 SQL)을 만들어 두어요.

---

## 5. Step by Step

### Step 0. 프로젝트 준비 — DB와 화면 뼈대

**할 일**
1. 의존성 6개(Web, Security, JPA, Thymeleaf, MySQL, Lombok)로 프로젝트를 만들어요.
2. `application.yaml`에 데이터소스를 설정하고, MySQL에 `user` 테이블을 만들어요.
3. `User` 엔티티(+`Role` enum)와 `UserRepository`(`findByUserId`, `existsByUserId`)를 만들어요.
4. 화면 3개(`sign-up.html`, `login.html`, `home.html`)와 라우팅 컨트롤러(`UserController`, `HomeController`)를 만들어요. (화면/JS는 완성 코드에서 가져다 써도 돼요 — 이 과제의 초점은 화면이 아니에요)

<details>
<summary>💡 힌트 보기</summary>

- 엔티티 필드: `id(PK)`, `name`, `email`, `userId`, `password`, `role`. `password`는 `length = 100` — BCrypt 해시(60자)가 들어갈 자리예요.
- `role`은 `@Enumerated(EnumType.STRING)` + `@Builder.Default`로 기본값 `ROLE_USER`.
- 리포지토리는 메서드 이름만 선언하면 끝: `Optional<User> findByUserId(String userId)`, `boolean existsByUserId(String userId)`.
- 이 시점에 실행하면? **시큐리티 의존성 때문에 모든 화면이 잠겨 있어요**(기본 로그인 페이지로 리다이렉트). 지난 과제에서 배운 그 현상이에요. Step 3에서 우리 규칙으로 풀어 줄 거예요.

</details>

**확인**: 앱이 뜨고, DB 연결 에러가 없으면 준비 완료예요. (화면 접근이 막혀 있는 건 정상!)

---

### Step 1. 회원가입 — BCrypt 해시가 DB에 저장되게

**목표**: 회원가입은 시큐리티 필터 **밖**의 일반 MVC 흐름이에요. 대신 **비밀번호를 평문이 아니라 BCrypt 해시로** 저장해요. 로그인 때 대조할 해시가 여기서 만들어져요.

**할 일**
1. `PasswordEncoder` 빈을 등록해요(`BCryptPasswordEncoder`). 임시로 아무 `@Configuration` 클래스에 둬도 돼요(Step 3에서 SecurityConfig로 이사).
2. `UserService.signUp()`: **중복 아이디 검사** → 중복이면 `DuplicateUserIdException` → `@RestControllerAdvice`에서 **409**로 응답. 통과하면 `passwordEncoder.encode()`로 해싱해서 저장해요.
3. `UserApiController`: `POST /api/users/join`에서 JSON(`@RequestBody`)을 받아 서비스를 호출해요.
4. 시큐리티가 이 API를 막고 있으니, **일단 curl로 못 부르는 게 정상**이에요. 임시로 확인하고 싶으면 Step 3의 `permitAll`을 먼저 살짝 적용해도 좋아요.

<details>
<summary>💡 힌트 보기</summary>

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(SignUpRequestDto request) {
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new DuplicateUserIdException("이미 사용 중인 아이디입니다.");
        }
        User user = request.toUser(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }
}
```

- DTO에 변환 메서드를 두면 깔끔해요: `request.toUser(encodedPassword)` — "인코딩된 비밀번호를 **주입받아** 엔티티를 만든다"는 의도가 드러나요.
- 예외 처리는 배운 그대로: `@RestControllerAdvice` + `@ExceptionHandler(DuplicateUserIdException.class)` → `ResponseEntity.status(HttpStatus.CONFLICT)`.
- **절대 하면 안 되는 것**: `request.getPassword()`를 그대로 저장. 지금은 되는 것처럼 보여도 Step 3에서 로그인이 **전부 실패**하게 돼요.

</details>

**확인**: 회원가입 후 DB를 직접 조회해 보세요. `password` 컬럼이 `$2a$10$...`으로 시작하는 60자 문자열이면 성공이에요. 같은 아이디로 또 가입하면 409가 와야 해요.

---

### Step 2. DB 유저를 시큐리티에 연결 — `CustomUserDetails` + `UserDetailService`

**목표**: 시큐리티는 우리 `User` 엔티티를 몰라요. **"아이디로 유저를 찾는 방법"**(`UserDetailsService`)과 **"유저 정보를 읽는 규격"**(`UserDetails`)을 구현해서 다리를 놓아요. 지난 과제의 `InMemoryUserDetailsManager` 자리에 **우리 DB 조회**가 들어가는 거예요.

**할 일**
1. `CustomUserDetails`: `UserDetails`를 구현하고 내부에 `User` 엔티티를 품어요. `getUsername()` → `userId`, `getPassword()` → DB의 해시, `getAuthorities()` → `Role`을 `SimpleGrantedAuthority`로.
2. `UserDetailService`: `UserDetailsService`를 구현해요. `loadUserByUsername()`에서 `userRepository.findByUserId()`로 조회하고, 없으면 `UsernameNotFoundException`을 던져요.

<details>
<summary>💡 힌트 보기</summary>

```java
@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserId(username)
                .orElseThrow(() -> new UsernameNotFoundException(username + " not found"));

        return CustomUserDetails.builder().user(user).build();
    }
}
```

- 나머지 메서드 4개(`isAccountNonExpired` 등)는 전부 `true`로 — 계정 만료/잠금 기능은 이번에 안 써요.
- 비밀번호 대조는 여기서 **하지 않아요.** 우리는 조회만 하고, 대조는 `DaoAuthenticationProvider`가 `PasswordEncoder.matches()`로 해요. 역할 분리!
- 알아두기: `UsernameNotFoundException`은 시큐리티가 **`BadCredentialsException`으로 감싸서** 처리해요. "아이디가 없다"와 "비밀번호가 틀렸다"를 구분해서 알려주면 **계정 존재 여부가 노출**되기 때문이에요(보안). 그래서 이 예외는 `GlobalExceptionHandler`가 아니라 Step 4의 실패 핸들러로 흘러가요.

</details>

**확인**: 컴파일되면 통과예요. 이 빈이 등록되는 순간, 콘솔의 `Using generated security password`도 사라져요(부트가 "유저 관리를 직접 하는구나" 하고 물러남).

---

### Step 3. `SecurityConfig` — 인가 규칙 + 폼 로그인 조립

**목표**: 어떤 URL을 열고 잠글지 정하고, 폼 로그인을 **우리 화면·우리 파라미터 이름**에 맞게 설정해요. 이 Step이 끝나면 curl로 실제 로그인이 돼요.

**할 일**
1. `SecurityFilterChain` 빈에서: CSRF 비활성화, 인가 규칙 설정 — `/users/join`, `/api/users/join`, `/css/**`, `/js/**`는 `permitAll()`, 나머지는 `authenticated()`.
2. `formLogin`: `loginPage("/users/login")`, `loginProcessingUrl("/users/login")`, **`usernameParameter("userId")`**, `passwordParameter("password")`, `permitAll()`.
3. Step 1에서 임시로 만들었던 `PasswordEncoder` 빈을 여기로 옮겨요.
4. curl로 회원가입 → 로그인을 처음부터 끝까지 확인해요.

<details>
<summary>💡 힌트 보기</summary>

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/users/join", "/api/users/join", "/css/**", "/js/**").permitAll()
                    .anyRequest().authenticated())
            .formLogin(form -> form
                    .loginPage("/users/login")
                    .loginProcessingUrl("/users/login")
                    .usernameParameter("userId")
                    .passwordParameter("password")
                    .permitAll());

    return http.build();
}
```

- **인가 규칙은 위에서부터 순서대로** 매칭돼요. 구체적인 경로 먼저, `anyRequest()`는 항상 마지막.
- 정적 리소스(`/css/**`, `/js/**`)를 안 열면 → 로그인 페이지가 **깨진 채로**(CSS 없이) 떠요. 로그인 페이지는 미인증 사용자가 보는 화면인데, 거기 딸린 리소스가 잠겨 있으면 안 되겠죠.
- `loginPage`에 `permitAll()`을 빼먹으면 → 미인증자가 로그인 페이지로 리다이렉트 → 그 페이지도 잠김 → 또 리다이렉트… **무한 리다이렉트**로 브라우저가 에러를 띄워요.
- curl 확인 (아직 커스텀 핸들러가 없어서 기본 동작 = 리다이렉트):
```bash
# 회원가입
curl -i -H "Content-Type: application/json" \
     -d '{"userId":"kim","password":"1234","userName":"김개발"}' \
     http://localhost:8080/api/users/join

# 로그인 성공 → 302 Location: /  (+ Set-Cookie: JSESSIONID)
curl -i -d "userId=kim&password=1234" http://localhost:8080/users/login

# 로그인 실패 → 302 Location: /users/login?error
curl -i -d "userId=kim&password=wrong" http://localhost:8080/users/login
```
- ⭐ 여기서 실험 하나: `-d "username=kim&password=1234"`로 보내 보세요(파라미터 이름을 기본값으로). `usernameParameter("userId")` 설정 때문에 **실패**해요. 반대로 설정을 빼면 `userId=`로 보내는 게 실패하고요. "필터는 딱 정해진 이름의 파라미터만 읽는다"를 직접 확인하는 거예요.

</details>

**확인**: curl 로그인 성공 시 `302 Location: /`와 `Set-Cookie: JSESSIONID=...`가 보이면 통과예요. 인증 흐름(필터 → Provider → 우리 `UserDetailService` + BCrypt 대조)이 완성된 거예요.

---

### Step 4. 성공/실패 핸들러 — AJAX에 맞게 JSON으로 응답하기

**목표**: 기본 동작(리다이렉트)은 일반 `<form>` 제출용이에요. 우리 화면은 **AJAX**로 로그인하니까, 성공/실패를 **JSON으로** 알려주는 커스텀 핸들러로 교체해요. 성공 시 화면에서 쓸 유저 정보를 **세션에** 담아요.

**할 일**
1. `CustomAuthenticationSuccessHandler` (`AuthenticationSuccessHandler` 구현):
   - `authentication.getPrincipal()`을 `CustomUserDetails`로 캐스팅해 `User`를 꺼내요.
   - 세션에 `userId`, `userName`을 저장해요 (`home.html`이 `${session.userName}`으로 사용).
   - 200 + `SignInResponseDto` JSON(`url: "/"` 포함)을 응답해요.
2. `CustomAuthenticationFailureHandler` (`AuthenticationFailureHandler` 구현): 401 + 실패 메시지 JSON.
3. `SecurityConfig`의 `formLogin`에 `.successHandler(...)`, `.failureHandler(...)`로 끼워요.
4. 브라우저에서 회원가입 → 로그인 → 홈까지 전체 흐름을 확인해요.

<details>
<summary>💡 힌트 보기</summary>

```java
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        HttpSession session = request.getSession();
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("userName", user.getName());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                SignInResponseDto.builder()
                        .isLoggedIn(true).message("로그인 성공")
                        .url("/").userId(user.getUserId()).userName(user.getName())
                        .build()));
    }
}
```

- 실패 핸들러도 같은 패턴이에요: 상태만 `SC_UNAUTHORIZED`(401), `url`은 `/users/login`.
- `@Component`로 등록하고 `SecurityConfig`에서 생성자 주입(`@RequiredArgsConstructor`)으로 받아 끼우면 돼요.
- `signIn.js`의 대응 관계: 성공 핸들러의 200 JSON → `success:` 콜백 → `alert` 후 `response.url`로 이동. 실패 핸들러의 401 → `error:` 콜백. **핸들러가 내려주는 JSON 모양과 JS가 읽는 필드가 계약**인 셈이에요.
- ⚠️ `signIn.js`에서 `data: formData`를 **`JSON.stringify(formData)`로 바꾸면 안 돼요.** jQuery는 객체를 주면 form-urlencoded로 보내 줘요(그래서 필터가 읽을 수 있음). 회원가입(`signUp.js`)은 반대로 JSON으로 보내요 — **우리 컨트롤러**(`@RequestBody`)가 받으니까요. 두 JS를 나란히 놓고 비교해 보세요.

</details>

**확인**: 브라우저에서 로그인 성공 → `alert("로그인 성공")` → 홈에서 **"김개발님 환영합니다"**가 보이면 통과예요. 틀린 비밀번호는 `alert`로 실패 메시지가 떠야 해요.

---

### Step 5. 로그아웃 + 세션 관찰 — "이번엔 진짜 로그아웃이 된다"

**목표**: `LogoutFilter` 설정으로 로그아웃을 완성하고, **세션 기반 인증이 Basic과 어떻게 다른지** 개발자도구로 직접 관찰해요.

**할 일**
1. `SecurityConfig`에 logout 설정을 추가해요: `logoutUrl("/users/logout")`, `logoutSuccessUrl("/users/login")`, `invalidateHttpSession(true)`, `deleteCookies("JSESSIONID")`, `permitAll()`.
2. 관찰 ①: 로그인 후 개발자도구 → Application(Storage) → Cookies에서 `JSESSIONID`를 확인하고, Network 탭에서 이후 요청마다 이 쿠키가 자동 전송되는 걸 봐요. (`Authorization` 헤더는 이제 없어요!)
3. 관찰 ②: 로그아웃 → 쿠키가 사라지고 `/` 접근 시 다시 로그인 페이지로 가는지 확인해요.
4. 관찰 ③: 로그인 상태에서 **서버를 재시작** → 새로고침. 어떻게 되나요? 지난 과제(Basic)와 비교해 보세요.

<details>
<summary>💡 힌트 보기</summary>

```java
.logout(logout -> logout
        .logoutUrl("/users/logout")          // LogoutFilter가 가로챈다 (컨트롤러 불필요)
        .logoutSuccessUrl("/users/login")
        .invalidateHttpSession(true)         // 서버 쪽: 세션 무효화 → SecurityContext, userName 모두 소멸
        .deleteCookies("JSESSIONID")         // 브라우저 쪽: 죽은 세션 ID가 계속 전송되지 않게 정리
        .permitAll())
```

- `invalidateHttpSession`과 `deleteCookies`는 **역할이 달라요.** 전자는 서버의 세션 저장소에서 세션을 없애고, 후자는 브라우저에 남은 (이미 죽은) 세션 ID 쿠키를 지워요. 전자만으로도 로그아웃은 되지만, 후자까지 해야 깔끔해요.
- 관찰 ③의 답: **로그인이 풀려요.** 세션은 서버 메모리에 있어서 재시작하면 사라지니까요. Basic은 정반대였죠 — 로그인 상태의 주인이 브라우저(자격증명 캐싱)라 재시작해도 유지됐어요. **"로그인 상태의 주인이 누구냐"**가 두 방식의 본질적 차이예요.
- CSRF를 켜면 로그아웃도 POST + 토큰이 필요해져요(강제 로그아웃 공격 방지). 지금은 꺼둬서 `<a>` 링크(GET)로도 동작해요.

</details>

**확인**: 로그아웃 후 `/`에 접근하면 로그인 페이지로 리다이렉트되고, 재시작 실험까지 Basic과의 차이를 설명할 수 있으면 완성이에요. 🎉

---

## ⚠️ 자주 나는 오류 (막히면 여기부터)

| 증상 | 원인 / 해결 |
|------|------|
| 아이디/비밀번호가 정확한데 로그인이 **항상** 실패 | ① 로그인 요청을 **JSON으로** 보냈다 → form-urlencoded로 (`data: formData`, `JSON.stringify` 금지). ② 파라미터 이름 불일치 → `usernameParameter("userId")` 확인. ③ 회원가입 때 `encode()`를 안 해서 DB에 평문이 저장됨 → 콘솔에 "Encoded password does not look like BCrypt" 경고 확인, 해당 유저 재가입. |
| 로그인 페이지가 CSS 없이 깨져서 나옴 | 정적 리소스가 잠혀 있어요 → `/css/**`, `/js/**`에 `permitAll()`. |
| 리다이렉트가 반복되다 브라우저 에러 (`ERR_TOO_MANY_REDIRECTS`) | `loginPage("/users/login")` 자체가 잠겨 있어요 → `formLogin`에 `.permitAll()` 추가. |
| POST /users/login 이 **403** | CSRF가 켜져 있는데 토큰을 안 보냈어요 → 학습 단계에선 `.csrf(disable)`, 실서비스라면 토큰을 실어 보내는 방식으로. |
| 로그인은 되는데 홈에 "null님 환영합니다" | 성공 핸들러에서 세션에 `userName`을 저장하지 않았거나 키 이름이 달라요 → `session.setAttribute("userName", ...)`과 `${session.userName}` 일치 확인. |
| `Table 'java_basic.user' doesn't exist` | 테이블을 안 만들었어요 → 완성 코드의 CREATE TABLE 실행. |
| 비밀번호가 DB에서 잘려 있음 / 가입은 되는데 로그인 실패 | `password` 컬럼이 60자 미만 → BCrypt 해시(60자)가 잘림. `VARCHAR(100)`으로. |
| 없는 아이디로 로그인해도 "비밀번호 틀림"과 같은 응답 | 오류가 아니라 **의도된 보안 동작**이에요. `UsernameNotFoundException`은 `BadCredentialsException`으로 감싸져요 — 계정 존재 여부를 노출하지 않기 위해서. |
| 컨트롤러에 `POST /users/login`을 만들었는데 호출이 안 됨 | 만들 필요가 없어요! 필터가 DispatcherServlet **앞에서** 가로채므로 그 컨트롤러엔 영원히 도달하지 않아요. |

---

## 6. 학습 체크

- [ ] 폼 로그인과 HTTP Basic의 차이 3가지(화면·자격증명 전달·상태)를 말할 수 있다
- [ ] `encode`(회원가입)와 `matches`(로그인)의 역할을 구분해 설명할 수 있다
- [ ] 로그인 요청은 왜 JSON이 아니라 form-urlencoded여야 하는지 안다
- [ ] `loginProcessingUrl`에 컨트롤러가 필요 없는 이유(필터가 가로챔)를 안다
- [ ] `UserDetailsService`는 조회만 하고, 비밀번호 대조는 Provider가 한다는 역할 분리를 안다
- [ ] 인가 규칙에서 `anyRequest()`가 왜 마지막이어야 하는지 안다
- [ ] JSESSIONID 쿠키로 로그인이 유지되는 원리(세션에서 SecurityContext 복원)를 안다
- [ ] CSRF 공격의 성립 조건("쿠키 자동 전송")과, 토큰 방식(JWT)에선 왜 성립하지 않는지 안다
- [ ] "로그인 상태의 주인"이 Basic은 브라우저, 폼 로그인은 서버(세션)임을 설명할 수 있다

## 7. 최종 완성 체크리스트

- [ ] 회원가입하면 DB에 `$2a$...` BCrypt 해시가 저장된다 (중복 아이디는 409)
- [ ] 미인증 상태로 `/` 접근 시 `/users/login`으로 리다이렉트된다
- [ ] 로그인 성공 시 200 JSON + JSESSIONID 쿠키, 홈에 "OO님 환영합니다"가 뜬다
- [ ] 로그인 실패 시 401 JSON을 받아 alert가 뜬다
- [ ] 로그아웃하면 세션·쿠키가 정리되고 보호된 페이지 접근이 다시 막힌다
- [ ] 서버 재시작 시 로그인이 풀리는 이유를 Basic과 대조해 설명할 수 있다