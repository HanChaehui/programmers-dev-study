# 로그인 게시판 만들기 (JDBC · DAO · DTO 종합 프로젝트)

> 지금까지 배운 걸 모두 모은 **종합 프로젝트**입니다. 로그인하는 사용자가 글을 쓰고/고치고/지우는 게시판을 만들어요.
> 핵심은 **계층을 나누는 것**입니다 — 화면·흐름(Impl), 데이터 접근(DAO), 데이터 운반(DTO)을 분리합니다.
> **아래 Step을 순서대로 따라가면, 로그인 게시판이 완성됩니다.**
>
> 💡 각 Step의 **힌트는 접혀 있어요.** 먼저 스스로 해보고, 막히면 "힌트 보기"를 펼치세요.

---

## 0. 먼저 알아둘 점

이 과제는 **JDBC 기초**(Connection·PreparedStatement·ResultSet)와 **인터페이스**를 배웠다는 전제예요. 실행하려면 **MySQL + `notice` 데이터베이스 + 테이블 2개 + JDBC 드라이버**가 필요합니다.

> 학습용이라 **단순화한 부분**이 있어요: 비밀번호를 평문으로 저장하고, 메서드마다 `Scanner`를 새로 만들고, 입력에 `nextInt`/`nextLine`을 섞어 씁니다. 실무에선 위험하거나 버그가 될 수 있는데, 이건 **도전 과제에서 개선**합니다. 지금은 "계층을 나눠 동작하게 만들기"에 집중하세요.

---

## 1. 무엇을 만드나요?

로그인 기반 게시판입니다.

```
=========================== [My Notice] ===========================
홍길동(hong) 님! 환영합니다.
[1]로그인 [2]회원가입 [3]글목록보기 [4]글등록 [5]글수정 [6]글삭제
[7]로그아웃 [8]회원탈퇴 [9]프로그램종료
==================================================================
```

- 회원가입/로그인 → 로그인하면 "환영합니다"로 바뀌고, 내 이름이 보임
- 글 등록/목록/수정/삭제 (수정·삭제는 **내 글만**)
- 로그아웃 / 회원 탈퇴

---

## 2. 학습 목표

| 개념 | 어디서 배우나 |
|------|------|
| 계층 분리 | Impl(화면·흐름) ↔ DAO(데이터 접근) |
| DTO | 로그인 결과를 한 덩어리로 운반 |
| 로그인 세션 상태 | Impl이 status/userId/name을 기억 |
| 인가(권한) 체크 | 로그인해야 글쓰기 가능 |
| 두 테이블 다루기 | `user` + `content`, 관계 있음 |
| JDBC CRUD | 회원/글 INSERT·SELECT·UPDATE·DELETE |

---

## 3. 핵심 개념

### (1) ⭐ 계층(layer)을 나눈다
역할을 셋으로 나눠요. 각자 한 가지만 책임집니다.
```
Start            : 메뉴를 띄우고 번호에 따라 기능 호출 (흐름)
  ↓
NoticeImpl       : 입력 받기 / 결과 출력 / 로그인 상태 관리 / DAO 호출 (화면·서비스)
  ↓
NoticeDAO        : 오직 SQL — DB 연결·쿼리·결과 반환 (데이터 접근)
  ↓
DB (user, content 테이블)
```
- **Impl은 SQL을 모르고**, DAO에게 시킵니다. **DAO는 화면을 모르고**, 데이터만 다룹니다.
- 이렇게 나누면 DB를 바꿔도 Impl은 안 바뀌고, 화면을 바꿔도 DAO는 안 바뀌어요. **역할이 명확하고 변경에 강합니다.**

### (2) DTO — 데이터를 실어 나르는 상자
로그인 결과는 단순 성공/실패가 아니라 **세 가지**예요.
- 아이디가 없음 → `null`
- 아이디는 있는데 비번 틀림 → 상태만 false
- 성공 → 상태 true + 아이디 + 이름

`boolean` 하나로는 이름·아이디를 같이 못 돌려줘요. 그래서 **여러 값을 묶은 `SignInResponseDTO`** 로 계층 사이를 건너게 합니다.

### (3) 로그인 세션 상태
`NoticeImpl`이 `status`(로그인 여부) / `userId` / `name`을 **필드로 기억**해요. 메뉴 루프가 도는 동안 같은 Impl 객체를 쓰니, 한 번 로그인하면 상태가 유지됩니다. 로그인하면 `setUserInfo(true, ...)`, 로그아웃하면 `setUserInfo(false, null, null)`.

### (4) 인가(권한) 체크
글쓰기·수정·삭제는 로그인해야 가능해요. 각 기능 앞에서 `checkSignIn()`으로 "로그인 했나?"를 확인하고, 안 했으면 막습니다.

---

## 4. 파일 구조

| 파일 | 역할 | 계층 |
|------|------|------|
| `Notice.java` (인터페이스) | 게시판 기능 선언 | |
| `NoticeImpl.java` | 화면·입력·흐름·로그인상태 | 서비스 |
| `NoticeDAO.java` | SQL (회원/글 CRUD) | 데이터 접근 |
| `SignInResponseDTO.java` | 로그인 결과 운반 | DTO |
| `Start.java` | main 메뉴 루프 | 실행 |
| `user`, `content` 테이블 | DB |

> import: `java.sql.*`, `java.util.*`

---

## 5. Step by Step

각 Step에 **목표 / 할 일 / 힌트(접힘) / 확인**이 있습니다.

---

### Step 1. DB 테이블 2개 만들기 (SQL)

**목표**: 회원(`user`)과 글(`content`) 테이블을 만든다. 글은 누가 썼는지(`user_id`)를 가진다.

<details>
<summary>💡 힌트 보기</summary>

```sql
CREATE TABLE user (
    user_id  VARCHAR(50)  PRIMARY KEY,
    password VARCHAR(100) NOT NULL,
    name     VARCHAR(50)  NOT NULL
);

CREATE TABLE content (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    user_id  VARCHAR(50)  NOT NULL,
    content  TEXT         NOT NULL,
    created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP   -- 글 쓴 시각 자동 기록
);
```

`content.user_id`는 "이 글의 주인"이에요. `created`에 DEFAULT를 주면 글 등록 시 시각이 자동으로 채워집니다.

</details>

**확인**: `DESC user;`, `DESC content;`로 컬럼이 보이면 OK.

---

### Step 2. 뼈대 — 인터페이스 + 메뉴 루프

**목표**: 기능은 비어 있어도 메뉴가 반복 출력되고 `9`로 종료되게 만든다.

**할 일**: `Notice` 인터페이스 선언 → `NoticeImpl`에 빈 메서드 → `Start`에서 메뉴 루프.

<details>
<summary>💡 힌트 보기</summary>

```java
public interface Notice {
    int printMenu();
    void signUp();   void signIn();
    void newNotice(); void getList();
    void updateNotice(); void deleteNotice();
    void signOut();  void leave();
}
```
```java
// Start
Notice notice = new NoticeImpl();
while (true) {
    int choice = notice.printMenu();
    switch (choice) {
        case 1: notice.signIn(); break;
        // 2~8 ...
        case 9: System.out.println("다음에 또 만나요!"); return;
    }
}
```

`printMenu()`는 메뉴를 출력하고 입력 번호를 반환해요. (지금은 `NoticeImpl`의 나머지 메서드는 빈 몸통)

</details>

**확인**: 메뉴가 반복되고 9를 누르면 종료되면 OK.

---

### Step 3. 데이터 접근 골격 — NoticeDAO + DB 연결

**목표**: SQL을 담당할 `NoticeDAO`를 만들고 연결 메서드를 둔다. `NoticeImpl`이 DAO를 갖게 한다.

<details>
<summary>💡 힌트 보기</summary>

```java
public class NoticeDAO {
    private Connection getConnection() {
        String url = "jdbc:mysql://localhost:3306/notice";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, "root", "1234");
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
```
```java
// NoticeImpl 필드
private NoticeDAO noticeDAO = new NoticeDAO();
private boolean status;     // 로그인 여부
private String userId, name;
```

Impl은 DAO를 하나 들고서, 데이터가 필요할 때마다 DAO에게 시킵니다.

</details>

**확인**: 컴파일 OK.

---

### Step 4. 회원가입 — checkUserId + sigupExc + signUp

**목표**: 아이디 중복을 확인하고, 새 회원을 `user` 테이블에 INSERT 한다.

**할 일**
1. (DAO) `checkUserId(userId)` → `SELECT COUNT(*)`로 있으면 true.
2. (DAO) `sigupExc(...)` → `INSERT INTO user`.
3. (Impl) `signUp()` → 입력받고 → 중복이면 안내, 아니면 가입.

<details>
<summary>💡 힌트 보기</summary>

```java
// DAO
public boolean checkUserId(String userId) {
    String sql = "SELECT COUNT(*) FROM user WHERE user_id = ?";
    // executeQuery → rs.next() → rs.getInt(1) > 0
}
public boolean sigupExc(String userId, String password, String name) {
    String sql = "INSERT INTO user (user_id, password, name) VALUES (?, ?, ?)";
    // executeUpdate() > 0
}
```
```java
// Impl
public void signUp() {
    // userId/password/name 입력
    if (noticeDAO.checkUserId(userId)) { System.out.println("이미 가입된 사용자입니다."); return; }
    if (noticeDAO.sigupExc(userId, password, name)) System.out.println("회원가입 완료!");
}
```

**입력과 DB가 나뉜 게 보이나요?** Impl은 입력/안내, DAO는 SQL. 역할이 갈립니다.

</details>

**확인**: 가입 후 DB `user` 테이블에 행이 생기고, 같은 아이디로 또 가입하면 막히면 OK.

---

### Step 5. 로그인 + 세션 — DTO + signInExc + signIn

**목표**: 로그인 결과를 DTO로 받아, 성공이면 세션 상태를 켠다.

**할 일**
1. `SignInResponseDTO`(status, userId, name) 만들기.
2. (DAO) `signInExc(userId, password)` → 아이디로 SELECT → 없으면 null, 비번 틀리면 status=false DTO, 맞으면 성공 DTO.
3. (Impl) `signIn()` → DTO를 받아 분기 → 성공 시 `setUserInfo(true, id, name)`.

<details>
<summary>💡 힌트 보기</summary>

```java
// DTO
public class SignInResponseDTO {
    private boolean status; private String userId, name;
    // 생성자 + getter(isStatus/getUserId/getName)
}
```
```java
// DAO
public SignInResponseDTO signInExc(String userId, String password) {
    String sql = "SELECT user_id, name, password FROM user WHERE user_id = ?";
    // rs.next()가 false → return null (아이디 없음)
    // 비번 불일치 → return new SignInResponseDTO(false, null, null)
    // 일치 → return new SignInResponseDTO(true, id, name)
}
```
```java
// Impl
public void signIn() {
    SignInResponseDTO res = noticeDAO.signInExc(userId, password);
    if (res == null) { System.out.println("존재하지 않습니다."); return; }
    if (res.isStatus()) setUserInfo(true, res.getUserId(), res.getName());
    else System.out.println("비밀번호가 일치하지 않습니다.");
}
private void setUserInfo(boolean status, String userId, String name) {
    this.status = status; this.userId = userId; this.name = name;
}
```

**DTO의 쓸모**: null/false/true 세 갈래로 "아이디 없음 / 비번 틀림 / 성공"을 구분하고, 성공 시 이름까지 같이 가져와요.

</details>

**확인**: 로그인 성공 후 메뉴 윗줄이 "○○○ 님! 환영합니다."로 바뀌면 OK.

---

### Step 6. 글 등록 + 전체 목록 — newNotice + getList

**목표**: 로그인한 사람이 글을 쓰고(INSERT), 전체 글을 본다(SELECT). **로그인 안 했으면 글쓰기 차단.**

**할 일**
1. (Impl) `checkSignIn()` — 로그인 안 했으면 안내 후 false.
2. (DAO) `newNotice(userId, content)` → `INSERT INTO content`.
3. (DAO) `getList()` → 전체 글을 문자열 리스트로.

<details>
<summary>💡 힌트 보기</summary>

```java
// Impl
private boolean checkSignIn() {
    if (!status) { System.out.println("로그인을 먼저 해주세요."); return false; }
    return true;
}
public void newNotice() {
    if (!checkSignIn()) return;        // 인가 체크!
    // content 입력 → noticeDAO.newNotice(userId, content)
}
public void getList() {
    noticeDAO.getList().forEach(System.out::println);
}
```
```java
// DAO
public boolean newNotice(String userId, String content) {
    String sql = "INSERT INTO content (user_id, content) VALUES (?, ?)";  // created는 DEFAULT
}
public List<String> getList() {
    String sql = "SELECT id, user_id, content, created FROM content";
    // while(rs.next()) → "[id] userId - content - created" 를 리스트에 add
}
```

</details>

**확인**: 로그인 후 글 등록 → 목록보기에 그 글이 나오고, 로그아웃 상태에선 글쓰기가 막히면 OK.

---

### Step 7. 내 글 수정 / 삭제 — getListByUserId + update + delete

**목표**: **내가 쓴 글만** 보여주고, 그중 하나를 골라 수정/삭제한다.

**할 일**
1. (DAO) `getListByUserId(userId)` → 내 글만 SELECT.
2. (DAO) `updateNotice(id, content)` → UPDATE (수정 시각도 갱신).
3. (DAO) `deleteNotice(id)` → DELETE.
4. (Impl) 내 글 목록을 보여주고 번호 입력받아 처리.

<details>
<summary>💡 힌트 보기</summary>

```java
// DAO
public List<String> getListByUserId(String userId) {
    String sql = "SELECT id, user_id, content, created FROM content WHERE user_id = ?";
}
public boolean updateNotice(int id, String content) {
    String sql = "UPDATE content SET content = ?, created = ? WHERE id = ?";
    // setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()))
}
public boolean deleteNotice(int id) {
    String sql = "DELETE FROM content WHERE id = ?";
}
```
```java
// Impl (수정)
public void updateNotice() {
    if (!checkSignIn()) return;
    List<String> list = noticeDAO.getListByUserId(userId);
    if (list.isEmpty()) { System.out.println("수정할 글이 없습니다."); return; }
    list.forEach(System.out::println);
    // 글 번호 입력 → 내용 입력 → noticeDAO.updateNotice(id, content)
}
```

내 글 목록(`getListByUserId`)을 먼저 보여주는 이유: 어떤 글을 고를지 번호를 알아야 하니까요.

</details>

**확인**: 내 글만 목록에 뜨고, 번호로 수정/삭제하면 DB에 반영되면 OK.

---

### Step 8. 로그아웃 + 회원 탈퇴 — signOut + leave

**목표**: 로그아웃은 세션을 끄고, 탈퇴는 회원과 **그 회원의 글까지** 지운다.

**할 일**
1. (Impl) `signOut()` → `setUserInfo(false, null, null)`.
2. (DAO) `leaveExc(userId)` → `user` 삭제, `deleteContentExc(userId)` → 그 사람 `content` 삭제.
3. (Impl) `leave()` → 없는 회원이면 안내, 있으면 회원+글 삭제.

<details>
<summary>💡 힌트 보기</summary>

```java
// Impl
public void signOut() { setUserInfo(false, null, null); }

public void leave() {
    // 삭제할 ID 입력 → checkUserId로 존재 확인
    // 있으면: leaveExc(userId) 성공 시 deleteContentExc(userId)
}
```
```java
// DAO
public boolean leaveExc(String userId) {
    String sql = "DELETE FROM user WHERE user_id = ?";
}
public void deleteContentExc(String userId) {
    String sql = "DELETE FROM content WHERE user_id = ?";
}
```

> ⚠️ **삭제 순서 주의**: 만약 `content.user_id`에 외래키(FK)를 걸었다면, **글(content)을 먼저 지우고 회원(user)을 지워야** 해요. (회원을 먼저 지우면 글이 그 회원을 참조하고 있어 오류가 날 수 있어요.) FK를 안 걸었다면 순서는 상관없지만, 글을 먼저 지우는 습관이 안전합니다.

</details>

**확인**: 탈퇴하면 그 회원과 그 회원의 글이 모두 사라지면 OK.

---

### Step 9. 마무리 점검 (완성!)

**점검 항목**
- [ ] 회원가입 → 로그인 → 글등록 → 목록 → 수정 → 삭제 → 로그아웃 → 탈퇴가 순서대로 되는가?
- [ ] 로그인 안 한 상태에서 글쓰기/수정/삭제가 막히는가?
- [ ] 수정·삭제는 **내 글만** 대상인가?
- [ ] Impl엔 SQL이 없고, DAO엔 화면 출력이 없는가? (계층 분리)
- [ ] 모든 DB 작업이 PreparedStatement + try-with-resources인가?

여기까지 통과하면 **로그인 게시판 완성입니다!** 🎉

---

## 6. 학습 체크

- [ ] Impl(서비스) / DAO(데이터) / DTO(운반)의 역할을 설명할 수 있다
- [ ] 로그인 상태를 Impl 필드로 유지하는 원리를 안다
- [ ] DTO가 왜 boolean보다 나은지(여러 값·세 갈래) 설명할 수 있다
- [ ] 인가 체크(checkSignIn)의 역할을 안다
- [ ] 두 테이블(user, content)의 관계를 이해한다

---

## 7. 최종 완성 체크리스트

- [ ] `user`, `content` 테이블
- [ ] `Notice`(인터페이스) / `NoticeImpl` / `NoticeDAO` / `SignInResponseDTO` / `Start`
- [ ] 회원가입·로그인·글 CRUD·로그아웃·탈퇴 동작
- [ ] 계층 분리 + 인가 체크 + DTO 활용

---

## 8. (선택) 도전 과제

1. **비밀번호 해싱**: 평문 저장은 위험해요. `BCrypt`나 해시로 바꿔 저장/검증하기
2. **Scanner 하나로**: 메서드마다 `new Scanner` 대신, 하나를 공유. `nextInt`/`nextLine` 섞임도 정리(전부 `nextLine` + 파싱)
3. **트랜잭션**: 탈퇴 시 user·content 삭제를 하나로 묶어, 둘 다 성공하거나 둘 다 취소(`commit`/`rollback`)
4. **본인 글만 수정/삭제 보장**: 남의 글 번호를 넣어도 못 고치게 `WHERE id=? AND user_id=?` 로 막기
5. **외래키 + CASCADE**: `content`에 FK를 걸고 `ON DELETE CASCADE`로 탈퇴 시 글 자동 삭제
6. **검색·페이징**: 글 내용 검색, 목록을 페이지로 나눠 보기
7. **DTO 더 쓰기**: 글 목록도 문자열 대신 `ContentDTO`(id/userId/content/created)로 다루기