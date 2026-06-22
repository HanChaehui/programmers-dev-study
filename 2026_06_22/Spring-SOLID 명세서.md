# SOLID 5원칙 익히기 (나쁜 코드를 좋은 코드로 리팩터링하기)

> 객체지향 설계의 5가지 원칙(SOLID)을, **"나쁜 예 → 좋은 예"로 직접 고쳐보며** 익혀요.
> 외우는 게 아니라, *왜 이렇게 짜야 변경에 강한 코드가 되는지* 손으로 체감하는 게 목표예요.
> **아래 Step을 순서대로 따라가면, 마지막 Step에서 5원칙이 모두 동작하는 프로그램이 완성됩니다.**
>
> 💡 각 Step의 **힌트는 접혀 있어요.** "나쁜 예"를 먼저 직접 고쳐보고, 막히면 "힌트 보기"를 펼치세요.

---

## 0. 먼저 알아둘 점

- SOLID는 **하나의 큰 프로그램**이 아니라 **5개의 작은 원칙**이에요. 그래서 이 가이드는 원칙마다 **짧고 독립적인 예제**를 하나씩 리팩터링해요. (억지로 한 도메인에 다 욱여넣으면 오히려 더 헷갈려요.)
- 모든 Step은 같은 흐름이에요: **나쁜 코드를 보고 → "뭐가 문제지?" 생각하고 → 좋은 구조로 고친다.**
- 인터페이스/상속/다형성은 배웠다고 가정해요. (SOLID는 이걸 *언제, 왜* 쓰는지에 대한 원칙이에요.)
- `ArrayList<String>`처럼 **구체 타입이 들어간 컬렉션**만 써요. `<K, V>` 같은 직접 만드는 제네릭은 다루지 않아요.

---

## 1. 무엇을 만드나요?

5개 원칙의 "좋은 설계"가 모두 살아 움직이는 `Main`을 완성해요. 다 만들면 출력이 이렇게 나와요.

```
===== SRP: 단일 책임 =====
- 오늘은 자바를 배웠다
- SOLID는 어렵지만 재밌다

===== OCP: 개방-폐쇄 =====
일반 회원 -> 10000원
골드 회원 -> 9000원
VIP 회원 -> 8000원

===== LSP: 리스코프 치환 =====
냠냠 먹습니다
냠냠 먹습니다
훨훨 납니다
첨벙 헤엄칩니다

===== ISP: 인터페이스 분리 =====
구형 프린터: 인쇄만 합니다
복합기: 인쇄
복합기: 스캔

===== DIP: 의존관계 역전 =====
[이메일] 주문이 완료되었습니다
[SMS] 주문이 완료되었습니다
```

핵심은 출력 자체가 아니라, **이 출력을 만드는 클래스 구조**예요. 각 Step에서 "나쁜 구조"를 "좋은 구조"로 바꾸는 게 진짜 과제예요.

## 2. 학습 목표

| 개념 | 어디서 배우나 |
|------|------|
| SRP — 클래스는 책임 1개만 | Step 1 (`Srp.java`) |
| OCP — 수정 없이 확장 | Step 2 (`Ocp.java`) |
| LSP — 자식이 부모를 안전하게 대체 | Step 3 (`Lsp.java`) |
| ISP — 인터페이스는 작게 쪼개기 | Step 4 (`Isp.java`) |
| DIP — 추상에 의존하고 주입받기 | Step 5 (`Dip.java`) |
| 다형성으로 전체 조립 | Step 6 (`Main.java`) |

## 3. 핵심 개념

SOLID는 **"나중에 코드를 고칠 때 덜 아프게"** 만드는 5가지 습관이에요. 한 줄 요약부터 잡고 가요.

### (1) SRP — 단일 책임 원칙
> 한 클래스는 **바뀔 이유가 하나뿐**이어야 해요.

일기 내용 관리도 하고, 파일 저장도 하고, 화면 출력도 하는 클래스는 바뀔 이유가 3개죠. 저장 방식만 바꿔도 이 클래스를 건드려야 해요. → **책임별로 클래스를 나눠요.**

### (2) OCP — 개방-폐쇄 원칙
> **확장에는 열려**(새 기능 추가 OK), **수정에는 닫혀**(기존 코드는 안 건드림) 있어야 해요.

`if (grade.equals("GOLD")) ... else if ("VIP") ...` 이런 코드는 등급이 늘 때마다 그 메서드를 또 고쳐요. → **인터페이스 + 새 클래스 추가**로 바꾸면, 기존 코드를 안 건드리고 확장돼요.

### (3) LSP — 리스코프 치환 원칙
> **부모 자리에 자식을 넣어도** 프로그램이 멀쩡해야 해요.

`Bird`는 `fly()` 한다고 약속했는데 자식 `Penguin`이 "난 못 날아!" 하고 예외를 던지면, `Bird`인 줄 알고 쓰던 코드가 터져요. → **억지 상속을 끊고**, 날 수 있는 새와 없는 새를 구조로 구분해요.

### (4) ISP — 인터페이스 분리 원칙
> 큰 인터페이스 하나보다 **작은 인터페이스 여러 개**가 나아요.

`print + scan + fax`를 다 가진 `Machine` 인터페이스를 인쇄만 하는 프린터가 구현하면, 안 쓰는 `scan/fax`를 빈 껍데기로 채워야 해요. → **기능별로 인터페이스를 쪼개고**, 필요한 것만 `implements` 해요.

### (5) DIP — 의존관계 역전 원칙
> 상위 모듈은 **구체 클래스가 아니라 추상(인터페이스)에 의존**해야 해요.

알림 서비스가 `EmailSender`를 직접 `new` 하면, SMS로 바꿀 때 서비스 코드를 고쳐야 해요. → **`MessageSender` 인터페이스에 의존**하고, 실제 구현체는 **생성자로 주입**받아요.

```
[기억법] S-O-L-I-D
S 책임 하나   O 수정 말고 추가   L 자식이 부모 대체
I 인터페이스 쪼개   D 추상에 의존 + 주입
```

## 4. 파일 구조

원칙 하나당 파일 하나로 묶었어요. "이 파일 = 이 원칙"으로 외우기 좋아요.

| 파일 | 역할 |
|------|------|
| `Srp.java` | 일기장 예제 — 책임 분리 (`Journal`, `JournalSaver`) |
| `Ocp.java` | 할인 정책 예제 — 인터페이스로 확장 (`DiscountPolicy` 외) |
| `Lsp.java` | 새 예제 — 안전한 상속 (`Bird`, `FlyingBird`, `Penguin` 외) |
| `Isp.java` | 복합기 예제 — 인터페이스 분리 (`Printer`, `Scanner` 외) |
| `Dip.java` | 알림 예제 — 의존성 주입 (`MessageSender` 외) |
| `Main.java` | 5개 원칙을 모두 실행해 보는 진입점 |

> 실행: `javac *.java` → `java -Dstdout.encoding=UTF-8 Main`

---

## 5. Step by Step

### Step 1. SRP — 일기장의 책임을 둘로 나누기 (`Srp.java`)

**목표**: 여러 책임이 뒤섞인 클래스를, 책임별로 나눠요.

아래는 **나쁜 예**예요. `Journal`이 *일기 내용 관리*도 하고 *저장/출력*까지 떠맡고 있어요.

```java
// ❌ 나쁜 예: 바뀔 이유가 2개 (내용 규칙 변경 / 저장 방식 변경)
class Journal {
    private ArrayList<String> entries = new ArrayList<>();
    void add(String text) { entries.add(text); }

    void saveToFile(String filename) {   // ← 이게 두 번째 책임!
        // 파일에 저장하는 코드...
    }
}
```

**할 일**
1. `Journal`에는 *일기 내용*과 관련된 것만 남겨요. (`add`, 그리고 내용을 글자로 돌려주는 `getText`)
2. 저장/출력 책임은 **새 클래스 `JournalSaver`** 로 옮겨요.

<details>
<summary>💡 힌트 보기</summary>

```java
import java.util.ArrayList;

class Journal {                    // 책임 1: 내용만
    private ArrayList<String> entries = new ArrayList<>();
    void add(String text) { entries.add(text); }

    String getText() {
        StringBuilder sb = new StringBuilder();
        for (String e : entries) sb.append("- ").append(e).append("\n");
        return sb.toString();
    }
}

class JournalSaver {               // 책임 2: 보여주기/저장
    void print(Journal j) {
        System.out.print(j.getText());
    }
}
```

`JournalSaver`가 `Journal`을 **인자로 받아** 쓰는 게 포인트예요. 이렇게 나누면 "저장 방식을 바꿀 때 `Journal`은 안 건드려도 돼요."

</details>

**확인**: `Journal`에는 저장 코드가 전혀 없고, 출력은 `JournalSaver`가 담당하면 성공이에요.

---

### Step 2. OCP — if-else 대신 인터페이스로 확장하기 (`Ocp.java`)

**목표**: 새 등급이 생겨도 **기존 코드를 수정하지 않고** 클래스만 추가해요.

```java
// ❌ 나쁜 예: 등급이 늘 때마다 이 메서드를 '수정'해야 함
class DiscountCalculator {
    int calc(String grade, int price) {
        if (grade.equals("GOLD"))      return price * 90 / 100;
        else if (grade.equals("VIP"))  return price * 80 / 100;
        else                           return price;
    }
}
```

**할 일**
1. `DiscountPolicy` 인터페이스를 만들어요. 메서드는 `int discount(int price);`
2. 등급별로 클래스를 따로 만들어요: `BasicDiscount`, `GoldDiscount`, `VipDiscount`.
3. (체감 포인트) 나중에 `SilverDiscount`를 추가할 때 **기존 코드를 한 줄도 안 고친다**는 걸 확인해요.

<details>
<summary>💡 힌트 보기</summary>

```java
interface DiscountPolicy {
    int discount(int price);
}

class BasicDiscount implements DiscountPolicy {
    public int discount(int price) { return price; }
}
class GoldDiscount implements DiscountPolicy {
    public int discount(int price) { return price * 90 / 100; }
}
class VipDiscount implements DiscountPolicy {
    public int discount(int price) { return price * 80 / 100; }
}
```

새 등급 = 새 `class` 하나 추가. **인터페이스를 쓰는 쪽(Main) 코드는 그대로**라는 게 OCP의 맛이에요.

</details>

**확인**: 세 등급이 각각 10000원에 대해 10000 / 9000 / 8000원을 돌려주면 성공이에요.

---

### Step 3. LSP — 억지 상속을 끊기 (`Lsp.java`)

**목표**: 부모 자리에 자식을 넣어도 **터지지 않는** 상속 구조를 만들어요.

```java
// ❌ 나쁜 예: 부모는 "날 수 있다"고 약속했는데 자식이 그 약속을 깸
class Bird {
    void fly() { System.out.println("훨훨 납니다"); }
}
class Penguin extends Bird {
    void fly() { throw new RuntimeException("펭귄은 못 날아요!"); } // 💥
}
```
이러면 `Bird b = new Penguin(); b.fly();` 가 예외로 터져요. 펭귄은 `Bird`를 **안전하게 대체하지 못한** 거예요.

**할 일**
1. `Bird`에는 **모든 새가 할 수 있는 것**만 둬요. (예: `eat()`)
2. `fly()`는 **날 수 있는 새**만 갖도록 중간 클래스 `FlyingBird`로 빼요.
3. `Sparrow`는 `FlyingBird`를, `Penguin`은 `Bird`를 상속하게 해요. (펭귄에겐 `swim()`을 줘도 좋아요.)

<details>
<summary>💡 힌트 보기</summary>

```java
class Bird {
    void eat() { System.out.println("냠냠 먹습니다"); }
}
class FlyingBird extends Bird {
    void fly() { System.out.println("훨훨 납니다"); }
}
class Sparrow extends FlyingBird { }   // 참새: 날 수 있음
class Penguin extends Bird {           // 펭귄: fly 자체가 없음
    void swim() { System.out.println("첨벙 헤엄칩니다"); }
}
```

이제 `Bird` 타입으로 다룰 땐 `eat()`만 호출하니, 어떤 자식이 와도 **절대 안 터져요.** 그게 LSP를 지킨 상태예요.

</details>

**확인**: `Bird` 배열에 참새·펭귄을 담아 `eat()`을 호출했을 때 둘 다 정상 동작하면 성공이에요.

---

### Step 4. ISP — 큰 인터페이스를 쪼개기 (`Isp.java`)

**목표**: 안 쓰는 메서드를 억지로 구현하지 않도록, 인터페이스를 기능별로 나눠요.

```java
// ❌ 나쁜 예: 인쇄만 하면 되는데 scan/fax까지 강제로 구현
interface Machine {
    void print();
    void scan();
    void fax();
}
class SimplePrinter implements Machine {
    public void print() { System.out.println("인쇄"); }
    public void scan()  { throw new RuntimeException("스캔 못 해요"); } // 빈 껍데기
    public void fax()   { throw new RuntimeException("팩스 못 해요"); }
}
```

**할 일**
1. `Machine`을 기능별로 쪼개요: `Printer`, `Scanner`, `Faxer` (각각 메서드 1개).
2. `SimplePrinter`는 `Printer`만 구현해요.
3. 인쇄+스캔이 되는 `SmartMachine`은 `Printer, Scanner`를 **둘 다** 구현해요.

<details>
<summary>💡 힌트 보기</summary>

```java
interface Printer { void print(); }
interface Scanner { void scan(); }   // java.util.Scanner와는 '이름만' 같은 별개예요
interface Faxer   { void fax();   }

class SimplePrinter implements Printer {
    public void print() { System.out.println("구형 프린터: 인쇄만 합니다"); }
}
class SmartMachine implements Printer, Scanner {
    public void print() { System.out.println("복합기: 인쇄"); }
    public void scan()  { System.out.println("복합기: 스캔"); }
}
```

이제 `SimplePrinter`엔 `throw`로 막은 빈 메서드가 **하나도 없어요.** 자바는 인터페이스를 여러 개 `implements` 할 수 있으니 가능한 구조예요.

⚠️ 이 파일에선 `java.util.Scanner`(콘솔 입력)를 `import` 하지 마세요. 같은 이름이라 충돌해요.

</details>

**확인**: `SimplePrinter`에 구현하지 않은(=강제당한) 메서드가 없으면 성공이에요.

---

### Step 5. DIP — 구체 클래스 대신 추상에 의존하기 (`Dip.java`)

**목표**: 상위 모듈이 인터페이스에 의존하고, 실제 구현체는 **주입**받게 해요.

```java
// ❌ 나쁜 예: 알림 서비스가 EmailSender를 '직접' new 함
class NotificationService {
    private EmailSender sender = new EmailSender();   // 구체 클래스에 못 박힘
    void notifyUser(String msg) { sender.send(msg); }
}
```
SMS로 바꾸려면 이 서비스 코드를 고쳐야 해요. 상위 모듈이 하위(구체)에 끌려다니는 상태예요.

**할 일**
1. `MessageSender` 인터페이스(`void send(String msg);`)를 만들어요.
2. `EmailSender`, `SmsSender`가 이를 구현하게 해요.
3. `NotificationService`는 `MessageSender`를 **필드로 두고, 생성자로 주입**받게 해요.

<details>
<summary>💡 힌트 보기</summary>

```java
interface MessageSender {
    void send(String msg);
}
class EmailSender implements MessageSender {
    public void send(String msg) { System.out.println("[이메일] " + msg); }
}
class SmsSender implements MessageSender {
    public void send(String msg) { System.out.println("[SMS] " + msg); }
}

class NotificationService {
    private MessageSender sender;                 // 추상에 의존
    NotificationService(MessageSender sender) {   // 생성자 주입
        this.sender = sender;
    }
    void notifyUser(String msg) { sender.send(msg); }
}
```

이제 이메일↔SMS 교체는 **`new NotificationService(new SmsSender())`** 처럼 바깥에서 결정해요. 서비스 내부 코드는 그대로! 그게 "의존관계가 역전됐다"는 뜻이에요.

</details>

**확인**: 같은 `NotificationService`에 `EmailSender`/`SmsSender`를 각각 주입해 서로 다른 출력이 나오면 성공이에요.

---

### Step 6. 마무리 — Main에서 5원칙을 모두 실행 (`Main.java`)

**목표**: 지금까지 만든 좋은 설계들을 한자리에서 동작시켜요.

**할 일**
1. Step 1~5에서 만든 클래스들을 `Main`의 `main`에서 차례로 사용해요.
2. OCP·LSP·DIP는 **배열/다형성**으로 묶어 호출하면 깔끔해요.

<details>
<summary>💡 힌트 보기</summary>

```java
public class Main {
    public static void main(String[] args) {
        // OCP: 인터페이스 배열로 묶어서 한 번에
        DiscountPolicy[] policies = { new BasicDiscount(), new GoldDiscount(), new VipDiscount() };
        String[] names = { "일반", "골드", "VIP" };
        for (int i = 0; i < policies.length; i++) {
            System.out.println(names[i] + " 회원 -> " + policies[i].discount(10000) + "원");
        }

        // LSP: Bird 타입으로 묶어도 안 터짐
        Bird[] birds = { new Sparrow(), new Penguin() };
        for (Bird b : birds) b.eat();

        // DIP: 구현체를 '주입'으로 교체
        new NotificationService(new EmailSender()).notifyUser("주문이 완료되었습니다");
        new NotificationService(new SmsSender()).notifyUser("주문이 완료되었습니다");
    }
}
```

SRP·ISP 부분도 같은 식으로 객체를 만들어 호출하면 돼요. **1번 섹션의 출력과 똑같이** 나오는지 맞춰보세요.

</details>

**확인**: `javac *.java` → `java -Dstdout.encoding=UTF-8 Main` 했을 때 1번 섹션의 출력이 그대로 나오면 **완성입니다!** 🎉

## 6. 학습 체크

- [ ] SRP: "이 클래스가 바뀔 이유가 2개 이상인가?"로 책임을 점검할 수 있다
- [ ] OCP: `if-else` 분기를 인터페이스+다형성으로 바꾸면 왜 "수정에 닫힌"지 설명할 수 있다
- [ ] LSP: 자식이 부모 약속(메서드)을 깨면 어떤 코드가 터지는지 예로 들 수 있다
- [ ] ISP: 인터페이스를 쪼개면 어떤 "빈 껍데기 메서드"가 사라지는지 안다
- [ ] DIP: "의존성 주입"이 왜 교체를 쉽게 만드는지 설명할 수 있다

## 7. 최종 완성 체크리스트

- [ ] `Srp.java` — `Journal`에 저장/출력 코드가 없다
- [ ] `Ocp.java` — 등급 추가가 새 클래스만으로 가능하다
- [ ] `Lsp.java` — `Bird` 타입으로 다룰 때 예외가 안 난다
- [ ] `Isp.java` — `throw`로 막은 빈 메서드가 없다
- [ ] `Dip.java` — `NotificationService`가 `new EmailSender()`를 직접 호출하지 않는다
- [ ] `Main.java` — 1번 섹션과 같은 출력이 나온다

## 8. (선택) 도전 과제

1. OCP: `SilverDiscount`(15% 할인)를 추가해보고, **기존 파일은 한 줄도 안 고쳤는지** 확인해요.
2. DIP: 콘솔에 찍는 대신 "보낸 메시지를 리스트에 모아두는" `MockSender`를 만들어 주입해보세요. (테스트용 구현체 감 잡기)
3. ISP: `Faxer`까지 구현하는 `AllInOneMachine`을 만들고, `Printer`/`Scanner`/`Faxer` 타입으로 각각 받아 호출해보세요.
4. LSP ★: "정사각형은 직사각형이다"라는 상속이 왜 LSP를 깨뜨리는지(`setWidth` 문제) 조사해보세요. SOLID의 가장 유명한 함정이에요.
5. 통합 ★: 위 5개를 "온라인 주문" 같은 하나의 작은 시나리오로 엮어보세요. 어디서 자연스럽고 어디서 어색한지가, 원칙을 진짜 이해했는지 보여줘요.