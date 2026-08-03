package com.example.spring.boardtoken.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "board")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            length = 200
    )
    private String title;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String content;

    @Column(
            nullable = false,
            length = 50
    )
    private String userId;

    @Column(length = 255)
    private String filePath;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime created;

    @OneToMany(
            mappedBy = "board",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    public void update(
            String title,
            String content,
            String filePath
    ) {
        this.title = title;
        this.content = content;
        this.filePath = filePath;
    }
}