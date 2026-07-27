# HTTP Basic 인증 직접 붙여보기 (Spring Security 첫걸음)

> 수업에서 배운 **Spring Security의 필터 체인** 위에서, 가장 단순한 인증 방식인 **HTTP Basic**을 직접 붙여 봐요.
> 핵심은 두 가지예요. ① 시큐리티의 모든 것은 **필터**에서 일어난다. ② HTTP Basic의 로그인 상태는 서버가 아니라 **브라우저가 들고 있다**(stateless).
> 이 과제는 코드 양은 적지만, **"내가 지금 뭘 보고 있는 건지"**를 헷갈리게 만드는 함정이 많아요(재시작마다 바뀌는 비밀번호, 갑자기 뜨는 999 에러 페이지, 로그인 창이 안 뜨고 뚫리는 현상…). 함정을 직접 밟고 → 원리로 설명하는 것까지가 과제예요.
>
> 💡 각 Step의 **힌트는 접혀 있어요.** 먼저 스스로 만들어 보고 막히면 펼치세요.

<details>
<summary>✅ 최종 완성 코드 보기 (먼저 직접 만들어 본 뒤 펼쳐서 비교하세요)</summary>

> Step 5까지 모두 반영한 **완성본**이에요. 파일별로 그대로 옮기면 동작합니다.

**`build.gradle`** (의존성)
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'  // 부트 3.x라면 spring-boot-starter-web
}
```

**`HelloApiController.java`** — 보호할 자원
```java
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloApiController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
    }
}
```

**`SecurityConfig.java`** — HTTP Basic만 켠 커스텀 설정
```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // httpBasic(): BasicAuthenticationFilter가 필터 체인에 등록된다.
        // "이제부터 Authorization: Basic ... 헤더를 읽어서 인증을 처리하겠다"는 선언.
        http
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();

        UserDetails user = User.withUsername("user")
                .password("12345")
                .authorities("USER")
                .build();

        manager.createUser(user);
        return manager;
    }

    // 학습용: 비밀번호를 암호화 없이 그대로 비교 (실무 금지! 실무는 BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
```

**실행 결과(예시) — 터미널에서**
```bash
$ curl -i http://localhost:8080/hello
HTTP/1.1 401
WWW-Authenticate: Basic realm="Realm", charset="UTF-8"

$ curl -u user:12345 http://localhost:8080/hello
Hello World!
```

> 인증 없이 401, `user:12345`로 200이 나오면 성공이에요. 브라우저에서는 팝업 창이 떠요(안 뜨면 아래 **"자주 나는 오류"**를 보세요 — 그게 이 과제의 백미예요).

</details>

---

## 0. 먼저 알아둘 점

- 이 과제는 **새 기능을 많이 짜는 과제가 아니에요.** 컨트롤러 1개 + 설정 1개가 전부예요. 대신 **매 Step마다 "지금 무슨 일이 일어났는지" 관찰하고 설명하는 것**이 진짜 과제예요.
- `spring-boot-starter-security`를 의존성에 추가하는 **순간부터** 모든 요청이 잠겨요. 아무 설정을 안 해도요. "왜 갑자기 로그인 창이 뜨지?"가 아니라 **그게 기본 동작**이에요.
- ⚠️ **자동 생성 비밀번호는 재시작할 때마다 바뀌어요.** 콘솔의 `Using generated security password: ...`는 **지금 떠 있는 인스턴스**의 것만 유효해요. 이전 실행 콘솔에서 복사하면 100% 실패해요.
- ⚠️ **HTTP Basic은 반드시 HTTPS와 함께** 써야 해요. Base64는 암호화가 아니라서, 평문 HTTP에선 아이디/비밀번호가 그대로 노출된 것과 같아요. (이 과제는 localhost 학습용이라 HTTP로 진행)
- 관찰 도구로 **curl**과 **브라우저 개발자도구(Network 탭)**를 계속 써요. 브라우저만 쓰면 브라우저가 몰래 해 주는 일(자격증명 캐싱)에 속아요.

---

## 1. 무엇을 만드나요?

`/hello` 하나를 만들고, 그 앞을 HTTP Basic 인증으로 잠가요. 완성하면 이렇게 동작해요.

| 접근 방법 | 결과 |
|------|------|
| `curl http://localhost:8080/hello` | **401** + `WWW-Authenticate: Basic` 헤더 |
| `curl -u user:12345 .../hello` | **200** `Hello World!` |
| 브라우저에서 `/hello` | 아이디/비밀번호 **팝업** → 입력하면 `Hello World!` |
| 팝업에서 한 번 성공한 뒤 재접근 | 팝업 없이 **바로 통과** (왜? Step 5에서 확인) |

핵심은 **요청 → 필터 체인 → 컨트롤러** 흐름에서, 인증이 **컨트롤러에 도달하기 전에** 필터에서 끝난다는 감각을 잡는 거예요.

---

## 2. 학습 목표

| 개념 | 어디서 배우나 |
|------|------|
| 의존성 추가만으로 전부 잠기는 기본 자동 설정 | Step 1 |
| 자동 생성 비밀번호(재시작마다 변경)로 로그인 | Step 1 |
| HTTP Basic 프로토콜: 401 → 팝업 → Base64 헤더 | Step 2 (curl로 직접 관찰) |
| `SecurityFilterChain` 빈으로 직접 설정 (httpBasic만) | Step 3 |
| `UserDetailsService` + `PasswordEncoder`로 유저 등록 | Step 4 |
| 브라우저의 자격증명 캐싱 & "로그아웃이 없다"의 의미 | Step 5 |

---

## 3. 핵심 개념

### (1) Spring Security는 필터 위에서 돈다
웹 요청이 컨트롤러에 도달하기 **전에**, 여러 보안 필터를 순서대로 통과해요.
```
요청 → DelegatingFilterProxy → FilterChainProxy → [보안 필터들] → DispatcherServlet → Controller
       (톰캣에 등록된 진입점)   (스프링 빈, 체인 보유)   (BasicAuthenticationFilter 등)
```
`DelegatingFilterProxy`가 "톰캣 필터 ↔ 스프링 빈으로 관리되는 보안 필터들" 사이에 다리를 놓아 줘요. 우리가 `httpBasic()`을 켜면 이 체인 안에 `BasicAuthenticationFilter`가 들어가는 거예요.

### (2) HTTP Basic 프로토콜 (RFC 7617) — 4단계
```
1. 클라이언트가 그냥 접근 → 서버: 401 + WWW-Authenticate: Basic realm="..."
2. 브라우저: 아이디/비밀번호 팝업을 띄움
3. 클라이언트: username:password를 Base64로 인코딩해 재요청
   → Authorization: Basic dXNlcjoxMjM0NQ==
4. 서버: 헤더를 디코딩해 검증 → 성공 시 자원 응답
```
스프링에서는 이 중 3~4번을 `BasicAuthenticationFilter`가 처리하고, 실패 시 401을 만드는 건 `BasicAuthenticationEntryPoint`가 담당해요.

### (3) Base64는 암호화가 아니다
`dXNlcjoxMjM0NQ==`는 누구나 1초 만에 원문으로 되돌릴 수 있어요.
```bash
echo dXNlcjoxMjM0NQ== | base64 -d   # → user:12345
```
그래서 HTTP Basic은 **HTTPS가 전제**예요. "인코딩 ≠ 암호화"를 입으로 설명할 수 있어야 해요.

### (4) 로그인 상태는 브라우저가 들고 있다 (이 과제의 최종 보스)
세션 로그인은 서버가 상태를 기억하지만, Basic은 **서버가 아무것도 기억하지 않아요(stateless)**. 대신:
- 브라우저가 성공한 자격증명을 **realm 단위로 캐싱**하고, 이후 **모든 요청에 자동으로** `Authorization` 헤더를 붙여 보내요.
- 그래서 서버를 **재시작해도 로그인이 유지**돼요(인메모리 유저는 똑같이 다시 만들어지고, 브라우저는 계속 자격증명을 보내니까).
- 같은 이유로 **명시적인 로그아웃이 사실상 불가능**해요. 서버가 세션을 끊어도 브라우저가 다음 요청에 또 붙여 보내면 그대로 재인증이거든요.

```
[기억법] 시큐리티=필터에서 끝난다 / Basic=Base64(암호화 아님)+HTTPS 필수 / 로그인 상태는 브라우저가 소유(로그아웃 애매)
```

---

## 4. 파일 구조 & 준비물

| 파일 | 역할 |
|------|------|
| `HelloApiController.java` | 보호할 자원 (`GET /hello`) |
| `SecurityConfig.java` | `SecurityFilterChain` + 인메모리 유저 + 인코더 |

**의존성** — Spring Initializr에서 **Spring Security** + **Spring Web**을 추가하는 게 가장 안전해요. 수동이면:
```gradle
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'org.springframework.boot:spring-boot-starter-webmvc'   // 부트 3.x는 spring-boot-starter-web
```

**관찰 도구**: 터미널(curl), 브라우저 개발자도구(Network 탭), 그리고 **시크릿 창**(자격증명 캐시 없는 깨끗한 브라우저).

---

## 5. Step by Step

### Step 0. 프로젝트 준비 — 보호할 자원부터 만들기

**할 일**
1. 스프링 부트 프로젝트에 **Spring Web + Spring Security** 의존성을 추가해요.
2. `GET /hello` → `"Hello World!"`를 반환하는 `@RestController`를 만들어요.
3. 아직 `SecurityConfig`는 **만들지 마세요.** (기본 동작을 먼저 관찰해야 해요)

<details>
<summary>💡 힌트 보기</summary>

```java
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloApiController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
    }
}
```

시큐리티 설정 클래스가 하나도 없어도 괜찮아요. 오히려 그 상태가 이번 Step의 핵심이에요.

</details>

**확인**: 컴파일이 되고 앱이 뜨면 준비 완료예요.

---

### Step 1. 기본 자동 설정 관찰 — "아무것도 안 했는데 다 잠겼다"

**목표**: 의존성 추가만으로 무슨 일이 벌어지는지 관찰하고, **자동 생성 비밀번호로 로그인**해 봐요.

**할 일**
1. 앱을 실행하고 콘솔에서 `Using generated security password: ...` 줄을 찾아 **복사**해요.
2. 브라우저에서 `http://localhost:8080/hello` 접속 → 무엇이 뜨는지 관찰해요. (팝업일까요, 로그인 페이지일까요?)
3. `user` + 복사한 비밀번호로 로그인해서 `Hello World!`를 확인해요.
4. **앱을 재시작**하고 방금 그 비밀번호로 다시 로그인해 보세요. 무슨 일이 생기나요?

<details>
<summary>💡 힌트 보기</summary>

- 기본 자동 설정은 **formLogin + httpBasic 둘 다** 켜요. 그래서 브라우저(HTML 요청)에는 Basic 팝업이 아니라 **폼 로그인 페이지**가 떠요. Basic 팝업은 Step 3에서 우리가 직접 설정해야 볼 수 있어요.
- 자동 생성 비밀번호는 **UUID이고, 재시작마다 새로 만들어져요.** 4번에서 로그인이 실패하는 게 정상이에요. 반드시 **지금 떠 있는 인스턴스의 콘솔**에서 복사하세요.
- curl로도 확인해 보세요. curl은 HTML 요청이 아니라서 응답이 달라요:
```bash
curl -i http://localhost:8080/hello
# → 401 + WWW-Authenticate: Basic ...   (폼이 아니라 Basic으로 응답!)
```
같은 서버가 **요청의 Accept 헤더에 따라** 폼(302 /login)과 Basic(401)을 골라 응답하고 있는 거예요.

</details>

**확인**: ① 로그인 성공해서 `Hello World!`를 봤다 ② "재시작하면 비밀번호가 바뀐다"를 직접 겪었다 — 두 개 다 했으면 통과예요.

---

### Step 2. curl로 HTTP Basic 프로토콜 직접 관찰

**목표**: 브라우저가 몰래 해 주는 일을 **손으로 직접** 해 봐요. 401 → Base64 인코딩 → Authorization 헤더 → 200 사이클을 눈으로 확인해요.

**할 일**
1. `curl -i`로 인증 없이 `/hello`를 호출하고, 상태 코드와 `WWW-Authenticate` 헤더를 확인해요.
2. `user:비밀번호`를 **직접 Base64로 인코딩**해요. (`echo -n "user:비밀번호" | base64`)
3. 인코딩한 값을 `Authorization: Basic ...` 헤더에 담아 **직접** 호출해요.
4. 같은 것을 `curl -u`로도 호출해서, `-u`가 2~3번을 대신해 준다는 걸 확인해요.

<details>
<summary>💡 힌트 보기</summary>

```bash
# 1. 인증 없이 → 401 관찰
curl -i http://localhost:8080/hello
# HTTP/1.1 401
# WWW-Authenticate: Basic realm="Realm", charset="UTF-8"

# 2. 직접 인코딩 (-n 필수: 개행이 섞이면 인증 실패!)
echo -n "user:여기에_현재_비밀번호" | base64
# 예: dXNlcjo3ZGU0Y2M3OS0uLi4=

# 3. 헤더에 직접 담아 호출
curl -H "Authorization: Basic dXNlcjo3ZGU0Y2M3OS0uLi4=" http://localhost:8080/hello
# Hello World!

# 4. curl이 대신 해 주는 버전 (2~3번과 완전히 같은 일)
curl -u user:여기에_현재_비밀번호 http://localhost:8080/hello

# 보너스: 디코딩해서 "암호화가 아니다" 확인
echo dXNlcjo3ZGU0Y2M3OS0uLi4= | base64 -d
```

브라우저 팝업에 입력하는 행위 = 2~3번을 브라우저가 대신하는 것뿐이에요. 마법이 없다는 걸 확인하는 게 이 Step의 목적이에요.

</details>

**확인**: 직접 만든 `Authorization` 헤더로 `Hello World!`가 나오면 통과예요. "Base64는 왜 암호화가 아닌가?"에 한 문장으로 답할 수 있어야 해요.

---

### Step 3. `SecurityFilterChain` 직접 설정 — httpBasic만 켜기

**목표**: 자동 설정을 우리 설정으로 교체해요. **httpBasic만** 켜서, 브라우저에서도 폼 대신 **Basic 팝업**이 뜨게 만들어요.

**할 일**
1. `@Configuration` + `@EnableWebSecurity`를 붙인 `SecurityConfig` 클래스를 만들어요.
2. `SecurityFilterChain` 빈을 등록해요: `httpBasic(Customizer.withDefaults())` + 모든 요청 인증 필요.
3. 재시작 후 **시크릿 창**에서 `/hello` 접속 → 이제 폼이 아니라 **팝업**이 뜨는지 확인해요.

<details>
<summary>💡 힌트 보기</summary>

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(Customizer.withDefaults())                    // BasicAuthenticationFilter 등록
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());

        return http.build();
    }
}
```

- 우리가 `SecurityFilterChain` 빈을 등록하는 순간, 부트의 기본 체인(formLogin+httpBasic)은 **통째로 물러나요.** 이제 폼 로그인은 없어요.
- 이 시점의 로그인 계정은? 아직 유저 빈을 안 만들었으니 **여전히 자동 생성 비밀번호**예요(콘솔 확인). 유저 교체는 다음 Step에서.
- 팝업이 안 뜨고 그냥 통과된다면 → 일반 창에 캐싱된 자격증명이 남은 거예요. **시크릿 창**으로 확인하세요.

</details>

**확인**: 시크릿 창에서 팝업이 뜨고, `user` + 콘솔의 생성 비밀번호로 `Hello World!`가 나오면 통과예요.

---

### Step 4. 유저 직접 등록 — `UserDetailsService` + `PasswordEncoder`

**목표**: 재시작마다 바뀌는 자동 생성 비밀번호 대신, **우리가 정한 계정**(`user`/`12345`)을 등록해요.

**할 일**
1. `InMemoryUserDetailsManager`로 `user`/`12345` 계정을 만들어 `UserDetailsService` 빈으로 등록해요.
2. `PasswordEncoder` 빈을 등록해요. (학습용으로 `NoOpPasswordEncoder` — 평문 비교)
3. 재시작 후: 콘솔에서 **생성 비밀번호 로그가 사라졌는지** 확인하고, `curl -u user:12345`로 로그인해요.

<details>
<summary>💡 힌트 보기</summary>

```java
@Bean
public UserDetailsService userDetailsService() {
    InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();

    UserDetails user = User.withUsername("user")
            .password("12345")
            .authorities("USER")
            .build();

    manager.createUser(user);
    return manager;
}

@Bean
public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();   // 학습용! 실무는 BCryptPasswordEncoder
}
```

- `UserDetailsService` 빈을 등록하면 부트가 "직접 유저를 관리하는구나" 하고 **자동 생성 유저를 만들지 않아요.** 그래서 콘솔의 비밀번호 로그도 사라져요.
- `PasswordEncoder` 빈 없이 평문 `"12345"`를 쓰면 `There is no PasswordEncoder mapped for the id "null"` 에러가 나요. 인코더 빈을 등록하거나, 인코더 없이 `.password("{noop}12345")`처럼 접두사를 붙이는 방법도 있어요.
- `NoOpPasswordEncoder`는 이름 그대로 "아무것도 안 하는" 인코더예요. **비밀번호가 평문 저장**되므로 학습에서만 써요. 인증 흐름(조회 → 비교)을 눈으로 보기 위한 선택이에요.

</details>

**확인**: `curl -u user:12345 http://localhost:8080/hello` → `Hello World!`, 그리고 **재시작해도 같은 비밀번호로 로그인**되면 통과예요.

---

### Step 5. 최종 관찰 — "로그인 창이 안 뜨고 그냥 뚫리는데요?"

**목표**: 브라우저의 **자격증명 캐싱**을 직접 관찰하고, "Basic은 로그아웃이 애매하다"를 몸으로 이해해요. 이번 Step은 코드를 안 짜요. **관찰과 설명**이 과제예요.

**할 일**
1. 일반 창에서 팝업에 `user`/`12345`를 입력해 로그인해요.
2. 탭을 닫고 다시 `/hello`에 접속해요. 팝업이 뜨나요? (안 뜨고 바로 `Hello World!`가 나올 거예요)
3. 개발자도구 → Network 탭 → `/hello` 요청 → Request Headers에서 **범인을 찾아요.**
4. **서버를 재시작**하고 새로고침해요. 로그인이 풀렸나요? 왜 안 풀렸을까요?
5. 팝업을 다시 보려면 어떻게 해야 하는지 2가지 이상 찾아보세요.

<details>
<summary>💡 힌트 보기</summary>

- 3번의 범인: `Authorization: Basic dXNlcjoxMjM0NQ==`가 **자동으로** 붙어 있어요. 브라우저가 성공한 자격증명을 **(사이트, realm) 단위로 캐싱**해서, 이후 요청마다 알아서 실어 보내는 거예요. 심지어 401을 기다리지 않고 **선제적으로** 보내요 — 그래서 팝업은커녕 401조차 발생하지 않아요.
- 4번: 재시작해도 안 풀려요. 서버엔 끊을 세션이 없고(stateless), 인메모리 유저는 재시작 시 `user`/`12345`로 **똑같이 다시** 만들어지고, 브라우저는 캐싱한 자격증명을 계속 보내니까요. **로그인 상태의 주인이 브라우저**라서 서버가 할 수 있는 게 없어요. 이게 "Basic은 로그아웃이 애매하다"의 실체예요.
- 5번: ① **시크릿 창** ② **브라우저 완전 종료 후 재실행**(캐시는 브라우저 프로세스 수명 동안 유지) ③ 서버에서 realm 이름을 바꾸는 것도 효과가 있어요(브라우저는 realm이 다르면 별개 인증 영역으로 취급).
- 검증은 curl로: `curl -i http://localhost:8080/hello`는 **여전히 401**이에요. 서버가 뚫린 게 아니라, 그 브라우저만 자격증명을 기억하고 있는 거예요.

</details>

**확인**: "브라우저에서 팝업 없이 통과되는데, 서버 보안이 뚫린 게 아닌 이유"를 Network 탭 스크린샷(또는 헤더 값)과 함께 한 단락으로 설명할 수 있으면 완성이에요.

---

## ⚠️ 자주 나는 오류 (막히면 여기부터)

| 증상 | 원인 / 해결 |
|------|------|
| `user` + 생성 비밀번호로 로그인이 안 됨 | ① 비밀번호는 **재시작마다 바뀜** → 지금 떠 있는 인스턴스의 콘솔에서 다시 복사. ② 복사할 때 앞뒤 공백 포함 여부 확인. ③ 브라우저 자동완성이 옛 비밀번호를 채웠을 수 있음 → 시크릿 창에서 시도. |
| **Whitelabel Error Page (status=999)** | 로그인 실패가 아니에요! 실제 에러가 없는데 `/error` 경로가 일반 페이지처럼 열린 것(999는 부트의 placeholder 코드). 이전 에러 페이지 탭을 새로고침 → 로그인 → `/error`로 되돌아올 때 자주 발생. **로그인은 이미 성공한 상태**이니 주소창에 `/hello`를 직접 입력해 확인하세요. |
| Basic 팝업이 아니라 로그인 폼 페이지가 뜸 | 기본 자동 설정은 formLogin+httpBasic **둘 다** 켜져 있고, 브라우저(HTML) 요청엔 폼이 우선이에요. Step 3처럼 **직접 `SecurityFilterChain`을 등록**해서 httpBasic만 켜야 팝업이 떠요. |
| 팝업이 안 뜨고 바로 접근됨 | 브라우저가 자격증명을 캐싱해 **매 요청 자동 전송** 중. 보안이 꺼진 게 아님(curl로 401 확인). 시크릿 창 또는 브라우저 완전 종료 후 재시도. |
| `There is no PasswordEncoder mapped for the id "null"` | 평문 비밀번호를 인코더 없이 사용. `PasswordEncoder` 빈 등록 또는 `.password("{noop}12345")` 접두사 사용. |
| 재시작했는데도 로그인이 안 풀림 | 정상이에요(Step 5). Basic은 stateless — 로그인 상태는 브라우저가 들고 있어서 서버 재시작과 무관해요. |
| `Port 8080 was already in use` | 이전 인스턴스가 아직 떠 있어요. 이전 실행을 종료하고 다시 실행. (구버전 인스턴스에 테스트하면 코드 수정이 반영 안 된 것처럼 보여요!) |

---

## 6. 학습 체크

- [ ] "요청 → DelegatingFilterProxy → FilterChainProxy → 보안 필터들 → 컨트롤러" 흐름을 그릴 수 있다
- [ ] 401 + `WWW-Authenticate` → `Authorization: Basic` 재요청 사이클을 설명할 수 있다
- [ ] Base64가 암호화가 아닌 이유와, 그래서 HTTPS가 필수인 이유를 안다
- [ ] 자동 생성 비밀번호가 재시작마다 바뀌는 걸 알고, 어디서 확인하는지 안다
- [ ] `SecurityFilterChain` 빈을 등록하면 기본 자동 설정이 물러난다는 걸 안다
- [ ] status=999 화이트라벨 페이지가 "로그인 실패"가 아님을 설명할 수 있다
- [ ] Basic 인증에서 로그인 상태의 주인이 브라우저이고, 그래서 로그아웃이 애매한 이유를 안다

## 7. 최종 완성 체크리스트

- [ ] `curl -i /hello` → 401 + `WWW-Authenticate: Basic` 헤더가 나온다
- [ ] 직접 Base64 인코딩한 `Authorization` 헤더로 200을 받아 봤다
- [ ] 시크릿 창에서 Basic **팝업**이 뜨고, `user`/`12345`로 로그인된다
- [ ] Network 탭에서 자동으로 붙는 `Authorization` 헤더를 찾아 디코딩해 봤다
- [ ] "팝업 없이 통과되는 현상"이 서버 문제가 아닌 이유를 한 단락으로 설명할 수 있다