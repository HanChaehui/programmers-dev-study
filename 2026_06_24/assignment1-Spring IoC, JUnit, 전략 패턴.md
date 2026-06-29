## 1. 의존 관계 주입 (DI, Dependency Injection)

스프링 IoC 기능의 대표적인 동작 원리는 **의존관계 주입 DI**이다.

- 의존 대상이 변했을 때, 그 대상을 사용하는 오브젝트도 영향을 받는다.
- 의존관계 주입은 구체적인 의존 오브젝트와 그것을 사용할 오브젝트, 즉 클라이언트를 런타임 시점에 연결해주는 작업이다.
- 의존 관계 주입의 핵심은 설계 시점에는 알 수 없었던 두 오브젝트 사이의 관계를 제3의 존재가 런타임 시점에 맺어준다는 점이다.
- `UserDAO`가 `ConnectionMaker` 인터페이스에만 의존하면, `DConnectionMaker` 같은 구현 클래스가 다른 구현 클래스로 바뀌거나 내부 메서드 내용이 바뀌어도 `UserDAO`는 영향을 덜 받는다.
- 이처럼 인터페이스에만 의존하도록 설계하면 구현 클래스와의 관계가 느슨해지고, 변화에 강한 낮은 결합도의 구조를 만들 수 있다.
- 코드나 클래스 구조에서 드러나는 의존관계 외에도, 런타임 시점에 실제 오브젝트 사이에서 만들어지는 의존관계가 있다.
- 이를 런타임 의존관계 또는 오브젝트 의존관계라고 하며, 설계 시점의 의존관계가 실행 시점에 실제 객체 관계로 실체화된 것이라고 볼 수 있다.

→ DI : 설계 시점에는 인터페이스에 의존하게 하고, 런타임 시점에 제3의 존재가 실제 구현 객체를 연결해주는 것

---

## 2. 테스트 코드

#### Start.java 테스트의 문제점 (ch02/ex_2_1)

```java
...
public class Start {
    static void main(String[] args) throws SQLException, ClassNotFoundException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);
        UserDAO userDAO = context.getBean("userDAO", UserDAO.class);
        User user = userDAO.get("test1");
        System.out.println(user.getName());
    }
}
```

1. 수동 확인 작업의 번거로움
    
    콘솔에 출력된 값을 보고 등록과 조회가 정상적으로 되었는지 확인하는 것은 사람의 책임이다. 즉, 테스트 결과를 코드가 자동으로 판단하지 못하고 개발자가 직접 눈으로 확인해야 한다.
    
2. 실행 작업의 번거로움
    
    DAO가 수백 개로 늘어나고, 각각을 테스트하기 위한 `main()` 메서드도 많아진다면 전체 기능을 테스트하기 위해 여러 `main()` 메서드를 일일이 실행해야 한다. 이 방식은 반복 실행이 어렵고, 테스트 자동화에도 적합하지 않다.
    

### 단위 테스트

- 테스트는 가능하면 작은 단위로 쪼개서 집중적으로 수행해야 한다.
- 관심사의 분리는 테스트에도 적용된다.
- 단위 테스트는 개발자가 설계하고 작성한 코드가 의도한 대로 동작하는지 빠르게 확인하기 위해 사용한다.
- 작은 단위로 테스트하면 문제가 발생했을 때 원인을 찾기 쉽다.

### 자동 수행 테스트 코드

- 테스트는 사람이 직접 확인하는 방식이 아니라, 자동으로 수행되도록 코드로 작성하는 것이 중요하다.
- 자동화된 테스트의 장점은 같은 테스트를 자주, 반복해서 실행할 수 있다는 점이다.
- 테스트가 자동화되면 코드 변경 후에도 기존 기능이 정상적으로 동작하는지 빠르게 확인할 수 있다.
- 테스트 코드는 애플리케이션을 구성하는 실제 코드 안에 섞어두기보다, 별도의 테스트 클래스로 분리하는 것이 좋다.
- 일반적으로 실제 애플리케이션 코드는 `src/main/java`에 두고, 테스트 코드는 `src/test/java`에 작성한다.

### 테스트의 결과

- 모든 테스트는 기본적으로 성공과 실패의 두 가지 결과를 가진다.
- 테스트 실패는 크게 두 가지로 나눌 수 있다.
    1. 테스트 실행 중 에러가 발생한 경우
    2. 테스트는 실행되었지만 결과가 기대와 다른 경우

따라서 좋은 테스트는 단순히 코드를 실행하는 데서 끝나지 않고, 실행 결과가 기대한 값과 일치하는지 자동으로 검증해야 한다.

---

# 3. JUnit

자바에서 단위 테스트를 자동으로 작성하고 실행할 수 있게 해주는 표준 테스트 프레임워크

위 `Start.java`처럼 `main()`으로 직접 실행하고 콘솔에 출력된 값을 사람이 눈으로 확인하던 방식을 대체한다.

### JUnit - 프레임워크의 특징 : 제어의 역전 (IoC)

- `main()` 메서드를 사용하는 방식에서는 개발자가 직접 테스트 실행 흐름을 제어한다.
- 반면 JUnit에서는 개발자가 테스트 메서드만 작성해두면, JUnit 프레임워크가 해당 메서드를 찾아 자동으로 실행한다.
- 즉, 개발자가 직접 테스트 흐름을 호출하는 것이 아니라, 프레임워크가 개발자가 작성한 테스트 코드를 호출하고 실행 흐름을 제어한다.
- 이 점에서 JUnit도 **제어의 역전 IoC**이 적용된 프레임워크라고 볼 수 있다.

### Junit - 기본 사용법 (JUnit 5 기준)

- `@SpringJUnitConfig(컨테이너명.class)` : 애플리케이션 컨텍스트 지정 및 생성
- `@Autowired` : 일치하는 빈 찾아 연결
- `@Test` : 해당 메서드가 테스트 메서드라는 것을 표시하며, JUnit이 이 메서드들을 자동으로 찾아 실행한다. (throws exception 함께 사용?)
- `@BeforeEach` : 각 `@Test` 메서드가 실행되기 전에 매번 실행되며 공통 준비 작업에 사용한다. (예: 테스트용 객체 생성, 컨텍스트 준비 등)
- `@AfterEach` : 각 `@Test` 메서드가 실행된 후에 매번 실행되며 테스트 후 정리 작업에 사용한다.
- `assertEquals(기대값, 실제값)` : 테스트 결과가 기대한 값과 같은지 코드로 검증한다.
→ 사람이 콘솔을 보고 판단할 필요가 없어진다. (수동 확인의 번거로움 해결).
    
    → 기대값과 실제값이 다르면 JUnit이 자동으로 테스트 실패로 표시하며, 테스트 실행 중 예외나 에러가 발생해도 실패로 처리된다.  (테스트의 두 가지 결과)
    
- `assertThrows(기대하는 예외.class, 실행할 코드)` : 특정 코드를 실행했을 때 기대한 예외가 발생하는지 검증한다. (예외가 발생해야 정상인 상황)

### 테스트코드 예제 - UserDAOTest

`UserDAO`의 DB 저장, 조회, 예외 상황을 JUnit과 Spring Test를 이용해 자동으로 검증하는 단위 테스트 코드

Start.java의 main() + System.out.println 방식을 JUnit 테스트로 옮긴 것이다.
사람이 직접 테스트를 실행하고 콘솔 출력 결과를 눈으로 확인하지 않아도, assert(단언)로 결과를 '코드가' 자동 검증해주며, DAO가 늘어나도 테스트 메서드만 추가하면 한 번에 자동 실행할 수 있다.

- `UserDAOTest`는 `UserDAO`의 기능을 테스트하는 클래스
- `@SpringJUnitConfig(DaoFactory.class)`  : 
- JUnit 테스트를 스프링 컨테이너 안에서 실행하게 해주는 설정
- 테스트 실행 시 스프링이 `DaoFactory`를 읽어서 애플리케이션 컨텍스트를 만들고, 그 안에 등록된 빈들을 사용할 수 있게 한다.
- `@Autowired` : 
- 스프링 컨테이너 안에서 `UserDAO` 타입의 빈을 찾아서 이 필드에 넣어준다.
- `@SpringJUnitConfig(DaoFactory.class)`가 없으면 스프링 컨테이너가 만들어지지 않기 때문에 `@Autowired`도 동작하지 않는다. 그러면 `userDAO`가 `null`이 되고, 테스트 중에 `NullPointerException`이 날 수 있다.
    
    <aside>
    
    1. JUnit 5에 스프링 테스트 기능 연결
    2. DaoFactory 설정 클래스로 스프링 컨테이너 생성
    3. UserDAO타입의 빈을 찾아 UserDAOTest 필드에 주입
    
    ```java
    // @SpringJUnitConfig(DaoFactory.class) 사용으로 인해 
    // 하단 코드 생략 가능
    AnnotationConfigApplicationContext context =
            new AnnotationConfigApplicationContext(DaoFactory.class);
    
    // @Autowired 사용으로 인해 하단 코드 생략 가능
    UserDAO userDAO = context.getBean("userDAO", UserDAO.class);
    ```
    
    </aside>
    
- `@BeforeEach`  : 각 테스트 메서드가 실행되기 직전에 매번 실행, 
예를 들어 테스트가 5개 있으면 `setUp()`도 5번 실행된다.
테스트를 항상 같은 상태에서 시작하므로 반복 가능하고 안정적이다.
- `@Test` : given-when-then 구조로 then에서 asserEquals() 사용, throws exception을 함께 사용하여 시스템 에러도 함께 체크
- `assertThrows()` : userDAO.add(user)를 실행했을 때 SQLException이 발생해야 테스트 성공
    
    1. 람다식 버전
    
    `assertThrows(SQLException.class, () -> userDAO.add(user));` 
    
    2. 익명 클래스 버전 : 이름 없는 클래스를 그 자리에서 정의하고 바로 객체로 만드는 문법으로 요즘은 보통 람다식으로 더 간단히 사용
    
    ```java
    // Executable은 메서드가 하나뿐인 함수형 인터페이스 -> 람다식 표현 가능
    // 익명 클래스 : Executable 인터페이스를 구현과 동시에 객체 생성
    Executable action = new Executable() {
        @Override
        public void execute() throws Throwable {
        // 예외가 날 코드
            userDAO.add(user);
        }
    };
    
    assertThrows(SQLException.class, action);
    ```
    
- `@Disabled` : 이 테스트를 실행하지 않음
- 테스트 전체 코드
    
    src/test/java/com/example/spring/springtheory/ch02/ex_2_1/dao/UserDAOTest
    
    ```java
    package com.example.spring.springtheory.ch02.ex_2_1.dao;
    
    import com.example.spring.springtheory.ch02.ex_2_1.domain.User;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Disabled;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
    
    import java.sql.SQLException;
    
    import static org.junit.jupiter.api.Assertions.assertEquals;
    import static org.junit.jupiter.api.Assertions.assertThrows;
    
    // * UserDAO 단위 테스트 (JUnit 5)
    // Start.java의 main() + System.out.println 방식을 JUnit 테스트로 옮긴 것이다.
    //  - 사람이 콘솔을 눈으로 확인하지 않아도, assert(단언)로 결과를 '코드가' 자동 검증한다.
    //  - DAO가 늘어나도 테스트 메서드만 추가하면 한 번에 자동 실행할 수 있다.
    
    // * @Autowired로 빈을 주입받으려면? -> 테스트를 '스프링 컨테이너 안에서' 실행해야 한다.
    //   @SpringJUnitConfig(DaoFactory.class) 가 그 역할을 한다. 이 한 줄은 사실 두 가지를 합친 것이다.
    //    1) @ExtendWith(SpringExtension.class) : JUnit5에 스프링 테스트 기능을 끼워 넣는다.
    //    2) @ContextConfiguration(classes = DaoFactory.class) : 어떤 설정으로 컨테이너를 띄울지 지정.
    //   -> 그래서 테스트 실행 시 스프링이 DaoFactory로 컨텍스트를 만들고,
    //      @Autowired가 붙은 필드에 맞는 타입의 빈(UserDAO)을 자동으로 꽂아준다(필드 주입).
    //   (애너테이션이 없으면 컨테이너가 없어 userDao가 null -> NullPointerException 발생)
    @SpringJUnitConfig(DaoFactory.class)
    public class UserDAOTest {
    
        // @Autowired : 타입(UserDAO)이 일치하는 빈을 스프링이 찾아 이 필드에 주입해준다.
        @Autowired // 더 이상 직접 new AnnotationConfigApplicationContext / getBean 을 호출할 필요가 없다.
        private UserDAO userDAO;
    
        // @BeforeEach : 각 @Test 메서드가 실행되기 '직전'마다 매번 호출된다(공통 준비 작업).
        //  - deleteAll()로 '항상 동일한 출발 상태(0건)'에서 테스트가 시작되게 한다.
        //    -> 테스트끼리 데이터가 섞이지 않고, 같은 테스트를 몇 번 돌려도 결과가 같다(반복 가능).
        //       (이전에는 고정 id 때문에 두 번 실행하면 중복키로 실패했는데, 그 문제가 사라진다.)
        @BeforeEach
        void setUp() throws SQLException, ClassNotFoundException {
            userDAO.deleteAll();
        }
    		
    		// 테스트용 User 객체 생
        private User newUser(String id, String name, String password) {
            User user = new User();
            user.setId(id);
            user.setName(name);
            user.setPassword(password);
    
            return user;
        }
    
        // @Test : add() 자체가 정상 동작하는지에 집중한 테스트.
        @Test
        void add_메서드_테스트() throws SQLException, ClassNotFoundException {
            // given
            User user = newUser("test123", "test234", "321");
    
            // when
            userDAO.add(user);
    
            // then
            assertEquals("test234", userDAO.get("test123").getName());
        }
    
        @Test
        void get_메서드_테스트() throws SQLException, ClassNotFoundException {
            // given
            User user = newUser("test123", "test234", "321");
    
            // when
            userDAO.add(user);
    
            // then
            assertEquals("test234", userDAO.get("test123").getName());
        }
    
        @Test
        void add_중복_id_예외() throws SQLException, ClassNotFoundException {
    
            final User user = newUser("dup_id", "사용자1", "3210");
    
            // 정상동작
            userDAO.add(user);
    
            // 익명클래스
            /*
            Executable action = new Executable() {
                @Override
                public void execute() throws Throwable {
                    // 예외
                    userDAO.add(user);
                }
            };
    
            assertThrows(SQLException.class, action);
            */
    
            assertThrows(SQLException.class, () -> userDAO.add(user));
        }
    
        @Test
        void get_없는_id_예외() {
            assertThrows(SQLException.class, () -> userDAO.get("존재하지_않는_id"));
        }
    
        @Disabled("일부러 틀린 기댓값을 넣은 학습용 실패 예제 - 실패 메시지를 보고 싶을 때만 활성화")
        @Test
        void 일부러_실패하는_테스트() throws SQLException, ClassNotFoundException {
            userDAO.add( newUser("fail_demo", "fail", "1234") );
    
            assertEquals(2, userDAO.getCount());
        }
    
    }
    ```
    
- 테스트 할 원본 코드
    
    src/main/java/com/example/spring/springtheory/ch02/ex_2_1/dao/UserDAO
    
    ```java
    package com.example.spring.springtheory.ch02.ex_2_1.dao;
    
    import com.example.spring.springtheory.ch02.ex_2_1.domain.User;
    
    import java.sql.Connection;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.SQLException;
    
    public class UserDAO {
    
        private SimpleConnectionMaker simpleConnectionMaker;
    
        public UserDAO(SimpleConnectionMaker simpleConnectionMaker) {
            this.simpleConnectionMaker = simpleConnectionMaker;
        }
    
        public void add(User user) throws ClassNotFoundException, SQLException {
    
            String query = "INSERT INTO users (id, name, password) VALUES (?, ?, ?)";
    
            try (
                    Connection conn = simpleConnectionMaker.makeNewConnection();
                    PreparedStatement pstmt = conn.prepareStatement(query);
            ) {
                pstmt.setString(1, user.getId());
                pstmt.setString(2, user.getName());
                pstmt.setString(3, user.getPassword());
                pstmt.executeUpdate();
            }
    
        }
    
        public User get(String id) throws ClassNotFoundException, SQLException {
            String query = "SELECT * FROM users WHERE id = ?";
    
            try (
                    Connection conn = simpleConnectionMaker.makeNewConnection();
                    PreparedStatement pstmt = conn.prepareStatement(query);
            ) {
                pstmt.setString(1, id);
                ResultSet resultSet = pstmt.executeQuery();
    
                resultSet.next();
    
                User user = new User();
                user.setId( resultSet.getString("id") );
                user.setName( resultSet.getString("name") );
                user.setPassword( resultSet.getString("password") );
    
                return user;
            }
    
        }
    
        // 테스트 시작 전에 호출해 DB를 깨끗한 상태로 만드는 용도
        public void deleteAll() throws SQLException, ClassNotFoundException {
    
            String query = "DELETE FROM users";
    
            try (
                    Connection conn = simpleConnectionMaker.makeNewConnection();
                    PreparedStatement pstmt = conn.prepareStatement(query);
            ) {
                pstmt.executeUpdate();
            }
        }
    
        public int getCount() throws ClassNotFoundException, SQLException {
            String query = "SELECT COUNT(*) FROM users";
    
            try (
                    Connection conn = simpleConnectionMaker.makeNewConnection();
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    ResultSet resultSet = pstmt.executeQuery();
            ) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    
    }
    ```
    

---

## 템플릿

- 변하지 않는 흐름은 한 곳에 고정해두고, 변하는 부분만 따로 분리해서 바꿔 끼울 수 있게 만드는 것이 템플릿의 핵심
- 이 방식은 **OCP, 개방 폐쇄 원칙**과 연결
    
    <aside>
    
    변하지 않는 템플릿 코드는 수정하지 않음 → 변경에는 닫힘
    변하는 부분은 새로 구현하거나 교체 가능 → 확장에는 열림
    
    </aside>
    

## 4. 템플릿 메서드 패턴

- 템플릿 구조를 **상속**으로 구현하는 디자인 패턴
- 중복 제거, OCP 구현이 가능함
- 구현 방법
    
    <aside>
    
    변하지 않는 전체 흐름은 부모(추상) 클래스에 둔다.
    변하는 부분은 추상 메서드로 만들어둔다.
    자식 클래스가 그 추상 메서드를 오버라이드해서 구체적인 동작을 정한다.
    
    </aside>
    
- 예시 : JDBC 작업
    
    <aside>
    
    <변하지 않는 흐름>
    Connection 가져오기
    PreparedStatement 만들기
    SQL 실행하기
    리소스 정리하기
    
    <변하는 부분>
    어떤 SQL을 실행할지
    어떤 파라미터를 넣을지
    어떤 PreparedStatement를 만들지
    
    </aside>
    

### 템플릿 메서드 패턴의 한계

템플릿 메서드 패턴은 변하지 않는 흐름과 변하는 부분을 분리할 수 있다는 장점이 있지만, **상속**을 사용하기 때문에 한계가 있다.

- 가장 큰 문제는 **변하는 로직마다 새로운 서브 클래스를 만들어야 한다**는 점이다.예를 들어 DAO 로직마다 다른 PreparedStatement를 사용해야 한다면, 그 차이를 표현하기 위해 계속 새로운 하위 클래스를 만들어야 한다.
- 또한 변하지 않는 JDBC 처리 흐름을 가진 `UserDAO`와, 변하는 `PreparedStatement` 생성 로직을 가진 서브 클래스들은 **클래스 레벨에서 컴파일 시점에 관계가 고정**된다.
- 따라서 실행 중에 관계를 유연하게 바꾸기 어렵고, 구조가 점점 복잡해질 수 있다.

+상속의 한계 

1. 자바는 단일 상속만 가능
2. 슈퍼클래스와 서브클래스가 강하게 결합
슈퍼클래스의 흐름이나 메서드 구조가 바뀌면 서브클래스들도 영향
3. 변하는 부분이 많아질수록 서브클래스가 계속 늘어날 수 있다.
작은 차이 하나 때문에 새로운 클래스를 만들어야 할 수도 있다.

결국 템플릿 메서드 패턴은 공통 흐름을 재사용하는 데는 유용하지만, **상속 기반 확장 방식이기 때문에 유연성이 떨어지고 클래스가 많아지는 한계**가 있다.

→ 전략 패턴이나 DI 방식으로 더 개선!

+템플릿 메서드는 UserDAO가 추상 클래스이므로 DaoFactory에서 @Bean 지정 불가능?

### 템플릿 메서드 적용 예제 (ch03/ex_3_1)

- 기존 UserDAO.java (ch02/ex_2_1)
    
    ```java
    package com.example.spring.springtheory.ch02.ex_2_1.dao;
    
    import com.example.spring.springtheory.ch02.ex_2_1.domain.User;
    
    import java.sql.Connection;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.SQLException;
    
    public class UserDAO {
    
        private SimpleConnectionMaker simpleConnectionMaker;
    
        public UserDAO(SimpleConnectionMaker simpleConnectionMaker) {
            this.simpleConnectionMaker = simpleConnectionMaker;
        }
    
        public void add(User user) throws ClassNotFoundException, SQLException {
    
            String query = "INSERT INTO users (id, name, password) VALUES (?, ?, ?)";
    
            try (
                    Connection conn = simpleConnectionMaker.makeNewConnection();
                    PreparedStatement pstmt = conn.prepareStatement(query);
            ) {
                pstmt.setString(1, user.getId());
                pstmt.setString(2, user.getName());
                pstmt.setString(3, user.getPassword());
                pstmt.executeUpdate();
            }
    
        }
    
        public User get(String id) throws ClassNotFoundException, SQLException {
            String query = "SELECT * FROM users WHERE id = ?";
    
            try (
                    Connection conn = simpleConnectionMaker.makeNewConnection();
                    PreparedStatement pstmt = conn.prepareStatement(query);
            ) {
                pstmt.setString(1, id);
                ResultSet resultSet = pstmt.executeQuery();
    
                resultSet.next();
    
                User user = new User();
                user.setId( resultSet.getString("id") );
                user.setName( resultSet.getString("name") );
                user.setPassword( resultSet.getString("password") );
    
                return user;
            }
    
        }
    
        // 테스트 시작 전에 호출해 DB를 깨끗한 상태로 만드는 용도
        public void deleteAll() throws SQLException, ClassNotFoundException {
    
            String query = "DELETE FROM users";
    
            try (
                    Connection conn = simpleConnectionMaker.makeNewConnection();
                    PreparedStatement pstmt = conn.prepareStatement(query);
            ) {
                pstmt.executeUpdate();
            }
        }
    
        public int getCount() throws ClassNotFoundException, SQLException {
            String query = "SELECT COUNT(*) FROM users";
    
            try (
                    Connection conn = simpleConnectionMaker.makeNewConnection();
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    ResultSet resultSet = pstmt.executeQuery();
            ) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    
    }
    ```
    
- 템플릿 메서드 패턴 적용한 UserDAO.java
    
    → 메서드 추출 불완전, makeStatement() 메서드만 분리됨
    
    ```java
    package com.example.spring.springtheory.ch03.ex_3_1.dao;
    
    import com.example.spring.springtheory.ch03.ex_3_1.domain.User;
    
    import java.sql.Connection;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.SQLException;
    
    // * 템플릿 메서드 패턴의 적용
    // 상속을 통해 기능을 확장해서 사용하는 부분이다.
    // 변하지 않는 부분은 슈퍼클래스에 두고 변하는 부분은 추상 메서드로 정의해둬서
    // 서브클래스에서 오버라이드하여 새롭게 정의해 쓰도록 하는 것이다.
    
    public abstract class UserDAO {
    
        private SimpleConnectionMaker simpleConnectionMaker;
    
        public UserDAO(SimpleConnectionMaker simpleConnectionMaker) {
            this.simpleConnectionMaker = simpleConnectionMaker;
        }
    
        protected UserDAO() {}
    
        public void add(User user) throws ClassNotFoundException, SQLException {
    
            String query = "INSERT INTO users (id, name, password) VALUES (?, ?, ?)";
    
            try (
                    Connection conn = simpleConnectionMaker.makeNewConnection();
                    PreparedStatement pstmt = conn.prepareStatement(query);
            ) {
                pstmt.setString(1, user.getId());
                pstmt.setString(2, user.getName());
                pstmt.setString(3, user.getPassword());
                pstmt.executeUpdate();
            }
    
        }
    
        public User get(String id) throws ClassNotFoundException, SQLException {
            String query = "SELECT * FROM users WHERE id = ?";
    
            try (
                    Connection conn = simpleConnectionMaker.makeNewConnection();
                    PreparedStatement pstmt = conn.prepareStatement(query);
            ) {
                pstmt.setString(1, id);
                ResultSet resultSet = pstmt.executeQuery();
    
                resultSet.next();
    
                User user = new User();
                user.setId( resultSet.getString("id") );
                user.setName( resultSet.getString("name") );
                user.setPassword( resultSet.getString("password") );
    
                return user;
            }
    
        }
    
        // 테스트 시작 전에 호출해 DB를 깨끗한 상태로 만드는 용도
        public void deleteAll() throws SQLException, ClassNotFoundException {
            try (
                    Connection conn = simpleConnectionMaker.makeNewConnection();
                    PreparedStatement pstmt = makeStatement(conn); // 변하는 부분을 메서드로 추출
            ) {
                pstmt.executeUpdate();
            }
        }
    
        public int getCount() throws ClassNotFoundException, SQLException {
            String query = "SELECT COUNT(*) FROM users";
    
            try (
                    Connection conn = simpleConnectionMaker.makeNewConnection();
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    ResultSet resultSet = pstmt.executeQuery();
            ) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    
        protected abstract PreparedStatement makeStatement(Connection conn) throws ClassNotFoundException, SQLException;
    
    }
    ```
    
- 템플릿 메서드 패턴 적용한 UserDAODeleteAll.java
    
    ```java
    package com.example.spring.springtheory.ch03.ex_3_1.dao;
    
    import java.sql.Connection;
    import java.sql.PreparedStatement;
    import java.sql.SQLException;
    
    public class UserDAODeleteAll extends UserDAO {
    
        @Override
        protected PreparedStatement makeStatement(Connection conn) throws ClassNotFoundException, SQLException {
            return conn.prepareStatement("DELETE FROM users");
        }
    }
    ```
    

---

## 5. 전략 패턴 ⭐

**전략 패턴**은 개방 폐쇄의 원칙을 잘 지키는 구조이면서도 템플릿 메서드 패턴보다 유연하고 확장성이 뛰어나다.

전략 패턴은 변하지 않는 부분과 변하는 부분을 아예 **별도의 오브젝트로 분리**하고, 클래스 레벨에서는 **인터페이스를 통해서만 의존**하도록 만드는 방식이다.

### **컨텍스트 Context**

변하지 않는 공통 흐름 담당 
ex) JDBC 작업 (DB 커넥션 가져오기/PreparedStatement 실행/예외 처리/자원 반환)

### **전략 Strategy**

변하는 부분 담당 
ex) CRUD마다 다른 PreparedStatement → `StatementStrategy` 같은 인터페이스로 추상화

### 컨텍스트와 전략의 관계

- 컨텍스트는 구체적인 전략 클래스를 직접 알지 않는다.
- 컨텍스트는 인터페이스(StatementStrategy)에만 의존하고, 실제 전략은 런타임에 주입받아 사용한다.
- 그래서 새 기능을 추가해도 
컨텍스트 코드는 닫혀 있고 (수정 X) 전략만 새로 만들면 된다 (확장O) = OCP

### 전략 패턴의 장점

- 전략 패턴을 사용하면 새로운 기능이 필요할 때 컨텍스트 코드를 수정하지 않아도 된다.
- 새로운 SQL 실행 방식이 필요하면 새로운 전략 클래스를 만들면 된다.

### 전략 패턴 적용 예제 (ch03/ex_3_2)

<aside>

컨텍스트 : UserDAO의 jdbcContextWithStatementStrategy()
→ 변하지 않는 JDBC 공통 흐름 담당

전략 : UserDAOAdd, UserDAODeleteAll
→ 어떤 PreparedStatement를 만들지 담당

클라이언트 : UserDAO의 add(), deleteAll() 메서드
→ 어떤 전략을 쓸 지 결정하고 컨텍스트에 전달

</aside>

- 전략 패턴 적용한 UserDAO.java
    
    컨텍스트 : 변하지 않는 JDBC 작업의 공통 흐름
    
    - 커넥션을 얻고, 전달받은 '전략'에게 statement 생성을 맡기고, 실행하고, 자원을 정리한다.
    - 어떤 SQL을 실행할지는 전혀 모른다. 그건 strategy가 결정한다(인터페이스에만 의존).
    
    ```java
    package com.example.spring.springtheory.ch03.ex_3_2.dao;
    
    import com.example.spring.springtheory.ch03.ex_3_2.domain.User;
    
    import java.sql.Connection;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.SQLException;
    
    // * 전략 패턴의 적용
    // - 컨텍스트
    // 변하지 않는 부분 : JDBC 커넥션/실행/자원관리 공통 흐름
    // - 전략
    // 변하는 부분 : 어떤 PreparedStatement를 만들지 -> 인터페이스로 추상화
    
    // 컨텍스트는 '인터페이스(StatementStrategy)에만' 의존하고, 실제 전략은 런타임에 주입받는다.
    // 그래서 새 기능을 추가해도 컨텍스트 코드는 닫혀 있고(수정X) 전략만 새로 만들면 된다(확장O) = OCP.
    
    public class UserDAO {
    
        private SimpleConnectionMaker simpleConnectionMaker;
    
        public UserDAO(SimpleConnectionMaker simpleConnectionMaker) {
            this.simpleConnectionMaker = simpleConnectionMaker;
        }
    
        protected UserDAO() {}
    
        // 컨텍스트 : 변하지 않는 JDBC 작업의 공통 흐름
        //  - 커넥션을 얻고, 전달받은 '전략'에게 statement 생성을 맡기고, 실행하고, 자원을 정리한다.
        //  - 어떤 SQL을 실행할지는 전혀 모른다. 그건 strategy가 결정한다(인터페이스에만 의존).
        public void jdbcContextWithStatementStrategy(StatementStrategy statementStrategy) throws SQLException, ClassNotFoundException {
            try (
                    Connection conn = simpleConnectionMaker.makeNewConnection();
                    PreparedStatement pstmt = statementStrategy.makeStatement(conn); // 변하는 부분을 전략에 위임
            ) {
                pstmt.executeUpdate();
            }
        }
    
        public void add(User user) throws ClassNotFoundException, SQLException {
            jdbcContextWithStatementStrategy( new UserDAOAdd(user) );
        }
    
        public void deleteAll() throws SQLException, ClassNotFoundException {
            jdbcContextWithStatementStrategy( new UserDAODeleteAll() );
        }
    
    }
    ```
    
- 전략 패턴 적용한 UserDAOAdd.java
    - add 전략(StatementStrategy 구현체) : 변하는 부분인 PreparedStatement 생성 로직만 담은 전략 클래스다. deleteAll과 달리 add는 저장할 User 데이터가 필요하므로, 생성자로 User를 받아 전략 안에서 파라미터까지 채워 완성된 statement를 돌려준다.
    
    ```java
    package com.example.spring.springtheory.ch03.ex_3_2.dao;
    
    import com.example.spring.springtheory.ch03.ex_3_2.domain.User;
    
    import java.sql.Connection;
    import java.sql.PreparedStatement;
    import java.sql.SQLException;
    
    public class UserDAOAdd implements StatementStrategy {
    
        private final User user;
    
        public UserDAOAdd(User user) {
            this.user = user;
        }
    
        @Override
        public PreparedStatement makeStatement(Connection conn) throws SQLException {
            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO users(id, name, password) VALUES(?, ?, ?)"
            );
    
            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getPassword());
    
            return pstmt;
        }
    }
    ```
    
- 전략 패턴 적용한 UserDAODeleteAll.java
    
    ```java
    package com.example.spring.springtheory.ch03.ex_3_2.dao;
    
    import java.sql.Connection;
    import java.sql.PreparedStatement;
    import java.sql.SQLException;
    
    public class UserDAODeleteAll implements StatementStrategy {
    
        @Override
        public PreparedStatement makeStatement(Connection conn) throws SQLException {
            return conn.prepareStatement("DELETE FROM users");
        }
    }
    ```
    
- 전략 패턴 적용한 StatementStrategy.java
    
    ```java
    package com.example.spring.springtheory.ch03.ex_3_2.dao;
    
    import java.sql.Connection;
    import java.sql.PreparedStatement;
    import java.sql.SQLException;
    
    public interface StatementStrategy {
        PreparedStatement makeStatement(Connection conn) throws SQLException;
    }
    ```
    

⭐ 템플릿 메서드 패턴 : 상속을 사용 vs 전략 패턴 : 인터페이스와 위임을 사용

<aside>

템플릿 메서드 패턴 = 변하는 부분을 상속으로 분리

전략 패턴 = 변하는 부분을 별도 객체로 분리해서 런타임에 주입

컨텍스트 = 변하지 않는 흐름

전략 = 바뀌는 로직

</aside>

---

2026.06.26 수업 - ch03/ex_3_3

## 6. 전략 패턴 + 리팩토링 ⭐

#### 기존 전략 패턴 적용 문제점 (ch03/ex_3_2)

메서드마다 새로운 구현 클래스 파일을 만들어야 한다. 
→ UserDAOAdd, UserDAODeleteAll

#### 전략 패턴 적용 + 리팩토링 예제 (ch03/ex_3_3)

별도의 UserDAOAdd, UserDAODelete 구현 클래스 파일을 없애고
UserDAO 내부의 add(), deleteAll() 메서드의 내부로 이동

- 기존 버전 - UserDAO의 add(), deleteAll() 내부가 아닌 따로 클래스 생성
    
    ```java
    	public void add(User user) throws ClassNotFoundException, SQLException {
            jdbcContextWithStatementStrategy( new UserDAOAdd(user) );
        }
    
        public void deleteAll() throws SQLException, ClassNotFoundException {
            jdbcContextWithStatementStrategy( new UserDAODeleteAll() );
        }
    ```
    
- 로컬 클래스 버전 - UserDAO의 add(), deleteAll() 수정
    
    ```java
    	public void add(User user) throws ClassNotFoundException, SQLException {
    
            class UserDAOAdd implements StatementStrategy {
    
                @Override
                public PreparedStatement makeStatement(Connection conn) throws SQLException {
                    PreparedStatement pstmt = conn.prepareStatement(
                            "INSERT INTO users(id, name, password) VALUES(?, ?, ?)"
                    );
    
                    pstmt.setString(1, user.getId());
                    pstmt.setString(2, user.getName());
                    pstmt.setString(3, user.getPassword());
    
                    return pstmt;
                }
            }
    
            jdbcContextWithStatementStrategy( new UserDAOAdd() );
        }
    
        public void deleteAll() throws SQLException, ClassNotFoundException {
    
            class UserDAODeleteAll implements StatementStrategy {
    
                @Override
                public PreparedStatement makeStatement(Connection conn) throws SQLException {
                    return conn.prepareStatement("DELETE FROM users");
                }
            }
    
            jdbcContextWithStatementStrategy( new UserDAODeleteAll() );
        }
    ```
    
- 익명 클래스 버전 - UserDAO의 add(), deleteAll() 수정
    
    ```java
    	public void add(User user) throws ClassNotFoundException, SQLException {
    
            StatementStrategy strategy = new StatementStrategy() {
                @Override
                public PreparedStatement makeStatement(Connection conn) throws SQLException {
                    PreparedStatement pstmt = conn.prepareStatement(
                            "INSERT INTO users(id, name, password) VALUES(?, ?, ?)"
                    );
    
                    pstmt.setString(1, user.getId());
                    pstmt.setString(2, user.getName());
                    pstmt.setString(3, user.getPassword());
    
                    return pstmt;
                }
            };
    
            jdbcContextWithStatementStrategy( strategy );
        }
    
        public void deleteAll() throws SQLException, ClassNotFoundException {
    
            StatementStrategy strategy = new StatementStrategy() {
                @Override
                public PreparedStatement makeStatement(Connection conn) throws SQLException {
                    return conn.prepareStatement("DELETE FROM users");
                }
            };
    
            jdbcContextWithStatementStrategy( strategy );
        }
    ```
    ---

2026.06.29 수업 - ch03/ex_3_4

## 7. 컨텍스트 클래스 분리 및 재사용

ex_3_3 문제점

UserDAO의 컨텍스트 메서드인 jdbcContextWithStatementStrategy()는 PreparedStatement를 실행하는 기능을 가진 메서드에서 공유할 수 있다. 즉, 다른 DAO에서도 사용이 가능하다. → '클래스 분리'

(JdbcContext 분리 ← ConnectionMaker와 Strategy 필요)

- UserDAO에서 JdbcContext는 고정된 공통 기능이므로 구체 클래스에 의존
- SimpleConnectionMaker는 DConnectionMaker, NConnectionMaker 등 구현체를 바꿔 끼울 가능성이 큰 부분이므로 인터페이스로 느슨하게 의존
- StatementStrategy는 함수형 인터페이스로 선언 해둔 후 UserDAO의 클라이언트(add, deleteAll)에서 원하는 대로 오버라이딩하여 사용 (쿼리문 작성, 값 세팅한 전략 인스턴스를 JdbcContext에 인자로 넘김 → JdbcContext는 DB연결 후 해당 쿼리문을 바탕으로 PreparedStatement 만들어 실행)

```java
UserDAO → JdbcContext
: JDBC 공통 흐름을 맡기는 관계. 필수적이고 비교적 고정된 협력 관계라 구체 클래스에 의존.

JdbcContext → StatementStrategy
: SQL마다 바뀌는 PreparedStatement 생성 로직을 실행 시점에 전달받는 관계라 인터페이스에 의존.

SimpleConnectionMaker
: DB 연결 방식이 바뀔 수 있으므로 인터페이스로 분리하는 것이 적절.
```