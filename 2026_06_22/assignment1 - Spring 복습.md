- Spring 프로젝트 초기 세팅
    1. Spring Boot , 그룹 com.example.spring, Java 25, 구성 YAML, 종속성 추가X (build.gradle에서 추가)
    2. 패키지 구조
        
        ```groovy
        ch01
         └─ ex_1_1
            ├─ dao
            │  └─ UserDAO.java
            ├─ domain
            │  └─ User.java
            └─ Start.java
        ```
        
- build.gradle에 종속성 추가
    
    ```groovy
    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'org.springframework.boot:spring-boot-starter-data-jdbc'
    implementation 'mysql:mysql-connector-java:8.0.32'
    runtimeOnly 'com.mysql:mysql-connector-j'
    ```
    
- MySQL 연결
    
    MySQL 데이터베이스 추가 후 계정 연결, 콘솔로 DB, table 생성
    

## Domain

- 데이터 자체를 표현하는 객체

## DAO (Data Access Object)

- DB에 접근해 데이터를 조회하거나 조작하는 기능을 맡는 객체
- SQL 실행하는 코드가 DAO에 들어감 (INSERT, SELECT, UPDATE, DELETE)

# 관심사 분리

코드에서 **역할이 다른 것들을 분리하는 설계 원칙**
변경 이유가 다른 코드들을 분리해서, 나중에 수정이 쉬운 구조로 만드는 것

<aside>

Domain/User = 데이터 객체 (DB 테이블 필드, getter, setter)
DAO/UserDAO = DB 접근 객체 (INSERT, SELECT, UPDATE, DELETE)
DAO/SimpleConnectionMaker = DB Connection 생성 객체
Start = 실행해보는 곳

---

Controller → 요청 처리
Service → 비즈니스 로직
Repository/DAO → DB 접근
Domain/Entity → 데이터 객체

</aside>

## 관심사 분리 - DB 접근,수정 / DB Connection 생성

<aside>

1단계: UserDAO 안에 DB 연결 코드가 직접 있음
→ 관심사가 섞임

2단계: getConnection() 메서드로 분리
→ 메서드 분리

3단계: 추상 메서드 + 상속
→ DB 연결 방식을 자식 클래스에서 구현
→ 하지만 상속은 결합도가 높음

4단계: SimpleConnectionMaker 클래스로 분리
→ DB 연결을 별도 클래스로 분리
→ 하지만 UserDAO가 구체 클래스를 직접 알고 있음

5단계: 인터페이스 도입
→ UserDAO가 구체 클래스가 아니라 인터페이스에 의존
→ 하지만 생성자에서 아직 new NConnectionMaker_2()를 직접 함

</aside>

### 1. UserDAO 내부 DB 접근,수정 / 연결 모두 구현

관심사 분리 X, 코드의 중복
(한 메서드에 DB 접근,수정 및 연결이 모두 들어감)

### 2. 메서드 추출

관심사 분리 X, 중복 코드의 메서드 추출
(DB 접근, 수정을 맡는 메서드들과 같은 클래스의 다른 메서드로 분리)

### 3. 추상 클래스(메서드) + 상속

DB 접근, 수정 메서드는 UserDAO에 구현, DB 연결 방식은 추상 메서드로 남겨둔 후, 자식 클래스에서 상속받아 구현

#### 문제점 :

1. **다중 상속 불가**
    
    단지 Connection 만드는 방법 하나 바꾸려고 상속을 써버리면, 나중에 UserDAO가 다른 목적으로 상속이 필요할 때 곤란
    
2. **상속의 높은 결합도**
    
    DB 연결 방식만 바꾸고 싶은데, UserDAO와 하위 클래스가 너무 밀접하게 엮여버림
    
3. **재사용 불가**
    
    DB 연결 로직이 `UserDAO`의 상속 구조 안에 들어가 있으면, 다른 DAO에서 재사용하기 애매
    

### 4. 별도의 클래스로 분리

DB 연결 로직을 기존 DAO 클래스(DB 접근,수정 역할)의 하위 클래스가 아닌 별도의 클래스로 분리

#### 문제점 :

1. **특정 클래스에 종속**
    
    `UserDAO`가 내부에서 멤버변수로 `SimpleConnectionMaker`를 직접 생성하고 있음 → 여전히 UserDAO가 SimpleConnectionMaker(DB 연결 클래스)에 의존, DB 연결 클래스를 바꾸려면 UserDAO 코드를 수정해야 함
    

### 5. 인터페이스 도입

UserDAO와 DB 연결 클래스 사이에 인터페이스를 두고, UserDAO는 구체 클래스가 아니라 인터페이스만 바라보게 함

```groovy
UserDAO <- SimpleConnectionMaker interface ---> NConnectionMaker
																					 ---> DConnectoinMaker
```

#### 문제점 :

1. `UserDAO`가 내부에서 멤버변수로 `SimpleConnectionMaker`를 직접 생성하고 있음 → 여전히 UserDAO가 SimpleConnectionMaker에 의존