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

