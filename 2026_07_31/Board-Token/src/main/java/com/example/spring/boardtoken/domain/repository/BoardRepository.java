package com.example.spring.boardtoken.domain.repository;

import com.example.spring.boardtoken.domain.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long>, BoardRepositoryCustom {
}