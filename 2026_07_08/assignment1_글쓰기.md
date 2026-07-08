spring-boot/board/basic-board

#### `MultipartFile` - BoardWriteRequestDto

multipart/form-data로 업로드된 "파일 한 개"를 스프링이 감싸서 넘겨주는 타입이다. 파일의 바이트뿐만 아니라 메타데이터도 함께 들고 있다.

자주 쓰는 메서드:

- getOriginalFilename() :업로드된 원본 파일명 (예: "고양이.png")
- getContentType()      : MIME타입 (예: "image/png")
- getSize()             :파일 크기(byte)
- isEmpty()             :파일을 안 골랐거나 빈 파일이면 true
- getInputStream()      :내용을 읽는 스트림
- transferTo(dest)      :실제 디스크 경로로 저장

#### `@ModelAttribute` - BoardWriteRequestDto

form 필드가 같은 이름의 필드에 자동으로 채워진다.

- 파일은 JSON에 못 실으므로 multipartFile + `@ModelAttribute`로 쓴다.
- 그래서 아래 필드 이름은 화면 폼의 name 속성과 똑같아야 한다.
- `@Setter` / `@NoArgsConstructor` : `@ModelAttribute`는 기본 생성자로 객체를 만든 뒤 setter로 값을 하나씩 채우는 방식이다.
- 그래서 응답 DTO들처럼 @Builder만 있으면 안된다.

#### `transferTo()` vs `getBytes() + File.write()`  - FileService

`getBytes() + File.write()` 방식

```
byte[] bytes = file.getBytes(); Files.write(...);
```

메모리 문제 : getBytes()는 파일 "전체"를 byte[]로 힙 메모리에 올린다. 
→ 큰 파일/동시 업로드 시 OutOfMemory위험

`transferTo()` 방식

통째로 올리지 않고 옮기며, 같은 디스크면 복사가 아니라 이동 방식이라 가볍고 빠르다.

#### `ResponseEntity` - BoardApiController

ResponseEntity는 HTTP응답의 3가지를 직접 제어하게 해주는 상자이다.

[상태코드] + [헤더] + [본문(body)]

- 그냥 Resource만 리턴하면 파일 내용은 내려가지만,
- Content-Disposition : attachment헤더를 붙일 방법이 없다.
- → 그러면 다운로드가 아니라 브라우저가 파일을 그냥 열어버리고,저장 파일명도 정할 수 없다.

#### `@RequestBody` - BoardDeleteRequestDto

컨트롤러에서 `@RequestBody`로 받으면, JSON을 Jackson라이브러리가 이 객체(Request DTO)로 바꾼다. (역직렬화)
Jackson은 setter로 값을 채운다.