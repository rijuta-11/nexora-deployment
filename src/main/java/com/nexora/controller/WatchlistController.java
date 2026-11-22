package com.nexora.controller;

import com.nexora.dto.ApiResponse;
import com.nexora.dto.discovery.WatchlistItemDTO;
import com.nexora.service.UserService;
import com.nexora.service.WatchlistService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<WatchlistItemDTO>> addToWatchlist(
            @RequestBody AddWatchlistRequest request,
            Authentication auth) {
        Long userId = userService.getUserByEmail(auth.getName()).getId();
        WatchlistItemDTO item = watchlistService.addToWatchlist(userId, request.getInstrumentId(), request.getNotes());
        return ResponseEntity.ok(ApiResponse.success("Added to watchlist", item));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WatchlistItemDTO>>> getMyWatchlist(Authentication auth) {
        Long userId = userService.getUserByEmail(auth.getName()).getId();
        return ResponseEntity.ok(ApiResponse.success(watchlistService.getMyWatchlist(userId)));
    }

    @DeleteMapping("/{instrumentId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWatchlist(
            @PathVariable Long instrumentId,
            Authentication auth) {
        Long userId = userService.getUserByEmail(auth.getName()).getId();
        watchlistService.removeFromWatchlist(userId, instrumentId);
        return ResponseEntity.ok(ApiResponse.success("Removed from watchlist", null));
    }

    @Data
    public static class AddWatchlistRequest {
        @com.fasterxml.jackson.annotation.JsonAlias({"instrument_id", "instrumentId"})
        private Long instrumentId;
        private String notes;
    }
}
