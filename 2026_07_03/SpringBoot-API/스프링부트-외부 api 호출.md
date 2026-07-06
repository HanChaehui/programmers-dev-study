# Feign Client로 날씨 공공데이터 가져오기 (기상청 초단기실황)

> 수업에서 배운 **Feign Client**로, 기상청 공공 API를 호출해 **지금 서울의 실황 날씨**(기온·습도·강수 등)를 가져와요.
> Feign의 핵심은 **"HTTP 호출 코드를 직접 안 짜고, 인터페이스로 선언만 한다"**예요. 나머지 구현은 스프링이 대신 만들어 줘요.
> 이 API는 **서비스키 처리**에서 다들 한 번씩 막혀요. 그 함정을 미리 알고 가면 훨씬 수월해요.
>
> 💡 각 Step의 **힌트는 접혀 있어요.** 먼저 스스로 만들어 보고 막히면 펼치세요.

<details>
<summary>✅ 최종 완성 코드 보기 (먼저 직접 만들어 본 뒤 펼쳐서 비교하세요)</summary>

> Step 5까지 모두 반영한 **완성본**이에요. 파일별로 그대로 옮기면 동작합니다.
> DTO 6개는 각각 별도 파일로 두거나, 편의상 한 패키지에 모아 두세요. Lombok(`@Getter/@Setter/@ToString/@RequiredArgsConstructor`)이 설정돼 있어야 해요.

**`build.gradle`** (의존성)
```gradle
// Spring Initializr에서 'OpenFeign'을 추가하면 아래 + Spring Cloud BOM이 자동 설정됩니다.
implementation 'org.springframework.cloud:spring-cloud-starter-openfeign'
implementation 'org.springframework.boot:spring-boot-starter-web'
compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'
```

**`application.yml`**
```yaml
weather:
  api:
    key: 여기에_일반인증키(Decoding)_그대로_붙여넣기   # ★ 인코딩 키 아님, 디코딩 키!
```

**`WeatherApplication.java`** — Feign 스캔 켜기
```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients          // ★ 이게 있어야 @FeignClient 인터페이스를 찾아 구현해 준다
@SpringBootApplication
public class WeatherApplication {
    public static void main(String[] args) {
        SpringApplication.run(WeatherApplication.class, args);
    }
}
```

**`WeatherClient.java`** — Feign 인터페이스 (호출을 '선언'만 한다)
```java
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "weatherClient",
    url = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0"  // 서비스 URL까지만
)
public interface WeatherClient {

    // 요청주소의 마지막 조각. 파라미터는 우리가 배운 @RequestParam 그대로.
    @GetMapping("/getUltraSrtNcst")
    WeatherResponse getUltraSrtNcst(
        @RequestParam("serviceKey") String serviceKey,
        @RequestParam("numOfRows")  int numOfRows,
        @RequestParam("pageNo")     int pageNo,
        @RequestParam("dataType")   String dataType,   // "JSON"으로 보내야 DTO로 바로 받는다
        @RequestParam("base_date")  String baseDate,   // 파라미터명은 base_date (밑줄)
        @RequestParam("base_time")  String baseTime,   // base_time (밑줄)
        @RequestParam("nx")         int nx,
        @RequestParam("ny")         int ny
    );
}
```

**응답 DTO 6종** — JSON 층 구조와 똑같은 모양 (필드 이름 = JSON 키)
```java
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Getter @Setter @ToString
public class WeatherResponse {
    private Response response;   // 최상위 껍데기 (이미 제공된 클래스)
}

@Getter @Setter @ToString
class Response {
    private Header header;
    private Body body;
}

@Getter @Setter @ToString
class Header {
    private String resultCode;   // "00"이면 정상
    private String resultMsg;
}

@Getter @Setter @ToString
class Body {
    private Items items;
    private int pageNo;
    private int numOfRows;
    private int totalCount;
}

@Getter @Setter @ToString
class Items {
    private List<Item> item;     // item은 배열 → List
}

@Getter @Setter @ToString
class Item {
    private String baseDate;
    private String baseTime;
    private String category;     // T1H, REH, PTY ...
    private int nx;
    private int ny;
    private String obsrValue;    // 실황값(관측값)
}
```
> ※ 위처럼 한 파일에 모으려면 `WeatherResponse`만 `public`이고 나머지는 (같은 파일 내) 패키지-프라이빗이어야 해요. 각 클래스를 **별도 .java 파일**로 나누면 전부 `public class`로 두면 됩니다(더 일반적).

**`WeatherService.java`** — 발표시각 계산 + 호출 + 결과 확인 + 사람이 읽는 변환
```java
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherClient weatherClient;

    @Value("${weather.api.key}")
    private String serviceKey;

    // 원본 실황 목록 가져오기
    public List<Item> getCurrentWeather(int nx, int ny) {
        // 실황은 매시각 40분 이후 제공 → 40분 전이면 한 시간 전 자료를 요청
        LocalDateTime now = LocalDateTime.now();
        if (now.getMinute() < 40) {
            now = now.minusHours(1);      // 자정을 넘겨도 날짜가 알아서 하루 전으로
        }
        String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = now.format(DateTimeFormatter.ofPattern("HH")) + "00"; // 예: 1400

        WeatherResponse res = weatherClient.getUltraSrtNcst(
                serviceKey, 10, 1, "JSON", baseDate, baseTime, nx, ny);

        Header header = res.getResponse().getHeader();
        if (!"00".equals(header.getResultCode())) {   // "00"이 정상
            throw new RuntimeException("기상청 API 오류: "
                    + header.getResultCode() + " " + header.getResultMsg());
        }
        return res.getResponse().getBody().getItems().getItem();
    }

    // 코드값을 사람이 읽는 문장으로 변환
    public List<String> getReadableWeather(int nx, int ny) {
        List<Item> items = getCurrentWeather(nx, ny);
        List<String> result = new ArrayList<>();
        for (Item item : items) {
            String value = item.getObsrValue();
            switch (item.getCategory()) {
                case "T1H" -> result.add("기온: " + value + " ℃");
                case "REH" -> result.add("습도: " + value + " %");
                case "RN1" -> result.add("1시간 강수량: " + value + " mm");
                case "WSD" -> result.add("풍속: " + value + " m/s");
                case "PTY" -> result.add("강수형태: " + ptyText(value));
                default    -> { /* UUU, VVV, VEC 등은 생략 */ }
            }
        }
        return result;
    }

    private String ptyText(String code) {
        return switch (code) {
            case "0" -> "없음";
            case "1" -> "비";
            case "2" -> "비/눈";
            case "3" -> "눈";
            case "5" -> "빗방울";
            case "6" -> "빗방울눈날림";
            case "7" -> "눈날림";
            default  -> "알 수 없음(" + code + ")";
        };
    }
}
```

**`WeatherController.java`** — `/weather`로 확인
```java
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    // 사람이 읽는 목록으로 반환 (원본 JSON을 보고 싶으면 getCurrentWeather 사용)
    @GetMapping("/weather")
    public List<String> weather() {
        return weatherService.getReadableWeather(60, 127); // 서울
    }
}
```

**실행 결과(예시) — `GET http://localhost:8080/weather`**
```json
["기온: 18.5 ℃", "습도: 62 %", "1시간 강수량: 0 mm", "강수형태: 없음", "풍속: 2.3 m/s"]
```

> 값은 호출 시각·날씨에 따라 달라져요. 리스트가 채워져 나오면 성공이에요. 비어 있거나 오류가 나면 문서 아래 **"자주 나는 오류"** 표를 보세요.

</details>

---

## 0. 먼저 알아둘 점

- 이 과제는 **진짜 외부 API**를 부르기 때문에, 먼저 **공공데이터포털에서 서비스키를 발급**받아야 해요. (아래 Step 0에서 안내)
- 대상 API: 기상청 **초단기실황조회**(`getUltraSrtNcst`). "지금 이 시각의 실제 관측값"을 줘요.
  - 요청주소: `http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst`
  - 데이터 신청 페이지: https://www.data.go.kr/data/15084084/openapi.do
- ⚠️ **가장 중요한 함정**: 공공데이터 서비스키는 **인코딩 키**와 **디코딩 키** 두 종류로 줘요. Feign은 파라미터를 **자동으로 인코딩**하기 때문에, 우리는 **디코딩(Decoding) 키**를 넣어야 해요. 인코딩 키를 넣으면 **두 번 인코딩**돼서 인증에 실패해요.
- ⚠️ **좌표는 위도·경도가 아니에요.** 기상청은 자체 **격자 좌표(nx, ny)**를 써요. **서울 = nx 60, ny 127**.
- ⚠️ **실황은 최근 데이터만** 있어요. 발표 시각을 잘못 주면 데이터가 비어서 나와요(Step 3에서 자동 계산).

---

## 1. 무엇을 만드나요?

인터페이스 하나 선언하고 서비스에서 부르면, 콘솔/브라우저에 이런 실황 데이터가 나오게 만들어요.

**결과 예시 (`GET /weather`)**
```
기온(T1H): 18.5 ℃
습도(REH): 62 %
강수형태(PTY): 없음
1시간 강수량(RN1): 0 mm
풍속(WSD): 2.3 m/s
```

핵심은 **HTTP 호출 코드를 한 줄도 직접 안 짠다**는 거예요. `weatherClient.getUltraSrtNcst(...)`처럼 **메서드를 부르기만** 하면 스프링이 실제 통신을 해 줘요.

---

## 2. 학습 목표

| 개념 | 어디서 배우나 |
|------|------|
| 공공데이터 서비스키 발급 & 인코딩 함정 | Step 0 |
| `@FeignClient` 인터페이스 선언 | Step 1 |
| 쿼리 파라미터 = `@RequestParam` (배운 것과 동일) | Step 1 |
| 중첩 JSON 응답을 DTO로 매핑 | Step 2 |
| 발표시각 규칙에 맞춰 호출 + 결과코드 확인 | Step 3 |
| 실행 & 결과 확인 | Step 4 |
| 코드값(category)을 사람이 읽는 의미로 변환 | Step 5 |

---

## 3. 핵심 개념

### (1) Feign이 하는 일
예전엔 `RestTemplate`으로 URL 만들고, 파라미터 붙이고, 응답 받아 파싱하는 코드를 **손으로** 짰어요. Feign은 그걸 **선언형**으로 바꿔요.
```
"이런 주소에 이런 파라미터로 GET 요청을 보낸다" 를 인터페이스로 '선언'만 하면
→ 실제 호출 코드는 스프링이 프록시로 '구현'해 준다
```
우리가 배운 AOP 자동 프록시처럼, **인터페이스만 있고 실제 구현은 스프링이 런타임에** 만들어 줘요.

### (2) 쿼리 파라미터 = `@RequestParam`
API 주소 뒤 `?serviceKey=...&nx=60&ny=127` 부분이 쿼리 파라미터예요. Feign 인터페이스에선 이걸 **우리가 이미 배운 `@RequestParam`**으로 표현해요. 위치만 컨트롤러가 아니라 Feign 인터페이스일 뿐이에요.

### (3) 서비스키: 디코딩 키를 쓴다 (제일 중요!)
| | 인코딩 키 | 디코딩 키 |
|---|---|---|
| 생김새 | `%2B`, `%2F` 같은 게 섞여 있음 | `+`, `/`, `=` 원래 문자 |
| Feign에 넣으면 | 이미 인코딩된 걸 **또 인코딩** → 실패 | Feign이 한 번 인코딩 → **정상** |

→ **application.yml에는 반드시 "디코딩(Decoding) 키"를 붙여넣어요.**

### (4) 응답이 중첩되어 있으면, DTO도 중첩된다
이 API의 JSON은 `response → header/body → items → item[]` 구조예요. 그래서 자바 DTO도 **같은 모양으로 껍데기 클래스를 층층이** 만들어요. 준 코드의 `WeatherResponse{ Response response }`가 그 첫 껍데기예요.

```
[기억법] Feign=인터페이스로 선언(구현은 스프링) / 서비스키는 디코딩 / 좌표는 격자(서울 60,127)
```

---

## 4. 파일 구조 & 준비물

| 파일 | 역할 |
|------|------|
| `WeatherClient.java` | Feign 인터페이스 (API 호출 선언) |
| `WeatherResponse` 외 DTO들 | 중첩 JSON을 담을 그릇 |
| `WeatherService.java` | 발표시각 계산 + 호출 + 결과 확인 |
| `WeatherController.java` | `/weather`로 결과 확인 |
| `application.yml` | 서비스키 보관 |

**의존성** — Spring Initializr에서 **OpenFeign**을 추가하는 게 가장 안전해요(호환 버전 자동). 수동이면:
```gradle
implementation 'org.springframework.cloud:spring-cloud-starter-openfeign'
// + Spring Cloud BOM(dependencyManagement) 필요. Initializr로 만들면 자동 처리됨.
```

---

## 5. Step by Step

### Step 0. 서비스키 발급 + Feign 켜기

**할 일**
1. https://www.data.go.kr 로그인 → 위 데이터 페이지에서 **[활용신청]** (개발단계 자동승인).
2. 마이페이지 → 오픈API → 인증키에서 **일반 인증키(Decoding)**를 복사.
3. `application.yml`에 키를 넣고, 메인 클래스에 `@EnableFeignClients`를 붙여요.

<details>
<summary>💡 힌트 보기</summary>

```yaml
# application.yml
weather:
  api:
    key: 여기에_일반인증키(Decoding)_그대로_붙여넣기   # 인코딩 키 아님!
```
```java
// 메인 애플리케이션 클래스
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients            // ← 이게 있어야 Feign 인터페이스를 스캔해서 구현해 준다
@SpringBootApplication
public class WeatherApplication {
    public static void main(String[] args) {
        SpringApplication.run(WeatherApplication.class, args);
    }
}
```

승인 직후엔 키가 서버에 반영되기까지 몇 분~한 시간 걸릴 수 있어요. 인증 오류가 나면 잠시 후 다시 시도해요.

</details>

**확인**: `@EnableFeignClients`가 붙어 있고, application.yml에 **디코딩 키**가 들어갔으면 준비 완료예요.

---

### Step 1. Feign 클라이언트 인터페이스 만들기 (`WeatherClient.java`)

**목표**: "이 주소로, 이 파라미터로 GET 요청" 을 **인터페이스로 선언**해요. 구현은 안 짜요.

**할 일**
1. `@FeignClient(name, url)`을 붙인 인터페이스를 만들어요. url은 서비스 URL까지만.
2. `@GetMapping("/getUltraSrtNcst")` 메서드를 만들고, 파라미터들을 `@RequestParam`으로 받아요.
3. 파라미터 이름은 API 문서 그대로 써요. 특히 날짜/시각은 **`base_date`, `base_time`**(밑줄) 이에요.

<details>
<summary>💡 힌트 보기</summary>

```java
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "weatherClient",
    url = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0"  // 서비스 URL까지만
)
public interface WeatherClient {

    @GetMapping("/getUltraSrtNcst")   // 요청주소의 마지막 조각
    WeatherResponse getUltraSrtNcst(
        @RequestParam("serviceKey") String serviceKey,
        @RequestParam("numOfRows")  int numOfRows,
        @RequestParam("pageNo")     int pageNo,
        @RequestParam("dataType")   String dataType,
        @RequestParam("base_date")  String baseDate,   // 파라미터명은 base_date
        @RequestParam("base_time")  String baseTime,   // base_time
        @RequestParam("nx")         int nx,
        @RequestParam("ny")         int ny
    );
}
```

반환 타입을 **`WeatherResponse`(DTO)로 바로** 두면, Feign이 JSON을 알아서 그 DTO로 바꿔 줘요. (수업에서 본 "String으로 받아 ObjectMapper로 파싱"을 Feign이 대신 해 주는 셈이에요.) 그러려면 `dataType`을 `"JSON"`으로 보내야 해요.

</details>

**확인**: 인터페이스에 `@FeignClient`가 붙고, 메서드에 `@RequestParam`이 8개 있으면 돼요. 아직 실행은 안 돼요(DTO가 없으니까).

---

### Step 2. 응답 DTO 만들기 — 중첩 구조 그대로 (`WeatherResponse` 등)

**목표**: JSON의 층 구조와 **똑같은 모양으로** 그릇 클래스를 만들어요.

응답 JSON은 이렇게 생겼어요.
```json
{ "response": {
    "header": { "resultCode": "00", "resultMsg": "NORMAL_SERVICE" },
    "body": {
      "items": { "item": [
        { "baseDate":"20241008","baseTime":"1400","category":"T1H","nx":60,"ny":127,"obsrValue":"18.5" }
      ] },
      "pageNo":1, "numOfRows":10, "totalCount":8
} } }
```

**할 일**: `WeatherResponse → Response → Header/Body → Items → Item` 순서로 클래스를 만들어요. 필드 이름은 JSON 키와 **똑같이**.

<details>
<summary>💡 힌트 보기</summary>

```java
// 이미 준 것
@Getter @Setter @ToString
public class WeatherResponse { private Response response; }

@Getter @Setter @ToString
public class Response {
    private Header header;
    private Body body;
}

@Getter @Setter @ToString
public class Header {
    private String resultCode;   // "00"이면 정상
    private String resultMsg;
}

@Getter @Setter @ToString
public class Body {
    private Items items;
    private int pageNo;
    private int numOfRows;
    private int totalCount;
}

@Getter @Setter @ToString
public class Items {
    private java.util.List<Item> item;   // 배열이라 List
}

@Getter @Setter @ToString
public class Item {
    private String baseDate;
    private String baseTime;
    private String category;    // T1H, REH, PTY ...
    private int nx;
    private int ny;
    private String obsrValue;   // 실황값(관측값)
}
```

핵심은 **필드 이름 = JSON 키**예요(대소문자까지). 이름만 맞으면 Jackson이 알아서 채워 줘요. `item`은 배열이므로 `List<Item>`으로 받아요.

</details>

**확인**: 클래스 6개가 층층이 만들어졌으면 돼요. 아직 값은 안 채워져요(호출 전이니까).

---

### Step 3. 서비스 — 발표시각 계산 + 호출 + 결과 확인 (`WeatherService.java`)

**목표**: 지금 시각에 맞는 `base_date/base_time`을 만들어 API를 부르고, **결과 코드가 정상(00)인지** 확인해요.

**할 일**
1. `@Value("${weather.api.key}")`로 서비스키를 주입받아요.
2. 발표시각을 계산해요: **현재 분이 40분 미만이면 한 시간 전**으로. (실황은 매시각 40분 이후에 제공돼요.)
3. `weatherClient.getUltraSrtNcst(...)`를 부르고, `resultCode`가 "00"이 아니면 예외로 알려요.
4. `item` 리스트를 반환해요.

<details>
<summary>💡 힌트 보기</summary>

```java
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherClient weatherClient;

    @Value("${weather.api.key}")
    private String serviceKey;

    public List<Item> getCurrentWeather(int nx, int ny) {
        // 발표시각 계산: 40분 전이면 아직 이번 시각 자료가 없으니 한 시간 전으로
        LocalDateTime now = LocalDateTime.now();
        if (now.getMinute() < 40) {
            now = now.minusHours(1);
        }
        String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = now.format(DateTimeFormatter.ofPattern("HH")) + "00"; // 예: 1400

        WeatherResponse res = weatherClient.getUltraSrtNcst(
                serviceKey, 10, 1, "JSON", baseDate, baseTime, nx, ny);

        // 결과 코드 확인: "00"이 정상. 아니면 메시지를 그대로 알려 준다.
        Header header = res.getResponse().getHeader();
        if (!"00".equals(header.getResultCode())) {
            throw new RuntimeException("기상청 API 오류: "
                    + header.getResultCode() + " " + header.getResultMsg());
        }

        return res.getResponse().getBody().getItems().getItem();
    }
}
```

`if (분 < 40) 한 시간 전`이 핵심이에요. 이걸 빼고 아무 시각이나 넣으면 `resultCode 03`(NO_DATA)로 데이터가 비어서 와요. `minusHours(1)`은 자정을 넘겨도 날짜를 알아서 하루 전으로 돌려 줘요.

</details>

**확인**: 컴파일이 되면 돼요. 실제 호출 확인은 다음 Step에서.

---

### Step 4. 실행하고 결과 확인 (`WeatherController.java`)

**목표**: `/weather`로 들어가면 서울의 실황 데이터가 나오게 해요.

**할 일**
1. `@RestController`를 만들고 서비스를 주입받아요.
2. `GET /weather`에서 서울 좌표(60, 127)로 서비스를 호출해 결과를 반환해요.

<details>
<summary>💡 힌트 보기</summary>

```java
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/weather")
    public List<Item> weather() {
        return weatherService.getCurrentWeather(60, 127); // 서울
    }
}
```

`@RestController`는 반환한 객체(List)를 **JSON으로 그대로** 응답해 줘요. 브라우저에서 `http://localhost:8080/weather`로 확인해요.

</details>

**확인**: 브라우저나 콘솔에 `category`/`obsrValue`가 여러 개 담긴 JSON이 나오면 **호출 성공**이에요! 🎉 값이 비어 있으면 아래 "자주 나는 오류"를 보세요.

---

### Step 5. 코드값을 사람이 읽는 의미로 (`WeatherService`)

**목표**: `T1H`, `REH` 같은 코드는 사람이 못 알아봐요. 한글 의미로 바꿔서 예쁘게 보여줘요.

**할 일**
1. `category` 코드를 한글로 바꾸는 변환을 만들어요. (T1H=기온, REH=습도, RN1=1시간 강수량, WSD=풍속, PTY=강수형태 …)
2. `PTY`(강수형태)는 값도 코드예요: 0=없음, 1=비, 2=비/눈, 3=눈, 5=빗방울, 6=빗방울눈날림, 7=눈날림.
3. `"기온: 18.5 ℃"`처럼 정리한 문자열 목록을 반환하는 메서드를 추가해요.

<details>
<summary>💡 힌트 보기</summary>

```java
import java.util.ArrayList;

public List<String> getReadableWeather(int nx, int ny) {
    List<Item> items = getCurrentWeather(nx, ny);
    List<String> result = new ArrayList<>();

    for (Item item : items) {
        String category = item.getCategory();
        String value = item.getObsrValue();
        switch (category) {
            case "T1H" -> result.add("기온: " + value + " ℃");
            case "REH" -> result.add("습도: " + value + " %");
            case "RN1" -> result.add("1시간 강수량: " + value + " mm");
            case "WSD" -> result.add("풍속: " + value + " m/s");
            case "PTY" -> result.add("강수형태: " + ptyText(value));
            default -> { /* UUU, VVV, VEC 등은 생략 */ }
        }
    }
    return result;
}

private String ptyText(String code) {
    return switch (code) {
        case "0" -> "없음";
        case "1" -> "비";
        case "2" -> "비/눈";
        case "3" -> "눈";
        case "5" -> "빗방울";
        case "6" -> "빗방울눈날림";
        case "7" -> "눈날림";
        default  -> "알 수 없음(" + code + ")";
    };
}
```

컨트롤러에서 이 메서드를 쓰면 첫 화면(1번 항목)처럼 사람이 읽는 목록이 나와요. 코드 → 의미 매핑은 API 문서의 "실황 자료구분(category)" 표를 참고해요.

</details>

**확인**: `/weather` 결과가 `기온: 18.5 ℃`, `습도: 62 %`처럼 사람이 읽는 형태로 나오면 완성이에요.

---

## ⚠️ 자주 나는 오류 (막히면 여기부터)

| 증상 | 원인 / 해결 |
|------|------|
| 인증 실패 / `SERVICE KEY IS NOT REGISTERED` | ① **인코딩 키**를 넣었을 확률이 높아요 → **디코딩 키**로 교체. ② 활용신청 승인 반영 전일 수 있어요(잠시 후 재시도). |
| `resultCode 03` (NO_DATA), 데이터가 비어 옴 | 발표시각 문제. Step 3의 "분<40이면 한 시간 전" 규칙을 적용했는지 확인. 미래 시각·먼 과거는 안 돼요. |
| JSON이 아니라 XML/에러 문서가 옴 | `dataType`을 `"JSON"`으로 보냈는지 확인. |
| DTO 필드가 다 null | JSON 키와 **필드 이름/철자**가 다른 거예요. `obsrValue` 같은 이름을 정확히 맞춰요. |
| 데이터는 오는데 값이 이상 | `nx/ny`가 위경도 아님을 기억! 서울은 **60, 127**. |

---

## 6. 학습 체크

- [ ] Feign이 "인터페이스 선언 → 구현은 스프링"이라는 걸 설명할 수 있다
- [ ] 쿼리 파라미터를 `@RequestParam`으로 표현하는 걸 안다
- [ ] 서비스키는 왜 **디코딩 키**를 써야 하는지 안다(이중 인코딩)
- [ ] 중첩 JSON을 DTO로 매핑할 때 "필드 이름 = JSON 키"임을 안다
- [ ] 실황 발표시각 규칙(40분)을 왜 맞춰야 하는지 안다

## 7. 최종 완성 체크리스트

- [ ] `/weather` 호출 시 실황 데이터가 JSON으로 나온다
- [ ] `resultCode`가 "00"이고, 여러 category 값이 채워져 있다
- [ ] 코드값이 `기온: … ℃`처럼 사람이 읽는 형태로 변환된다
- [ ] 서비스키·좌표·발표시각 관련 오류를 스스로 진단할 수 있다