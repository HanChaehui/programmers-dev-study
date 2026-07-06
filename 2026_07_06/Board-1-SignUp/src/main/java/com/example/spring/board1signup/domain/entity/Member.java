package com.example.spring.board1signup.domain.entity;

import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30);
    private String userId;

    @Column(nullable = false, length = 50);
    private String password;

    @Column(nullable = false, length = 10);
    private String userName;

}
