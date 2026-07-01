# 스프링 AOP 자동 프록시 만들기 (Advice → Pointcut → Advisor → 자동 프록시)

> 토비의 스프링 6장에서 배운 **Advice 방식**과 **`DefaultAdvisorAutoProxyCreator`(빈 후처리기)**를, 트랜잭션이 아닌 **"메서드 실행 시간 측정"** 부가기능으로 직접 만들어봐요.
> 이번 과제의 클라이맥스는 마지막 Step이에요. **서비스를 새로 추가해도 AOP 설정은 한 줄도 안 바꾸는데**, 부가기능이 그 서비스에 **자동으로** 붙는 걸 직접 확인합니다.
>
> 💡 각 Step의 **힌트는 접혀 있어요.** "이건 Advice야, Pointcut이야?"를 먼저 고민하고 막히면 펼치세요.

<details>
<summary>✅ 전체 정답 코드 보기 (먼저 직접 만들어본 뒤 펼쳐서 비교하세요)</summary>

> 아래는 Step 6(서비스 추가)까지 모두 반영된 **완성본**이에요. 파일별로 나눠서 그대로 옮기면 동작합니다.
> 패키지: 서비스/구현체는 `com.example.shop.service`, 설정·Advice·Main은 `com.example.shop`.

**`com/example/shop/service/OrderService.java`** — 주문 서비스 인터페이스
```java
package com.example.shop.service;

// 클라이언트(그리고 자동 생성될 프록시)가 의존하는 '계약'.
// 인터페이스가 있으므로 스프링은 JDK 다이나믹 프록시를 만든다.
public interface OrderService {
    String placeOrder(String item);
}
```

**`com/example/shop/service/OrderServiceImpl.java`** — 주문 서비스 구현(target)
```java
package com.example.shop.service;

// target 빈. 시간 측정/로그 코드가 '전혀' 없다 — 순수 비즈니스 로직만.
// 부가기능은 나중에 프록시가 바깥에서 입혀준다.
public class OrderServiceImpl implements OrderService {
    @Override
    public String placeOrder(String item) {
        sleep(80);                       // 실제 작업에 시간이 걸리는 상황을 흉내
        return "주문완료: " + item;
    }

    private void sleep(long ms) {        // 측정값이 0ms가 아니도록 잠깐 멈춤
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
```

**`com/example/shop/service/MemberService.java`** — 회원 서비스 인터페이스
```java
package com.example.shop.service;

public interface MemberService {
    String register(String id);
}
```

**`com/example/shop/service/MemberServiceImpl.java`** — 회원 서비스 구현(target)
```java
package com.example.shop.service;

// 이 역시 부가기능 코드가 없는 순수 구현체.
public class MemberServiceImpl implements MemberService {
    @Override
    public String register(String id) {
        sleep(50);
        return "가입완료: " + id;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
```

**`com/example/shop/service/ProductService.java`** — (Step 6) 상품 서비스 인터페이스
```java
package com.example.shop.service;

public interface ProductService {
    String getProduct(String code);
}
```

**`com/example/shop/service/ProductServiceImpl.java`** — (Step 6) 상품 서비스 구현(target)
```java
package com.example.shop.service;

// Step 6에서 '나중에 추가'되는 서비스.
// AopConfig의 Advisor/Pointcut/후처리기는 손대지 않아도
// service 패키지에 속하므로 Pointcut에 걸려 자동으로 프록시가 적용된다.
public class ProductServiceImpl implements ProductService {
    @Override
    public String getProduct(String code) {
        sleep(30);
        return "상품: " + code;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
```

**`com/example/shop/PerformanceMonitorAdvice.java`** — Advice(부가기능 본체)
```java
package com.example.shop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

// Advice = '무엇을' 할 것인가. 여기서는 '실행 시간 측정 + 로그'.
// 어떤 서비스에 붙을지 모르지만 동작한다 → 그래서 업무 단어가 하나도 없다.
public class PerformanceMonitorAdvice implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        // 가로챈 메서드 이름을 "구현클래스.메서드" 형태로 구성 (로그용)
        String name = invocation.getMethod().getDeclaringClass().getSimpleName()
                    + "." + invocation.getMethod().getName();

        long start = System.nanoTime();          // (앞) 실행 전 시각 기록
        try {
            return invocation.proceed();         // ★ 실제 target 메서드 실행 + 반환값 그대로 전달
        } finally {
            // (뒤) 예외가 나도 시간은 반드시 찍히도록 finally 사용
            long ms = (System.nanoTime() - start) / 1_000_000;
            System.out.printf("[PERF] %s : %dms%n", name, ms);
        }
    }
}
```

**`com/example/shop/AopConfig.java`** — Pointcut + Advisor + 자동 프록시 설정
```java
package com.example.shop;

import com.example.shop.service.*;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AopConfig {

    // ① 자동 프록시 엔진 (빈 후처리기).
    //    컨테이너의 모든 Advisor를 모아두고, 빈이 생성될 때마다
    //    그 빈이 Advisor의 Pointcut에 걸리는지 검사 → 걸리면 프록시로 교체해 등록한다.
    @Bean
    public DefaultAdvisorAutoProxyCreator autoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }

    // ② Advisor = Pointcut(어디에) + Advice(무엇을).
    //    빈으로 등록만 해두면 ①번 후처리기가 자동으로 수집한다.
    @Bean
    public Advisor performanceAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        // service 패키지(하위 포함)의 모든 클래스, 모든 메서드, 모든 인자
        pointcut.setExpression("execution(* com.example.shop.service..*.*(..))");
        return new DefaultPointcutAdvisor(pointcut, new PerformanceMonitorAdvice());
    }

    // ③ target 빈들 — '평범하게' 등록한다. 프록시에 대한 언급이 전혀 없다!
    //    "누구를 프록시로 만들지"를 여기서 지정하지 않는다. Pointcut이 알아서 고른다.
    @Bean public OrderService   orderService()   { return new OrderServiceImpl();   }
    @Bean public MemberService  memberService()  { return new MemberServiceImpl();  }
    @Bean public ProductService productService() { return new ProductServiceImpl(); } // Step 6에서 이 한 줄만 추가
}
```

**`com/example/shop/Main.java`** — 실행 진입점
```java
package com.example.shop;

import com.example.shop.service.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        var ctx = new AnnotationConfigApplicationContext(AopConfig.class);

        // 빈은 '인터페이스 타입'으로 꺼낸다.
        // 자동 생성된 프록시는 OrderServiceImpl이 아니라 OrderService를 구현한 별개 객체이기 때문.
        OrderService   orderService   = ctx.getBean(OrderService.class);
        MemberService  memberService  = ctx.getBean(MemberService.class);
        ProductService productService = ctx.getBean(ProductService.class);

        System.out.println("===== 주문 서비스 호출 =====");
        System.out.println(orderService.placeOrder("기계식 키보드")); // [PERF] 로그가 자동으로 찍힘

        System.out.println("\n===== 회원 서비스 호출 =====");
        System.out.println(memberService.register("kim"));

        System.out.println("\n===== 상품 서비스 호출 (Step 6: 설정 무수정으로 자동 적용) =====");
        System.out.println(productService.getProduct("A-100"));

        System.out.println("\n===== 진짜 프록시인지 확인 =====");
        // OrderServiceImpl이 아니라 $Proxy... (또는 CGLIB) 가 찍히면 자동 프록시 성공
        System.out.println("orderService 의 실제 타입: " + orderService.getClass());

        ctx.close();
    }
}
```

**실행 결과(예시)**
```
===== 주문 서비스 호출 =====
[PERF] OrderServiceImpl.placeOrder : 82ms
주문완료: 기계식 키보드

===== 회원 서비스 호출 =====
[PERF] MemberServiceImpl.register : 51ms
가입완료: kim

===== 상품 서비스 호출 (Step 6: 설정 무수정으로 자동 적용) =====
[PERF] ProductServiceImpl.getProduct : 30ms
상품: A-100

===== 진짜 프록시인지 확인 =====
orderService 의 실제 타입: class jdk.proxy3.$Proxy23
```

> 시간(ms)·프록시 번호(`$Proxy23`)는 환경마다 달라요. `OrderServiceImpl`이 아닌 `$Proxy...`가 나오는지가 핵심이에요.

</details>

---

## 0. 먼저 알아둘 점

- 우리가 만들 **부가기능**은 "메서드가 끝나는 데 몇 ms 걸렸나"를 재서 로그로 찍는 거예요. 비즈니스 코드(주문/회원)와는 **관심사가 다른** 횡단 관심사(cross-cutting concern)죠.
- 핵심은 **"target 코드를 한 줄도 안 건드리고"** 이 부가기능을 모든 서비스에 적용하는 거예요.
- 수업에서 만든 `ProxyFactoryBean` 방식은 **서비스마다 프록시 빈을 따로 등록**해야 했어요. 서비스가 100개면 설정도 100개. 이번엔 **`DefaultAdvisorAutoProxyCreator` + Advisor 하나**로 그걸 자동화해요.
- 우리는 `@Aspect`/`@EnableAspectJAutoProxy`를 **일부러 안 써요.** 그게 내부에서 대신 해주는 일(Advisor 수집 + 자동 프록시)을 손으로 만들어보는 게 목적이에요. 그래야 나중에 `@Aspect`가 뭘 자동화한 건지 알게 돼요.

---

## 1. 무엇을 만드나요?

쇼핑몰 서비스(주문/회원)를 **평범하게** 등록만 했는데, 호출하면 실행 시간 로그가 **자동으로** 찍히는 프로그램을 완성해요.

**콘솔 출력 예시**
```
===== 주문 서비스 호출 =====
[PERF] OrderServiceImpl.placeOrder : 82ms
주문완료: 기계식 키보드

===== 회원 서비스 호출 =====
[PERF] MemberServiceImpl.register : 51ms
가입완료: kim

===== 진짜 프록시인지 확인 =====
orderService 의 실제 타입: class jdk.proxy3.$Proxy23   ← OrderServiceImpl 이 아니다!
```

핵심은 두 가지예요.
1. `OrderServiceImpl` 안에는 시간 측정 코드가 **하나도 없는데** `[PERF]` 로그가 찍힌다.
2. 꺼내온 빈의 실제 타입이 `OrderServiceImpl`이 아니라 **프록시**다.

---

## 2. 학습 목표

| 개념 | 어디서 배우나 |
|------|------|
| Advice: 부가기능 본체 (`MethodInterceptor`) | Step 1 (`PerformanceMonitorAdvice.java`) |
| Pointcut: 어디에 적용할지 (선정 조건) | Step 2 (`AopConfig.java`) |
| Advisor = Pointcut + Advice | Step 3 (`AopConfig.java`) |
| 자동 프록시: `DefaultAdvisorAutoProxyCreator` | Step 4 (`AopConfig.java`) |
| 실행 + 프록시 확인 | Step 5 (`Main.java`) |
| ★ 서비스 추가 시 자동 적용 확인 | Step 6 |

---

## 3. 핵심 개념

### (1) 용어 한눈에
| 용어 | 한 줄 | 이 과제에서는 |
|------|------|------|
| Advice | **무엇을** 할 부가기능 | "실행 시간을 재서 로그 남기기" |
| Pointcut | **어디에** 적용할지 고르는 조건 | "service 패키지의 모든 메서드" |
| Advisor | Advice + Pointcut **묶음** | 위 둘을 하나로 포장한 빈 |
| `DefaultAdvisorAutoProxyCreator` | 빈 후처리기. **걸리는 빈을 자동 프록시로** 교체 | 우리가 만들 자동화 엔진 |

### (2) 자동 프록시의 동작 (이번 과제의 중심)
```
컨테이너가 빈을 만들 때마다, 빈 후처리기가 끼어든다
   → 등록된 Advisor들의 Pointcut에 이 빈이 걸리나?
      → 걸리면: 원본 대신 '프록시'를 만들어 컨테이너에 등록
      → 안 걸리면: 원본 그대로 등록
결과: Pointcut 조건에 맞는 빈은 전부 자동으로 프록시가 된다.
      서비스가 10개든 100개든 Advisor 하나면 끝.
```

### (3) Advice 안에서의 호출 흐름
```
클라이언트 → (자동 생성된 프록시) → Advice.invoke()
                                       ├ (앞) 시작 시각 기록
                                       ├ invocation.proceed() ── target 실제 메서드 실행
                                       └ (뒤) 걸린 시간 로그
```
`proceed()`가 **실제 target을 부르는 스위치**예요. 그 앞뒤에 부가기능을 끼워 넣어요.

```
[기억법] Advice=무엇을 / Pointcut=어디에 / Advisor=둘의 묶음 / AutoProxyCreator=자동으로 갈아끼우기
         그리고 proceed() 앞뒤가 부가기능 자리
```

---

## 4. 파일 구조 & 준비물

| 파일 | 역할 |
|------|------|
| `OrderService` / `OrderServiceImpl` | 주문 서비스 (target) |
| `MemberService` / `MemberServiceImpl` | 회원 서비스 (target) |
| `PerformanceMonitorAdvice` | 부가기능(Advice) — 실행 시간 측정 |
| `AopConfig` | Pointcut + Advisor + 자동 프록시 빈 등록 |
| `Main` | 실행 진입점 |

> 모든 클래스는 `com.example.shop.service`(서비스/구현체)와 `com.example.shop`(설정/Main) 아래에 둬요. Pointcut이 패키지로 대상을 고르거든요.

**의존성** (스프링 부트 기준)
```gradle
implementation 'org.springframework.boot:spring-boot-starter'
implementation 'org.springframework.boot:spring-boot-starter-aop'   // spring-aop + aspectjweaver
```

---

## 5. Step by Step

### Step 0. target 서비스 준비 (시간 측정 코드 없이!)

먼저 **순수한** 서비스 두 개를 만들어요. 부가기능(시간 측정)은 **절대 여기 넣지 않아요.** 작업하는 척 잠깐 `sleep`만 해요.

```java
package com.example.shop.service;

public interface OrderService { String placeOrder(String item); }
```
```java
package com.example.shop.service;

public class OrderServiceImpl implements OrderService {
    @Override
    public String placeOrder(String item) {
        sleep(80);                       // 실제 작업 흉내
        return "주문완료: " + item;
    }
    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
```
회원 서비스도 같은 모양으로 만들어요. (`MemberService.register(String id)` → `sleep(50)` 후 `"가입완료: " + id` 반환)

**확인**: 두 서비스 안에 시간 측정/로그 코드가 **한 줄도 없어야** 정상이에요.

---

### Step 1. Advice 만들기 — 실행 시간 측정 (`PerformanceMonitorAdvice.java`)

**목표**: "메서드 실행을 가로채서, 앞뒤로 시간을 재는" 부가기능을 **한 곳에** 만들어요.

**할 일**
1. `org.aopalliance.intercept.MethodInterceptor`를 구현해요.
2. `invoke()` 안에서 ① 시작 시각 기록 → ② `invocation.proceed()`로 실제 메서드 실행 → ③ 걸린 시간 로그.
3. 예외가 나도 시간이 찍히도록 `try/finally`를 써요.

<details>
<summary>💡 힌트 보기</summary>

```java
package com.example.shop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class PerformanceMonitorAdvice implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String name = invocation.getMethod().getDeclaringClass().getSimpleName()
                    + "." + invocation.getMethod().getName();
        long start = System.nanoTime();
        try {
            return invocation.proceed();            // ★ 실제 target 메서드 실행
        } finally {
            long ms = (System.nanoTime() - start) / 1_000_000;
            System.out.printf("[PERF] %s : %dms%n", name, ms);
        }
    }
}
```

`proceed()` **앞**이 "메서드 실행 전", **뒤(finally)**가 "메서드 실행 후"예요. 부가기능은 이 앞뒤에 끼워 넣어요. `proceed()`의 반환값을 그대로 `return` 하는 걸 잊지 마세요 — 안 그러면 target의 결과가 사라져요.

</details>

**확인**: 이 클래스 어디에도 "주문/회원" 같은 업무 단어가 없어야 해요. 부가기능은 어떤 서비스에 붙을지 몰라도 동작하니까요.

---

### Step 2. Pointcut 만들기 — 어디에 적용할지 (`AopConfig.java`)

**목표**: "service 패키지 안 모든 클래스의 모든 메서드"를 적용 대상으로 고르는 Pointcut을 만들어요.

**할 일**
1. `AspectJExpressionPointcut`을 만들어요.
2. 표현식으로 대상을 지정해요: `execution(* com.example.shop.service..*.*(..))`

<details>
<summary>💡 힌트 보기</summary>

```java
import org.springframework.aop.aspectj.AspectJExpressionPointcut;

AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
pointcut.setExpression("execution(* com.example.shop.service..*.*(..))");
```

표현식을 끊어 읽으면:
`execution(`반환형 `*` · 패키지 `com.example.shop.service..` · 클래스 `*` · 메서드 `.*` · 인자 `(..)` `)`
= "service 패키지(하위 포함)의 아무 클래스, 아무 메서드, 아무 인자" 전부.
나중에 `*Service`처럼 **이름 패턴**으로 바꾸면 선정 범위가 달라져요(도전 과제 참고).

</details>

**확인**: Pointcut은 "선정 기준"일 뿐이에요. 아직 아무 일도 안 일어나요. 다음 Step에서 Advice와 묶어야 의미가 생겨요.

---

### Step 3. Advisor 만들기 — Pointcut + Advice 묶기 (`AopConfig.java`)

**목표**: "이 조건(Pointcut)에 이 부가기능(Advice)을 적용한다"를 하나로 포장해 **빈으로 등록**해요.

**할 일**
1. `DefaultPointcutAdvisor(pointcut, advice)`로 둘을 묶어요.
2. 이걸 `@Bean`으로 등록해요. (빈으로 올려두기만 하면 다음 Step의 후처리기가 자동 수집해요.)

<details>
<summary>💡 힌트 보기</summary>

```java
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;

@Bean
public Advisor performanceAdvisor() {
    AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
    pointcut.setExpression("execution(* com.example.shop.service..*.*(..))");
    return new DefaultPointcutAdvisor(pointcut, new PerformanceMonitorAdvice());
}
```

Advisor = **"어디에(Pointcut) + 무엇을(Advice)"**. 이게 자동 프록시의 '레시피' 한 장이에요. 레시피만 빈으로 올려두면, 다음 Step의 후처리기가 알아서 요리해요.

</details>

**확인**: Advisor 빈이 하나 등록되면 돼요. 아직 target 빈에는 아무 설정도 안 했다는 점에 주목하세요.

---

### Step 4. 자동 프록시 켜기 + target 빈은 평범하게 (`AopConfig.java`)

**목표**: `DefaultAdvisorAutoProxyCreator`를 빈으로 등록해서, **Pointcut에 걸리는 빈을 자동으로 프록시로** 바꾸게 해요. target 서비스들은 **프록시 설정 없이** 평범하게 등록해요.

**할 일**
1. `DefaultAdvisorAutoProxyCreator`를 `@Bean`으로 등록해요.
2. `orderService`, `memberService`를 그냥 `new ...Impl()`로 등록해요. (프록시 관련 코드 일절 없음!)

<details>
<summary>💡 힌트 보기</summary>

```java
package com.example.shop;

import com.example.shop.service.*;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.*;

@Configuration
public class AopConfig {

    // ① 자동 프록시 엔진 (빈 후처리기)
    @Bean
    public DefaultAdvisorAutoProxyCreator autoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }

    // ② 레시피: 어디에 + 무엇을
    @Bean
    public Advisor performanceAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* com.example.shop.service..*.*(..))");
        return new DefaultPointcutAdvisor(pointcut, new PerformanceMonitorAdvice());
    }

    // ③ target 빈들 — '평범하게' 등록 (프록시 설정 전혀 없음)
    @Bean public OrderService  orderService()  { return new OrderServiceImpl();  }
    @Bean public MemberService memberService() { return new MemberServiceImpl(); }
}
```

여기가 토비 6장의 핵심이에요. **target 빈(③)에는 프록시에 대한 언급이 전혀 없어요.** "누구를 프록시로 만들지"를 우리가 지정하지 않아요. ①번 후처리기가 빈을 만들 때마다 ②번 Advisor의 Pointcut에 걸리는지 검사하고, 걸리면 알아서 프록시로 바꿔치기해요.

</details>

**확인**: `AopConfig`에 빈이 4개(후처리기 1 + Advisor 1 + 서비스 2)면 준비 완료예요.

---

### Step 5. 실행하고 '진짜 프록시인지' 확인 (`Main.java`)

**목표**: 서비스를 호출해서 `[PERF]` 로그가 자동으로 찍히는지, 그리고 꺼낸 빈이 정말 프록시인지 확인해요.

**할 일**
1. `AnnotationConfigApplicationContext(AopConfig.class)`로 컨테이너를 띄워요.
2. **인터페이스 타입으로** 빈을 꺼내요. (`ctx.getBean(OrderService.class)`)
3. 메서드를 호출하고, `서비스.getClass()`를 출력해 실제 타입을 봐요.

<details>
<summary>💡 힌트 보기</summary>

```java
package com.example.shop;

import com.example.shop.service.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        var ctx = new AnnotationConfigApplicationContext(AopConfig.class);

        OrderService  orderService  = ctx.getBean(OrderService.class);
        MemberService memberService = ctx.getBean(MemberService.class);

        System.out.println("===== 주문 서비스 호출 =====");
        System.out.println(orderService.placeOrder("기계식 키보드"));

        System.out.println("\n===== 회원 서비스 호출 =====");
        System.out.println(memberService.register("kim"));

        System.out.println("\n===== 진짜 프록시인지 확인 =====");
        System.out.println("orderService 의 실제 타입: " + orderService.getClass());

        ctx.close();
    }
}
```

빈을 꺼낼 때 **인터페이스 타입**으로 꺼내는 게 포인트예요. 자동 생성된 프록시는 `OrderServiceImpl`이 아니라 `OrderService` 인터페이스를 구현한 별개의 객체라서, `getBean(OrderServiceImpl.class)`로는 못 꺼낼 수 있어요. "인터페이스에 의존하라"가 여기서도 그대로 통해요.

</details>

**확인**: `[PERF] OrderServiceImpl.placeOrder : 80ms`처럼 로그가 자동으로 찍히고, `getClass()` 출력이 `$Proxy...`(또는 `...EnhancerBySpringCGLIB...`)이면 성공이에요. `OrderServiceImpl`이 그대로 나오면 자동 프록시가 안 걸린 거예요(주로 Pointcut 표현식/패키지 경로 문제).

---

### Step 6. ★핵심★ 서비스를 추가해도 AOP는 무수정

**목표**: 새 서비스를 추가하고, **AOP 설정(Advice/Pointcut/Advisor/후처리기)은 한 줄도 안 고친 채** 부가기능이 자동으로 붙는 걸 확인해요. 이게 "자동화"의 진짜 의미예요.

**할 일**
1. `ProductService` / `ProductServiceImpl`을 `service` 패키지에 추가해요. (`getProduct(String code)` → `sleep(30)` 후 `"상품: " + code` 반환)
2. `AopConfig`에는 **target 빈 한 줄만** 추가해요: `@Bean public ProductService productService() { return new ProductServiceImpl(); }`
   → **Advisor도, Pointcut도, 후처리기도 절대 건드리지 않아요.**
3. `Main`에서 `productService.getProduct("A-100")`을 호출해요.

**확인**: 부가기능 설정을 전혀 안 바꿨는데도 `[PERF] ProductServiceImpl.getProduct : 30ms`가 **자동으로** 찍히면 완성이에요! 🎉
이게 바로 "서비스가 10개든 100개든 Advisor 하나면 끝"의 의미예요.

---

## 6. 학습 체크

- [ ] Advice / Pointcut / Advisor를 각각 한 줄로 설명할 수 있다
- [ ] `proceed()`가 무엇을 하는지, 그 앞뒤가 왜 부가기능 자리인지 안다
- [ ] `DefaultAdvisorAutoProxyCreator`가 빈을 언제, 어떻게 프록시로 바꾸는지 안다
- [ ] target 빈에는 왜 프록시 관련 설정이 없는지 설명할 수 있다
- [ ] 서비스를 추가해도 AOP 설정이 안 바뀌는 이유를 안다

## 7. 최종 완성 체크리스트

- [ ] 서비스 구현체에 시간 측정 코드가 전혀 없다
- [ ] 호출 시 `[PERF]` 로그가 자동으로 찍힌다
- [ ] 꺼낸 빈의 실제 타입이 프록시다(`$Proxy...` 등)
- [ ] 새 서비스를 추가했을 때 AOP 설정 변경 없이 부가기능이 적용된다

## 8. (선택) 도전 과제

1. **이름 패턴 Pointcut**: 표현식을 `execution(* com.example.shop.service..*Service.get*(..))`로 바꿔, `get`으로 시작하는 조회 메서드에만 적용되게 해보세요. 어떤 메서드가 빠지는지 관찰!
2. **Advice 추가**: 예외가 던져지면 그 예외를 로그로 남기는 `ExceptionLoggingAdvice`를 만들어 **Advisor를 하나 더** 등록해보세요. (Advisor가 여러 개여도 후처리기가 다 수집해요.)
3. **직접 Pointcut 구현**: `AspectJExpressionPointcut` 대신, `Pointcut` 인터페이스를 직접 구현해서 `ClassFilter`(클래스 거르기) + `MethodMatcher`(메서드 거르기)가 무엇인지 코드로 확인해보세요. "Pointcut = 클래스 필터 + 메서드 매처"를 체감하는 게 목표예요.
4. ★ **연결 정리**: 지난 시간에 손으로 짠 데코레이터(예: `RetryNotificationSender`)와 이번 자동 프록시를 비교해, "무엇이 자동화되었는가"를 한 문단으로 적어보세요.