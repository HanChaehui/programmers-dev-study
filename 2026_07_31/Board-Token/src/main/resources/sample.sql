CREATE DATABASE IF NOT EXISTS board_token
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE board_token;

-- JWT 인증에 사용하는 사용자 테이블
CREATE TABLE `user` (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(20) NOT NULL,
                        email VARCHAR(50),
                        user_id VARCHAR(50) NOT NULL UNIQUE,
                        password VARCHAR(100) NOT NULL,
                        role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER'
);

-- 게시글 테이블
CREATE TABLE board (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       title VARCHAR(200) NOT NULL,
                       content TEXT NOT NULL,
                       user_id VARCHAR(50) NOT NULL,
                       file_path VARCHAR(255),
                       created DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 댓글 테이블
CREATE TABLE comment (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         content TEXT NOT NULL,
                         user_id VARCHAR(50) NOT NULL,
                         created DATETIME DEFAULT CURRENT_TIMESTAMP,
                         board_id BIGINT NOT NULL,

                         CONSTRAINT fk_comment_board
                             FOREIGN KEY (board_id)
                                 REFERENCES board(id)
);

DESC board;
DESC user;
SELECT * FROM user;