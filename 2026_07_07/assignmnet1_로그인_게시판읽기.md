## GlobalExceptionHandler

#### `@RestControllerAdvice`

모든 컨트롤러에 공통으로 적용되는 보조 클래스임을 선언하는 어노테이션

- 특정 컨트롤러 한 개가 아니라, 애플리케이션의 모든 `@Controller` / `@RestController` 에서 발생하는 예외를 가로챈다.

#### 전역 예외 처리

- 예외가 터질 때마다 컨트롤러 안에서 try-catch로 일일이 잡으면, 컨트롤러마다 같은 코드가 반복된다.
- 핵심 로직과 예외 처리 코드가 뒤섞여서 지저분해지고, 응답 형태 (상태코드/메시지)도 제각각이 되기 쉽다.
- 그래서 "예외 처리"라는 공통 관심사를 한 곳에 모아두고, 컨트롤러/서비스는 예외를 "던지기만"하게 만든다. → 컨트롤러는 성공 흐름(정상 로직)에만 집중하고,예외 → 응답 변환은 전부 이 클래스가 책임진다.

#### 전체 흐름 - MemberService → GlobalExceptionHandler

서비스: throw new DuplicateUserIdException("이미 존재하는 아이디입니다.")
→ (컨트롤러는 잡지 않고 그대로 위로 전파됨)
→ 여기 GlobalExceptionHandler가 가로챔
→ 상태코드(409) + ErrorResponseDto(JSON)로 변환해 응답
→ signUp.js의 error콜백이 message를 꺼내 화면에 표시

#### `@ExceptionHandler`

어떤 예외를 처리할 지 지정한다.

- 괄호 안에 적은 예외 타입이 발생하면, 스프링이 이 메서드를 자동으로 호출한다.
- 메서드 파라미터로 그 예외 객체(e)를 받아, 메시지 등 상세 정보를 꺼내 쓸 수 있다.

#### `ResponseEntity<T>`

HTTP 응답 전체를 표현하는 객체이다. 

- 응답 본문(Body)뿐 아니라 상태 코드와 헤더까지 직접 지정할 수 있다.
- 단순히 DTO만 반환하면 상태 코드가 항상 200(OK)로 나간다.
- 에러 상황에서는 상태 코드를 4XX/5XX 등으로 바꿔야 하므로 ResponseEntity로 감싼다.

## MemberService - Optional

#### `Optional<Member>`

NPE(NullPointerException) 예방

- 예전에는 값이 없음을 null로 표현했는데, null을 깜빡하고 그냥 쓰면 실행 중에 NPE가 터졌다.
- ex : `Member m = findByUserId("test"); m.getUserName();` m이 null이면 터진다.
- 게다가 반환 타입만 봐서는 null이 올 수 있는지 알 수가 없어 실수하기가 쉬웠다.

## `Optional`

“값이 없을 수도 있다”를 타입으로 알려주는 상자(Wrapper)

- 반환 타입이 Optional이면 “값이 없을 수 있으나 처리해라”라고 컴파일 단계에서 강제된다.
- 즉 ‘없을 수 있음’을 문서가 아니라 “타입”으로 표현해 실수를 막는 장치이다.

#### 상자(Wrapper)를 여는(값을 꺼내는) 주요 메서드 
- MemberService, MemberApiController

- `isPresent()` / `isEmpty()` : 값이 있는지/없는지 boolean으로 확인
- `get()` : 값을 꺼냄(비어있으면 예외, 되도록 사용하지 않음)
- `orElse(기본값)` : 값이 있으면 그 값, 없으면 인자의 기본값
(기본값은 항상 미리 계산됨)
- `orEsleGet(함수)` : 값이 있으면 그 값, 없으면 함수를 실행
(없을 때만 계산 실행)
- `map(함수)` : 값이 있으면 다른 값으로 변환, 없으면 그대로 empty
- `filter(조건)` : 값이 있고 조건을 만족하면 유지, 아니면 empty

#### 세 가지 상태

- `Optional.of(값)` : 값이 확실히 있을 때 (값이 null이면 즉시 예외)
- `Optional.empty()` : 빈 상자 (값이 없음)
- `Optional.ofNullable(값)` : 값이 null일 수도 있을 때 (null이면 empty, 아니면 of)

#### 주의 (자주 하는 실수)

- get() 남발 금지 : 비어있으면 예외이다. ofElse* 계열로 안전하게 꺼내야한다.
- `if(opt.isPresent()) opt.get()`패턴은 사실상 null 체크와 다를 게 없다 
→ map / ofElseGet으로 흘려보내자
- 보통 “반환 타입”에만 쓴다. 필드나 파라미터 타입으로는 잘 사용하지 않는다. (설계 관례)

```yaml
return 한 줄에 쓰인 람다식과 Optional.filter 이해하기
---------------------------------------------------------------------------------
# findByUserId 는 Optional<Member> 를 돌려준다 (회원이 있을 수도, 없을 수도)
# .filter( member -> 조건 ) 의 동작:
		- Optional 안에 값이 "있고" + 람다가 true -> 그 값을 그대로 유지
		- 값이 "없거나" + 람다가 false -> 빈 Optional(Optional.empty)로 만든다
=> "아이디로 찾은 회원이 있고, 비밀번호까지 일치하면 남기고, 아니면 비운다" = 로그인 성공/실패 판정

# member -> member.getPassword().equals(...) 가 바로 람다식(이름 없는 함수)이다
     member                         : 입력 파라미터 (Optional 안에 든 Member)
     ->                             : "이것을 받아서 ~를 반환한다"
     member.getPassword().equals(..): 반환값 (boolean). 비밀번호가 같으면 true

# 만약 람다(와 Optional)를 쓰지 않았다면, 아래처럼 풀어 쓴 것과 같다:

     // 1) 아이디로 회원을 조회한다 (없으면 null 이 나오도록 orElse(null) 사용)
     Member member = memberRepository.findByUserId(request.getUsername()).orElse(null);

     // 2) 회원이 존재하고(null 아님) + 비밀번호가 일치하면 -> 로그인 성공
     if (member != null && member.getPassword().equals(request.getPassword())) {
         return Optional.of(member);   // 성공: 회원을 담아 반환
     }

     // 3) 아이디가 없거나 비밀번호가 틀리면 -> 로그인 실패
     return Optional.empty();          // 실패: 빈 Optional 반환

   => 위 if 분기(널 체크 + 비밀번호 비교)를 .filter(람다) 한 줄로 압축한 것이 아래 코드다
```

람다식 + Optional.filter(람다) 사용 O

```java
public Optional<Member> login(LoginRequestDto dto) {

    return memberRepository.findByUserId(dto.getUsername())
            .filter(
                    member -> member.getPassword().equals(dto.getPassword())
            );
}
```

람다식 사용 X

```java
public Optional<Member> login(LoginRequestDto dto) {

     Member member = memberRepository.findByUserId(request.getUsername()).orElse(null);
     if (member != null && member.getPassword().equals(request.getPassword())) {
         return Optional.of(member);   // 성공: 회원을 담아 반환
     }
     return Optional.empty();          // 실패: 빈 Optional 반환

}
```