package com.pharmacore.pos.repository;

import com.pharmacore.pos.entity.StockTakeSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockTakeSessionRepository extends JpaRepository<StockTakeSession, Long> {
    List<StockTakeSession> findAllByOrderByStartedAtDesc();
    Optional<StockTakeSession> findBySessionCode(String sessionCode);
    Optional<StockTakeSession> findFirstByStatusOrderByStartedAtDesc(StockTakeSession.Status status);
}

