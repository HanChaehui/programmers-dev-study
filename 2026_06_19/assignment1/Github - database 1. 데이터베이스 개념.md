## 데이터베이스란?

**데이터베이스**: 구조화된 데이터의 집합

- 여러 사람과 실시간으로 공유하여 사용
- 효율적인 데이터 관리
- 효율적인 데이터 검색
- 일관성 있는 방법으로 데이터 관리
- 데이터 누락 및 중복 제거

### 파일 시스템을 통한 데이터 관리

서로 다른 여러 응용 프로그램이 제공하는 기능에 맞게 필요한 데이터를 각각 저장하고 관리한다. 각 파일에 저장한 데이터는 서로 연관이 없고 **중복 또는 누락**이 발생할 수 있다.

### DBMS를 통한 데이터베이스 관리

> **DBMS** (Database Management System)
> 

효율적인 데이터 관리 조건을 만족하며 서비스 제공의 효율성을 높이기 위해 데이터베이스 관리 시스템이 등장했다.

- 여러 목적으로 사용할 데이터의 접근, 관리 등의 업무를 DBMS가 전담하는 방식
- DBMS는 자신이 관리하는 데이터베이스로 관련 작업을 수행하고 결과 값을 제공한다.
- 여러 응용 프로그램이 하나의 통합된 데이터를 같은 방식으로 사용·관리할 수 있으므로 데이터 누락이나 중복을 방지할 수 있다.

---

## 데이터 모델

컴퓨터에 데이터를 저장하는 방식을 정의해 놓은 개념이다.

대표 데이터 모델: **계층형, 네트워크형, 관계형, 객체 지향형** 등

> 관계형 데이터베이스 → **RDBMS***(아래 SQL 예제는 모두 **MySQL** 기준)*
> 

---

## SQL (Structured Query Language)

RDBMS에서 데이터를 다루고 관리하는 데 사용하는 데이터베이스 질의 언어이다.

### SQL 명령어 종류

| 종류 | 이름 | 설명 |
| --- | --- | --- |
| **DQL** | Data Query Language | RDBMS에 저장한 데이터를 원하는 방식으로 조회하는 명령어 |
| **DML** | Data Manipulation Language | 테이블의 데이터를 저장·수정·삭제하는 명령어 |
| **DDL** | Data Definition Language | 테이블을 포함한 여러 객체를 생성·수정·삭제하는 명령어 |
| **TCL** | Transaction Control Language | 트랜잭션 데이터의 영구 저장·취소 등과 관련된 명령어 |
| **DCL** | Data Control Language | 데이터 사용 권한과 관련된 명령어 |

---

## 테이블과 키

**테이블**: 표 형태의 데이터 저장 공간 (행 = `row`, 열 = `column`)

**키(key)**: 수많은 데이터를 구별할 수 있는 유일한 값. 하나의 테이블을 구성하는 여러 열 중에서 특별한 의미를 지닌 하나 또는 여러 열의 조합을 의미한다.

| 키 종류 | 영문 | 설명 |
| --- | --- | --- |
| **기본키** | Primary Key (PK) | 한 행(데이터)을 **대표·식별**하는 키. 중복될 수 없고(`UNIQUE`), 비어 있을 수 없다(`NOT NULL`). 테이블당 1개. <br>예) 학생 테이블의 `학번` |
| **후보키** | Candidate Key | 기본키가 **될 수 있는 자격**을 갖춘 키들. 이 중 하나를 골라 기본키로 정한다. <br>예) `학번`, `주민번호` 모두 후보키 → 둘 중 하나가 기본키 |
| **외래키** | Foreign Key (FK) | **다른 테이블의 기본키**를 참조하는 키. 테이블끼리 관계를 연결한다. <br>예) 사원 테이블의 `부서번호` → 부서 테이블의 `부서번호` 참조 |
| **복합키** | Composite Key | **두 개 이상의 열을 합쳐** 하나의 키로 사용하는 것. 한 열만으로 구별이 안 될 때 사용. <br>예) (`수강신청` 테이블) `학번` + `과목코드` |

> 💡 한눈에 보기
> 
> - **기본키** = 행을 구별하는 대표 열 (중복 ✕, 빈 값 ✕)
> - **후보키** = 기본키 후보 (기본키가 될 수 있는 열들)
> - **외래키** = 다른 테이블을 가리키는 연결 고리
> - **복합키** = 여러 열을 묶어서 만든 키

---

## 실습 테이블 구조

### EMP 테이블 (사원 정보)

| 컬럼 | 설명 |
| --- | --- |
| EMPNO | 사원번호 |
| ENAME | 사원이름 |
| JOB | 사원직책 |
| MGR | 직속 상관의 사원번호 |
| HIREDATE | 입사일 |
| SAL | 급여 |
| COMM | 인센티브 |
| DEPTNO | 부서번호 |

### DEPT 테이블 (부서 정보)

| 컬럼 | 설명 |
| --- | --- |
| DEPTNO | 부서번호 |
| DNAME | 부서이름 |
| LOC | 부서위치 |

### SALGRADE 테이블 (연봉 정보)

| 컬럼 | 설명 |
| --- | --- |
| GRADE | 급여등급 |
| LOSAL | 급여 등급의 최소 급여액 |
| HISAL | 급여 등급의 최대 급여액 |

---

## SELECT는 어떻게 동작할까?

`SELECT`는 "데이터를 꺼내 보여 달라"는 명령이다. 그런데 우리가 **쓰는 순서**와 DB가 **실제로 처리하는 순서**가 다르다. 이 순서를 알면 SQL이 왜 그렇게 동작하는지 이해할 수 있다.

### 우리가 쓰는 순서 (문법 순서)

```sql
SELECT   열          -- ⑤ 어떤 열을 보여줄지
FROM     테이블      -- ① 어떤 테이블에서
WHERE    조건        -- ② 행을 거르고
GROUP BY 열          -- ③ 그룹으로 묶고
HAVING   그룹조건    -- ④ 그룹을 거르고
ORDER BY 열          -- ⑥ 정렬하고
LIMIT    개수;       -- ⑦ 개수를 자른다
```

### DB가 실제로 처리하는 순서 (실행 순서)

| 순서 | 절 | 하는 일 | 비유 |
| --- | --- | --- | --- |
| ① | **FROM** | 어느 테이블에서 데이터를 가져올지 정함 | 재료가 든 창고를 연다 |
| ② | **WHERE** | 조건에 맞는 **행만** 골라냄 | 필요한 재료만 집는다 |
| ③ | **GROUP BY** | 같은 값끼리 그룹으로 묶음 | 재료를 종류별로 모은다 |
| ④ | **HAVING** | 그룹 중에서 조건에 맞는 그룹만 남김 | 양이 적은 묶음은 버린다 |
| ⑤ | **SELECT** | 보여줄 **열을 고르고** 계산함 | 접시에 담을 것만 고른다 |
| ⑥ | **ORDER BY** | 결과를 정렬함 | 보기 좋게 순서를 맞춘다 |
| ⑦ | **LIMIT** | 앞에서 N개만 잘라냄 | 필요한 만큼만 낸다 |

> 💡 핵심: **`FROM`(어디서) → `WHERE`(거르고) → ... → `SELECT`(고르고) → `ORDER BY`(정렬)** 순으로 처리된다.
`SELECT`는 우리가 맨 위에 쓰지만 **거의 마지막**에 실행된다.
> 

### 이 순서를 알면 이해되는 것들

- **`WHERE`에서는 `SELECT`로 만든 별칭(`AS`)을 쓸 수 없다.**
→ `WHERE`(②)가 `SELECT`(⑤)보다 **먼저** 실행되므로, 그 시점엔 별칭이 아직 안 만들어졌기 때문.
- **행을 거르는 조건은 `WHERE`, 그룹을 거르는 조건은 `HAVING`.**
→ `WHERE`는 그룹으로 묶기 **전**(②), `HAVING`은 묶은 **후**(④)에 실행되기 때문.
- **`ORDER BY`에서는 `SELECT`의 별칭을 쓸 수 있다.**
→ `ORDER BY`(⑥)가 `SELECT`(⑤) **다음**에 실행되므로 별칭이 이미 존재하기 때문.

### SELECT가 실행될 때 물리적으로 일어나는 일

위 순서는 "논리적인" 처리 순서다. 그렇다면 실제 하드웨어에서는 무슨 일이 벌어질까? 데이터는 **디스크 → 메모리(RAM) → CPU** 로 이동하며 비교·계산된다.

**1. 데이터는 원래 디스크(HDD/SSD)에 있다**

- 테이블의 실제 데이터는 **디스크에 파일로 저장**되어 있다. (MySQL InnoDB 기준 16KB 단위의 **페이지**로 묶여 저장)
- CPU는 디스크의 데이터를 **직접 계산할 수 없다.** 반드시 먼저 **RAM으로 올려야** 한다.

**2. 필요한 페이지를 RAM(버퍼 풀)으로 올린다**

- `FROM`으로 정한 테이블에서, 조회에 필요한 페이지를 디스크에서 읽어 **버퍼 풀(Buffer Pool)** 이라는 RAM 영역에 올린다.
- 이미 버퍼 풀에 있으면 디스크를 안 읽어도 되므로 **훨씬 빠르다.** (디스크 I/O 생략)
- 디스크 읽기가 가장 느린 단계라서, 조회 속도는 사실상 **"디스크를 몇 번 읽느냐"** 로 결정된다.

**3. CPU가 RAM 위의 행들을 비교·계산한다**

- `WHERE` 조건(`SAL >= 3000` 등)을 **CPU가 한 행씩 비교**해서 맞는 행만 남긴다.
- `SELECT`의 계산(`SAL*12` 등)이나 집계(`SUM`, `AVG`)도 CPU가 수행한다.
- 결과가 크거나 `ORDER BY`로 정렬해야 하면 **정렬 버퍼/임시 테이블**을 RAM에 만들고, RAM이 모자라면 **디스크의 임시 파일로 흘려보낸다(spill)** → 이때 다시 느려진다.

### 저장 계층별 속도 차이 (왜 디스크 읽기를 줄여야 하나)

| 계층 | 역할 | 대략적인 접근 속도 |
| --- | --- | --- |
| CPU 레지스터/캐시 | 실제 비교·계산 | ~1 ns |
| RAM (버퍼 풀) | 데이터 임시 적재 | ~100 ns |
| SSD | 영구 저장 | ~100 µs (RAM의 약 1,000배 느림) |
| HDD | 영구 저장 | ~10 ms (RAM의 약 10만 배 느림) |

> 💡 핵심: **디스크 ≫ RAM ≫ CPU** 순으로 빠르다. 그래서 조회 최적화의 목표는 결국 **디스크에서 읽어 올리는 페이지 수를 줄이는 것**이다.
> 

### 인덱스(Index)란?

**인덱스**: 데이터를 빠르게 찾기 위해 만들어 두는 **"찾아보기(색인)"**. 책 맨 뒤의 **찾아보기**와 똑같은 원리다.

B+Tree 인덱스의 leaf 노드는 **정렬된 인덱스 값들과 그 값에 해당하는 행을 찾을 수 있는 정보**를 가지고 있고, 중복 값은 정렬 순서상 연속으로 모여 있다.

MySQL InnoDB 기준

1. 클러스터형 인덱스 = Primary Key 인덱스
    
    PK 인덱스의 leaf 노드에 실제 행 데이터 전체가 들어 있음 
    (leaf에서 바로 실제 행을 찾음)
    
2. 보조 인덱스 = 일반 컬럼에 건 인덱스
    
    보조 인덱스 leaf에는 보통 **실제 행 주소가 아니라 Primary Key 값**이 들어감
    (바로 실제 행으로 가는 게 아니라, **PK 값을 얻고 → PK 인덱스를 다시 타고 → 실제 행을 가져오는 구조**)
    

### 인덱스가 없을 때 vs 있을 때

`SELECT * FROM EMP WHERE EMPNO = 7782;`

| 구분 | 동작 | 비용 |
| --- | --- | --- |
| 인덱스 **없음** | 첫 행부터 끝까지 모두 읽으며 `EMPNO = 7782` 비교 (**Full Scan**) | 디스크에서 많은 페이지를 RAM으로 올림 → 느림 |
| 인덱스 **있음** | 인덱스에서 7782의 위치를 바로 찾아 그 행만 읽음 | 적은 페이지만 읽음 → 빠름 |

**인덱스가 있다면**
1. B+tree에서 특정 값을 가진 행을 빠르게 찾아갈 수 있음 (정렬된 tree)
2. 특정 값 or 범위 조건 → 해당하는 행들만 읽어올 수 있으므로 적은 수의 페이지만 메모리로 올림, Disk I/O 횟수가 줄어들어 빨라짐!

### 인덱스는 어떻게 빨리 찾을까? (B+Tree)

- 대부분의 DB는 인덱스를 **B+Tree(균형 트리)** 구조로 저장한다.
- 값이 **정렬된 채로** 가지처럼 나뉘어 있어서, 위에서부터 "크다/작다"를 따라 내려가면 **몇 단계 만에** 원하는 값에 도달한다.
- 데이터가 100만 건이어도 몇 번의 비교만으로 찾을 수 있다. (전체를 훑는 것과 비교해 압도적으로 적은 횟수)

### 인덱스 만들기

```sql
-- 형식: CREATE INDEX 인덱스이름 ON 테이블(열);
CREATE INDEX idx_emp_sal ON EMP(SAL);
```

> ℹ️ **기본키(PK)** 를 지정하면 그 열에는 인덱스가 **자동으로** 만들어진다.
> 

### 인덱스의 장점과 단점

| 장점 | 단점 |
| --- | --- |
| `SELECT` / `WHERE` 검색이 빨라진다 | 인덱스도 디스크 공간을 차지한다 |
| 정렬(`ORDER BY`)·조인에도 도움 | `INSERT`/`UPDATE`/`DELETE` 시 인덱스도 같이 고쳐야 해서 **느려질 수 있다** |

> 💡 그래서 인덱스는 **검색에 자주 쓰는 열**(예: 자주 조건으로 거는 열)에 거는 것이 좋고, 아무 열에나 무작정 많이 만들면 오히려 손해다.
> 

자주 WHERE 조건에 사용되는 컬럼
중복 값이 적은 컬럼
JOIN에 자주 사용되는 컬럼
ORDER BY에 자주 사용되는 컬럼

---

## SELECT 기본

### EMP 테이블 조회

```sql
SELECT * FROM EMP;
SELECT DEPTNO, ENAME, JOB FROM EMP;
```

### DISTINCT : 중복 제거

```sql
SELECT DISTINCT deptno FROM EMP;

-- 복합 중복 제거
SELECT DISTINCT deptno, job FROM EMP;

-- 모든 컬럼 값이 완전히 같은 행만 중복 제거
SELECT DISTINCT *
FROM EMP;
```

### ALL : 중복 상관없이 모두 출력

```sql
SELECT ALL DEPTNO, JOB FROM EMP;
SELECT DEPTNO, JOB FROM EMP;
```

### 연산

```sql
SELECT ENAME, SAL, SAL*12*COMM AS ANNSAL, COMM FROM EMP;
```

---

## ORDER BY : 정렬

여기저기 흩어져 있는 데이터를 특정 기준에 따라 가지런히 순서를 맞추는 것은 자원을 많이 사용하기 때문에 꼭 필요한 경우가 아니면 사용하지 말 것.

```sql
SELECT * FROM EMP ORDER BY sal ASC;   -- 오름차순
SELECT * FROM EMP ORDER BY sal DESC;  -- 내림차순
```

실습) EMP 테이블의 전체 열을 부서번호(오름차순)와 급여(내림차순)으로 정렬하시오.

```sql
SELECT * FROM EMP ORDER BY deptno ASC, sal DESC;
```

---

## WHERE : 조건

```sql
SELECT * FROM EMP WHERE DEPTNO = 30;
```

### AND / OR

```sql
SELECT * FROM EMP WHERE DEPTNO = 30 AND JOB = 'SALESMAN';
SELECT * FROM EMP WHERE DEPTNO = 30 OR  JOB = 'SALESMAN';
```

### 비교 연산

```sql
SELECT * FROM EMP WHERE sal * 12 = 36000;
SELECT * FROM EMP WHERE sal >= 3000;
```

```sql
-- 문자도 부등호 비교 가능 (사전 순)
SELECT * FROM EMP WHERE ENAME >= 'F';
```

실습) 연봉이 3000이 아닌 경우 정보 출력 (동일 표현)

```sql
SELECT * FROM EMP WHERE SAL != 3000;
SELECT * FROM EMP WHERE SAL <> 3000;
SELECT * FROM EMP WHERE NOT SAL = 3000;
```

### IN 연산자

```sql
SELECT * FROM EMP WHERE JOB = 'MANAGER' OR JOB = 'SALESMAN' OR JOB = 'CLERK';

-- 동일 표현
SELECT * FROM EMP WHERE JOB IN ('MANAGER', 'SALESMAN', 'CLERK');

-- 반대
SELECT * FROM EMP WHERE JOB NOT IN ('MANAGER', 'SALESMAN', 'CLERK');

+
SELECT * FROM EMP WHERE SAL IN (1000, 2000);
```

---

## BETWEEN A AND B

```sql
SELECT * FROM EMP WHERE SAL >= 2000 AND SAL <= 3000;
-- 동일 표현
SELECT * FROM EMP WHERE SAL BETWEEN 2000 AND 3000;

-- 부정
SELECT * FROM EMP WHERE SAL NOT BETWEEN 2000 AND 3000;
```

---

## LIKE 연산자와 와일드카드

**LIKE**: 이메일이나 게시판 제목·내용 검색 기능처럼 일부 문자열이 포함된 데이터를 조회할 때 사용.

### 와일드카드

- `%` : 길이와 상관없이 모든 문자 데이터를 의미
- `_` : 한 개의 문자 데이터를 의미

```sql
-- 이름이 S로 끝나는 데이터 조회
SELECT * FROM EMP WHERE ENAME LIKE '%S';

-- 이름이 S로 시작하는 데이터 조회
SELECT * FROM EMP WHERE ENAME LIKE 'S%';

-- 와일드카드 사이의 문자(열)가 포함된 모든 데이터 추출
SELECT * FROM EMP WHERE ENAME LIKE '%AM%';
SELECT * FROM EMP WHERE ENAME NOT LIKE '%AM%';
```

---

## IS NULL 연산자

```sql
SELECT * FROM EMP WHERE COMM = NULL;   -- 데이터가 안 나옴
-- NULL + 100 = NULL, 즉 '없음'

SELECT * FROM EMP WHERE COMM IS NULL;
SELECT * FROM EMP WHERE COMM IS NOT NULL;
SELECT * FROM EMP WHERE MGR  IS NOT NULL;
```

SQL에서 `NULL`은 **0도 아니고, 빈 문자열도 아니고, 어떤 값도 아니라서** `=`로 비교할 수 없음
(NULL : 값이 없음, 모름, 아직 정해지지 않음)

---

## 집합 연산

두 개 이상의 SELECT문의 결과 값을 연결할 때 사용한다.

> **주의점**: 열 개수와 각 열의 자료형이 순서별로 일치해야 한다.
> 

```sql
SELECT EMPNO, ENAME, SAL, DEPTNO FROM EMP WHERE DEPTNO = 10
UNION
SELECT EMPNO, ENAME, SAL, DEPTNO FROM EMP WHERE DEPTNO = 20;
```

### UNION 종류

| 종류 | 설명 |
| --- | --- |
| **UNION** | 연결된 SELECT문의 결과 값을 합집합으로 묶어준다. 결과 값의 중복은 제거된다. |
| **UNION ALL** | 연결된 SELECT문의 결과 값을 합집합으로 묶어준다. 중복된 결과 값도 제거 없이 모두 출력한다. |

```sql
SELECT EMPNO, ENAME, SAL, DEPTNO FROM EMP WHERE DEPTNO = 10
UNION
SELECT EMPNO, ENAME, SAL, DEPTNO FROM EMP WHERE DEPTNO = 10;

SELECT EMPNO, ENAME, SAL, DEPTNO FROM EMP WHERE DEPTNO = 10
UNION ALL
SELECT EMPNO, ENAME, SAL, DEPTNO FROM EMP WHERE DEPTNO = 10;
```

---

## 문자 함수

### UPPER, LOWER : 대/소문자 변환

```sql
SELECT ENAME, UPPER(ENAME), LOWER(ENAME) FROM EMP;
```

### LENGTH : 문자열 길이

```sql
SELECT ENAME, LENGTH(ENAME) FROM EMP;
```

### SUBSTR : 문자열 일부 추출

```sql
-- SUBSTR(문자열 데이터, 시작위치, 추출길이)
-- SUBSTR(문자열 데이터, 시작위치)
SELECT JOB, SUBSTR(JOB, 1, 2), SUBSTR(JOB, 3, 2), SUBSTR(JOB, 5) FROM EMP;
```

### REPLACE : 특정 문자를 다른 문자로 변환

```sql
-- REPLACE([문자열 데이터 또는 열 이름(필수)], [찾는 문자열(필수)], [대체할 문자(필수)])
SELECT '010-1234-5678' AS REPLACE_BEFORE,
       REPLACE('010-1234-5678', '-', ' ') AS REPLACE_1,
       REPLACE('010-1234-5678', '-', '')  AS REPLACE_2;
```

> ℹ️ MySQL의 `REPLACE`는 세 번째 인자(대체 문자열)가 **필수**이므로, 문자를 제거하려면 빈 문자열 `''`을 명시한다.
> 

---

## 연습 문제

문제 9) 연봉이 1000~2000인 데이터 (IN 사용)

```sql
SELECT * FROM EMP WHERE SAL IN (1000, 2000);
```

---

## 날짜 함수

### NOW / CURDATE : 현재 날짜

```sql
SELECT NOW()    AS NOW,                          -- 날짜 + 시간
       CURDATE() AS TODAY,                        -- 날짜만
       CURDATE() - INTERVAL 1 DAY AS YESTERDAY,
       CURDATE() + INTERVAL 1 DAY AS TOMORROW;
```

> ℹ️ 날짜 산술은 `INTERVAL` 또는 `DATE_ADD` / `DATE_SUB`를 사용한다.
> 

<aside>

- 날짜/시간 산술 → `INTERVAL` / `DATE_ADD`, `DATE_SUB` , `+`, `-`
- `CURDATE()` : 날짜 ( `DATE` )
연산 가능 단위 → `DAY`, `MONTH`, `YEAR`
- `NOW()` : 날짜+ 시간 ( `TIMESTAMP` , `DATETIME` )
연산 가능 단위 → `DAY`, `MONTH`, `YEAR` + `HOUR`, `MINUTE`, `SECOND`

* `INTERVAL` 뒤는 숫자+단위

*`DATE` 타입 : YYYY-MM-DD
*`TIMESTAMP` 타입 or `DATETIME` 타입 : YYYY-MM-DD HH:MM:SS

</aside>

### DATE_ADD : 몇 개월 이후 날짜

```sql
SELECT EMPNO, ENAME, HIREDATE,
       DATE_ADD(HIREDATE, INTERVAL 120 MONTH) AS WORK10YEAR
FROM EMP;
```

문제) 32년(384개월)이 되지 않은 사원 이름 출력

```sql
SELECT ENAME FROM EMP
WHERE DATE_ADD(HIREDATE, INTERVAL 384 MONTH) > NOW();
```

```sql
-- 불가능!!!!!!!!!!!!!!!!!!! --
SELECT ename 
FROM EMP
WHERE DATE_SUB(CURDATE(), INTERVAL HIREDATE) < 32 YEAR;
```

---

## NULL 처리 함수

### IFNULL

데이터가 NULL이 아니면 그 데이터를 그대로 출력하고, NULL이면 사용자가 입력한 데이터를 출력한다.

```sql
SELECT empno, ename, sal, comm, sal + comm,
       IFNULL(COMM, 0), sal + IFNULL(comm, 0)
FROM EMP;
```

> ℹ️ `IFNULL(값, 대체값)` 또는 표준 함수 `COALESCE(값, 대체값)`을 사용할 수 있다.
> 

### IF : NULL 여부에 따른 분기

```sql
-- IF(조건, 참일 때 값, 거짓일 때 값)
SELECT EMPNO, ENAME, COMM,
       IF(COMM IS NOT NULL, 'O', 'X'),
       IF(COMM IS NOT NULL, SAL*12+COMM, SAL*12) AS ANNUAL
FROM EMP;
```

---

## 집계 함수

| 함수 | 설명 | 예시 |
| --- | --- | --- |
| **SUM** | 합계 | `SELECT SUM(SAL) FROM EMP;` |
| **COUNT** | 개수 | `SELECT COUNT(*) FROM EMP;` |
| **MAX** | 최댓값 | `SELECT MAX(SAL) FROM EMP;` |
| **MIN** | 최솟값 | `SELECT MIN(SAL) FROM EMP;` |
| **AVG** | 평균 | `SELECT AVG(SAL) FROM EMP;` |

문제) 입사일(HIREDATE)이 '1980-12-17' 이상인 데이터 조회

```sql
SELECT * FROM EMP WHERE HIREDATE >= '1980-12-17';
```

> ℹ️ 날짜 리터럴은 `'YYYY-MM-DD'` 형식을 사용한다.
> 

문제) 부서번호별로 묶고, 각 부서의 사원 수를 세라.

```sql
SELECT DEPTNO, COUNT(*)
FROM EMP
GROUP BY DEPTNO;
```

⭐SELECT절에 집계함수가 있고, 집계함수가 아닌 일반 컬럼도 같이 출력한다면 그 일반 컬럼은 GROUP BY에 있어야 함.
⭐ 집계함수 조건은 WHERE에 절대 불가, HAVING 에 사용!!!!
(집계함수는 어떤 그룹 단위로 동작, 하나의 행에 각각 동작 X)
⭐ COUNT(*)는 NULL을 포함한 모든 행의 개수, COUNT(EMPNO)는 EMPNO가 NULL이 아닌 행의 개수