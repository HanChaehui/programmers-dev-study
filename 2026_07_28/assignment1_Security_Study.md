1. spring-boot/security/http-basic
    
    config/SecurityConfig, controller/HelloController
    
2. spring-boot/security/form-login

spring-boot/security/http-basic

## Spring Security의 동작 메커니즘

- Spring Security의 모든 것은 필터(Filter) 위에서 돌아간다.
- 웹 요청이 들어오면 실제 컨트롤러에 도달하기 전에 여러 개의 보안 필터를 순서대로 통과한다.
- 이 진입점이 DelegatingFilterProxy이다.
- 서블릿 컨테이너(톰캣)에 등록된 이 필터가 요청을 받아서 스프링이 관리하는FilterChainProxy에게 위임하고, 이 FilterChainProxy가 내부적으로 여러SecurityFilterChain을 들고 있다.
- 즉 "톰캣 필터 → 스프링 빈으로 관리되는 보안 필터들"로 다리를 놓아주는 구조이다.
- 요청 → DelegatingFilterProxy → FilterChainProxy → [보안 필터 체인] → DispatcherServlet → Controller

<aside>
🔗

1. 브라우저가 요청을 보냄
2. Tomcat이 요청을 받음
3. Tomcat에 등록된 DelegatingFilterProxy가 요청을 가로챔
4. DelegatingFilterProxy가 Spring Bean인 FilterChainProxy에게 요청을 위임함
5. FilterChainProxy가 요청에 맞는 SecurityFilterChain을 선택함
6. SecurityFilterChain 안의 여러 보안 필터들이 순서대로 실행됨
7. 인증/인가 검사를 통과하면 DispatcherServlet으로 넘어감
8. DispatcherServlet이 Controller를 찾아 실행함
</aside>

<aside>
🔗

Tomcat
**→ DelegatingFilterProxy (서블릿 필터)**

Spring Container
**→ FilterChainProxy (Spring Bean)
     └── SecurityFilterChain 예시**
                 ├── 인증 처리 필터
                 ├── 세션 관리 필터
                 ├── CSRF 필터
                 ├── 로그인/로그아웃 처리 필터
                 ├── 인가 검사 필터
                 └── 예외 처리 필터
**→ DispatcherServlet
→ Controller**

</aside>

#### DelegatingFilterProxy

"서블릿 컨테이너(톰캣)의 세계와 스프링의 세계를 이어주는 다리 역할을 하는 필터"이다.

- Delegating(위임) +  Filter + Proxy(대리인) : 실제 일은 다른 녀석에게 위임하는 껍데기 필터
- 왜 이런 게 필요한가 : 핵심은 톰캣과 스프링이 서로 다른 세계라는 점이다.
- 서블릿 컨테이너는 Filter를 자기 규칙대로 등록하고 생성/관리한다.
- 하지만 톰캣은 스프링 Bean을 전혀 모른다. 스프링 컨테이너 안에 뭐가 있는지 전혀 모른다.
- 그런데 우리가 쓰고 싶은 실제 보안 필터들(FilterChainProxy와 그 안의 인증/인가 필터)은 스프링 Bean이다.
- DI, 라이프사이클 관리 등 스프링 기능을 다 써야 하기 때문이다.
- 필터는 톰캣에 등록되어야 하는데, 정작 실행하고 싶은 로직은 스프링 Bean이다. 톰캣에 직접 스프링 Bean을 필터로 꽂을 수는 없다.
- 그래서 DelegatingFilterProxy 클래스가 중간에서 다리를 놓는다.
- DelegatingFilterProxy 자신은 평범한 서블릿 필터라서 톰캣에 정상적으로 등록될 수 있다. (톰캣 입장에선 그냥 일반 필터 중 하나, 보안 필터가 아님)
- DelegatingFilterProxy 자체가 인증/인가 판단을 직접 하는 것은 아니다.
실제 보안 처리는 Spring Bean인 FilterChainProxy에게 위임한다.
- 하지만 실제로 요청이 들어오면, 스스로 처리하지 않고 스프링 컨테이너(ApplicationContext)에서 특정 이름의 Bean을 찾아 그 Bean에게 일을 넘긴다.
- Spring Security의 경우, 찾는 Bean 이름은 springSecurityFilterChain이고, 이 Bean의 정체가 바로 FilterChainProxy이다.

DelegatingFilterProxy
→ 톰캣과 스프링을 연결하는 다리

FilterChainProxy
→ 실제 Spring Security 보안 필터 체인을 실행하는 핵심 프록시

## HTTP BASIC이란?

- HTTP 표준(RFC 7617)에 정의된 가장 기본적인 인증 방식이다.
- 별도의 로그인 페이지나 폼이 없어, HTTP 요청 헤더에 아이디/비밀번호를 실어 보내는 가장 단순한 인증 방식
- 세션 사용 X, 매 요청마다 헤더에 인증 정보 실어 보내야 함, realm이 같은 경우 기존에 캐싱한 인증 정보를 자동 재사용하여 헤더에 보냄, 서버는 해당 헤더를 매번 검증해야 함

#### HTTP BASIC 인증 (Spring Security)

프로토콜 관점의 동작 원리

1. 클라이언트가 보호된 자원에 그냥 접근하면, 서버가 401 Unauthorized와 함께 헤더를 응답한다. 
→ WWW-Authenticate: Basic realm=”…”
2. 브라우저는 이걸 받으면 아이디/비밀번호를 입력하는 작은 팝업 창을 띄운다.
3. 사용자가 입력하면, 클라이언트는 username:password를 Base64로 인코딩해서 헤더에 담아 다시 요청한다. 
→ Authorization: Basic ZHVzZXI6cGFzc3dvcmQ=
4. 서버는 이 헤더를 디코딩해서 인증을 검증한다.

#### Spring Security에서의 동작 원리 - BasicAuthenticationFilter가 담당

위 프로토콜을 필터 체인 안에서 실제로 처리하는 호출

1. 요청이 들어오면 BasicAuthenticationFilter가 “Authorization: Basic ZHVzZXI6cGFzc3dvcmQ=” 헤더가 있는지 확인한다.
2. 헤더가 있으면 Base64를 디코딩해 아이디/비밀번호를 꺼내고, UsernamePasswordAuthenticationToken을 만든다.
*UsernamePasswordAuthenticationToken: 아직 인증되기 전의 username/password 묶음
3. 이 토큰을 AuthenticationManager에게 넘겨 인증을 검증한다. 
    
    ```
    AuthenticationManager(ProviderManager)
      → DaoAuthenticationProvider
           ├─ UserDetailsService(사용자 조회)
           └─ PasswordEncoder(비밀번호 대조)
    ```
    
4. 인증에 성공하면 인증 완료된 `Authentication` 객체를 만들어 SecurityContext에 저장하고 다음 필터로 넘어간다.
*SecurityContext : 인증된 사용자 정보를 보관하는 공간
5. 인증 정보가 없거나 인증에 실패하면,
결국 Spring Security가 BasicAuthenticationEntryPoint를 통해
401 Unauthorized와 WWW-Authenticate 헤더를 응답한다. (다시 1번 상황으로)

<aside>
🔗

요청
→ Security Filter Chain
→ BasicAuthenticationFilter
→ Authorization 헤더 확인
→ Base64 디코딩하여 username/password 추출
→ username/password로 UsernamePasswordAuthenticationToken 생성
→ AuthenticationManager에게 토큰을 넘김
→ AuthenticationManager는 적절한 AuthenticationProvider에게 인증을 위임
→ DaoAuthenticationProvider는 UserDetailsService로 사용자를 조회하고, PasswordEncoder로 비밀번호를 검증
→ 인증 성공 시 Authentication 객체를 SecurityContext에 저장하고 다음 필터로 넘어감
→ 인증 정보가 없거나 인증 실패 시, BasicAuthenticationEntryPoint가 401 Unauthorized와 WWW-Authenticate: Basic 헤더를 응답

</aside>

#### 특징

Base64는 암호화가 아니라 단순 인코딩이다. 누구나 즉시 디코딩해서 원래 아이디/비밀번호를 볼 수 있다.

- HTTP Basic은 반드시 HTTPS(TLS)와 함께 써서 통신 자체를 암호화해야 한다. 평문 HTTP에서 쓰면 비밀번호가 그대로 노출되는 것이나 마찬가지이다.
- 모든 요청마다 Authorization 헤더를 계속 보내야 한다. 서버가 인증 상태를 세션처럼 기억하지 않는 stateless 방식이기 때문, 그래서 REST API나 서버 간 통신에서 간단하게 쓰기 좋다.
- 로그아웃 개념이 애매하다. 브라우저가 자격 증명을 캐싱해두기 때문에 명시적인 로그아웃 처리가 어렵다. (세션 사용X)

#### realm

HTTP 인증에서 “보호 영역의 이름”을 뜻한다. 쉽게 말해 “지금 어느 구현에 로그인하려는 건지”를 나타내는 라벨이다. 

실제 하는 역할

1. 사용자에게 보여주는 안내 문구
브라우저가 아이디/비밀번호 팝업 창을 띄울 때,이 realm이름이 팝업 창에 그대로 표시됩니다. 예를 들어 팝업에 "MyApp에 로그인" 또는 "'MyApp’영역의 자격 증명을 입력하세요" 같은 문구가 뜨죠. 사용자가 "아, 지금 이 사이트/구역에 로그인하는 거구나"를 알 수 있게 해주는 안내판 역할입니다.
2. 보호 영역을 구분하는 식별자
한 서버 안에 서로 다른 보호 구역이 여러 개 있을 수 있어요. 예를 들면
realm="User Area" - 일반 사용자 구역 
realm="Admin Area" - 관리자 구역
realm 이름이 다르면 브라우저는 이들을 별개의 인증 영역으로 취급합니다. 그래서 사용자 구역에 로그인한 자격 증명을 관리자 구역에 자동으로 재사용하지 않아요. 같은 realm안에서는 브라우저가 한 번 입력한 자격 증명을 캐싱해서 재사용합니다.

<aside>
🔗

같은 realm
→ 이전에 입력한 Authorization 정보를 다시 보낼 수 있음

다른 realm
→ 다른 보호 영역으로 판단
→ 별도 인증 요구 가능

</aside>

### ⭐ **로그인 상태는 브라우저가 들고 있다 (이 과제의 최종 보스)**

세션 로그인은 서버가 상태를 기억하지만, Basic은 **서버가 아무것도 기억하지 않아요(stateless)**. 대신:

- 브라우저가 성공한 자격증명을 **realm 단위로 캐싱**하고, 이후 **모든 요청에 자동으로** `Authorization` 헤더를 붙여 보내요.
- 그래서 서버를 **재시작해도 로그인이 유지**돼요(인메모리 유저는 똑같이 다시 만들어지고, 브라우저는 계속 자격증명을 보내니까).
- 같은 이유로 **명시적인 로그아웃이 사실상 불가능**해요. 서버가 세션을 끊어도 브라우저가 다음 요청에 또 붙여 보내면 그대로 재인증이거든요.

spring-boot/security/form-login

## 폼 로그인이란?

개발자가 직접 만든 HTML 로그인 화면(폼)을 통해 아이디/비밀번호를 받아 인증하는 방식이다.

- HTTP Basic : 브라우저 기본 팝업 + 헤더 방식
- 폼 로그인 : 개발자가 만든 로그인 페이지 + 세션 기반

사람이 사용하는 일반적인 웹 애플리케이션의 표준 방식이다.

사용자가 직접 만든 로그인 페이지에서 아이디/비밀번호를 입력하면, Spring Security 필터가 그 요청을 가로채 인증하고, 성공하면 세션 기반 로그인 상태를 만든다

<aside>
📌

HTTP Basic
→ 브라우저 기본 팝업
→ Authorization 헤더에 인증 정보 전송
→ 보통 세션 없이 매 요청마다 헤더 인증

폼 로그인
→ 개발자가 만든 login.html
→ POST /login으로 아이디/비밀번호 제출
→ 인증 성공 시 세션 생성
→ 이후 세션으로 로그인 상태 유지

</aside>

#### 전체 동작 흐름

로그인부터 그 이후 흐름까지의 과정

1. 로그인하지 않은 사용자가 보호된 페이지에 접근 → 로그인 페이지로 리다이렉트
이 처리는 AuthenticationEntryPoint(폼 로그인 방식에서 로그인 페이지로 리다이렉트시키는 구현체인 LoginUrlAuthenticationEntryPoint)가 담당
2. 사용자가 폼에 아이디, 비밀번호 입력하고 제출하면, 자격 증명이 헤더가 아니라 요청 본문 (Body)에 파라미터로 담겨 POST /login 으로 전송된다.
3. 해당 요청을 UsernamePasswordAuthenticationFilter가 가로챈 후 Body에서 username, password 값을 꺼내 UsernamePasswordAuthenticationToken를 생성하고AuthenticationManager에게 넘김
4. 인증 검증 (공통 흐름)
    
    ```
    AuthenticationManager(ProviderManager)
      → DaoAuthenticationProvider
           ├─ UserDetailsService(사용자 조회)
           └─ PasswordEncoder(비밀번호 대조)
    ```
    
5. 인증 성공 시 인증 정보를 담은 객체 Authentication을 만들어 SecurityContext에 저장하고 SecurityContext를 세션에 저장하며, JSESSIONID라는 쿠키로 브라우저에 내려준다.
    - 성공 시 처리는 AuthenticationSuccessHandler가 담당 
    (기본적으로 원래 가려던 페이지 또는 지정된 페이지로 리다이렉트)
    - 실패 시 처리는 AuthenticationFailureHandler가 담당 
    (보통 /login?error로 되돌림)
6. 이후 요청 → 쿠키로 인증 유지
한 번 로그인하면 그 다음 요청부터는 아이디, 비밀번호를 다시 보내지 않는다. 브라우저가 자동으로 JSESSIONID 쿠키를 실어 보내고, 서버는 이 쿠키로 세션을 찾아 저장해둔 SecurityContext를 복원한다. (복원은 SecurityContextHolderFilter가 담당)
즉, 상태 유지(stateful) 방식이다. HTTP Basic은 매 요청 헤더를 보내는 무상태였던 것과 차이가 있다.

<aside>
📌

AuthenticationManager
→ 인증 처리를 총괄하는 관리자

ProviderManager
→ AuthenticationManager의 대표 구현체
→ 여러 AuthenticationProvider 중 알맞은 Provider에게 인증 위임

DaoAuthenticationProvider
→ username/password 방식 인증 처리

UserDetailsService
→ username으로 사용자 정보 조회

PasswordEncoder
→ 사용자가 입력한 비밀번호와 저장된 비밀번호 비교

</aside>

<aside>
📌

세션
   └─ SecurityContext
           └─ Authentication
                  └─ 인증된 사용자 정보

브라우저
→ JSESSIONID 쿠키 전송

서버
→ 세션 조회
→ SecurityContext 확인
→ 인증된 사용자로 판단

</aside>

#### 인증 (Authentication)

사용자가 누구인지 확인하는 과정
아이디, 비밀번호가 일치하고 해당 서비스에 속한 사용자인가?
(로그인 시 아이디, 비밀번호 확인)

#### 인가 (Authorization)

인증된 사용자가 특정 자원에 접근할 권한이 있는지 확인하는 과정
로그인한건 맞는데 해당 페이지에 접근할 권한이 있는가?

- 일반 사용자 
→ /mypage 접근 가능
→ /admin 접근 불가
- 관리자
→ /admin 접근 가능

#### 핵심 컴포넌트 정의

- SecurityFilterChain : 어떤 필터들을 어떤 순서로 태울지 정의
- AuthenticationManager / AuthenticationProvider : 인증 검증
- UserDetailService / UserDetails : 사용자 정보 조회
- PasswordEncode : 비밀번호 검증
- SecurityContextHolder : 인증 결과 보관
- AuthorizationManager : 접근 권한 판단

<aside>
📌

폼 로그인
→ 사용자가 로그인 폼에 입력
→ 서버가 세션 생성
→ 브라우저는 JSESSIONID 쿠키만 보냄
→ 서버가 세션을 보고 로그인 상태 확인

HTTP Basic
→ 로그인 페이지 없음
→ 브라우저 팝업 또는 클라이언트가 인증 정보 입력
→ 매 요청마다 Authorization 헤더에 username:password 정보를 보냄
→ 서버가 매번 헤더를 검증

realm
→ 보호 영역 이름
→ 같은 realm이면 브라우저가 인증 정보를 캐싱해서 재사용 가능
→ 다른 realm이면 별도 인증 영역으로 취급

</aside>

- 쿠키 : 브라우저에 저장되는 작은 데이터 
ex) 최근 방문 시각, 테마 정보, 세션 키 JSESSIONID
- 세션 : 서버가 사용자별 상태를 저장하는 공간
ex) username 등을 저장? 로그인 상태?