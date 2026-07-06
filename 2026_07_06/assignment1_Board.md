오늘 프로젝트

spring-boot/board/basic-board

application.yaml 설정

datasource:
url : `jdbc:mysql://localhost:3306/java_basic?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8`

```yaml
jdbc:mysql://localhost:3306/java_basic
└─┬─┘ └─┬─┘   └───┬───┘ └┬┘ └───┬────┘
프로토콜 DB종류   호스트  포트   DB이름
```

## 1. 네트워크

#### jdbc MySQL 연결 방식에 TCP 통신을 이용하는 이유

- TCP는 데이터를 보내기 전, 먼저 연결을 수립한다. (3-way handshake : SYN → SYN/ACK → ACK)
- 그 뒤엔 (a) 순서 보장 (b) 유실 시 재전송 (c) 도착 확인 을 해준다. → SQL/결과가 깨지거나 순서가 꼬이면 안되므로 적합하다.
- 그래서 커넥션 풀(HikariCP)이 필요하다. (로그에서 보이던 HikariPool)
- 위 handshake + 인증은 매번 하면 느리고 비싸다. 요청마다 새로 연결하면 성능이 크게 떨어진다.
- 그래서 앱은 시작할 때 TCP 연결 여러 개를 미리 만들어 풀(pool)에 담아두고 재사용한다.

#### localhost (IP 주소)

- localhost는 127.0.0.1, 즉 내 컴퓨터 자신을 가리키는 특별한 주소이다.

#### 3306 (Port)

- 한 컴퓨터엔 프로그램이 많이 띄워져 있다. IP주소 만으로는 특정 지을 수 없다.
- 포트는 그 컴퓨터 안의 창구 번호이다. OS가 들어온 데이터를 포트 번호로 보고 알맞은 프로세스에게 전달한다.

#### 소켓(socket)과 연결(connection)

- 내 쪽 IP:port와 상대 IP:port 한 쌍이 이어지면 하나의 연결(connection)이 된다.
- 프로그램은 이 연결의 양 끝의 소켓이라는 창구로 다룬다.

## 2. RESTful (REST, Representational State Transfer)

자원을 URI로 표현하고, HTTP 메서드로 그 자원에 대한 행위를 표현하는 API 설계 원칙

controller의 매핑을 아래 규칙에 맞춰 설계하면 RESTful하다고 한다.

#### RESTful 핵심원칙

- 자원 중심 URI : 
URI는 명사(자원)로, 동사는 사용하지 않는다. (ex : `/boards` (O), `/getBoard`(X))
- HTTP 메서드로 행위 구분
    - GET : 조회 (ex : GET /boards, GET /boards/1)
    - POST : 생성 (ex : POST /boards)
    - PUT : 전체 수정 (ex : PUT /boards/1)
    - PATCH : 부분 수정 (ex : PATCH /boards/1)
    - DELETE : 삭제 (ex : DELETE /boards/1 )

## 3. 영속성 (Persistence) 
- 관계 : ORM(개념) > JPA(표준) > Hibernate(구현체)

프로그램이 종료되어도 데이터가 사라지지 않게 영구히 보존하는 성질

- 자바 객체는 메모리에만 있어 프로그램이 꺼지면 사라진다. (휘발성)
- 이를 DB같은 영구 저장소에 저장해 다시 꺼내 쓸 수 있게 만드는 것이 영속성이다.
- 이 “객체 ↔ DB” 변환을 담당하는 계층을 영속성 계층이라고 하며, basicboard.domain.repository가 그 역할이다.
- 관련 용어
    - 영속화 : 객체를 DB에 저장하는 행위
    - 영속성 컨텍스트 : JPA가 엔티티를 관리하는 1차 캐시 공간, 엔티티의 상태 변화를 추적해 자동으로 SQL을 실행해준다.

## ORM (Object Relational Mapping 객체 관계 매핑)

자바의 “**객체(Object)**”와 관계형 DB의 “**테이블(Relation)**”을 서로 연결(매핑)해주는 기술

- 자바 객체(Entity) ↔ DB 테이블/행을 자동으로 매핑하고, 우리가 짠 코드 대신 SQL을 만들어 실행한다.
- spring-boot-starter-data-jpa를 넣으면 기본 구현체로 Hibernate가 사용된다.

## HikariCP (히카리 커넥션 풀)

- DB 커넥션은 만들때마다 비용이 크므로, 미리 여러 개 만들어 두고 빌려주는 “커넥션 풀”을 사용한다.
- HikariCP는 스프링 부트의 "기본" 커넥션 풀이며 빠르고 가벼워서 널리 쓰인다.
- 요청이 오면 풀에서 커넥션을 빌려 쓰고, 끝나면 반납한다. (매번 새로 연결하지 않아 성능이 좋다)
- 별도 설정 없이 자동 적용되며, 필요하면 spring.datasource.hikari 아래에서 풀 크기 등을 조정한다

## 계층 구조와 의존성 규칙

- 프레젠테이션(controller) -> 애플리케이션(service) -> 도메인(domain) 순으로 의존
- 도메인은 어떤 계층에도 의존하지 않는 가장 안쪽 핵심이다.
- 기술(JPA 등)이 도메인을 침범하지 않도록, 도메인은 인터페이스에만 의존한다.

spring-boot/board/basic-board

#### 패키지 구조 및 요청 처리 흐름

- controller : 프레젠테이션 계층, HTTP 요청/응답 처리, DTO로 입출력
- service : 애플리케이션 계층, 유스케이스 조율 및 트랜잭션 관리
- dto : 계층 간 데이터 전송 객체 (요청/응답)
- domain : 도메인 계층 (핵심 비즈니스 규칙)
    - entity : 엔티티 / 애그리거트 (Board, Comment 등)
    - repository : 리포지토리 인터페이스 (JpaRepository 상속, 도메인이 규약 정의)
- mapper : Entity ↔ DTO 변환
- exception : 도메인/공통 예외 처리
- aop : 공통 관심사(로깅 등)를 @Aspect 로 분리, 학습용이라 System.out 으로만 출력

#### 요청 흐름

1. controller가 HTTP 요청을 받아 DTO로 변환
2. service가 유스케이스를 실행하며 트랜잭션을 관리
3. service가 domain.repository 인터페이스로 엔티티를 조회/저장
4. Spring Data JPA가 인터페이스 구현체를 런타임에 자동 생성해 DB 접근
5. entity를 mapper로 DTO 변환해 controller를 통해 응답

aop는 위 흐름을 가로질러 controller/service 메서드 호출 전후를 가로채 로그를 남긴다.

의존성 방향: controller → service → domain.repository(interface) ← JPA 구현
도메인이 기술(JPA)에 의존하지 않도록 인터페이스에만 의존한다

#### 회원가입 과제 명세서 개념 참고

### **(1) 계층 구조 — 요청이 흐르는 길**

```
[브라우저] --JSON--> Controller ---> Service ---> Repository ---> [DB]
                     (웹 담당)     (로직 담당)   (저장 담당)
```

| **계층** | **책임** | **이번 과제에서** |
| --- | --- | --- |
| Controller | HTTP 요청/응답 처리 (JSON 받고 주기) | 가입 요청을 받아 서비스에 넘김 |
| Service | 비즈니스 로직 | **아이디 중복 검사** 후 저장 지시 |
| Repository | DB 접근 | INSERT / 존재여부 조회 |

> 왜 나누나요? 각 계층이 **한 가지 일**만 하면, 바뀔 때 그 계층만 고치면 되고 테스트도 쉬워져요.
> 

### **(2) JpaRepository — 인터페이스만 있는데 어떻게 동작하지?**

```
public interface MemberRepository extends JpaRepository<Member, Long> { }
```

- 구현 클래스를 **우리가 안 만들어요.** 스프링 데이터 JPA가 뜰 때 **구현체를 자동 생성**해 빈으로 등록해요.
- `JpaRepository<Member, Long>` = "`Member` 엔티티를, 기본키 타입 `Long`으로 다루는 리포지토리".
- 상속만 해도 `save` / `findById` / `count` / `existsById` … 가 **공짜로** 딸려와요.
- 조건 조회는 **메서드 이름 규칙**으로: `existsByUserId(String)` → `SELECT ... WHERE user_id = ?` 를 자동 생성.

### **(3) DTO vs Entity — 왜 나눠요?**

|  | **Entity (`Member`)** | **DTO (`MemberJoinRequestDto`)** |
| --- | --- | --- |
| 역할 | DB 테이블과 매핑 | 요청/응답 데이터 그릇 |
| 노출 | 밖에 그대로 노출하면 위험(비번 등) | 필요한 필드만 담아 주고받기 |
| 예시 | `id, userId, password, userName` | `userId, password, userName` |

### **(4) 예외 공통 처리 — `@RestControllerAdvice`**

- 서비스에서 문제가 생기면 `throw` 로 **던지기만** 해요.
- 컨트롤러는 그걸 잡지 않아요. 대신 `@RestControllerAdvice`가 **한 곳에서** 가로채 알맞은 상태코드(예: 409)와 JSON으로 바꿔줘요.
- 덕분에 컨트롤러는 성공 흐름에만 집중하고, 예외→응답 변환은 한 곳에 모여요.