package com.nexora.controller;

import com.nexora.dto.ApiResponse;
import com.nexora.dto.discovery.*;
import com.nexora.service.AiDiscoveryService;
import com.nexora.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai-discovery")
@RequiredArgsConstructor
public class AiDiscoveryController {

    private final AiDiscoveryService aiDiscoveryService;
    private final UserService userService;

    @PostMapping("/analyses")
    public ResponseEntity<ApiResponse<AnalysisResponseDTO>> createAnalysis(
            @RequestBody AnalysisRequestDTO request,
            Authentication auth) {
        Long userId = userService.getUserByEmail(auth.getName()).getId();
        AnalysisResponseDTO response = aiDiscoveryService.runAnalysis(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Analysis generated successfully", response));
    }

    @GetMapping("/analyses/{id}")
    public ResponseEntity<ApiResponse<AnalysisResponseDTO>> getAnalysis(
            @PathVariable String id,
            Authentication auth) {
        Long userId = userService.getUserByEmail(auth.getName()).getId();
        AnalysisResponseDTO response = aiDiscoveryService.getAnalysis(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/analyses/{id}/recommendations")
    public ResponseEntity<ApiResponse<List<RecommendationDTO>>> getRecommendations(
            @PathVariable String id,
            Authentication auth) {
        Long userId = userService.getUserByEmail(auth.getName()).getId();
        AnalysisResponseDTO response = aiDiscoveryService.getAnalysis(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response.getRecommendations()));
    }

    @GetMapping("/stocks/{symbol}/research")
    public ResponseEntity<ApiResponse<StockResearchDTO>> getStockResearch(
            @PathVariable String symbol) {
        StockResearchDTO research = aiDiscoveryService.getStockResearch(symbol);
        return ResponseEntity.ok(ApiResponse.success(research));
    }

    @PostMapping("/compare")
    public ResponseEntity<ApiResponse<StockCompareResponseDTO>> compareCandidates(
            @RequestBody StockCompareRequestDTO request) {
        StockCompareResponseDTO comparison = aiDiscoveryService.compareCandidates(request);
        return ResponseEntity.ok(ApiResponse.success(comparison));
    }
}
