#### Spring 이론 복습 - 1 에서의 문제점

테스트용 클라이언트 코드가 `UserDAO`를 생성하면서 어떤 `ConnectionMaker`를 사용할지까지 직접 결정했다.

하지만 이는 클라이언트가 단순히 기능을 테스트하거나 사용하는 역할을 넘어서, **객체 생성과 관계 설정이라는 책임**까지 떠맡는 구조였다.

## 오브젝트 팩토리

객체의 생성 방법과 객체 간 관계 설정을 담당하고, 그렇게 만들어진 객체를 반환해주는 역할을 하는 객체 - `DaoFactory`

#### Spring 이론 복습 - 2

Start.java (클라이언트 코드)에서는 오직 테스트만,
DaoFactory.java(오브젝트 팩토리)에서 오브젝트를 결정하는 기능을 맡게 됨 

## 제어의 역전 (Inversion of Control)

객체가 자신이 사용할 객체를 직접 생성하거나 결정하지 않고, 객체의 생성, 관계 설정, 사용 흐름에 대한 제어권을 외부 객체나 컨테이너에게 넘기는 것 
즉, 객체 생성과 의존관계 설정의 책임이 객체 자신이 아니라 외부로 이동하는 것

예제) `UserDAO` 에서 사라진 두 가지 제어권

1. 어떤 `ConnectionMaker`를 사용할지 결정하는 권한

```groovy
public class UserDAO {
	
	public UserDAO(SimpleConnectionMaker simpleConnectionMaker) {
      this.simpleConnectionMaker = simpleConnectionMaker;
  }
...  
```

1. 자기 자신의 생성과 의존 관계 설정에 대한 제어권 

`UserDAO`가 언제 생성되는지, 어떤 `ConnectionMaker` 구현체와 함께 생성되는지는 `DaoFactory`가 결정한다.

```groovy
public class DaoFactory {
	
	public UserDAO userDao() {
			// UserDAO 생성 + 관계설정을 팩토리가 담당
      return new UserDAO(connectionMaker());
  }

  private SimpleConnectionMaker connectionMaker() {
      return new DConnectionMaker();
  }
...  
```

즉, “어떤 구현체를 사용할지 결정하는 일”과 “객체를 생성하고 연결하는 일”의 제어 권한이 `UserDAO`에서 `DaoFactory`로 넘어갔다. 이것이 제어의 역전이다.

기존에는 테스트용 클라이언트 코드가 `UserDAO`를 생성하면서 어떤 `ConnectionMaker`를 사용할지까지 직접 결정했다. 하지만 이는 클라이언트가 단순히 기능을 테스트하거나 사용하는 역할을 넘어서, **객체 생성과 관계 설정이라는 책임**까지 떠맡는 구조였다.

이를 해결하기 위해 객체를 생성하고, 어떤 객체와 어떤 객체가 관계를 맺을지 결정하는 역할을 담당하는 `DaoFactory` 팩토리를 도입한다.

팩토리를 도입하면 다음과 같이 책임이 분리된다.

```
UserDAO
→ 사용자 정보를 저장하고 조회하는 기능 담당

DConnectionMaker
→ DB 연결 생성 기능 담당

DaoFactory
→ 객체 생성과 의존관계 설정 담당
```

“컴포넌트 역할을 하는 오브젝트”와 “애플리케이션 구조를 결정하는 오브젝트"를 분리한 것이다.

#### 템플릿 메서드와 IoC

템플릿 메서드는 제어의 역전이라는 개념을 활용해 문제를 해결하는 디자인 패턴이라고 볼 수 있다.

일반적으로는 하위 클래스가 상위 클래스의 기능을 호출하지만, 템플릿 메서드 패턴에서는 상위 클래스가 전체 실행 흐름을 정해두고, 필요한 일부 동작만 하위 클래스가 오버라이딩하도록 한다.

즉, 하위 클래스가 흐름을 직접 제어하는 것이 아니라, 상위 클래스가 실행 흐름을 제어하고 하위 클래스의 메서드를 호출한다. 이 점에서 제어의 역전이 일어난다.

---

## Spring의 제어의 역전 (IoC, Inversion of Control)

스프링은 **제어의 역전 IoC**을 핵심 기반 기술로 사용하는 프레임워크이다.

기존에는 개발자가 직접 객체를 생성하고 관계를 설정했다.
하지만 스프링에서는 개발자가 직접 객체를 만들고 연결하지 않고, **스프링 컨테이너**가 객체를 생성하고 의존관계를 설정한다.
즉, 객체 생성과 관계 설정의 제어권이 **개발자 코드에서 스프링 컨테이너**로 넘어간다.

### 빈 (Bean)

**스프링 IoC 컨테이너가 생성하고 관리하는 객체 (컴포넌트)**

일반 객체는 개발자가 직접 new로 생성하고 관리, 스프링 빈은 스프링 컨테이너가!

#### Java 빈 vs Spring 빈

- Java 빈 : 특정한 정보(private 필드, getter, setter…) 등을 가지고 있는 클래스를 표현하는 하나의 규칙인 Java 빈 규약을 따르는 객체
- Spring 빈 : **개발자가 관리하는 객체가 아닌 컨테이너에게 생성과 관계 설정, 사용 등의 제어권을 넘긴 객체**. 즉, 제어의 역전이 적용된 오브젝트를 가리키는 말이다.

#### 빈 팩토리 / 애플리케이션 컨텍스트 (Spring IoC 컨테이너)

스프링에서는 **빈의 생성, 관계 설정, 관리를 담당하는 Ioc 컨테이너의 기본 형태**를 ‘빈 팩토리’라고 한다.

실제로 스프링에서는 `BeanFactory`를 직접 쓰기보다는, 이것을 더 확장한 **ApplicationContext**를 주로 사용한다.

```groovy
// DaoFactory 설정 정보를 기반으로 스프링 컨테이너를 만들고
ApplicationContext context =
        new AnnotationConfigApplicationContext(DaoFactory.class);
// 그 안에서 userDao라는 이름의 빈을 꺼내온다.
UserDAO userDAO = context.getBean("userDao", UserDAO.class);
```

#### 컴포넌트 (Component)

애플리케이션을 구성하는 **독립적인 부품 역할의 객체**

우리 예제에서는 실제 일을 하는 `UserDAO`, `DConnectionMaker` 같은 오브젝트가 컴포넌트에 해당한다.

컴포넌트의 핵심은 자기 역할에만 집중하고, 자신이 어떻게 생성되는지, 누구와 연결되는지 직접 신경 쓰지 않는 것이다.

예를 들어 `UserDAO` 는 사용자를 저장하고 조회하는 일만 알면 되고, 어떤 `ConnectionMaker` 구현체와 연결될지는 외부에서 결정한다.

따라서 `DConnectionMaker`를 `NConnectionMaker`로 바꿔도 `UserDAO` 코드는 수정하지 않아도 된다.

#### 컨테이너 (Container)

**컴포넌트들을 생성하고, 서로 연결하고, 생명주기까지 관리해주는 실행 환경**

컴포넌트가 **부품**이라면, 컨테이너는 그 부품들을 만들고 조립하는 **공장**이라고 볼 수 있다.

우리가 직접 만든 `DaoFactory`가 바로 컨테이너의 아주 단순한 형태이다.

```groovy
public class DaoFactory { 
		
		public UserDAO userDAO() { 
				return new UserDAO(connectionMaker()); 
		} 
		
		private SimpleConnectionMaker connectionMaker() { 
				return new DConnectionMaker(); 
		} 
}
```

- 컴포넌트를 생성하고 (new UserDAO …) - 객체 생성
- 어떤 ConnectionMaker와 연결할 지 관계를 맺어준다. - 관계 설정

하지만 `DaoFactory`는 개발자가 직접 작성한 코드이기 때문에, 컴포넌트가 많아질수록 직접 관리해야 할 내용도 많아진다.

#### Spring 컨테이너 - 빈 팩토리 / 애플리케이션 콘텍스트

스프링은 `DaoFactory`가 하던 역할을 **프레임워크 차원에서 대신 수행**한다.

- 빈 팩토리(Bean Factory) / 애플리케이션 컨텍스트 (Application Context)가 바로 그것이다.

이 컨테이너가 빈(컴포넌트)들의 생성과 관계 설정, 사용, 소멸까지 모두 제어해준다.

→ 즉, 객체 생성과 관계 설정의 제어권이 개발자 코드에서 스프링 컨테이너로 넘어간다. 이것이 바로 **제어의 역전 IoC**

#### Annotation `@`

컨테이너에게 어떤 클래스를 설정 정보로 사용할지, 어떤 객체를 빈으로 등록할지 알려주기 위해 어노테이션을 사용한다.

`@Configuration` : 이 클래스가 컨테이너(애플리케이션 컨텍스트)가 사용할 스프링 설정 정보 클래스라는 뜻
→ 해당 오브젝트 팩토리를 new로 직접 생성하지 않아도 됨

`@Bean` : 이 메서드가 반환하는 객체를 스프링 컨테이너가 관리하는 스프링 빈으로 등록하겠다는 뜻

```groovy
...
// DaoFactory를 스프링 빈 팩토리가 사용할 수 있는 설정정보로 리팩토링
@Configuration // 애플리케이션 컨텍스트 또는 빈 팩토리가 사용할 설정 정보라는 표시
public class DaoFactory {

    @Bean // 오브젝트 생성을 담당하는 IoC용 메서드라는 표시
    public UserDAO userDAO() {
        return new UserDAO(connectionMaker());
    }

    @Bean
    public SimpleConnectionMaker connectionMaker() {
        return new DConnectionMaker();
    }

}
```

정리:

- 컴포넌트 → 기능을 수행하는 부품 객체
- 빈 → 스프링 컨테이너가 관리하는 컴포넌트 객체
- 컨테이너 → 빈을 생성하고 연결하고 관리하는 객체/환경
- BeanFactory → 스프링의 기본 컨테이너
- ApplicationContext → BeanFactory를 확장한 실질적인 스프링 컨테이너
- DaoFactory = 우리가 직접 만든 미니 컨테이너

#### 애플리케이션 컨텍스트 사용의 장점

`DaoFactory`를 오브젝트 팩토리로 직접 사용하는 방식과 비교했을 때, 스프링의 `ApplicationContext`를 사용하면 다음과 같은 장점이 있다.

1. 클라이언트가 구체적인 팩토리 클래스를 알 필요가 없다

`DaoFactory`를 직접 사용하는 경우,
클라이언트는 필요한 오브젝트를 가져오려면 어떤 팩토리 클래스를 사용해야 할지 알아야 하며 new로 해당 오브젝트를 직접 생성해야 한다.

`ApplicationContext`를 사용하는 경우,
클라이언트는 구체적인 팩토리 클래스 내용을 직접 알 필요가 없다.
오브젝트 팩토리가 여러 개로 늘어나더라도, 이를 알아야 하거나 직접 사용할 필요가 없으며 클라이언트는 애플리케이션 컨텍스트를 통해 필요한 빈을 가져오면 된다.

1. 종합 IoC 서비스를 제공해준다. 
    
    `DaoFactory`는 단순히 객체를 생성하고 관계를 설정하는 역할만 한다.
    
    반면 `ApplicationContext`는 객체 생성과 의존관계 설정뿐만 아니라, 스프링 컨테이너가 제공하는 다양한 부가 기능을 함께 제공한다.
    (ex. 빈 생성 방식과 시점 제어, 싱글톤 관리, 의존관계 자동 주입, 빈 후처리, 설정 정보 조합, 다양한 설정 방식 지원, 인터셉터 적용, 외부 시스템 연동, 기반 기술 서비스 제공)
    
2. 빈을 검색하는 다양한 방법을 제공한다
    
    `ApplicationContext`는 등록된 빈을 다양한 방식으로 조회할 수 있다.
    
    ```groovy
    // 빈 이름으로 조회
    UserDAO userDAO = context.getBean("userDAO", UserDAO.class);
    // 빈 타입으로 조회
    UserDAO userDAO = context.getBean(UserDAO.class);
    ```
    

→ 애플리케이션 컨텍스트를 사용하면 클라이언트가 구체적인 팩토리 클래스를 알 필요가 없고, 스프링이 제공하는 다양한 IoC 서비스와 빈 검색 기능을 활용할 수 있다.

Start.java 코드 + 주석

```groovy
public class Start {
    static void main(String[] args) throws SQLException, ClassNotFoundException {
        // 1) 스프링 컨테이너(애플리케이션 컨텍스트)를 만든다.
        //    - AnnotationConfigApplicationContext : @Configuration 자바 클래스를 설정정보로 읽는 컨텍스트 구현체
        //    - 생성자에 넘긴 DaoFactory.class 가 바로 "어떤 빈을 어떻게 만들지" 알려주는 설정 정보다.
        //    - 이 줄이 실행되는 순간 컨테이너는 DaoFactory의 @Bean 메서드들을 호출해
        //      UserDAO, ConnectionMaker 오브젝트를 미리 만들어 관계까지 맺어 담아둔다.
        //      (즉, 객체 생성·연결의 제어권이 우리 코드가 아니라 컨테이너로 넘어갔다 = 제어의 역전)
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);

        **// 2) 컨테이너에서 필요한 빈을 꺼내 쓴다(우리가 직접 new 하지 않는다).**
        //    - getBean("userDao", UserDAO.class)
        //      · "userDao"     : 가져올 빈의 이름. 기본적으로 @Bean 메서드 이름(userDao)이 빈 이름이 된다.
        //      · UserDAO.class : 돌려받을 타입. 형변환 없이 바로 UserDAO로 받기 위해 타입을 함께 지정.
        //    - 이미 connectionMaker가 주입되어 완성된 UserDAO 오브젝트를 컨테이너가 돌려준다.
        UserDAO userDAO = context.getBean("userDAO", UserDAO.class);
        User user = userDAO.get("test1");
        System.out.println(user.getName());
    }
}
```

## 싱글톤 레지스트리와 오브젝트 스코프 (ex_1_6)

애플리케이션 컨텍스트는 싱글톤을 저장하고 관리하는 싱글톤 레지스트리이기도 하다.

#### 1. 스프링 빈은 기본적으로 싱글톤이다

- Spring은 기본적으로 별다른 설정을 하지 않으면 내부적으로 빈 오브젝트를 모두 싱글톤으로 만든다.
- 싱글톤이란 하나의 클래스에 대해 객체를 하나만 만들어두고, 여러 곳에서 그 하나의 객체를 공유해서 사용하는 방식을 말한다.

#### 2. 왜 싱글톤으로 만들까?

서버 환경에서 객체 생성 비용을 줄이고, 많은 요청을 효율적으로 처리하기 위해

- Spring은 엔터프라이즈 서버 환경을 위해 만들어진 기술이다.
- 서버 환경에서는 수많은 클라이언트 요청을 빠르게 처리해야 한다. 만약 요청이 들어올 때마다 매번 새로운 객체를 생성한다면 서버에 큰 부담이 생긴다.
- 예를 들어 요청 한 번을 처리할 때 5개의 객체가 필요하고, 초당 500개의 요청이 들어온다고 가정하면, 초당 2500개, 1분이면 15만 개, 한 시간이면 9백만 개의 새로운 오브젝트가 만들어진다.
- 아무리 자바의 객체 생성과 메모리, 가비지 컬렉션(GC)의 성능이 좋아졌다고 해도, 이렇게 많은 객체가 계속 생성되고 제거되면 서버에 부담이 커질 수밖에 없다.
- 그래서 스프링은 기본적으로 빈을 싱글톤으로 만들어, **하나의 객체를 여러 요청에서 재사용**하도록 한다.

#### 3. 서비스 오브젝트와 싱글톤

그래서 엔터프라이즈 시스템에서는 오래전부터 **서비스 오브젝트**라는 개념을 사용해왔다.

- **서비스 오브젝트**란 여러 클라이언트 요청을 처리하기 위해 서버에서 재사용되는 객체를 말하며 대표적인 예가 **서블릿 Servlet**이다.
- **서블릿**은 서버 환경에서 가장 기본이 되는 서비스 오브젝트라고 할 수 있다.
- 서블릿은 클래스 당 하나의 객체만 생성되고, 클라이언트의 요청을 처리하는 여러 스레드에가 이 하나의 객체를 동시에 사용한다.
- 즉, 서블릿은 일반적으로 **멀티스레드 환경에서 싱글톤**으로 동작한다.
- 서블릿/DAO 같은 '서비스 오브젝트'는 "기능만 제공하는 도구"일 뿐, 사용자별로 따로 보관할 데이터가 없어서 사람마다 새로 만들 이유가 없다. 
-> 클래스당 딱 1개(싱글톤)만 만들어 두고 여러 스레드가 공유해서 쓴다.

비유) ATM 한 대(=서블릿 오브젝트)를 여러 손님(=스레드)이 같이 쓰는 것과 같다. ATM은 출금/입금 '기능'만 제공할 뿐, 특정 손님의 돈을 자기 안에 보관하지 않는다.

#### 4. 애플리케이션 컨텍스트는 IoC 컨테이너 +  싱글톤 레지스트리

- 애플리케이션 컨텍스트는 직접 싱글톤 객체를 만들고 저장하고 관리한다.
- 이처럼 싱글톤 객체를 생성하고 보관하며 필요할 때 제공하는 기능을 하기 때문에, 애**플리케이션 컨텍스트**를 **싱글톤 레지스트리**라고도 한다.

#### 5. 빈의 기본 스코프

- 오브젝트 스코프란 객체가 생성되고, 존재하고, 사용되는 범위를 말한다.
- **스프링 빈의 기본 스코프는 `singleton`이다.**
- `singleton scope` → 스프링 컨테이너당 하나의 빈 객체만 생성되고 공유됨
- 즉, 같은 스프링 컨테이너 안에서는 같은 빈을 여러 번 요청해도 동일한 객체가 반환된다.
- 생성된 빈 객체는 강제로 제거하지 않는 한 스프링 컨테이너가 존재하는 동안 계속 유지 된다.
- 경우에 따라서는 싱글톤 외의 스코프를 가질 수 있다.
    
    ex) 웹을 통해 새로운 HTTP 요청이 생길 때마다 생성되는 요청 스코프가 있다.
    

#### 6. 싱글톤 빈, 서비스 오브젝트 : 무상태의 원칙

- 싱글톤 빈, 서비스 오브젝트는 여러 스레드가 동시에 하나의 객체를 사용할 수 있다.
- 따라서 사용자/요청별 데이터처럼 **요청마다 달라지는 값을 인스턴스 변수로 저장하면 위험**하다.
- 싱글톤 빈, 서비스 오브젝트는 가능하면 상태를 가지지 않는 **무상태 stateless 객체**로 만들어야 한다.
- 요청마다 달라지는 데이터는 필드 대신 **메서드의 지역 변수나 파라미터로 처리하는 것이 안전**하다. (지역 변수는 스레드마다 따로 생성)

위험 값 :  특정 요청의 사용자 정보, 처리 중간값 등

가능 값 : 읽기 전용 값, 다른 싱글톤에 대한 참조(예: UserDAO가 ConnectionMaker를 필드로 가짐)

#### HTTP 요청이 몰리면 대기하는가?

대기 여부는 빈 스코프가 아니라 스레드가(일꾼) 수가 결정한다.

| 구분 | 무엇이 정하나 |
| --- | --- |
| 빈이 새로 만들어지는가 | 스코프가 결정 (request면 요청마다 새로) |
| 요청이 대기하는가 | WAS의 스레드 풀 크기가 결정 |
- WAS(톰캣 등)는 스레드 풀을 둔다 (예: 기본 200개).
- 요청 200개까지는 스레드를 배정받아 동시에 처리 → 각자 요청 스코프 빈 생성.
- 201번째부터는 스레드가 빌 때까지 큐에서 대기 → 스레드를 받는 순간 그때 자기 빈이 생성된다.

#### 싱글톤과의 대비

- 싱글톤: 클래스당 1개를 모두가 공유 → 그래서 상태(필드)를 가지면 안 됨(무상태 원칙).
- 요청 스코프: 요청마다 독립된 빈 → 그 요청 동안의 상태를 안전하게 담아도 됨 (다른 요청과 안 섞임).

요약: 요청이 몰리면 요청 스코프 빈은 계속 새로 생성되고(대기 X), 정작 대기가 생긴다면 그건 스코프가 아니라 서버 스레드 풀이 꽉 찼기 때문

#### 직접 만든 싱글톤 - ex_1_6/UserDAO, DaoFactory

(자바 코드로 구현하는 전통적인 싱글톤 패턴 - GoF의 디자인 패턴)

만드는 순서  

1) 클래스당 오브젝트를 1개만 담아둘 자기 자신 타입의 static 필드를 만든다.

2) 생성자를 private으로 막아 외부에서 new 로 마구 만들지 못하게 한다.

3) 유일한 오브젝트를 돌려주는 static 메서드(getInstance)를 둔다.
     - 처음 호출될 때 한 번만 만들고, 그 다음부터는 이미 만든 오브젝트를 그대로 돌려준다.

UserDAO 예제 코드 + 주석

#### 직접 만든 싱글톤의 한계

- private 생성자라서 상속이 불가능하다. (객체지향의 장점인 상속/다형성 이용 불가)
- 테스트가 어렵다. (생성 방식이 고정되어 가짜(mock) 오브젝트로 갈아끼우기 힘들다.)
- getInstance처럼 의존 오브젝트 (simpleConnectionMaker) 주입이 어색하다. (두번재 호출부터는 넘긴 인자가 무시된다.)
- 클래스 로더가 여러 개인 환경에서는 싱글톤이 여러 개 생길 수 있다.
- static 필드라 사실상 전역 상태가 되어 아무 데서나 접근 가능해진다.

→ 그래서 스프링은 ‘싱글톤 레지스트리’로 대신 관리해준다.

```groovy
public class UserDAO {

    // 1) 클래스가 자기 자신의 유일한 오브젝트를 보관 (클래스당 1개)
    private static UserDAO instance;

    private SimpleConnectionMaker simpleConnectionMaker;

    // 2) 생성자를 private 으로 막는다 -> 이제 외부에서 new UserDAO(...) 불가
    private UserDAO(SimpleConnectionMaker simpleConnectionMaker) {
        this.simpleConnectionMaker = simpleConnectionMaker;
    }

    // 3) 유일한 오브젝트를 돌려주는 통로
    //    - synchronized: 여러 스레드가 동시에 들어와 오브젝트가 2개 만들어지는 것을 막는다.
    public static synchronized UserDAO getInstance(SimpleConnectionMaker simpleConnectionMaker) {
        if (instance == null) {
            instance = new UserDAO(simpleConnectionMaker);
        }

        return instance;
    }
    
    ...
}
```