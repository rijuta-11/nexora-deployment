package com.nexora.controller;

import com.nexora.dto.ApiResponse;
import com.nexora.dto.discovery.PortfolioSummaryDTO;
import com.nexora.service.PortfolioExposureService;
import com.nexora.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioExposureService portfolioExposureService;
    private final UserService userService;

    @GetMapping("/insights")
    public ResponseEntity<ApiResponse<PortfolioSummaryDTO>> getPortfolioInsights(Authentication auth) {
        Long userId = userService.getUserByEmail(auth.getName()).getId();
        PortfolioSummaryDTO insights = portfolioExposureService.calculatePortfolioExposure(userId);
        return ResponseEntity.ok(ApiResponse.success(insights));
    }
}
