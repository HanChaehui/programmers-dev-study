# 회원 관리에 저장 기능 더하기 (File I/O 영속성)

> 종합판 회원관리(인터페이스 + 등급 + enum + 매니저)에 **파일 저장**을 더합니다.
> 프로그램을 껐다 켜도 회원이 남도록, 회원 목록을 **파일에 저장하고 다시 불러와요.**
> 핵심은 **객체 ↔ 텍스트(CSV) 변환**과 **save/load**입니다.
> **아래 Step을 순서대로 따라가면, 영속성 있는 회원관리가 완성됩니다.**
>
> 💡 각 Step의 **힌트는 접혀 있어요.** 먼저 스스로 해보고, 막히면 "힌트 보기"를 펼치세요.

---

## 0. 먼저 알아둘 점

이 과제는 **종합판 회원관리**(`Member` 인터페이스 + `NormalMember`/`VipMember` + `PricePlan` enum + `MemberManager`)가 **이미 있다는 전제**예요. 거기에 **파일 저장/불러오기**만 더합니다.
- 저장 대상은 **회원 목록**이에요. (요금제는 이번엔 매 실행 다시 고릅니다. 요금제까지 저장하는 건 도전 과제로.)
- 모든 파일 작업은 **예외 처리(try-catch)** 가 필요해요.

---

## 1. 무엇을 만드나요?

기존 회원관리와 똑같이 동작하지만, **종료 후 다시 켜도 회원이 그대로 남아 있어요.**

```
(1차 실행) 회원 2명 추가 → 종료
(2차 실행) 시작하자마자 → 전체조회
[VIP] 홍길동 / hong@a.com / 010-1111 (혜택: 10% 할인 + 무료배송)
[일반] 김철수 / kim@b.com / 010-2222 (혜택: 기본 서비스)
```

> 메모리(List)에만 있던 1편과 달리, 이번엔 데이터가 **파일에 남아** 프로그램을 꺼도 유지됩니다.

---

## 2. 학습 목표

| 개념 | 어디서 배우나 |
|------|------|
| 직렬화(객체 → 텍스트) | 회원 1명 = `"등급,이름,이메일,연락처"` 한 줄 |
| 저장(save) | 전체 회원을 파일에 쓰기 |
| 불러오기(load) | 파일을 읽어 객체로 복원 (등급별 분기) |
| 다형성 복원 | 첫 칸(등급)으로 Normal/Vip 되살리기 |
| 예외 처리 | 파일 작업의 try-catch |

---

## 3. 핵심 개념

### (1) 객체 ↔ 텍스트 (직렬화)
회원 객체를 파일에 그대로 넣을 순 없어요. 그래서 **한 줄 문자열(CSV)** 로 바꿔 저장합니다.
```
홍길동(VIP) 객체  →  "VIP,홍길동,hong@a.com,010-1111"   (저장)
"VIP,홍길동,..." →  VipMember 객체                      (불러오기)
```
저장할 땐 객체 → 문자열, 불러올 땐 문자열 → 객체. 이 변환이 핵심이에요.

### (2) 저장 전략 — 전체 덮어쓰기
회원이 추가·수정·삭제될 때마다 **전체 목록을 파일에 다시 씁니다(덮어쓰기).**
> 가계부 File편은 `append`(이어쓰기)였지만, 여기선 **덮어쓰기**예요. 수정·삭제를 반영하려면 전체를 다시 써야 하니까요. (`new FileWriter(file)` — `true` 없이)

### (3) 불러오기 + 등급 복원
파일의 각 줄을 콤마로 쪼개고, **첫 칸(등급)** 을 보고 알맞은 구현체로 되살립니다.
```java
Member m = grade.equals("VIP")
    ? new VipMember(name, email, phone)
    : new NormalMember(name, email, phone);
```
이렇게 해야 불러온 뒤에도 `printInfo()`가 등급별로 다르게 나와요(다형성 복원).

### (4) 예외 처리
`FileWriter`/`BufferedReader`는 `IOException`이 날 수 있어 **반드시 try-catch**.

---

## 4. 파일 구조

| 파일 | 역할 |
|------|------|
| `Member.java` (인터페이스) | 기존 + **`toFileString()`**(CSV 한 줄) 추가 |
| `NormalMember` / `VipMember` | 기존 그대로 |
| `PricePlan.java` (enum) | 기존 그대로 |
| `MemberManager.java` | 기존 + **`save()` / `load()`** 추가 |
| `Main.java` | 기존 그대로 (매니저가 알아서 저장/불러옴) |
| `members.txt` | **데이터 저장 파일** (자동 생성) |

> import 추가: `java.io.*`

---

## 5. Step by Step

각 Step에 **목표 / 할 일 / 힌트(접힘) / 확인**이 있습니다.

---

### Step 1. 회원을 한 줄 문자열로 (`Member.java`)

**목표**: 회원 객체를 `"등급,이름,이메일,연락처"` 형태의 CSV 한 줄로 바꾸는 메서드를 추가한다.

**할 일**: `Member` 인터페이스에 `default String toFileString()` 추가.

<details>
<summary>💡 힌트 보기</summary>

```java
// Member 인터페이스 안에 추가
default String toFileString() {
    return getGrade() + "," + getName() + "," + getEmail() + "," + getPhone();
}
```

`getGrade()`/`getName()` 등 이미 있는 메서드만 써서 만들 수 있으니 `default`로 둡니다.

</details>

**확인**: VIP 회원의 `toFileString()`이 `"VIP,홍길동,hong@a.com,010-1111"`처럼 나오면 OK.

---

### Step 2. 저장 — save() (`MemberManager.java`)

**목표**: 현재 회원 전체를 파일에 쓴다(덮어쓰기).

**할 일**
1. 저장 파일명을 상수로 (`members.txt`).
2. 모든 회원을 `toFileString()`으로 한 줄씩 쓴다.

<details>
<summary>💡 힌트 보기</summary>

```java
import java.io.*;

// 필드
private static final String FILE = "members.txt";

// 메서드
public void save() {
    try (FileWriter fw = new FileWriter(FILE)) {   // true 없음 = 덮어쓰기
        for (Member m : members) {
            fw.write(m.toFileString() + "\n");
        }
    } catch (IOException e) {
        System.out.println("저장 오류: " + e.getMessage());
    }
}
```

`append`가 아니라 **덮어쓰기**인 게 포인트예요. 매번 전체를 다시 써서 수정·삭제가 반영됩니다.

</details>

**확인**: 호출 후 `members.txt`에 회원들이 한 줄씩 들어가면 OK.

---

### Step 3. 불러오기 — load() (`MemberManager.java`)

**목표**: 파일을 읽어 회원 객체로 복원한다. 등급에 맞게 Normal/Vip로 되살린다.

**할 일**
1. 파일이 없으면(처음 실행) 그냥 종료.
2. 한 줄씩 읽어 콤마로 쪼개고, 등급으로 분기해 객체를 만들어 `members`에 추가.

<details>
<summary>💡 힌트 보기</summary>

```java
public void load() {
    File file = new File(FILE);
    if (!file.exists()) return;   // 처음 실행이면 파일이 없음

    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        String line;
        while ((line = br.readLine()) != null) {
            if (line.isBlank()) continue;
            String[] p = line.split(",");        // [등급, 이름, 이메일, 연락처]
            String grade = p[0], name = p[1], email = p[2], phone = p[3];

            Member m = grade.equals("VIP")
                    ? new VipMember(name, email, phone)
                    : new NormalMember(name, email, phone);
            members.add(m);
        }
    } catch (IOException e) {
        System.out.println("불러오기 오류: " + e.getMessage());
    }
}
```

`split(",")`로 쪼갠 뒤 첫 칸으로 등급을 판단해 객체를 되살려요.

</details>

**확인**: 미리 만들어둔 `members.txt`가 있으면, load 후 `printAll()`에 그 회원들이 나오면 OK.

---

### Step 4. 시작할 때 불러오기 (`MemberManager.java`)

**목표**: 매니저가 만들어질 때 자동으로 기존 회원을 불러온다.

**할 일**: 생성자에서 `load()` 호출.

<details>
<summary>💡 힌트 보기</summary>

```java
public MemberManager(int capacity) {
    this.capacity = capacity;
    load();   // 시작 시 파일에서 회원 복원
}
```

</details>

**확인**: 프로그램을 켜자마자 전체조회하면, 지난번에 넣은 회원이 보이면 성공.

---

### Step 5. 변경될 때마다 저장 (`MemberManager.java`)

**목표**: 추가·수정·삭제 후 파일을 최신 상태로 유지한다.

**할 일**: `add` / `update` / `delete` 의 **성공 지점에서 `save()`** 를 호출.

<details>
<summary>💡 힌트 보기</summary>

```java
public void add(Member m) {
    members.add(m);
    save();             // 추가 후 저장
}

public boolean update(String email, String name, String newEmail, String phone) {
    Member m = findByEmail(email);
    if (m == null) return false;
    m.update(name, newEmail, phone);
    save();             // 수정 후 저장
    return true;
}

public boolean delete(String email) {
    Member m = findByEmail(email);
    if (m == null) return false;
    members.remove(m);
    save();             // 삭제 후 저장
    return true;
}
```

변경이 일어날 때마다 전체를 다시 써서 파일이 항상 메모리와 일치하게 합니다.

</details>

**확인**: 추가/수정/삭제 후 `members.txt`를 열어보면 즉시 반영돼 있으면 OK.

---

### Step 6. 마무리 — 껐다 켜기 테스트 (완성!)

**점검 항목**
- [ ] 회원을 추가하고 **종료한 뒤, 다시 켜면** 그 회원이 남아 있는가?
- [ ] 불러온 VIP/일반 회원이 등급별로 다르게 출력되는가? (다형성 복원)
- [ ] 수정·삭제가 파일에 바로 반영되는가?
- [ ] 처음 실행(파일 없음)에도 오류 없이 시작되는가?
- [ ] 파일 작업에 try-catch가 있는가?

여기까지 통과하면 **영속성 회원관리 완성입니다!** 🎉

---

## 6. 학습 체크

- [ ] 객체를 CSV 한 줄로 바꿔 저장했다(직렬화)
- [ ] 파일을 읽어 등급에 맞는 객체로 복원했다
- [ ] 변경 시마다 전체를 덮어써 파일을 동기화했다
- [ ] "append vs 덮어쓰기"를 언제 쓰는지 구분할 수 있다
- [ ] 파일 작업에 예외 처리를 했다

---

## 7. 최종 완성 체크리스트

- [ ] `Member`에 `toFileString()` 추가
- [ ] `MemberManager`에 `save()` / `load()` 추가 + 생성자 load + 변경 시 save
- [ ] `members.txt`가 자동 생성·갱신됨
- [ ] 껐다 켜도 회원이 유지됨

---

## 8. (선택) 도전 과제

1. **요금제도 저장**: 파일 첫 줄에 요금제를 적어, 다시 켤 때 요금제까지 복원 (그러면 시작 시 요금제 선택을 건너뜀)
2. **콤마 안전 처리**: 이름·이메일에 콤마(,)가 들어가면 `split`이 깨져요. 구분자를 바꾸거나 CSV 이스케이프 처리
3. **종료 시 한 번만 저장**: 변경마다 저장하지 말고, 7번(종료) 때 한 번만 저장하도록 바꿔 성능 비교
4. **객체 직렬화 사용**: `Serializable` + `ObjectOutputStream`/`ObjectInputStream`으로 텍스트 변환 없이 객체째 저장
5. **백업**: 저장 전에 기존 `members.txt`를 `members_backup.txt`로 복사해두기
6. **CSV → JSON**: 저장 형식을 JSON으로 바꿔보기 (라이브러리 없이 간단히)