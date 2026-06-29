## 예외 - ch04

- 프로그램 실행 중 발생한 catch 블록을 써서 잡아낸 후, 콘솔에 출력만 하고 아무 처리를 하지 않고 넘어가는 것은 매우 위험한 일
- 예외 처리의 핵심 원칙 - 모든 예외는 적절하게 복구되거나, 작업을 중단시키고 운영자 또는 개발자에게 분명하게 통보되어야 한다.

## 예외의 종류

자바에서 throw할 수 있는 객체는 모두 Throwable의 자식이다.

```java
Throwable
  ├── Error
  │    └── 시스템 레벨의 심각한 오류
  │        보통 애플리케이션 코드에서 직접 처리하지 않음
  │
  └── Exception
       ├── Checked Exception
       │    └── RuntimeException 계열이 아닌 Exception 하위 클래스
       │        예: IOException, SQLException
       │
       └── Unchecked Exception
            └── RuntimeException을 상속한 예외
                예: NullPointerException, IllegalArgumentException
```

핵심 구분선은 'RuntimeException을 상속했는가'이다.

- 상속 안 함 → Checked Exception (컴파일러가 처리를 강제)
- 상속함 → Unchecked Exception (컴파일러가 강제하지 않음)

1. **Error**
    - java.lang.Error의 자식
    - JVM/시스템 레벨의 비정상 상황(OutOfMemoryError, StackOverflowError 등)
    - 애플리케이션 코드로 복구 가능한 성격이 아니므로 catch 하려 하지 않는다.
2. **Checked Exception (체크 예외)**

'외부 요인으로 실패할 수 있는' 작업

- Exception의 자식 중 RuntimeException을 상속받지 않은 예외(IOException, SQLException 등)
- 컴파일러가 예외 처리 여부를 검사함
- 반드시 try-catch 하거나 throws로 선언해야 컴파일된다(컴파일러가 강제)
- 그래서 잘못 쓰면 아래 '안티패턴'들이 나오기 쉽다.
- 의미 : '발생할 수 있음을 미리 알리고, 호출하는 쪽이 대비하게 하라'는 의도
- 주로 외부 자원(파일, DB, 네트워크)처럼 '내 잘못이 아니어도 실패할 수 있는' 상황에 쓰인다
1. **Unchecked Exception (언체크 예외, 런타임 예외)**

'코드를 고치면 되는 버그' 또는 '복구 불가라 굳이 강제할 필요 없는' 상황

- RuntimeException의 자식 (NullPointerException, IllegalArgumentException, ArrayIndexOutOfBounds 등)
- 컴파일러가 예외 처리 여부를 강제하지 않음
- try-catch처리 혹은 throws 선언 하지 않아도 컴파일 된다.
- 의미 : 주로 '프로그램의 버그' 성격이라, 복구보다 코드 수정 대상이다.

*실무/스프링의 경향: 복구가 불가능한 체크 예외(SQLException 등)는 굳이 강제로 떠넘기지 말고 언체크 예외로 '전환(wrapping)'해 던지는 방식을 즐겨 쓴다. 그래야 코드가 불필요한 throws로 더럽혀지지 않으면서도 원인 정보는 보존된다.

#### Checked Exception 예외 처리 - 안티패턴

1. 예외 블랙홀 - 아무것도 하지 않음
    - 예외가 났다는 사실 자체가 흔적도 없이 사라짐
    - 가장 위험하다. 나중에 엉뚱한 곳에서 문제가 터지고, 원인 추적이 거의 불가능해진다.
2. 콘솔에 출력만 하고 진행 
    - `printStackTrace()`나 `println()`으로 '찍는 것'은 예외를 '처리'한 것이 아니다.
    - 운영 환경에선 그 출력이 묻혀서 아무도 못 보고, 프로그램은 잘못된 상태로 계속 동작한다.
3. 무의미하고 무책임한 throws
    - 어떤 예외가 왜 날 수 있는지 고민하지 않고, 그냥 throws Exception으로 전부 떠넘긴다.
    - 이 메서드를 쓰는 쪽도 의미 있는 정보를 못 받고 똑같이 throws Exception으로 떠넘기게 된다.

## 예외 처리 전략 3가지 - ch04/Exception_03

‘모든 예외는 복구되거나, 분명히 통보되어야 한다’는 원칙을 실제로 지키는 방법

1. **예외 복구 (재시도 복구와 실패 시 통보)**
    - 예외 상황을 파악하고, 문제를 해결해서 ‘정상 흐름으로 되돌리는 것’
    - 예외가 났어도 사용자/프로그램 입장에선 아무 문제 없이 작업이 끝난 것처럼 만든다.
    - 대표 예시 : 재시도 (retry), 대체값/대체경로(fallback)
    - 주의) 단순히catch로 잡고 무시하는 ‘예외 블랙홀’은 복구가 아니고 정상 상태로 되돌려야 복구이다.
2. **예외 회피 (로그 처리 후 그대로 호출자에게 넘김)**
    - 자신이 처리하지 않고, 자신을 호출한 쪽으로 예외를 넘기는 것
    - 두 가지 방식 : 
    (a) throws로 그대로 넘겨 처리 책임을 호출자에게 위임
    (b) catch하여 로그 등의 부가 작업만 하고, 다시 던져 처리 자체는 호출자에게 위임
    - 주의) 아무 생각 없이 던지는 회피는 Exception_01의 '무책임한 throws' 안티패턴이 된다. "이 예외는 나보다 호출한 쪽이 처리하는 게 맞다"는 분명한 이유가 있을 때만 회피한다.
3. **예외 전환 (더 적절한 예외로 바꿔 던짐 + 원인 보존)**
    - 잡은 예외를 그대로 넘기지 않고, ‘더 적절한 예외로 바꿔서’ 던지는 것
    - 반드시 원인 예외(e)를 담아서 던져야 한다. (원인 정보/스택트레이스 보존) 이를 ‘중첩 예외’라 한다.
    
    ```java
    		// 목적 (A) 더 의미 있는 예외로 바꾸기
        void 예외전환_의미부여(String id) {
            try {
                insertUser(id);
            } catch (SQLException e) {
                if (isDuplicateKey(e)) {
                    // 호출하는 쪽은 'SQL오류'가 아니라, '아이디 중복'이라는 의미를 받게 된다.
                    throw new DuplicateUserIdException(id, e);
                }
    
                // 중복이 아닌 다른 SQL 오류라면 아래 (B)처럼 런타임으로 전환해 던진다.
                throw new RuntimeException("회원 저장 중 DB 오류", e);
            }
        }
    
        // 목적 (B) 불필요한 체크 예외를 런타임(언체크) 예외로 포장하기
        //  - 어차피 복구할 수 없는 체크 예외라면, throws로 계속 떠넘기게 만들지 말고
        //    런타임 예외로 감싸 던져서 호출하는 코드가 깨끗해지게 한다.
        //  - 스프링이 SQLException을 DataAccessException(런타임)으로 바꿔주는 것이 바로 이 방식이다.
        void 예외전환_런타임포장() {
            try {
                fetchFromNetwork();
            } catch (SQLException e) {
                throw new RuntimeException("작업 중 DB 오류가 발생했습니다. ", e);
            }
        }
        
        // 업무적 의미를 가진 사용자 정의 예외(언체크). 원인 예외를 담을 수 있게 만든다.
        static class DuplicateUserIdException extends RuntimeException {
            DuplicateUserIdException(String id, Throwable cause) {
                super("이미 존재하는 아이디입니다: " + id, cause); // cause로 원인 보존
            }
        }
    ```
    

스프링-예외.md

---

## 서비스 추상화 - ch05/ex_5_1

- 여러 가지 구체적인 방법(구현)들에서 ‘공통된 개념/동작’만 뽑아내, 그것을 하나의 일관된 인터페이스로 표현하는 것
- 사용하는 쪽은 ‘무엇을 하는지’(추상)만 알고, ‘어떻게 하는지’(구체 구현)는 몰라도 되게 만든다.

#### 왜 필요한가? 추상화가 없다면

- 코드가 특정 기술/환경에 종속된다.
- 기술을 바꾸면 그 기술을 쓰는 코드를 전부 고쳐야 한다.(변경에 약함)
- 외부 시스템에 묶여 테스트가 어렵고 느려진다.

#### 핵심 아이디어

‘일관성 없는 여러 저수준 기술’을, ‘일관된 고수준 인터페이스’ 뒤로 숨긴다.

그리고 실제 구현은 DI로 갈아 끼운다.

→ 비즈니스 로직(애플리케이션 코드)은 추상화된 인터페이스에만 의존하므로, 기술이 바뀌어도 (또는 테스트용 가짜로 바꿔도) 비즈니스 로직 코드는 그대로이다.

이것은 1~4장에서 본 'IoC/DI', '전략 패턴', '예외 추상화'와 같은 원리의 연장선이다.

#### 5장의 전개 (이 원리를 단계별로 적용한다)

- ex_5_1 : 사용자 레벨 관리 기능 추가 (추상화를 적용할 '대상' 코드를 먼저 만든다)
- ex_5_2 : 트랜잭션 서비스 추상화 (PlatformTransactionManager)
- ex_5_3 : 서비스 추상화와 단일 책임 원칙 (비즈니스 로직 vs 트랜잭션 분리)
- ex_5_4 : 메일 서비스 추상화 (MailSender + 테스트 대역)