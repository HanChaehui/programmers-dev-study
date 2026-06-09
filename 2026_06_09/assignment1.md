## Java 객체지향 프로그래밍

#### 객체지향 프로그래밍 (Object Oriented Programming, OOP)

- 프로그램을 여러 개의 객체(Object)로 나누어 작성하는 방법을 말한다.
- 객체지향 프로그래밍은 코드의 **재사용성**과 **유지보수성**을 높이고, 복잡한 문제를 쉽게 해결할 수 있도록 하는 데에 중점을 둔다.

#### 클래스 Class

- 객체를 정의해 놓은 것, 객체를 생성하는데 사용
- 구성 요소 : 멤버변수, 메서드, 생성자

#### 객체 Object

- 실제로 존재하는 것, 사물 또는 개념

#### 인스턴스 Instance

- 클래스로부터 객체를 만드는 과정
- 클래스 Class → 인스턴스화 → 인스턴스(객체)
- new로 생성 → 힙 영역에 할당됨

#### 메서드 method

- 특정 작업을 수행하는 일련의 기능들을 하나로 묶은 것

#### 메서드를 사용하는 이유

1. 높은 재사용성
2. 중복된 코드의 제거
3. 프로그램의 구조화

#### 메서드의 선언과 구현

메서드는 크게 ‘선언부’와 ‘구현부’ 두 부분으로 이루어짐

- 메서드 선언부 : ‘메서드의 이름’과 ‘매개변수 선언’, 그리고 ‘반환타입’으로 구성되어 있음
    
    메서드가 작업을 수행하기 위해 어떤 값들을 필요로 하고 작업의 결과로 어떤 타입의 값을 반환하는지에 대한 정보를 제공
    
- 메서드 호출 : 메서드를 정의 했어도 호출되지 않으면 아무 일도 일어나지 않는다.

#### 멤버 변수

1. 인스턴스 변수 : 
    - 클래스의 인스턴스를 생성해야 사용 가능 (인스턴스명.인스턴스변수)
2. 클래스 변수 : 
    - `static` 키워드를 붙임, **모든 인스턴스가 공통된 저장공간(변수)를 공유함**
    - 한 클래스의 모든 인스턴스들이 공통적인 값을 유지해야하는 속성의 경우, 클래스 변수로 선언해야함
    - **인스턴스를 생성하지 않고도 언제라도 바로 사용할 수 있음** (클래스명.클래스변수)
    - 클래스가 메모리에 ‘로딩’될 때 생성되어 프로그램이 종료될 때까지 유지되며, `public` 키워드를 붙이면 같은 프로그램 내에서 어디서나 접근 가능한 ‘전역 변수’의 성격을 가짐
3. 지역 변수 : 
    - 메서드 내에 선언되어 메서드 내에서만 사용 가능하며, 메서드가 종료되면 소멸되어 사용할 수 없게 됨
    - {} 블럭을 벗어나면 소멸되어 사용할 수 없게 됨

## JVM의 메모리 구조

1. 메서드 영역 : 
    - 프로그램 실행 중 어떤 클래스가 사용되면, JVM은 해당 클래스의 클래스 파일을 읽고 분석하여 **클래스에 대한 정보**를 메서드 영역에 저장
    - 이때 그 클래스의 **클래스 변수**도 이 영역에 함께 생성됨
2. 힙(Heap) 영역 : 
    - 프로그램 실행 중 생성되는 **인스턴스**는 모두 이곳에 생성됨
    - 즉, **인스턴스 변수**들이 생성되는 곳
3. 호출 스택(Call Stack) : 
    - 메서드가 호출되면 수행에 필요한 만큼의 메모리를 스택에 할당받음
    - 이 메모리는 메서드가 작업을 수행하는 동안 **매개변수를 포함한 지역 변수**들과 **연산의 중간 결과** 등을 저장하는 데 사용
    - 메서드 호출 종료 시 사용했던 메모리를 반환하고 스택에서 제거
    - 호출 스택의 제일 위에 있는 메서드가 현재 실행 중인 메서드

## 참조형 - Shallow Copy vs Deep Copy

```java
public static void exam3() {
        // Shallow Copy
        Data d1 = new Data();
        Data d2 = d1; // &123
        // Deep Copy
        Data d3 = copy(d1);
        d1.x = 10;
        System.out.println(d1.x);
        System.out.println(d2.x);
        System.out.println(d3.x);
    }
    
// Deep Copy method
public static Data copy(Data d) {
        Data temp = new Data();
        temp.x = d.x;
        return temp;
    }
```

* 기본형 매개 변수 (Call by value): 변수의 값을 읽기만 할 수 있다.
* 참조형 매개 변수 (Call by reference): 변수의 값을 읽고 변경할 수 있다.

## 재귀호출 (Recursive Call)

메서드 내부에서 메서드 자신을 다시 호출하는 것을 ‘재귀 호출’이라고 하고, 재귀 호출을 하는 메서드를 ‘재귀 메서드’라고 한다.

- 기저 조건(종료 조건)이 필요

```java
	static int factorial(int n) {
        // 기저조건 -> 탈출조건
        if (n == 1) {
            return 1;
        }
        // 자기 자신으로 호출
        return n * factorial(n - 1);
    }
```

## 정적 메서드 Static method

- 자바에서 `static` 키워드로 선언된 메서드를 의미하며 **클래스 메서드**라고도 불림
- 클래스 변수처럼 인스턴스를 생성하지 않고도 클래스명을 통해 직접 호출 가능 (클래스명.메서드)
- 주로 **유틸리티 성격의 메서드**나, **클래스 자체의 특성과 관련된 기능을 제공하는 메서드**를 정의할 때 사용됨

주요 특징

1. 인스턴스 필요 없음 : **인스턴스 생성 없이 클래스명으로 호출 가능**
2. 정적 변수와 상수만 접근 가능 : 정적 메서드는 클래스의 **다른 정적 멤버(정적 변수/다른 정적 메서드)와만 상호작용이 가능**, **인스턴스 변수나 인스턴스 메서드에는 직접 접근 불가**
3. 유틸리티 메서드로 자주 사용 : 자주 사용되거나 공통적인 작업을 수행하는 유틸리티 메서드는 일반적으로 정적 메서드로 구현
    
    *예를 들어 Math클래스의 Math.sqrt() 메서드처럼 인스턴스와 관련이 없는 계산 작업이 대다수
    

## 정적 클래스 vs 비정적 클래스 - 중첩 클래스

- 자바에서는 클래스 안에 다른 클래스를 정의할 수 있음
- <중첩이 아닌 경우> 정적 메서드에서는 인스턴스 변수 바로 접근 불가능
- <중첩 클래스> 정적 중첩 클래스 내부 메서드는 바깥 클래스의 인스턴스 변수 바로 접근 불가능
- static → 인스턴스를 생성하지 않아도 선언하자마자 살아있음
- static영역(클래스/메서드) → 특정 객체 변수 접근하려면 static만 가능
- static → 소속 영역이 없고 독립

#### 정적 중첩 클래스

- 중첩된 클래스가 `static`으로 선언되면 해당 클래스는 정적 중첩 클래스가 됨
- **Outer 클래스의 static 변수에만 접근 가능, 인스턴스 변수는 불가능**
- 인스턴스를 사용하기 위해 **Outer 클래스의 인스턴스가 필요하지 않음,** Outer 클래스명으로 직접 인스턴스 생성이 가능
(Outer 클래스의 인스턴스와 독립적)
- 메모리 관리와 성능 측면에서 정적 중첩 클래스가 더 효율적일 수 있으므로 Outer 클래스의 인스턴스 데이터에 접근할 필요가 없는 경우, 정적 중첩 클래스를 사용하는 것이 더 적합

#### 비정적 중첩 클래스

- **Outer 클래스의 static변수와 인스턴스 변수 모두 접근 가능**
- 인스턴스를 사용하기 위해 **Outer 클래스의 인스턴스가 필요** 
(Outer 클래스의 인스턴스에 속함)
- Outer 클래스의 인스턴스와 연결되어 인스턴스당 추가적인 메모리를 소비할 수 있으므로 Outer 클래스의 인스턴스 데이터에 접근해야 하는 경우에만 사용하는 것이 좋음

```java
class OuterClass {

    private int instanceVariable = 10;
    private static int staticVariable = 20;

    // 자바에서는 클래스 안에 다른 클래스를 정의할 수 있다.
    // static -> 정적 중첩 클래스
    static class StaticNestedClass {

        void display() {
            // 정적 중첩 클래스에서는 바깥 클래스의 static변수에만 접근 가능
            System.out.println(staticVariable);
            // 바깥 클래스의 인스턴스 변수는 접근 불가능
						// System.out.println(instanceVariable);
        }

    }

    class InnerClass {
        void display() {
            System.out.println(staticVariable);
            System.out.println(instanceVariable);
        }
    }
}

... 

static void main(String[] args) {
        // Static Nested Class의 인스턴스를 생성하기 위해서는 바깥 클래스의 인스턴스가 필요하지 않음
        OuterClass.StaticNestedClass staticNestedClass = new OuterClass.StaticNestedClass();
        staticNestedClass.display();

        OuterClass outerClass = new OuterClass();
        OuterClass.InnerClass innerClass = outerClass.new InnerClass();
        innerClass.display();
    }
```