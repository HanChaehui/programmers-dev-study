## 시스템 아키텍처

```mermaid
flowchart LR
    A[Client<br/>HTML / CSS / JavaScript]
    -->|로그인 요청| B[Spring Boot]

    B --> C[UserController / BoardController]
    C --> D[Service]
    D --> E[Repository]
    E --> F[(MySQL)]

    D --> G[TokenProvider]
    G -->|Access Token 발급| A
    G -->|Refresh Token 쿠키 저장| A

    A -->|Authorization: Bearer Access Token| H[TokenAuthenticationFilter]
    H -->|토큰 검증 성공| C

    C -->|게시글 / 댓글 작성자| I[로그인 사용자 정보]
    I --> D

사용자가 로그인하면 서버에서 Access Token과 Refresh Token을 발급한다.
Access Token은 localStorage에 저장하고, Refresh Token은 쿠키에 저장한다.

게시판 API 요청 시 Access Token을 Authorization 헤더에 포함하여 전송한다.
TokenAuthenticationFilter에서 토큰을 검증한 후 게시글과 댓글 기능을 처리한다.

게시글과 댓글의 작성자는 클라이언트가 직접 전달하지 않고, JWT에서 확인한 로그인 사용자 정보를 사용한다. 게시글 수정과 삭제는 작성자 본인만 가능하다.
