package com.example.spring.boardtoken.domain.repository;

import com.example.spring.boardtoken.domain.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}