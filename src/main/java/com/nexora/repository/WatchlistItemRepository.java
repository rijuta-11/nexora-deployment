package com.nexora.repository;

import com.nexora.entity.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, Long> {
    List<WatchlistItem> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<WatchlistItem> findByUserIdAndInstrumentId(Long userId, Long instrumentId);
    boolean existsByUserIdAndInstrumentId(Long userId, Long instrumentId);
}
