# 0. 환경 설정 (셋팅)

실습에 사용할 **`scott` 학습용 데이터베이스**를 만들고, 4개의 테이블(`DEPT`, `EMP`, `SALGRADE`, `BONUS`)과 기본 데이터를 채워 넣는 스크립트다. 이후 모든 실습은 이 데이터를 기준으로 한다.

---

## 1. 데이터베이스 생성

```sql
CREATE DATABASE IF NOT EXISTS scott
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE scott;
```

> 💡 `utf8mb4`는 이모지를 포함한 모든 유니코드 문자를 저장할 수 있는 문자셋이다. 한글도 안전하게 처리된다.
> 

---

## 2. 기존 테이블 제거

```sql
-- 외래키로 참조당하는 테이블이 나중에 지워지도록, 자식 테이블부터 제거
DROP TABLE IF EXISTS BONUS;
DROP TABLE IF EXISTS SALGRADE;
DROP TABLE IF EXISTS EMP;
DROP TABLE IF EXISTS DEPT;
```

> ⚠️ 외래키 관계가 있을 때는 **참조하는 쪽(자식)** 을 먼저 지워야 한다. `EMP`가 `DEPT`를 참조하므로 `EMP` → `DEPT` 순으로 제거한다.
> 

---

## 3. 테이블 생성

### 테이블 관계 한눈에 보기

| 테이블 | 설명 | 관계 |
| --- | --- | --- |
| **DEPT** | 부서 정보 | `EMP.DEPTNO` 가 참조 |
| **EMP** | 사원 정보 | `DEPT`를 참조 + 자기 자신(`MGR`)을 참조 |
| **SALGRADE** | 급여 등급 | 독립 테이블 |
| **BONUS** | 보너스 (구조만, 데이터 없음) | 독립 테이블 |

### DEPT — 부서

```sql
CREATE TABLE DEPT (
    DEPTNO INT NOT NULL,          -- 부서번호
    DNAME  VARCHAR(14),           -- 부서명
    LOC    VARCHAR(13),           -- 위치
    CONSTRAINT PK_DEPT PRIMARY KEY (DEPTNO)
) ENGINE=InnoDB;
```

### EMP — 사원

```sql
CREATE TABLE EMP (
    EMPNO    INT NOT NULL,        -- 사원번호
    ENAME    VARCHAR(10),         -- 이름
    JOB      VARCHAR(9),          -- 직무
    MGR      INT,                 -- 관리자(상사) 사원번호
    HIREDATE DATE,                -- 입사일
    SAL      DECIMAL(7,2),        -- 급여
    COMM     DECIMAL(7,2),        -- 커미션(수당)
    DEPTNO   INT,                 -- 부서번호
    CONSTRAINT PK_EMP    PRIMARY KEY (EMPNO),
    CONSTRAINT FK_DEPTNO FOREIGN KEY (DEPTNO) REFERENCES DEPT (DEPTNO),
    CONSTRAINT FK_MGR    FOREIGN KEY (MGR)    REFERENCES EMP  (EMPNO)  -- 자기참조
) ENGINE=InnoDB;
```

> 💡 `FK_MGR`는 같은 `EMP` 테이블의 `EMPNO`를 참조하는 **자기참조(Self Reference)** 외래키다. 상사도 결국 사원이기 때문이다.
> 

### SALGRADE — 급여 등급

```sql
CREATE TABLE SALGRADE (
    GRADE INT,                    -- 등급
    LOSAL INT,                    -- 하한 급여
    HISAL INT                     -- 상한 급여
) ENGINE=InnoDB;
```

### BONUS — 보너스 (구조만)

```sql
CREATE TABLE BONUS (
    ENAME VARCHAR(10),
    JOB   VARCHAR(9),
    SAL   DECIMAL(7,2),
    COMM  DECIMAL(7,2)
) ENGINE=InnoDB;
```

---

## 4. 데이터 입력

### DEPT 데이터

```sql
INSERT INTO DEPT (DEPTNO, DNAME, LOC) VALUES
    (10, 'ACCOUNTING', 'NEW YORK'),
    (20, 'RESEARCH',   'DALLAS'),
    (30, 'SALES',      'CHICAGO'),
    (40, 'OPERATIONS', 'BOSTON');
```

| DEPTNO | DNAME | LOC |
| --- | --- | --- |
| 10 | ACCOUNTING | NEW YORK |
| 20 | RESEARCH | DALLAS |
| 30 | SALES | CHICAGO |
| 40 | OPERATIONS | BOSTON |

### EMP 데이터

```sql
-- MGR 자기참조 FK 때문에, 아직 입력 안 된 상사번호를 참조하는 행이 생긴다.
-- 입력하는 동안만 FK 체크를 잠시 끈다.
SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO EMP (EMPNO, ENAME, JOB, MGR, HIREDATE, SAL, COMM, DEPTNO) VALUES
    (7369, 'SMITH',  'CLERK',     7902, '1980-12-17',  800.00,    NULL, 20),
    (7499, 'ALLEN',  'SALESMAN',  7698, '1981-02-20', 1600.00,  300.00, 30),
    (7521, 'WARD',   'SALESMAN',  7698, '1981-02-22', 1250.00,  500.00, 30),
    (7566, 'JONES',  'MANAGER',   7839, '1981-04-02', 2975.00,    NULL, 20),
    (7654, 'MARTIN', 'SALESMAN',  7698, '1981-09-28', 1250.00, 1400.00, 30),
    (7698, 'BLAKE',  'MANAGER',   7839, '1981-05-01', 2850.00,    NULL, 30),
    (7782, 'CLARK',  'MANAGER',   7839, '1981-06-09', 2450.00,    NULL, 10),
    (7788, 'SCOTT',  'ANALYST',   7566, '1987-04-19', 3000.00,    NULL, 20),
    (7839, 'KING',   'PRESIDENT', NULL, '1981-11-17', 5000.00,    NULL, 10),
    (7844, 'TURNER', 'SALESMAN',  7698, '1981-09-08', 1500.00,    0.00, 30),
    (7876, 'ADAMS',  'CLERK',     7788, '1987-05-23', 1100.00,    NULL, 20),
    (7900, 'JAMES',  'CLERK',     7698, '1981-12-03',  950.00,    NULL, 30),
    (7902, 'FORD',   'ANALYST',   7566, '1981-12-03', 3000.00,    NULL, 20),
    (7934, 'MILLER', 'CLERK',     7782, '1982-01-23', 1300.00,    NULL, 10);

SET FOREIGN_KEY_CHECKS = 1;
```

> ⚠️ `SET FOREIGN_KEY_CHECKS = 0;` 은 외래키 검사를 잠시 끄는 명령이다. 예를 들어 SMITH(7369)의 상사는 FORD(7902)인데, FORD는 아직 입력 전이라 검사를 켜 두면 오류가 난다. 입력이 끝나면 다시 `= 1;` 로 켠다.
> 

| EMPNO | ENAME | JOB | MGR | HIREDATE | SAL | COMM | DEPTNO |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 7369 | SMITH | CLERK | 7902 | 1980-12-17 | 800 | NULL | 20 |
| 7499 | ALLEN | SALESMAN | 7698 | 1981-02-20 | 1600 | 300 | 30 |
| 7521 | WARD | SALESMAN | 7698 | 1981-02-22 | 1250 | 500 | 30 |
| 7566 | JONES | MANAGER | 7839 | 1981-04-02 | 2975 | NULL | 20 |
| 7654 | MARTIN | SALESMAN | 7698 | 1981-09-28 | 1250 | 1400 | 30 |
| 7698 | BLAKE | MANAGER | 7839 | 1981-05-01 | 2850 | NULL | 30 |
| 7782 | CLARK | MANAGER | 7839 | 1981-06-09 | 2450 | NULL | 10 |
| 7788 | SCOTT | ANALYST | 7566 | 1987-04-19 | 3000 | NULL | 20 |
| 7839 | KING | PRESIDENT | NULL | 1981-11-17 | 5000 | NULL | 10 |
| 7844 | TURNER | SALESMAN | 7698 | 1981-09-08 | 1500 | 0 | 30 |
| 7876 | ADAMS | CLERK | 7788 | 1987-05-23 | 1100 | NULL | 20 |
| 7900 | JAMES | CLERK | 7698 | 1981-12-03 | 950 | NULL | 30 |
| 7902 | FORD | ANALYST | 7566 | 1981-12-03 | 3000 | NULL | 20 |
| 7934 | MILLER | CLERK | 7782 | 1982-01-23 | 1300 | NULL | 10 |

### SALGRADE 데이터

```sql
INSERT INTO SALGRADE (GRADE, LOSAL, HISAL) VALUES
    (1,  700, 1200),
    (2, 1201, 1400),
    (3, 1401, 2000),
    (4, 2001, 3000),
    (5, 3001, 9999);
```

| GRADE | LOSAL | HISAL |
| --- | --- | --- |
| 1 | 700 | 1200 |
| 2 | 1201 | 1400 |
| 3 | 1401 | 2000 |
| 4 | 2001 | 3000 |
| 5 | 3001 | 9999 |

---

## 5. 확인용 쿼리

설치가 잘 됐는지 아래 쿼리로 확인한다.

```sql
SELECT * FROM DEPT;
SELECT * FROM EMP;
SELECT * FROM SALGRADE;
```