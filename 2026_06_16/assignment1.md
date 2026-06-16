## 제네릭 Generic

- 자바에서 클래스, 인터페이스, 메서드를 정의할 때 사용할 데이터 타입을 지정하지 않고, **필요할 때 사용할 데이터 타입을 지정**할 수 있게 하는 기능
- 제네릭을 사용하면 컴파일 시 타입 안정성을 보장하고, 불필요한 타입 캐스팅을 줄일 수 있음

#### 제네릭의 주요 특징

- 타입 안정성 : 컴파일 시점에서 잘못된 타입 사용을 방지하여 런타임 에러를 줄일 수 있음
- 재사용성 : 동일한 코드가 다양한 데이터 타입에서 동작할 수 있도록 함
- 캐스팅 제거 : 불필요한 타입 캐스팅을 줄임

*선언 시 참조형만 가능! 기본형 int는 불가, Wrapper class Integer사용
*선언 후 값 대입 시는 가능

```java
// 선언 시 참조형만 사용 가능
ArrayList<Integer> list = new ArrayList<>();
// ArrayList<int> list = new ArrayList<>();

// 1. 사용 시 참조형 <-> 기본형 자동 변환
list.add(10);        // int 10이 Integer로 자동 변환됨
int num = list.get(0); // Integer가 int로 자동 변환됨

// 2. 직접 참조형 <-> 기본형 변환
(Integer).intValue    // Integer -> int
Integer.valueOf(int)  // int -> Integer
```

#### 제네릭 사용 예시

```java
// 제네릭 정의

// 1.
public class B_generic<T> {

    private T item;

    public void set(T item) {
        this.item = item;
    }

    public T get() {
        return item;
    }
    ...
    
// 2.
// Number 객체 내에서 제한, String 등 사용 시 컴파일 에러
public class B_generic_2 <T extends Number> {

    // 덧셈
    public T add(T num1, T num2) {
        if ( num1 instanceof Integer && num2 instanceof Integer ) {
            int result = num1.intValue() + num2.intValue();
            return (T) Integer.valueOf( result );
        } else if ( num1 instanceof Double && num2 instanceof Double ) {
            double result = num1.doubleValue() + num2.doubleValue();
            return (T) Double.valueOf( result );
        }

        throw new UnsupportedOperationException("지원되지 않는 타입입니다.");
    }
    ...
    
    // 제네릭 사용
    static void main(String[] args) {
        B_generic<Integer> intBox = new B_generic<>();
        intBox.set(10);
        System.out.println("Integer value: " + intBox.get());

        B_generic<String> stringBox = new B_generic<>();
        stringBox.set("Hello");
        System.out.println("String value: " + stringBox.get());
        
        exam2_2();
    }
 }
```

---

## 예외처리 (try-catch)

- 프로그램 실행 중에 발생할 수 있는 오류(예외)를 처리하며, 프로그램이 갑자기 멈추지 않도록 하는 방법
- 예외가 발생해도 프로그램이 정상적으로 흐름을 이어가거나, 안전하게 마무리할 수 있게 해줌

#### 예외(Exception)의 종류

- **Checked Exception**: 컴파일 시점에 처리(try-catch 또는 throws)를 강제하는 예외 (ex: IOException, FileNotFoundException)
- **Unchecked Exception**: 실행 중에 발생하며 처리를 강제하지 않는 예외 (RuntimeException 계열, ex: NullPointerException, ArithmeticException)

#### 기본 구조

- `try`: 예외가 발생할 수 있는 코드를 작성하는 블록, 해당 블록 내부에서 throw로 직접 예외 상황 정의할 수 있음
- `catch`: try에서 예외가 발생하면 그 예외를 잡아 처리하는 블록
- `finally`: 예외 발생 여부와 상관없이 항상 실행되는 블록으로 주로 자원 정리(파일 닫기, 연결 해제)에 사용

- `throw`: try 블록 안에서 내가 **직접 특정 상황을 예외**로 만들고, 그 예외를 catch에서 처리하도록 던지는 문법. 다만 catch가 없으면 해당 예외는 메서드를 호출한 쪽으로 전달
- `try-with-resource` : **사용 후 반드시 닫아야 하는 자원**을 자동으로 닫아주는 문법으로 주로 파일, 입출력 스트림, DB 연결 같은 자원을 사용할 때 사용
    - `try()`괄호 안에서 선언한 자원을 try 블록 종료 시 자동으로 `close()`해줌
    - `AutoCloseable`을 구현한 객체만 사용할 수 있으며, `finally`에서 직접 자원을 닫지 않아도 되어 코드가 간결하고 자원 해제 누락을 막을 수 있음
    - FileInputStream, FileReader, BufferedReader 등 대부분의 입출력 스트림은 이미 `AutoCloseable`를 구현하고 있음

`throw` vs `throws`

```java
// throw : 실제로 예외를 발생시킴
throw new IllegalArgumentException("잘못된 값");

// throws : 이 메서드에서 예외가 발생할 수 있다고 선언
public void test() throws Exception {
    throw new Exception("예외 발생");
}
```

#### 예외처리 사용 예시

```java
		// 1. 기본 try-catch
    public static void exam1() {
        try {
            int a = 10;
            int b = 0;
            int result = a / b; // 0으로 나누면 예외발생
            System.out.println(result);
        } catch (ArithmeticException e) {
            // 발생한 예외를 e로 받아서 처리
            System.out.println("예외 발생 : 0으로 나눌 수 없습니다.");
            System.out.println("예외 메시지 : " + e.getMessage());
        }

        System.out.println("프로그램은 계속 실행됩니다.");
    }

    // 2. 멀티 catch
    public static void exam2() {
        try {
            int[] arr = new int[5];
            arr[9] = 10;
        } catch ( ArithmeticException e ) {
            System.out.println("산술 예외 처리");
        } catch ( ArrayIndexOutOfBoundsException e ) {
            System.out.println("배열 인덱스 예외 처리 : " + e.getMessage());
        } catch ( Exception e ) {
            System.out.println("그 외 예외처리 : " + e.getMessage());
        }
    }

    // 2_2. 멀티 catch
    //	•	처리 방식이 같은 여러 예외는 |(파이프)로 묶어 하나의 catch에서 처리할 수 있다. (Java 7 이상)
    public static void exam2_2() {
        try {
            String text = null;
            System.out.println(text.length());
        } catch ( NullPointerException | ArithmeticException e) {
            System.out.println("Null 이거나 산술 예외 발생 : " + e.getMessage());
        }
    }

    // 3. finally
    //	•	finally 블록은 예외가 발생하든 안 하든, try/catch가 끝나면 "항상" 실행된다.
    //	•	파일 닫기, 연결 해제 등 반드시 처리해야 하는 마무리 작업에 사용한다.
    public static void exam3() {
        try {
            System.out.println("try 블록 실행");
            int result = 10 / 0;
        } catch (ArithmeticException e) {
            System.out.println("catch 블록 실행");
        } finally {
            // 예외가 발생해도, 발생하지 않아도 이 블록은 무조건 실행.
            System.out.println("finally 항상 실행됩니다.");
        }
    }

    // 4. throw / throws
    // 19세 체크
    private void checkAge(int age) {
        if (age < 19) {
            throw new IllegalArgumentException("미성년자는 가입할 수 없습니다 : " + age);
        }
        System.out.println("가입이 가능한 나이입니다.");
    }

    public static void exam4() {
        try {
            B_try_catch tryCatch = new B_try_catch();
            tryCatch.checkAge(18);
        } catch ( IllegalArgumentException e ) {
            System.out.println("나이 검증 실패 : " + e.getMessage());
        }
    }
    
    // 5. try-with-resources
    //	•	try( ... ) 괄호 안에서 자원(스트림 등)을 선언하면, try 블록이 끝날 때 자동으로 close()가 호출된다.
    //	•	AutoCloseable 인터페이스를 구현한 객체만 사용할 수 있다. (대부분의 입출력 스트림이 해당)
    //	•	finally에서 직접 close()를 호출하지 않아도 되므로 코드가 간결하고, 자원 해제 누락을 막을 수 있다.
    public static void exam5() {
        try ( FileInputStream fis = new FileInputStream("test.txt") ) {
            int data = fis.read();
            System.out.println("읽은 데이터 : " + data);
        } catch (IOException e) {
            System.out.println("파일 처리 중 예외 발생 : " + e.getMessage());
        }
        // 위 try가 끝나면(정상이든 예외든) fis.close()가 자동 호출
    }

    public static void exam5_2() {
        FileInputStream fis = null;

        try {
            fis = new FileInputStream("test.txt");
            int data = fis.read();
            System.out.println("읽은 데이터 : " + data);
        } catch (IOException e) {
            System.out.println("파일 처리 중 예외 발생 : " + e.getMessage());
        } finally {
            if ( fis != null ) {
                try {
                    fis.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }
```