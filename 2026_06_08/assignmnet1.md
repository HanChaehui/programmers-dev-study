## 1차원 배열

- 같은 타입의 여러 변수를 하나의 묶음으로 다루는 것
- 변수와 달리 배열은 각 저장 공간이 연속적으로 배치되어 있음
- 배열의 첫번째 주소를 알면 나머지 주소를 알 수 있다.

`타입[] 변수명 = new 타입[길이] // 선언만`

`타입[] 변수명 = {값1, 값2, 값3…} // 선언과 동시에 초기화`

`변수명.length - 1` → 배열의 마지막 인덱스

`for ( int i : 배열 )` → 배열의 0번째 인덱스~끝까지 배열값을 순회함

#### Call by value vs Call by reference

함수의 인자로 기본형 vs 참조형 

함수 호출 시 인자로 기본형 변수를 넘겨주어도 callee 함수에서 caller 함수에서 선언된 해당 기본형 변수의 값을 변경할 수 없음 

- 배열의 이름, 변수명은 해당 배열의 값을 가지고 있는 것이 아닌 배열의 시작 주소를 가지고 있음
- 기본형 변수는 해당 값 자체를 가지고 있음 (해당 값이 저장된 공간의 이름이 변수명)

## 2차원 배열

`타입[][] 변수명 = new 타입[길이][길이] // 선언만`

`타입[][] 변수명 = {{a,b},{c,d}} // 선언과 동시에 초기화`

`변수명.length` → 행 길이

`변수명[0].lenth` → 열 길이

## 가변 배열

2차원 이상의 다차원 배열을 생성할 때 전체 배열 차수 중 마지막 차수의 길이를 지정하지 않고, 추후에 각기 다른 길이의 배열을 생성함으로써 고정된 형태가 아닌 보다 유동적인 가변 배열을 구성할 수 있다.

```java
int[][] scores = new int[2][];

scores[0] = new int[] {1, 2};
scores[1] = new int[3];

scores[1][0] = 3;
scores[1][1] = 4;
scores[1][2] = 5;
```

---

## 문자열 String

- 불변성 (Immutable): String 객체가 생성되면 그 객체의 문자열 값은 변경 불가, 문자열을 수저하려면 새로운 String 객체를 생성해야함
- 메모리 효율성 : 같은 값을 가진 String 리터럴은 같은 메모리에서 공유된다.
    
    *new 키워드로 새로운 String 객체 생성 시 다른 메모리 공간으로 분류
    

1. charAt(int index) : 문자열에서 특정 위치에 있는 문자(char)를 반환

```java
public static void exam1() {
        String str = "hello";

        for ( int i = 0; i < str.length(); i++ ) {
            char c = str.charAt(i);
            System.out.println(c);
        }
    }
```

1. substring(int beginIndex, int endIndex) : 문자열의 특정 부분을 추출하여 반환

```java
public static void exam2() {
        String str = "algorithm";
        String sub = str.substring(0, 5); // beginIndex 포함, endIndex 포함하지 않음 -> 0 ~ 4 algor
        System.out.println(sub);
    }
```

1. split(String regex) : 주어진 정규 표현식을 기준으로 문자열을 분리하여 배열로 반환

```java
public static void exam3() {
        String packet ="one#two#three";
        String[] packets = packet.split("#");
        for ( String p : packets ) {
            System.out.println(p);
        }
    }
```