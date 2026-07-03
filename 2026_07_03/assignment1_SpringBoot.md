## 디스패쳐 서블릿 (DispatcherServlet) - 요청 처리의 심장

- 스프링 MVC로 들어오는 **모든** HTTP 요청은 먼저 Dispatcherervlet 하나를 거친다.
- 그래서 이 서블릿을 ‘프론트 컨트롤러’라고 부른다. (프론트 컨트롤러 패턴)
- 개발자가 만들지 않아도 스프링 부트가 자동 구성으로 미리 등록해준다.

요청 하나가 들어와 응답이 나가기까지의 흐름은 다음과 같다.

1. 브라우저 요청이 (필터를 지나) DispatcherServlet에 도착한다.
2. HandlerMapping에게 “이 URL(/login)을 처리할 컨트롤러가 누구지?”하고 묻는다.
3. HandlerAdapter를 통해 실제 컨트롤러 메서드를 호출한다.
이때, 파라미터(@RequestParam, HttpSession 등)를 알맞게 만들어 넣어준다.
4. 컨트롤러가 값을 반환하면, 그 반환값을 어떻게 처리할지 갈린다.
- @Controller + 뷰 이름 → ViewResolver가 templates/이름.html을 찾는다.
- @RestController/@ResponseBody → HttpMessageConverter가 데이터(문자열/JSON)로 변환한다.
5. 최종 결과(HTML 또는 데이터)를 응답으로 만들어 브라우저에 돌려준다.
핵심은, 우리가 @GetMapping 메서드만 작성하면 그 앞뒤의 ‘분배와 변환’은 DispatcherServlet 이 정해진 순서대로 대신 처리해 준다는 점이다.
아래의 @Controller / @RestController 차이도, 결국 4번 단계에서 DispatcherServlet 이 반환값을 뷰로 볼지 데이터로 볼 지를 가르는 이야기다.

## `@Controller`와 `@RestController`의 차이

둘 다 웹 요청을 받아 처리하는 컨트롤러지만, 메서드가 반환되는 String을 ‘어떻게 해석하느냐’가 다르다.

`@Controller` : 반환하는 String을 ‘뷰(View)의 이름’으로 본다.

- 그래서 SessoinController, CookieController처럼 HTML 페이지를 보여 줄 때 사용한다.

`@RestController` : 반환하는 String(또는 객체)을 ‘데이터 그 자체’로 본다.

- return “Hello World!”는 뷰를 찾지 않고 그 글자를 그대로 응답 본문에 보여줄 때 사용한다.
- 객체를 반환하면 JSON으로 변환해주므로 REST API를 만들때 쓴다.
- 사실 `@RestController` 는 `@Controller`  + `@ResponseBody` 를 합친 것이다.

@Controller 에서도 메서드에 @ResponseBody 를 붙이면 데이터를 그대로 반환할 수 있다. 즉, @RestController 는 "이 클래스의 모든 메서드는 데이터를 반환한다"는 선언인 셈이다. 

정리 : 화면(HTML)을 보여 주려면 @Controller, 데이터(JSON/문자열)를 내려 주려면 @RestController 를 쓴다.

## 필터 - 디스패쳐 서블렛 앞단에서 실행

요청이 디스패쳐 서블렛에 도착하기도 전에 먼저 거치는 관문이 필터이다.

서블렛 컨테이너(톰캣) 수준에서 동작하며, 스프링 MVC의 바깥에 위치한다.

즉, 필터는 요청이 들어올 때와 응답이 나갈 때를 모두 가로챌 수 있다.

#### 하는 일

- 모든 요청에 공통으로 필요한 처리를 컨트롤러보다 먼저 해치운다.
(예: 인증/인가 검사, 요청 로깅, 문자 인코딩(UTF-8) 설정, CORS 처리 등)
- 문지기라서, 통과시키지 않고 여기서 바로 응답을 돌려보내며 막을 수도 있다.
(예: 로그인 안 된 요청을 컨트롤러까지 보내지 않고 필터에서 차단한다.)

#### 만드는 법

- jakarta.servlet.Filter를 구현하고 doFilter()안에 로직을 작성한다.
- doFilter()안에서 chain.doFilter(request, response)를 호출해야 '다음 단계 (다음 필터 또는 DispatcherServlet)'로 요청이 넘어간다. 
이 호출을 하지 않으면 요청은 여기서 멈춘다.

#### 등록 방식 - FilterRegistrationBean으로 수동 등록

- @Component와 달리 @Configuration은 **적용할 URL 패턴**과 **실행 순서**를 직접 지정할 수 있다.
- 필터가 여러 개일 때 순서를 제어하거나, 특정한 경로에만 적용하고 싶을 때 사용한다.

#### 뷰 (View)

사용자에게 보여줄 HTML 화면 (컨트롤러가 처리한 결과를 사용자에게 보여주는 HTML 화면)

<aside>

Controller → 요청을 받음
Model      → 화면에 전달할 데이터
View       → 실제로 사용자에게 보여줄 HTML 화면

</aside>

API……………lombok

[feign-client  port: 8081] → [feign-api  port: 8083]

#### 외부(다른 서버)와 통신하는 기술들

- **RestTemplate :** 
동기 HTTP클라이언트. 가장 오래된 방식이며 현재는 유지보수 모드(레거시)
- **WebClient** : 
비동기/논블로킹 HTTP 클라이언트.리액티브(WebFlux)스택용
- **RestClient :** 
Spring 6.1+신규 동기 HTTP클라이언트. RestTemplate의 현대적 대체제
- **Feign** : 
인터페이스 선언만으로 호출하는 선언형 HTTP클라이언트 (←이 프로젝트에서 사용)
- **gRPC :** 
HTTP/2 + Protobuf(바이너리)기반 RPC.주로 MSA내부 서비스 간 고속 통신에 사용.
- **메시지 큐(Kafka, RabbitMQ, SQS) :** 
요청-응답이 아닌 비동기 메시징(이벤트 발행/구독)방식.

위쪽 4개는 REST(HTTP+JSON)계열이고, gRPC와 메시지 큐는 통신 방식 자체가 다르다.

## Feign Client

Feign은 “다른 서버의 API를 호출하는 코드”를 인터페이스 선언만으로 자동 생성해주는 HTTP 클라이언트이다.

- 원래는 RestTemplate/WebClient로 URL/헤더/파라미터를 직접 조립해야 하지만, Feign은 “어디로 요청을 보낼지”만 인터페이스에 선언하면 구현은 Spring이 대신 만들어준다.
- 요약 : HTTP 호출을 그냥 자바 메서드 호출처럼 쓰게 해주는 도구, 서버간 통신을 위한 도구

#### `@EnableFeignClients`

`@FeignClient` 인터페이스들을 찾아 구현 객체(Bean)로 만들라고 지시하는 “스위치”
→ 없으면 Feign이 아예 동작하지 않는다.

#### Feign Client 사용법

1. build.gradle에 spring-cloud-starter-openfeign 의존성 추가
2. 시작 클래스(Application코드)에 @EnableFeignClients 를 붙여 Feign 기능을 켠다.
3. @FeignClient 를 붙인 interface를 만들고, 호출할 API를 @GetMapping/@PostMapping 등으로 선언한다.
(구현 클래스는 만들지 않는다. 필요한 곳에서 그냥 주입받아 메서드를 호출하면 된다.)

#### Feign Client 동작 메커니즘

- @EnableFeignClients가 @FeignClient 인터페이스들을 스캔한다.
- Spring이 런타임에 각 인터페이스의 "프록시(proxy) 구현체"를 만들어 Bean으로 등록한다.
- 그 메서드를 호출하면 프록시가 애너테이션 정보(url, 경로, 파라미터)를 실제 HTTP 요청으로 바꿔 보내고, 응답을 반환 타입으로 변환해 돌려준다.
- 이 프로젝트의 호출 흐름: [사용자] → Controller → Service → Client(@FeignClient) → [외부 서버]

#### Feign Client 선언부

- interface이므로 우리가 직접 구현하지 않는다.
- 즉, “다른 서버로 HTTP 요청을 보내는 코드”를 인터페이스 선언만으로 대신 만들어주는 것

#### 사용 예시

1. url 하드코딩
    
    `@FeignClient(name = "exampleClient", url = "http://localhost:8083")`
    
2. application.yaml 내부에 url 템플릿화 (설정으로 분리)
    
    `@FeignClient(name = "exampleClient", url = "${feign-api.url}")`
    
    ```yaml
    feign-api:
      url: http://localhost:8083
    ```
    
- name : 이 클라이언트의 고유 이름(Bean), 필수 값
- url : 호출할 대상 서버 주소

## DTO (Data Transfer Oject) - 데이터 전송용 객체

계층 간 또는 서버 간에 데이터를 담아 주고받을 때 사용

POST 등으로 요청 본문(body)에 데이터를 실어 보낼 때 활용된다.

ExampleController - createData

POST는 파라미터 @RequestParam 대신 @RequestBody로 받아야함….?