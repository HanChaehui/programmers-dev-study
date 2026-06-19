# 회원 관리에 DB 연결하기 (JDBC)

> File I/O로 저장하던 회원관리를, 이번엔 **데이터베이스(MySQL)** 에 저장하도록 바꿉니다.
> 핵심 전환은 **"파일을 DB가 대체한다"** 예요. 메모리 `List`와 `save()`/`load()`가 사라지고,
> 추가·조회·수정·삭제가 각각 **SQL 한 방(INSERT/SELECT/UPDATE/DELETE)** 이 됩니다.
> **아래 Step을 순서대로 따라가면, DB로 동작하는 회원관리가 완성됩니다.**
>
> 💡 각 Step의 **힌트는 접혀 있어요.** 먼저 스스로 해보고, 막히면 "힌트 보기"를 펼치세요.

---

## 0. 먼저 알아둘 점

이 과제는 두 가지를 **이미 배웠다는 전제**예요.
1. **File I/O 회원관리**(Member 인터페이스 + 등급 + enum + 매니저)
2. **JDBC 기초**(`Connection`, `PreparedStatement`, `ResultSet`, try-with-resources)

실행하려면 환경이 필요해요: **MySQL 서버**, `java_basic` 데이터베이스, `member` 테이블, 그리고 **JDBC 드라이버**(`mysql-connector-j`)가 프로젝트에 추가돼 있어야 합니다. (코드만으로는 실행되지 않고, DB가 켜져 있어야 동작해요.)

> 가장 큰 개념 변화: **DB가 곧 저장소라서, 파일처럼 통째로 읽고 쓰지 않아요.** 매 작업이 필요한 행(row)만 SQL로 직접 다룹니다. 그래서 `load()`/`save()`가 통째로 사라져요.

---

## 1. 무엇을 만드나요?

기능은 똑같아요(추가·조회·수정·삭제). 단, 저장소가 **파일 → 데이터베이스**로 바뀝니다.

```
[1]추가 → DB에 INSERT
[2]조회 → DB에서 SELECT
[5]수정 → DB에 UPDATE
[6]삭제 → DB에서 DELETE
```

프로그램을 꺼도 데이터는 DB에 남아요. (File I/O와 같은 영속성이지만, 방식이 DB로 바뀐 것)

---

## 2. 학습 목표

| 개념 | 어디서 배우나 |
|------|------|
| File → DB 전환 | 메모리/파일 대신 DB가 저장소 |
| 테이블 설계 | `member` 테이블 (grade 포함) |
| CRUD ↔ SQL | add/find/update/delete = INSERT/SELECT/UPDATE/DELETE |
| PreparedStatement | `?` 자리표시자 + 타입별 set |
| ResultSet → 객체 | 조회 결과를 등급에 맞는 객체로 복원 |
| executeUpdate / executeQuery | 변경 / 조회 구분 |

---

## 3. 핵심 개념

### (1) ⭐ File I/O → DB, 무엇이 바뀌나
| | File I/O 버전 | DB 버전 |
|---|---|---|
| 저장소 | `members.txt` | `member` 테이블 |
| 메모리 | `List<Member>`에 전체 로드 | **메모리에 안 들고 있음** |
| 시작 시 | `load()`로 파일 읽기 | **불필요** (DB에 이미 있음) |
| 변경 시 | `save()`로 전체 덮어쓰기 | **해당 행만** SQL로 |
| 추가 | `list.add` + save | `INSERT` |
| 조회 | list 순회 | `SELECT` |
| 수정 | 객체 수정 + save | `UPDATE` |
| 삭제 | `list.remove` + save | `DELETE` |

> 즉, `MemberManager`는 이제 **DB와 대화하는 창구(DAO)** 가 됩니다. 회원을 메모리에 쌓아두지 않고, 매번 DB에 물어봐요.

### (2) member 테이블
회원의 등급/이름/이메일/연락처를 컬럼으로. 이메일은 중복 금지라 `UNIQUE`.
```sql
CREATE TABLE member (
    id    INT AUTO_INCREMENT PRIMARY KEY,
    grade VARCHAR(10)  NOT NULL,   -- "일반" 또는 "VIP"
    name  VARCHAR(50)  NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20)
);
```

### (3) JDBC 흐름 (배운 그대로)
```
Connection 얻기 → PreparedStatement에 SQL + ? 채우기 → 실행 → (조회면 ResultSet)
```
- 변경(INSERT/UPDATE/DELETE) → `executeUpdate()` (바뀐 행 수 반환)
- 조회(SELECT) → `executeQuery()` (`ResultSet` 반환)

### (4) ResultSet → 객체 복원 (File I/O의 load와 같은 원리!)
조회 결과 한 행의 `grade`를 보고 알맞은 구현체로 되살려요. File I/O에서 CSV 한 줄을 객체로 바꾸던 것과 **똑같은 분기**, 데이터 출처만 "파일 줄 → DB 행"으로 바뀐 거예요.
```java
Member m = grade.equals("VIP")
    ? new VipMember(name, email, phone)
    : new NormalMember(name, email, phone);
```

---

## 4. 파일 구조

| 파일 | 역할 |
|------|------|
| `Member` / `NormalMember` / `VipMember` | **그대로** (변경 없음) |
| `PricePlan` (enum) | **그대로** |
| `MemberManager.java` | **DB 버전으로 교체** (List/save/load 제거, SQL로) |
| `Main.java` | 거의 그대로 (save/load 호출만 제거) |
| `member` 테이블 | DB에 미리 생성 |

> import: `java.sql.*`

---

## 5. Step by Step

각 Step에 **목표 / 할 일 / 힌트(접힘) / 확인**이 있습니다.

---

### Step 1. DB에 테이블 만들기 (SQL)

**목표**: 회원을 저장할 `member` 테이블을 만든다. (Java 아님, DB에서 실행)

<details>
<summary>💡 힌트 보기</summary>

```sql
CREATE TABLE member (
    id    INT AUTO_INCREMENT PRIMARY KEY,
    grade VARCHAR(10)  NOT NULL,
    name  VARCHAR(50)  NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20)
);
```

`email`에 `UNIQUE`를 걸면 DB 차원에서도 중복을 막아줘요.

</details>

**확인**: `DESC member;` 로 컬럼이 보이면 OK.

---

### Step 2. DB 연결 — connection() (`MemberManager.java`)

**목표**: 매니저를 DB 버전으로 바꾸고, 연결 메서드를 둔다. (배운 JDBC 그대로)

**할 일**: `List`/`save`/`load`를 걷어내고, `connection()` 추가. 생성자는 `capacity`만 받는다(load 호출 없음).

<details>
<summary>💡 힌트 보기</summary>

```java
import java.sql.*;

public class MemberManager {
    private final int capacity;
    public MemberManager(int capacity) { this.capacity = capacity; }   // load() 없음!

    private Connection connection() {
        String url = "jdbc:mysql://localhost:3306/java_basic";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, "root", "1234");
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int capacity() { return capacity; }
}
```

이제 회원을 메모리에 안 들고 있어요. 저장소는 DB입니다.

</details>

**확인**: 컴파일 OK. (실행은 DB가 켜져 있어야)

---

### Step 3. 추가 — add() → INSERT (`MemberManager.java`)

**목표**: 회원을 DB에 INSERT 한다.

<details>
<summary>💡 힌트 보기</summary>

```java
public void add(Member m) {
    String sql = "INSERT INTO member (grade, name, email, phone) VALUES (?, ?, ?, ?)";
    try (Connection conn = connection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, m.getGrade());
        ps.setString(2, m.getName());
        ps.setString(3, m.getEmail());
        ps.setString(4, m.getPhone());
        ps.executeUpdate();
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
}
```

`m.getGrade()`가 `"일반"`/`"VIP"`를 주니, 그걸 그대로 grade 컬럼에 넣어요. (나중에 조회할 때 이 값으로 등급을 복원)

</details>

**확인**: 추가 후 DB에서 `SELECT * FROM member;` 하면 행이 보이면 OK.

---

### Step 4. 조회 결과 → 객체 복원 헬퍼 (`MemberManager.java`)

**목표**: `ResultSet` 한 행을 `Member` 객체로 되살리는 공통 메서드를 만든다. (여러 조회에서 재사용)

<details>
<summary>💡 힌트 보기</summary>

```java
private Member toMember(ResultSet rs) throws SQLException {
    String grade = rs.getString("grade");
    String name  = rs.getString("name");
    String email = rs.getString("email");
    String phone = rs.getString("phone");
    return grade.equals("VIP")
            ? new VipMember(name, email, phone)
            : new NormalMember(name, email, phone);
}
```

File I/O의 `load()`에서 CSV를 쪼개 객체로 만들던 것과 **같은 분기**예요. 출처만 DB 행으로 바뀌었죠.

</details>

**확인**: 컴파일 OK.

---

### Step 5. 조회 — findByEmail / findByName / printAll → SELECT

**목표**: 이메일·이름으로 한 명 찾고, 전체를 출력한다.

<details>
<summary>💡 힌트 보기</summary>

```java
public Member findByEmail(String email) {
    String sql = "SELECT grade, name, email, phone FROM member WHERE email = ?";
    try (Connection conn = connection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, email);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return toMember(rs);
        }
    } catch (SQLException e) { throw new RuntimeException(e); }
    return null;   // 없으면 null
}

public Member findByName(String name) {
    String sql = "SELECT grade, name, email, phone FROM member WHERE name = ?";
    // findByEmail과 같은 구조, WHERE만 name으로
    ...
}

public void printAll() {
    String sql = "SELECT grade, name, email, phone FROM member";
    try (Connection conn = connection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        boolean empty = true;
        while (rs.next()) { toMember(rs).printInfo(); empty = false; }
        if (empty) System.out.println("등록된 회원이 없습니다.");
    } catch (SQLException e) { throw new RuntimeException(e); }
}
```

조회는 `executeQuery()` → `ResultSet`. 한 명은 `if(rs.next())`, 여러 명은 `while(rs.next())`.

</details>

**확인**: 추가한 회원이 조회/전체조회에 등급별로 출력되면 OK.

---

### Step 6. 정원 체크 — existsEmail / size → SELECT COUNT

**목표**: 중복 이메일·현재 인원을 DB에 물어본다.

<details>
<summary>💡 힌트 보기</summary>

```java
public boolean existsEmail(String email) {
    String sql = "SELECT COUNT(*) FROM member WHERE email = ?";
    try (Connection conn = connection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, email);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1) > 0;
        }
    } catch (SQLException e) { throw new RuntimeException(e); }
    return false;
}

public int size() {
    String sql = "SELECT COUNT(*) FROM member";
    try (Connection conn = connection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) { throw new RuntimeException(e); }
    return 0;
}

public boolean isFull() { return size() >= capacity; }
```

`COUNT(*)`의 결과는 첫 번째 컬럼이라 `rs.getInt(1)`로 꺼내요.

</details>

**확인**: 이미 있는 이메일을 추가하려 하면 막히고, `[현재 n/정원]`의 n이 DB 인원과 맞으면 OK.

---

### Step 7. 수정 / 삭제 → UPDATE / DELETE

**목표**: 이메일로 찾아 수정·삭제한다. 바뀐 행 수로 성공 여부를 판단한다.

<details>
<summary>💡 힌트 보기</summary>

```java
public boolean update(String email, String name, String newEmail, String phone) {
    String sql = "UPDATE member SET name = ?, email = ?, phone = ? WHERE email = ?";
    try (Connection conn = connection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, name);
        ps.setString(2, newEmail);
        ps.setString(3, phone);
        ps.setString(4, email);          // WHERE는 기존 이메일
        return ps.executeUpdate() > 0;   // 바뀐 행이 있으면 성공
    } catch (SQLException e) { throw new RuntimeException(e); }
}

public boolean delete(String email) {
    String sql = "DELETE FROM member WHERE email = ?";
    try (Connection conn = connection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, email);
        return ps.executeUpdate() > 0;
    } catch (SQLException e) { throw new RuntimeException(e); }
}
```

`executeUpdate()`는 바뀐 행 수를 돌려줘요. `0`이면 해당 이메일이 없었다는 뜻이라 `false`.

</details>

**확인**: 수정·삭제 후 DB에 바로 반영되면 OK.

---

### Step 8. Main 정리 + 마무리 (완성!)

**목표**: Main에서 `save()`/`load()` 호출을 없앤다. (이제 매 작업이 DB에 바로 반영되니 불필요)

**할 일**: 종료(7번)에서 `save(...)` 호출 제거. 나머지 메뉴 로직은 그대로.

**점검 항목**
- [ ] `member` 테이블을 만들었는가?
- [ ] add/find/update/delete가 INSERT/SELECT/UPDATE/DELETE로 동작하는가?
- [ ] 조회 결과가 등급별로 복원돼 출력되는가? (`toMember`)
- [ ] `?` 자리표시자(PreparedStatement)를 썼는가?
- [ ] Main에서 load/save 호출이 사라졌는가?

여기까지 통과하면 **DB 회원관리 완성입니다!** 🎉

---

## 6. 학습 체크

- [ ] DB가 파일/메모리를 대체함을 이해했다
- [ ] CRUD를 SQL(INSERT/SELECT/UPDATE/DELETE)로 매핑했다
- [ ] PreparedStatement + `?`로 값을 안전하게 넣었다
- [ ] ResultSet을 등급에 맞는 객체로 복원했다
- [ ] executeUpdate(변경)와 executeQuery(조회)를 구분했다

---

## 7. 최종 완성 체크리스트

- [ ] `member` 테이블 생성
- [ ] `MemberManager` — connection + INSERT/SELECT/UPDATE/DELETE + toMember
- [ ] `Member`/등급/enum은 그대로 재사용
- [ ] `Main`에서 save/load 제거
- [ ] 추가 → 조회 → 수정 → 삭제가 DB에 반영됨

---