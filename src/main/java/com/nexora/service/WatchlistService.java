package com.nexora.service;

import com.nexora.dto.discovery.WatchlistItemDTO;
import com.nexora.entity.Instrument;
import com.nexora.entity.User;
import com.nexora.entity.WatchlistItem;
import com.nexora.exception.ConflictException;
import com.nexora.exception.ResourceNotFoundException;
import com.nexora.repository.InstrumentRepository;
import com.nexora.repository.UserRepository;
import com.nexora.repository.WatchlistItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistItemRepository watchlistItemRepository;
    private final UserRepository userRepository;
    private final InstrumentRepository instrumentRepository;

    @Transactional
    public WatchlistItemDTO addToWatchlist(Long userId, Long instrumentId, String notes) {
        if (watchlistItemRepository.existsByUserIdAndInstrumentId(userId, instrumentId)) {
            throw new ConflictException("Stock is already in your watchlist");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new ResourceNotFoundException("Instrument not found: " + instrumentId));

        WatchlistItem item = watchlistItemRepository.save(WatchlistItem.builder()
                .user(user)
                .instrument(instrument)
                .notes(notes)
                .createdAt(LocalDateTime.now())
                .build());

        return toDTO(item);
    }

    public List<WatchlistItemDTO> getMyWatchlist(Long userId) {
        return watchlistItemRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional
    public void removeFromWatchlist(Long userId, Long instrumentId) {
        WatchlistItem item = watchlistItemRepository.findByUserIdAndInstrumentId(userId, instrumentId)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist item not found"));
        watchlistItemRepository.delete(item);
    }

    private WatchlistItemDTO toDTO(WatchlistItem item) {
        Instrument inst = item.getInstrument();
        return WatchlistItemDTO.builder()
                .id(item.getId())
                .instrumentId(inst.getId())
                .symbol(inst.getSymbol())
                .companyName(inst.getCompanyName())
                .exchange(inst.getExchange())
                .sector(inst.getSector())
                .industry(inst.getIndustry())
                .currentPrice(inst.getCurrentPrice())
                .marketCap(inst.getMarketCap())
                .notes(item.getNotes())
                .dataAsOf(inst.getDataAsOf() != null ? inst.getDataAsOf().toString() : "Current")
                .addedAt(item.getCreatedAt())
                .build();
    }
}
