package com.pharmacore.pos.repository;

import com.pharmacore.pos.entity.StockTakeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockTakeItemRepository extends JpaRepository<StockTakeItem, Long> {
    List<StockTakeItem> findBySessionSessionId(Long sessionId);
}

