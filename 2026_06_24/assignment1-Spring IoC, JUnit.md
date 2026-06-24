## 의존 관계 주입 (DI, Dependency Injection)

- 스프링 IoC 기능의 대표적인 동작 원리는 주로 의존 관계 주입이다.
- 의존한다는 것은 의존 대상이 변하면 의존하는 오브젝트에 영향을 미친다.
- 의존 관계 주입은 구체적인 의존 오브젝트와 그것을 사용할 주제, 보통 클라이언트라고 부르는 오브젝트를 런타임 시에 연결해주는 작업을 말한다.
- 의존 관계 주입의 핵심은 설계 시점에서는 알지 못했던 두 오브젝트와 관계를 맺도록 도와주는 제3의 존재가 있다는 것이다.
- ConnectionMaker 인터페이스를 구현한 클래스인 DConnectionMaker 등이 다른 것으로 바뀌거나 그 내부에서 사용하는 메서드에 변화가 생겨도 UserDAO에 영향을 주지 않는다.
- 이렇게 인터페이스에 대해서만 의존관계를 만들어두면 인터페이스 구현 클래스와의 관계는 느슨해지면서 변화에 영향을 덜 받는 상태가 된다. 결합도가 낮다고 할 수 있다.
- 모델이나 코드에서 클래스와 인터페이스를 통해 드러난 의존관계 말고, 런타임 시에 오브젝트 사이에서 만들어진 의존관계도 있다.
- 런타임 의존관계 또는 오브젝트 의존관계인데, 설계 시점의 의존관계가 실체화된 것이라고 볼 수 있다.

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
    
    콘솔에 나온 값을 보고 등록과 조회가 성공적으로 되고 있는 지를 확인하는 건 사람의 책임이다.
    
2. 실행 작업의 번거로움
    
    만약 DAO가 수백 개가 되고 그에 대한 main() 메서드도 그만큼 만들어진다면, 전체 기능을 테스트해보기 위해 main() 메서드를 수백 번 실행하는 수고가 필요하다.
    

#### 단위 테스트

- 테스트는 가능하면 작은 단위로 쪼개서 집중해서 할 수 있어야 한다.
- 관심사의 분리가 여기에도 적용된다.
- 단위 테스트를 하는 이유는 개발자가 설계하고 만든 코드가 원래 의도한 대로 동작하는 지를 개발자 스스로 빨리 확인받기 위해서다.

#### 자동 수행 테스트 코드

- 테스트는 자동으로 수행되도록 코드로 만들어지는 것이 중요하다.
- 애플리케이션을 구성하는 클래스 안에 테스트 코드를 포함시키는 것보다는 별도로 테스트용 클래스를 만들어서 테스트 코드를 넣는 편이 낫다.
- 자동으로 수행되는 테스트의 장점은 자주 반복될 수 있다는 것이다.
- (SpringTheoryApplication.java에 영향권 내에 테스트 코드 X ??)

#### 테스트의 결과

- 모든 테스트는 성공과 실패의 두 가지 결과를 가질 수 있다.
- 또 테스트의 실패는 테스트가 진행되는 동안에 에러가 발생해서 실패한 경우와, 테스트 작업 중에 에러가 발생하진 않았지만 그 결과가 기대한 것과 다르게 나오는 경우로 구분해볼 수 있다.

# JUnit

자바에서 단위 테스트를 자동으로 작성/실행하게 해주는 표준 테스트 프레임워크

위 Start.java처럼 main()으로 직접 실행하고 콘솔을 눈으로 확인하던 방식을 대체한다.

#### Junit - 프레임워크의 특징 : 제어의 역전 (IoC)

main()은 “내가 직접” 테스트 흐름을 호출하지만, JUnit에서는 우리가 테스트 메서드만 작성해두면 “프레임워크가 알아서” 그 메서드를 찾아 실행해준다.
(개발자가 흐름을 제어하는 게 아니라, 프레임워크가 개발자의 코드를 불러서 제어한다 → IoC)

#### Junit - 기본 사용법 (JUnit 5 기준)

- `@Test` : 이 메서드가 테스트라는 표시, JUnit이 이 메서드들을 자동으로 찾아 실행
- `@BeforeEach` : 각 @Test 실행 '전'마다 매번 실행(공통 준비 작업, 예: 컨텍스트/객체 셋업).
- `@AfterEach` : 각 @Test 실행 '후'마다 매번 실행(뒷정리).
- `assertEquals(기대값, 실제값)` 등 단언(assert) 메서드로 결과를 '코드로' 검증한다.
→ 사람이 콘솔을 보고 판단할 필요가 없어진다(수동 확인의 번거로움 해결).    
→ 기대값과 다르면 테스트 실패로 자동 표시되고, 에러가 나도 실패로 잡힌다 (테스트의 두 가지 결과)

위 코드를 JUnit 테스트로 바꾸면 대략 이런 모습이 된다

```java
@Test
void getUser() throws Exception {
    var context = new AnnotationConfigApplicationContext(DaoFactory.class);
		UserDAO userDao = context.getBean("userDao", UserDAO.class);
		User user = userDao.get("test1");
		assertEquals("기대하는이름", user.getName());  // System.out.println 대신 단언으로 검증
}
```

테스트코드 예제

src/test/java/com/example/spring/springtheory/ch02/ex_2_1/dao/UserDAOTest

```java

```

src/main/java/com/example/spring/springtheory/ch02/ex_2_1/dao/UserDAO

```java

```

#### 템플릿

- 개방 폐쇄의 원칙은 변화의 특성이 다른 부분을 구분해주고, 각각 다른 목적과 다른 이유에 의해 다른 시점에 독립적으로 변경될 수 있는 효율적인 구조를 만들어준다.
- 이렇게 바뀌는 성질이 다른 코드 중에서 변경이 거의 일어나지 않으며, 일정한 패턴으로 유지되는 특성을 가진 부분을 자유롭게 변경되는 성질을 가진 부분으로부터 독립시켜서 효과적으로 활용할 수 있도록 하는 방법

#### 템플릿 메서드 패턴의 적용, 상속의 한계 - ex_3_1

- 상속을 통해 기능을 확장해서 사용하는 부분
- 변하지 않는 부분은 슈퍼 클래스에 두고 변하는 부분은 추상 메서드로 정의해둬서 서브 클래스에서 오버라이드하여 새롭게 정의해 쓰도록 하는 방법

기존 UserDAO.java

```java

```

템플릿 적용한 ch03/ex_3_1 → 메서드 추출 불완전..

UserDAO.java 

```java

```

UserDAODeleteAll.java

```java

```

#### 문제점

- 하지만 템플릿 메서드 패턴으로의 접근은 제한이 많다.
- 가장 큰 문제는 DAO 로직마다 상속을 통해 새로운 클래스를 만ㄷ르어야 한다는 점이다.
- 변하지 않는 코드를 가진 UserDAO의 JDBC try블록과 변하는 PreparedStatement를 담고 있는 서브 클래스들이 이미 클래스 레벨에서 컴파일 시점에 이미 그 관계가 결정되어 있다.
- 따라서 관계에 대한 유연성이 떨어져 버린다.
- 상속을 통해 확장을 꾀하는 템플릿 메서드 패턴의 단점이 고스란히 드러난다.

#### 전략 패턴의 적용, 문제 해결 - ex_3_2

개방 폐쇄의 원칙을 잘 지키는 구조이면서도 템플릿 메서드 패턴보다 유연하고 확장성이 뛰어난 것이, 오브젝트를 아예 둘로 분리하고 클래스 레벨에서는 인터페이스를 통해서만 의존하도록 만드는 전략 패턴이다.

- 컨텍스트
변하지 않는 부분 : JDBC 커넥션/실행/자원관리 공통 흐름
- 전략
변하는 부분 : 어떤 PreparedStatement를 만들지 → 인터페이스로 추상화
- 컨텍스트는 인터페이스(StatementStrategy)에만 의존하고, 실제 전략은 런타임에 주입받는다.
- 그래서 새 기능을 추가해도 컨텍스트 코드는 닫혀 있고 (수정 X) 전략만 새로 만들면 된다(확장O) = OCP

StatementStrategy.java

UserDAODeleteAll.java

UserDAOAdd.java

- add 전략(StatementStrategy 구현체) : 변하는 부분인 PreparedStatement 생성 로직만 담은 전략 클래스다. deleteAll과 달리 add는 저장할 User 데이터가 필요하므로, 생성자로 User 데이터가 필요하므로, 생성자로 User를 받아 전략 안에서 파라미터까지 채워 완성된 statement를 돌려준다.

UserDAO.java

컨텍스트 : 변하지 않는 JDBC 작업의 공통 흐름

- 커넥션을 얻고, 전달받은 '전략'에게 statement 생성을 맡기고, 실행하고, 자원을 정리한다.
- 어떤 SQL을 실행할지는 전혀 모른다. 그건 strategy가 결정한다(인터페이스에만 의존).