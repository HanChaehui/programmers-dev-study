### InnoDB

MySQL에서 테이블 데이터를 실제로 저장하고 관리하는 **스토리지 엔진 (디폴트)**

데이터 저장, 인덱스 관리(클러스터형/보조 인덱스), 트랜잭션 처리(COMMIT, ROLLBACK), 외래키 규정, 행 단위 잠금 등을 담당

### SQL injection

- 사용자가 입력한 값이 SQL 문장에 그대로 합쳐지면서, 의도하지 않은 SQL이 실행되는 공격
- `PreparedStatement`를 사용하면 SQL 문장과 사용자 입력값을 분리해서 처리하므로 SQL Injection을 예방할 수 있음

```java
// 사용자가 id 값으로 이상한 SQL 조각을 넣으면 원래 의도와 다른 쿼리가 실행될 수 있다
String sql = "SELECT * FROM member WHERE id = '" + id + "'";

// PreparedStatement 사용하여 SQL injection 예방
String sql = "SELECT * FROM member WHERE id = ?"; 
PreparedStatement pstmt = conn.prepareStatement(sql); 
pstmt.setString(1, id);
```

# JDBC와 MySQL 연결 개념

### JDBC (Java Database Connectivity)

자바에서 DB에 접속하고 실행하기 위한 표준 API ( 공통 규칙 / 인터페이스 )

`Java 코드 → JDBC API → DB 드라이버 → DB 서버`

#### MySQL Driver (MySQL Connector/J)

JDBC 규칙에 맞춰 MySQL과 실제로 통신하게 해주는 라이브러리 

#### JDBC 표준 클래스 / 인터페이스

- `Connection`  : Java 코드와 DB 사이의 연결 통로를 나타내는 객체 ( 인터페이스 )
- `DriverManager`  : DB 드라이버를 관리하고 DB 연결을 만들어주는 클래스
- `Statement`  : SQL을 실행하는 객체
- `PreparedStatement`  : SQL을 미리 준비해두고 ? 자리에 값을 안전하게 넣어 실행하는 객체 ( .setInt(). setString(), .excuteUpdate(), .excuteQuery() )
- `ResultSet`  : SELECT 실행 결과를 담는 객체, 조회된 행들을 하나씩 꺼내서 읽을 수 있음 ( .next(), getInt(), getString() 이용, 다음 행이 없으면 false 반환)

### JDBC, Connection, Connector 관계

<aside>

Java 코드 // 개발자가 작성한 프로그램
↓
JDBC API // Java에서 DB를 다루기 위한 공통 규칙
↓
MySQL Connector/J  // JDBC 규칙을 MySQL에 맞게 구현한 드라이버 라이브러리
↓
MySQL Server // 실제 데이터베이스가 저장되고 SQL을 실행하는 서버

</aside>

## Java - MySQL 연결하는 법

1. DB tool → MySQL 데이터 소스 추가 및 계정 연결
2. MySQL 콘솔창에 원하는 DB 소 세팅
3. build.gradle의 dependencies에 외부 라이브러리 추가
(Spring JDBC 라이브러리, MySQL Connector/J 라이브러리)
    
    Maven Repository에서 원하는 라이브러리 검색
    
    ```groovy
    // Java 17 이상용
    // implementation('org.springframework:spring-jdbc:6.1.12')
    
    // Spring JDBC 라이브러리 : JDBC를 Spring 방식으로 더 편하게 쓰게 해주는 라이브러리 - 나중에 JdbcTemplate 같은 걸 사용할 수 있음
    implementation('org.springframework:spring-jdbc:5.3.39')
    // MySQL Connector/j: MySQL JDBC Driver 라이브러리
    implementation('com.mysql:mysql-connector-j:8.4.0')
    ```
    
4.  MySQL 접속 주소, 계정, 비밀번호 준비 
5. MySQL JDBC 드라이버 로딩 
6. DriverManager가 URL에 맞는 드라이버 선택하여 MySQL 서버에 접속 
7. Connection 객체 반환 
    
    ```java
    // import java.sql.*
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.SQLException;
    
    public class A_jdbc {
    
        // 반환타입이 Connection인 connection() 메서드
        // 이 메서드를 실행하면 DB 연결 객체를 반환
        public Connection connection() {
        
    				// DB 접속 정보 입력
    				// 내 컴퓨터의 MySQL 서버에서 java_basic 데이터베이스에 접속
            String url = "jdbc:mysql://localhost:3306/java_basic";
            String user = "root";
            String password = "sang1411";
    
            try {
    		        // MySQL 드라이버 클래스를 메모리에 로딩
                Class.forName("com.mysql.cj.jdbc.Driver");
                // 정보를 가지고 MySQL 서버에 접속해서 Connection 객체를 만들기
                // 성공하면 conn에 DB 연결 객체가 들어감
                Connection conn = DriverManager.getConnection(url, user, password);
                System.out.println("Connection Success!");
    						// 이 메서드를 호출한 쪽에서 SQL 실행할 때 이 연결을 쓸 수 있음
    						// 이 연결 객체를 사용하는 쪽에서 try-with-resource 혹은 직접 close()로 닫아야함
                return conn;
    
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    ```
    
8. Connection을 이용해서 SQL 실행 
9. try-with-resource 이용하여 사용 후 자동 Connection close(); 보장
    
    ```java
    	// INSERT 메서드
    	public void insertData(String name, int age, String phone) {
    				// INSERT 쿼리문 작성
            String query = "INSERT INTO member (name, age, phone) VALUES (?, ?, ?)";
    	      // try-with-resource 이용하여 자원 자동 close()
    	      // sql injection 방지용 prepareStatement
            try (
                    Connection conn = connection();
                    PreparedStatement pstmt = conn.prepareStatement( query );
            ) {
    		        // 쿼리문 값 세팅
                pstmt.setString(1, name);
                pstmt.setInt(2, age);
                pstmt.setString(3, phone);
    						// DB 데이터를 변경할 때 사용 (INSERT, UPDATE, DELETE)
                pstmt.executeUpdate();
                
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        
        public void selectAll() {
            String query = "SELECT id, name, age, phone FROM member";
    
            try (
                    Connection conn = connection();
                    PreparedStatement pstmt = conn.prepareStatement( query );
            ) {
    						// SELECT 실행 시 사용, 조회 결과를 ResultSet으로 반환
                ResultSet resultSet = pstmt.executeQuery();
    						
    						// 조회 결과가 다음 행으로 이동할 수 있는 동안 반복
                while (resultSet.next()) {
    								
    								// 필드명과 정확히 일치해야함
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    int age = resultSet.getInt("age");
                    String phone = resultSet.getString("phone");
    
                    System.out.println( id + " " + name + " " + age + " " + phone );
                    System.out.println("==========");
    
                }
    
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    
        public void selectOne (int id) {
            String query = "SELECT id, name, age, phone FROM member WHERE id = ?";
    
            try (
                    Connection conn = connection();
                    PreparedStatement pstmt = conn.prepareStatement( query );
            ) {
    
                pstmt.setInt(1, id);
    
                ResultSet resultSet = pstmt.executeQuery();
    
                if (resultSet.next()) {
                    int id2 = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    int age = resultSet.getInt("age");
                    String phone = resultSet.getString("phone");
    
                    System.out.println( id2 + " : " + name + " : " + age + " : " + phone );
                }
    
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    
        public void updateData( int id, String name, int age, String phone ) {
            String query = "UPDATE member SET name = ?, age = ?, phone = ? WHERE id = ?";
    
            try (
                    Connection conn = connection();
                    PreparedStatement pstmt = conn.prepareStatement(query);
            ) {
    
                pstmt.setString(1, name);
                pstmt.setInt(2, age);
                pstmt.setString(3, phone);
                pstmt.setInt(4, id);
    
                int result = pstmt.executeUpdate();
                if ( result > 0 ) {
                    System.out.println("update success!");
                }
    
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    
        public void deleteData(int id) {
            String query = "DELETE FROM member WHERE id = ?";
    
            try (
                    Connection conn = connection();
                    PreparedStatement pstmt = conn.prepareStatement(query);
            ) {
                pstmt.setInt(1, id);
                int result = pstmt.executeUpdate();
    
                if ( result > 0 ) {
                    System.out.println("delete success!");
                }
    
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
    
        }
    ```
    
    ```java
    // conn을 이용해서 SELECT, INSERT, UPDATE, DELETE 같은 SQL을 실행할 수 있음
    public static void main(String[] args) {
            A_jdbc aJdbc = new A_jdbc();
            // aJdbc.insertData("홍길순", 21, "010-1234-5678");
            // aJdbc.selectOne(1);
            // aJdbc.updateData(2, "홍홍홍", 30, "010-9876-5432");
            // aJdbc.deleteData(2);
            // aJdbc.selectAll();
        }
    ```
    

## executeUpdate()와 executeQuery() 차이

```
executeUpdate()
= INSERT, UPDATE, DELETE 실행
= DB 데이터를 변경할 때 사용
= 영향을 받은 행의 개수를 int로 반환

executeQuery()
= SELECT 실행
= DB 데이터를 조회할 때 사용
= 조회 결과를 ResultSet으로 반환
```

## DB 소스 세팅

## 1. MySQL 콘솔 사용

- Ctrl + Enter로 쿼리문 실행

```sql
# CHARACTER SET = 어떤 문자를 저장할 수 있는지
# COLLATE = 문자를 어떻게 비교하고 정렬할지
# ci = case insensitive

# DB 생성 및 설정 (이모지 포함 한글 사용)
CREATE DATABASE IF NOT EXISTS scott
    DEFAULT CHARACTER SET utf8mb4 # 문자셋
    DEFAULT COLLATE utf8mb4_general_ci; # 정렬/비교 규칙

# DB 삭제
DROP DATABASE 이름;

# DB 목록 조회
SHOW DATABASES;
# DB 사용
USE DB이름;

# Table 목록 조회
SHOW TABLES;

# Table 생성 및 설정
CREATE TABLE IF NOT EXISTS 테이블명 (
    id    INT          NOT NULL AUTO_INCREMENT,   -- PK, 자동 증가
    name  VARCHAR(50)  NOT NULL,                  -- 이름
    age   INT,                                    -- 나이
    phone VARCHAR(20),                            -- 전화번호
    PRIMARY KEY (id)
    );
    
-- MGR 자기참조 FK 때문에, 아직 입력 안 된 상사번호를 참조하는 행이 생긴다.
-- 입력하는 동안만 FK 체크를 잠시 끈다.
SET FOREIGN_KEY_CHECKS = 0;    
    
# Table 구조 조회    
DESC member; 

SELECT * FROM member;    
INSERT INTO member ( name, age, phone ) VALUES ( '홍길동', 20, '010-1234-5678' );
```

## 2. 기존 SQL 파일 열고 실행

`src/main/resources/sql/scott.sql`scott.sql 열기

1.
→ 오른쪽 위 또는 상단에서 MySQL Data Source 혹은 Schema 선택
→ 실행 버튼 / Ctrl + Enter 
    (파일 전체 실행 : 우클릭 후 Run ‘scott.sql’ / Alt + Shift + R)

2.

오른쪽 `Database` 창에서:
@localhost 또는 scott 우클릭
→ SQL Scripts
→ Run SQL Script...
→ 실행할 .sql 파일 선택
(파일 전체 실행)

## 3.  터미널에서 MySQL (클라이언트?)

`mysql -u root -p`

`source C:/Users/LG/OneDrive/사진/바탕 화면/scott.sql;`