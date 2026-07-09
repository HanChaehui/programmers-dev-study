package com.example.spring.board1signup.domain.repository;

import com.example.spring.board1signup.domain.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board,Long> {
}
