#### redirect하는 이유 - MemberController

```java
@GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/members/login";
    }
```

상태를 바꾸는 요청(로그아웃) 뒤엔 redirect하여 새로 고침 시 로그아웃이 재실행 되는 것을 막고 주소창도 /members/logout 에서 /members/login으로 맞춘다.

## 게시판 → AOP 적용

### `@Aspect`  - LoggingAspect

- 이 클래스는 공통 기능(횡단 관심사)을 모아둔 Aspect라고 선언하는 어노테이션
- 이 어노테이션이 붙어야 스프링 AOP가 이 클래스 안의 Pointcut, Advice를 인식한다.
- AOP 규칙을 담고 있다는 표시일 뿐, 스프링이 관리하는 빈으로 등록해주지 않는다.
- 스프링 컨테이너에 빈으로 등록해야, 스프링이 이 Aspect를 찾아서 실제로 적용한다.

### `@Pointcut`  - LoggingAspect

- **어디에** 적용할 지 정의한다.

```java
@Pointcut("execution(*com.example.spring.basicboard.controller..*(..))")
    public void controllerLog() {
		    // 메서드 본문(Body)은 비워둔다. 
		    // 실제 로직이 아니라, 대상을 가리키는 이름표 역할만 하기 때문
    }
```

표현식 해석 : execution(*com.example.spring.basicboard.controller..*(..))

- execution : 메서드 "실행"지점을 대상으로 한다는 지시어
- * : 반환 타입은 무엇이든(모든 타입)상관없다.
- com...controller.. : controller 패키지와 그 하위 패키지 전부
- * : 그 안의 모든 클래스의 모든 메서드
- (..) : 메서드 파라미터는 개수/타입 상관없이 모두

→ "controller 패키지 아래 모든 메서드"를 대상으로 삼겠다는 뜻

### `@Around`  - LoggingAspect

- **언제/무엇을** 할지 정의하는 어드바이스

어드바이스에는 5가지 종류가 있다.

- `@Before` : 대상 메서드 실행 직전에만 실행
- `@AfterReturning` : 대상 메서드가 정상 반환된 후 실행
- `@AfterThrowing` : 대상 메서드가 예외를 던졌을 때 실행
- `@After` : 정상/예외 상관없이 끝나면 항상 실행
- `@Around` : 대상 메서드 실행을 통째로 감싼다. 
                     전/후/예외를 모두 한 메서드에서 제어 가능

```java
		// * @Around("controllerLog()") : 정의한 포인트컷(별명)을 대상으로 이 어드바이스를 적용한다.
    @Around("controllerLog()")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        // * ProceedingJoinPoint
        // - 지금 가로챈 "그 지점(메서드 호출)"에 대한 정보를 담은 객체이다.
        // - 어떤 메서드가 호출됐는지(getSignature), 넘어온 인자는 무엇인지(getArgs) 등을 꺼낼 수 있다.
        // - @Around 에서만 쓰는 특별한 타입이며, proceed() 로 "진짜 대상 메서드를 실행"시킬 수 있다.
        String method = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();

        String httpInfo = "";
        // - RequestContextHolder : 스프링이 "지금 이 요청"의 정보를 담아두는 보관소. 어디서든 꺼낼 수 있다
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if ( attributes != null ) {
            HttpServletRequest request = attributes.getRequest();
            httpInfo = request.getMethod() + " " + request.getRequestURI();
        }

        // === 대상 메서드 실행 "전" 로깅 ===
        System.out.println("[요청 시작] " + httpInfo + " -> " + method);

        System.out.println("[파라미터] " + Arrays.toString(joinPoint.getArgs()) );

        long start = System.currentTimeMillis();

        try {
            // 이 한 줄을 기준으로 요청받은 메서드 실행 전, 실행 후 로 나뉜다.
            Object result = joinPoint.proceed();

            // === 대상 메서드가 "정상 종료"된 후 로깅 ===
            long end = System.currentTimeMillis() - start; // 걸린시간
            System.out.println("[요청 완료] " + method + " : " + end + "ms");

            return result;
        } catch ( Throwable e ) {
            // === 대상 메서드가 "예외를 던졌을 때" 로깅 ===
            long end = System.currentTimeMillis() - start; // 걸린시간
            System.out.println("[요청 실패] " + method + " : " + end + "ms" + " : 예외메시지 " + e.getMessage());

            // 잡은 예외를 다시 던진다.
            // - 여기서 예외를 삼켜버리면 컨트롤러는 정상 처리된 것처럼 보여 버그가 된다.
            throw e;
        }
    }
```

## 게시판 → 테스트 코드 적용

- 원본 코드 Ctrl + Shitf + t → 테스트 코드 생성
- resources/application.yaml : 테스트 전용 설정 - 테스트 실행 시에만 적용

application.yaml 

```yaml
# 테스트 전용 설정 : 테스트 실행 시에만 적용된다.
spring:
  datasource:
    # DB_CLOSE_DELAY=-1 : 커넥션이 닫혀도 테스트가 끝날 때까지 DB를 메모리에 유지한다.
    url: jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1
    # 임베디드(내장) 모드
    # - H2가 "테스트(앱) JVM 안"에서 함께 뜬다. 별도 프로세스가 아니다.
    # - 메모리 기반이라 테스트 끝나면 DB도 통째로 사라진다.
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
  sql:
    init:
      mode: never # data.sql 참조 방지

# 첨부파일 저장 경로 - FileService가 @Value 주입받는 값이라 테스트 설정에도 반드시 있어야 한다.
# 이 파일이 메인 application.yaml을 대체하므로, 여기에 없으면 전체 컨텍스트 로딩 시 주입 실패로 뜬다.
file:
  upload-dir: ./build/test-uploads
```

## `@DataJpaTest` - MemberRepositoryTest

```java
// * @DataJpaTest
// - 기본적으로 내 DataSource 설정을 버리고 "기본 모드 임베디드 H2"로 갈아 끼운다.
// - JPA 리포지토리와 EntityManager 등 "데이터 계층"에 필요한 빈만 로드한다. (컨트롤러/서비스는 안 뜬다 -> 가볍다)
// - 각 테스트는 트랜잭션 안에서 돌고 "끝나면 자동 롤백"된다 -> 테스트끼리 데이터가 안 섞인다.
@DataJpaTest
// yaml로 직접 설정한 값 사용
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemberRepositoryTest { ... }
```

## Mockito - MemberServiceTest

- "가짜 객체(Mock)"를 쉽게 만들어 주는 자바 테스트 라이브러리다.
- Mock =진짜와 유사한 모양의 빈 껍데기 ->시나리오를 심어줄 수 있다.

#### * 왜 가짜가 필요하나?

- 단위 테스트"대상 하나(MemberService)"가 제대로 동작하는지만 보고 싶음.
- 그런데MemberService 는MemberRepository에 의존한다.
→  진짜 리포지토리를 쓰면 (1)DB가 떠 있어야 하고 (2)느리고 (3)DB/리포지토리 버그까지 섞여"무엇을 틀렸는지"불분명해진다.
- 그래서 리포지토리를'가짜'로 바꿔, 그 행동을 내가 정해 놓고 순수하게 서비스 로직만 검증한다.

#### * 자주 쓰는Mockito 문법

- `@ExtendWith(MockitoExtension.class)` : 이 테스트에서Mockito 기능을 견다
- `@Mock`: 가짜 객체를 만든다.
- `@InjectMocks`: 테스트 대상을 만들고 위@Mock등을 주입한다.

#### * 스터빙 : 이렇게 부르면 이 값을 돌려줘라

- given( repo.existsByUserId("newbie") ).willReturn(false); 
특정인자 → false반환하도록
- given( repo.findByUserId("test") ).willReturn( Optional.of(member) ); 
회원을 담아 반환
- given( repo.count() ).willThrow(newRuntimeException()); 
호출되면 예외를 던지게

#### * 검증(verify)

 "그 메서드가 호출됐는지" : 주로 반환값 없는 void로직 확인에 사용

- verify(repo).save(entity); - save가 "그 엔티티로"정확히 1번 호출됐어야 한다 (기본=1번)
- verify(repo, times(2)).save(any());     //정확히 2번
- verify(repo, never()).save(any());      //한 번도 호출되면 안 된다

#### * 인자매처

"구체값 대신 '아무거나'로 느슨하게" (any, eq ...)

- any() : 아무값이나(타입 무관)         
             예) verify(repo).save(any());
- anyString()    : 아무 문자열이나
- eq("hong")     : 정확히"hong"인 인자
- 주의: 한 메서드의 인자 중 하나라도 매처(any 등)를 쓰면, 나머지 인자도 전부 매처로 써야 한다.
- 예) verify(repo).method(eq("hong"), any());   // "hong"은 그냥 값이 아니라 eq()로 감싼다.

#### # 핵심 아이디어:

MemberService 만 진짜 객체로 쓰고, 그것이 의존하는 것들(리포지토리/매퍼)은"가짜(Mock)"로 바꾼다

- 진짜DB 리포지토리를 쓰면DB가 떠 있어야 하고 느리다. 우리는"서비스 로직"만 보고 싶다
- 그래서 리포지토리를 Mock으로 두고"이 메서드는 이런 값을 돌려준다고 치자"라고 우리가 지정한다
- 스프링을 아예 띄우지 않으므로 매우 빠르다(@SpringBootTest없음에 주목)

# 참고: 서비스에 붙은 클래스 레벨@Transactional은 여기선 동작하지 않는다.
(트랜잭션은 스프링이 프록시로 걸어주는 기능인데, 지금은 스프링을 안 띄우고new로 만든 순수 객체라서)

7/9 수업

MeberApiControllerTest