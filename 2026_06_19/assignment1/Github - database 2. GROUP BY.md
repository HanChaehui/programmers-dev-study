# GROUP BY

> *(SQL 예제는 모두 **MySQL** 기준)*
> 

## GROUP BY 절

결과 값을 원하는 열로 **묶어서** 출력하는 절이다.

### 왜 필요한가? — 각 부서별 평균 급여를 알고 싶다

```sql
-- 부서마다 한 번씩… 너무 번거롭다
SELECT AVG(SAL) FROM EMP WHERE DEPTNO = 10;
SELECT AVG(SAL) FROM EMP WHERE DEPTNO = 20;
SELECT AVG(SAL) FROM EMP WHERE DEPTNO = 30;
```

```sql
-- UNION ALL로 합쳐도… 여전히 번거롭다
SELECT AVG(SAL) FROM EMP WHERE DEPTNO = 10
UNION ALL
SELECT AVG(SAL) FROM EMP WHERE DEPTNO = 20
UNION ALL
SELECT AVG(SAL) FROM EMP WHERE DEPTNO = 30;
```

→ 이럴 때 **GROUP BY**를 쓰면 한 번에 해결된다.

### GROUP BY 절 형식

```sql
SELECT [조회할 열1, 열2, ..., 열N]
FROM   [조회할 테이블]
WHERE  [조건식]
GROUP BY [그룹화할 열 지정(여러 개 가능)]
ORDER BY [정렬할 열 지정];
```

> ⚠️ 주의
> 
> - GROUP BY를 쓰면 열 위치에 을 쓸 수 없다.
> - 집계(다중행) 함수를 사용하지 않은 **일반 열**은, GROUP BY 절에 명시하지 않으면 SELECT 절에 사용할 수 없다.

### 사용 예시

```sql
-- 부서별 평균 급여
SELECT AVG(SAL), DEPTNO
FROM EMP
GROUP BY DEPTNO;
```

```sql
-- 부서별 + 직책별 평균 급여
SELECT AVG(SAL), DEPTNO, JOB
FROM EMP
GROUP BY DEPTNO, JOB
ORDER BY DEPTNO, JOB;
```

---

## HAVING 절

GROUP BY로 **그룹화된 결과에 조건**을 줄 때 사용한다.

- `HAVING` 절은 SELECT문에 `GROUP BY` 절이 있을 때만 사용할 수 있다.
- 그룹화된 결과 값의 범위를 제한하는 데 사용한다.

### HAVING 절 형식

```sql
SELECT [조회할 열1, 열2, ..., 열N]
FROM   [테이블명]
WHERE  [조건식]
GROUP BY [그룹화할 열 지정(여러 개 가능)]
HAVING [그룹화된 결과를 제한하는 조건]
ORDER BY [정렬할 열 지정];
```

### 사용 예시

각 부서의 직책별 평균 급여를 구하되, 그 평균 급여가 2000 이상인 그룹만 출력하시오.

```sql
SELECT DEPTNO, JOB, AVG(SAL)
FROM EMP
GROUP BY DEPTNO, JOB
HAVING AVG(SAL) >= 2000
ORDER BY DEPTNO, JOB;
```

```sql
-- WHERE와 HAVING을 함께 사용
SELECT DEPTNO, JOB, AVG(SAL)
FROM EMP
WHERE SAL >= 3000
GROUP BY DEPTNO, JOB
HAVING AVG(SAL) >= 2000
ORDER BY DEPTNO, JOB;
```

### 실행 순서 (WHERE vs HAVING)

> **WHERE** 절을 먼저 실행해 행을 걸러낸다.그 결과 데이터가 **GROUP BY** 절로 그룹화된다.**HAVING** 절이 그룹을 제한한다.
> 
> 
> 즉, 그룹이 만들어지기 **전에** WHERE로 걸러진 데이터는 그룹화 대상에 포함되지 않는다.
> 

| 구분 | WHERE | HAVING |
| --- | --- | --- |
| 대상 | 그룹화 **전**의 개별 행 | 그룹화 **후**의 그룹 |
| 집계 함수 | 사용 ✕ | 사용 ○ (`HAVING AVG(SAL) >= 2000`) |

---

## 연습 문제

문제 1) 10번 부서에 대해 급여의 평균값, 최대/최소값, 인원수를 구하시오. (인원수: COUNT(*))

```sql
SELECT AVG(SAL), MAX(SAL), MIN(SAL), COUNT(EMPNO)
FROM EMP
WHERE DEPTNO = 10;

-- 동일한 결과 --
SELECT AVG(sal), MAX(sal), MIN(sal), COUNT(*)
FROM emp
GROUP BY deptno
HAVING deptno = 10;
```

문제 2) 각 부서별 같은 직무를 갖는 사원의 인원수를 구하여 부서번호, 직무, 인원수를 출력하시오.

```sql
SELECT DEPTNO, JOB, COUNT(EMPNO)
FROM EMP
GROUP BY DEPTNO, JOB
ORDER BY DEPTNO;
```

문제 3) 사원들의 직무별 평균 급여와 최고 급여, 최저 급여를 평균 급여에 대해 오름차순으로 정렬하시오.

```sql
SELECT AVG(SAL), MAX(SAL), MIN(SAL)
FROM EMP
GROUP BY JOB
ORDER BY AVG(SAL);
```

문제 4) 각 직무별 사원 수와 최고/최저/평균 급여액을 구하되, 사원 수가 많은 직무부터 출력하시오. (평균 급여는 소수점 둘째 자리에서 반올림)

> 💡 `ROUND(값, 자릿수)` 형식 사용. 예) `ROUND(값, 1)` → 소수점 첫째 자리까지
> 

```sql
SELECT JOB, COUNT(EMPNO), MAX(SAL), MIN(SAL),
       ROUND(AVG(SAL), 1)
FROM EMP
GROUP BY JOB
ORDER BY COUNT(EMPNO) DESC;
```

문제 5) 부서별 월급 합계가 5000을 초과하는 각 업무에 대해, 업무와 월 급여 합계를 조회하시오. (GROUP BY ~ HAVING)

```sql
SELECT JOB, SUM(SAL)
FROM EMP
GROUP BY DEPTNO, JOB
HAVING SUM(SAL) > 5000;
```

문제 6) 급여가 1000 이상인 사원들의 부서별 평균 급여를 출력하시오. 단, 부서별 평균 급여가 2000 이상인 부서만 출력할 것.

```sql
SELECT DEPTNO, AVG(SAL)
FROM EMP
WHERE SAL >= 1000
GROUP BY DEPTNO
HAVING AVG(SAL) >= 2000;
```

문제 7) 각 부서별 같은 업무(JOB)를 하는 사람의 인원수를 구해서, 부서번호·업무·인원수를 부서번호에 대해 오름차순으로 정렬하여 출력하시오.

```sql
SELECT DEPTNO, JOB, COUNT(EMPNO)
FROM EMP
GROUP BY DEPTNO, JOB
ORDER BY DEPTNO ASC;
```

문제 8) EMP 테이블에서 부서 인원이 4명보다 많은 부서의 부서번호, 인원수, 급여의 합을 출력하시오.

```sql
SELECT DEPTNO, COUNT(*), SUM(SAL)
FROM EMP
GROUP BY DEPTNO
HAVING COUNT(*) > 4;
```

---

## WITH ROLLUP

각 그룹별 결과를 출력하고, 마지막에 **총계(전체 합계) 행**을 추가로 출력한다.
ex) 여러 그룹들의 COUNT값의 COUNT, MAX값들의 MAX값…

### 형식

```sql
SELECT [조회할 열1, 열2, ..., 열N]
FROM   [테이블명]
WHERE  [조건식]
GROUP BY [그룹화할 열 지정(여러 개 가능)] WITH ROLLUP;
```

### 사용 예시

```sql
SELECT DEPTNO, JOB, COUNT(*), MAX(SAL), MIN(SAL), AVG(SAL)
FROM EMP
GROUP BY DEPTNO, JOB WITH ROLLUP;
```

> ℹ️ 총계 행에서 그룹 열 값은 `NULL`로 표시된다.
> 

---

## 여러 기준으로 그룹화하기 (UNION ALL)

서로 다른 기준의 그룹화 결과를 한 번에 보고 싶을 때는, 각 GROUP BY 결과를 `UNION ALL`로 합친다.

예) 부서별 집계와 직무별 집계를 각각 구해 합친다.

```sql
-- 부서별 그룹화
SELECT DEPTNO, NULL AS JOB, COUNT(*), MAX(SAL), MIN(SAL), AVG(SAL)
FROM EMP
GROUP BY DEPTNO
UNION ALL
-- 직무별 그룹화
SELECT NULL AS DEPTNO, JOB, COUNT(*), MAX(SAL), MIN(SAL), AVG(SAL)
FROM EMP
GROUP BY JOB;
```