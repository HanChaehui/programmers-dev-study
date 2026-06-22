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
    

---

## Domain

- 데이터 자체를 표현하는 객체

## DAO (Data Access Object)

- DB에 접근해 데이터를 조회하거나 조작하는 기능을 맡는 객체
- SQL 실행하는 코드가 DAO에 들어감 (INSERT, SELECT, UPDATE, DELETE)

---

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

## 관심사 분리 - DB 접근,수정 / DB 연결

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

### 1. 같은 클래스, 같은 메서드

같은 메서드 내부 DB 접근,수정 / 연결 모두 구현 

관심사 분리 X, 코드의 중복
(한 메서드에 DB 접근,수정 및 연결이 모두 들어감)

---

### 2. 메서드 추출 (같은 클래스, 다른 메서드)

관심사 분리 X, 중복 코드의 메서드 추출
(DB 접근, 수정을 맡는 메서드들과 같은 클래스의 다른 메서드로 분리)

---

<aside>

#### 문제 상황

1. N사와 D사가 각기 **다른 종류의 DB를 사용**
2. UserDAO를 구매한 후에도 **DB 연결을 가져오는 방법 변경** 희망
</aside>

### 3. 추상 클래스&상속 (자식/구현 클래스의 메서드)

DB 접근, 수정 메서드는 `UserDAO`에 구현, 
DB 연결 방식은 추상 메서드 선언으로 남겨둔 후, 
자식 클래스(`NUserDAO`, `DUserDAO`)에서 상속받아 각각이 원하는 추상 메서드(`getConnection()`)를 오버라이딩하여 구현

UserDAO의 소스 코드를 제공하면, N사와 D사는 getConnection() 메서드를 각자가 원하는 방식으로 확장한 후 UserDAO의 기능과 함께 사용할 수 있다.

*템플릿 메서드 패턴, 팩토리 메서드 패턴 사용

```groovy
abstract UserDAO ---> NUserDAO extends
				         ---> DUserDAO extends
```

#### 문제점 :

1. **다중 상속 불가**
    
    DB 연결 방식이라는 하나의 관심사를 분리하려고 상속을 사용해버리면, 상속이라는 강한 확장 수단을 다른 목적에 사용하기 어려워짐
    
    자식 클래스들(`NUserDAO`, `DUserDAO`)이 나중에 다른 기능 확장을 위해 또 다른 클래스를 상속받고 싶어도 불가능
    
2. **상속의 높은 결합도**
    
    DB 연결 방식만 바꾸고 싶은데, `UserDAO`와 하위 클래스(`NUserDAO`, `DUserDAO`)가 너무 밀접하게 엮여버림
    
    부모 클래스 내부의 변경이 있을 때 모든 자식 클래스를 함께 수정하거나 다시 개발해야 할 수 있음
    
3. **재사용 불가**
    
    DB 연결 로직이 `UserDAO`의 상속 구조 안에 들어가 있으면, 다른 DAO에서 재사용하기 애매
    

---

### 4. 별도의 클래스로 분리 (다른 클래스의 메서드)

DB 연결 로직을 `UserDao`의 하위 클래스의 메서드가 아닌 아예 별도의 클래스( `SimpleConnectionMaker` )의 메서드로 분리

```groovy
UserDAO 
-> SimpleConnectionMaker ---> NConnectionMaker extends
												 ---> DConnectoinMaker extends
```

#### 문제점 :

1. **여전히 분리된 특정 클래스에 종속**
    
    `UserDAO`가 내부에서 멤버변수로 `SimpleConnectionMaker`를 직접 생성하고 있음 → 여전히 `UserDAO`가 `SimpleConnectionMaker`(DB 연결 클래스)에 종속됨
    
    실제로 받는 DB 연결 클래스의 타입을 `UserDAO`가 구체적으로 알고 있어야 함, 즉, DB 연결 클래스를 바꾸려면 `UserDAO` 코드를 수정해야 함
    
    (추상 클래스와 다르게 DB 연결 클래스에서 `UserDAO`의 기존 기능을 사용하지 못하므로 고객들이 DB 연결 클래스만 수정하여 사용할 수 없게 됨, `UserDAO` 코드에서 DB 연결 클래스 내용을 직접 관리해줘야함)
    

---

### 5. 느슨한 연결, 인터페이스 도입 (인터페이스를 구현한 클래스의 메서드)

DB 접근,수정 클래스 ( `UserDAO` )와 
DB 연결 클래스들( `NConnectionMaker`, `DConnectionMaker`) 사이에 
인터페이스( `SimpleConnectionMaker` )를 두어 중간에 추상적인 느슨한 연결고리를 만들어줌

`UserDAO`는 구현 클래스( `NConnectionMaker`, `DConnectionMaker`)가 아니라 인터페이스만 바라보게 함

인터페이스를 통해 접근하게 하면 실제 구현 클래스를 바꿔도 신경 쓸 일이 없음

```groovy
UserDAO 
-> SimpleConnectionMaker interface ---> NConnectionMaker implements
																	 ---> DConnectoinMaker implements
```

#### 5-1. `UserDAO`와 실제로 관계 맺을 DB 연결 오브젝트의 타입을 
`UserDAO` 의 생성자 코드에서 구체적으로 지정

```groovy
public abstract class UserDAO_2 {

    private SimpleConnectionMaker_2 simpleConnectionMaker;

    public UserDAO_2() {
        simpleConnectionMaker = new NConnectionMaker_2();
    }
```

#### 문제점 :

1. `UserDAO`가 생성자에서 실제로 관계 맺을 DB 연결 오브젝트의 정확한 구현 클래스 타입을 지정하여(`NConnectionMaker` ) 생성하고 있음 → 여전히 `UserDAO`가 `SimpleConnectionMaker`에 의존
    
    `UserDAO` 소스 코드를 함께 제공해서, 필요할 때마다  `UserDAO`의 생성자 메서드를 직접 수정하도록 해야함 (고객에게 자유로운 DB 연결 확장 기능을 가진 `UserDAO`를 제공할 수 없음)
    

### ⭐ 5-2. 관계 설정 책임의 분리

#### `UserDAO`와 실제 DB 연결 객체 사이의 관계를 인터페이스로 추상화

`UserDAO`가 직접 구현 클래스를 선택하지 않고, 외부에서 인터페이스 구현 객체를 주입받도록 하여 객체 간 관계 설정 책임을 `UserDAO` 밖으로 분리

```groovy
public class UserDAO {

    private SimpleConnectionMaker simpleConnectionMaker;

    public UserDAO(SimpleConnectionMaker simpleConnectionMaker) {
        this.simpleConnectionMaker = simpleConnectionMaker;
    }
```

- `UserDAO`는 구체적인 DB 연결 클래스가 무엇인지 직접 결정하지 않음
- `UserDAO`는 `SimpleConnectionMaker`라는 인터페이스 타입에만 의존
- 실제 구현 객체(`DConnectionMaker`, `NConnectionMaker` 등)는 외부에서 생성해 `UserDAO` 생성자의 파라미터로 전달
- 이처럼 필요한 의존 객체를 외부에서 넣어주는 방식을 **생성자 주입**이라고 함
- 객체가 실행되는 시점에 `UserDAO`와 실제 구현 객체 사이에 **런타임 사용 관계**가 맺어짐
- 따라서 `UserDAO` 코드는 수정하지 않고, 외부에서 어떤 구현체를 넣어주느냐만 바꿔서 DB 연결 방식을 변경할 수 있음

---

## 디자인 패턴

소프트웨어 설계 시 특정 상황에서 자주 만나는 문제를 해결하기 위해 사용할 수 있는 재사용 가능한 솔루션을 말한다.

#### 템플릿 메서드 패턴

변하지 않는 기능은 슈퍼클래스에 만들어두고 자주 변경되며 확장할 기능은 서브클래스에서 만들도록 한다.

슈퍼클래스에서 디폴트 기능을 정의해두거나 비워뒀다가서브클래스에서 선택적으로 오버라이드할 수 있도록 만들어둔 메서드를 훅(hook) 메서드라고 한다.

#### 팩토리 메서드 패턴

서브클래스에서 구체적인 오브젝트 생성 방법을 결정하게 하는 것을 "팩토리 메서드 패턴"이라고 부르기도 한다.

---

## 개방 폐쇄 원칙(OCP, Open-Closed Principle)

'클래스나 모듈은 확장에는 열려 있어야 하고 변경에는 닫혀 있어야 한다.'
반면 인터페이스를 이용하는 클래스는 자신의 변화가 불필요하게 일어나지 않도록 굳게 폐쇄되어있다.

새 기능을 추가할 수는 있어야 하지만, 기존 코드를 매번 고치지는 않아야 한다.

---

## 높은 응집도와 낮은 결합도

#### - 높은 응집도 :

- 하나의 모듈이 자기 역할에만 집중하는 것,
- 같은 관심사는 한곳에 모은다

하나의 변경이 생겼을 때, 그 모듈 안의 코드들이 같은 이유로 함께 수정된다면 응집도가 높다. 만약 모듈의 일부분에만 변경이 일어나도 된다면, 모듈 전체에서 어떤 부분이 바뀌어야하는지 파악해야 하고, 또 그 변경으로 인해 바뀌지 않는 부분에는 다른 영향을 미치지 않는지 확인해야하는 이중의 부담이 생긴다.

#### - 낮은 결합도 :

- 다른 모듈과 너무 강하게 얽히지 않는 것, 변경의 파급 효과를 줄이는 것
- 다른 관심사는 서로 떨어뜨린다

책임과 관심사가 다른 오브젝트 또는 모듈과는 낮은 결합도, 즉 **느슨하게 연결된 형태**를 유지하는 것이 바람직하다. 
느슨한 연결은 관계를 유지하는 데 꼭 필요한 최소한의 방법만 간접적인 형태로 제고하고, 나머지는 서로 독립적이고 알 필요도 없게 만들어주는 것이다.
하나의 변경이 발생할 때 마치 파문이 이는 것처럼 여타 모듈과 객체로 변경에 대한 요구가 전파되지 않는 상태를 말한다.

Spring은  이렇게 역할을 나눠서 **응집도는 높이고, 결합도는 낮추는 구조**를 만들게 해주는 프레임워크

```
Controller → 요청 처리
Service → 비즈니스 로직
Repository/DAO → DB 접근
Domain/Entity → 데이터 표현
```

---

## 객체지향 설계 원칙(SOLID)

변경에 강한 객체지향 코드를 만들기 위한 원칙

<aside>

SRP - 책임 분리
OCP - 인터페이스로 확장
LSP - 안전한 상속
ISP - 인터페이스 분리
DIP - 인터페이스에 의존, 의존성 주입

</aside>

- **SRP(Single Responsibility Principle) : 단일 책임 원칙**
    
    하나의 클래스는 하나의 책임만 가져야 한다. (관심사의 분리와 비슷)
    클래스가 바뀌는 이유는 하나여야 한다. 
    
- **OCP(Open-Closed Principle) : 개방 폐쇄 원칙**
    
    확장에는 열려 있고, 변경에는 닫혀 있어야 한다.
    새 기능은 추가할 수 있어야 하지만, 기존 코드는 최대한 고치지 않아야 한다.
    
- LSP(**Liskov** Substitution Principle) : 리스코프 치환 원칙
    
    자식 클래스는 부모 클래스를 대체할 수 있어야 한다.
    부모 타입으로 쓰던 자리에 자식 객체를 넣어도 프로그램이 정상 동작해야 한다.
    
- **ISP(Interface Segregation Principle) : 인터페이스 분리 원칙**
    
    클라이언트는 자신이 사용하지 않는 메서드에 의존하면 안 된다.
    큰 인터페이스 하나보다, 작고 명확한 인터페이스 여러 개가 낫다.
    
- **DIP(Dependency Inversion Principle) : 의존 관계 역전 원칙**
    
    구체 클래스가 아니라 추상화에 의존해야 한다.
    `new DConnectionMaker()` 같은 구체 클래스에 직접 의존하지 말고, `ConnectionMaker` 같은 인터페이스에 의존하고, 실제 구현체는 생성자로 주입받자.
    

<aside>

DIP = 추상화에 의존하라는 설계 원칙
DI (Dependency Injection)= 그 의존 객체를 외부에서 넣어주는 방법
Spring = DI를 자동으로 해주는 컨테이너

</aside>

---

## 전략 패턴

- 자신의 기능 맥락에서, 필요에 따라 변경이 필요한 알고리즘을 인터페이스를 통해 통째로 외부로 분리시키고, 이를 구현한 구체적인 알고리즘 클래스를 필요에 따라 바꿔서 사용할 수 있게 하는 디자인 패턴이다.
- `UserDAO`는 컨텍스트에 해당한다.
- 컨텍스트(`UserDAO`)를 사용하는 클라이언트(`Start`)는 컨텍스트가 사용할 전략(`SimpleConnectionMaker`를 구현한 클래스)을 **컨텍스트의 생성자** 등을 통해 제공해주는 게 일반적이다.

```groovy
public class Start {
    static void main(String[] args) {

        SimpleConnectionMaker conn = new DConnectionMaker();
        UserDAO userDAO = new UserDAO(conn);

    }
}
```