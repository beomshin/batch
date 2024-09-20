package com.example.batch.repository;

import com.example.batch.entity.domain.BoardTbEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<BoardTbEntity, Long> {
}
