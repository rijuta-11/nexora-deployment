package com.nexora.repository;

import com.nexora.entity.MarketDataSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketDataSnapshotRepository extends JpaRepository<MarketDataSnapshot, Long> {
    List<MarketDataSnapshot> findByInstrumentId(Long instrumentId);
    List<MarketDataSnapshot> findByInstrumentIdIn(List<Long> instrumentIds);
}
