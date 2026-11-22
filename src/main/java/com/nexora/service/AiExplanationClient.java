package com.nexora.service;

import com.nexora.dto.discovery.AiExplanationDTO;
import com.nexora.dto.discovery.AnalysisRequestDTO;
import com.nexora.dto.discovery.PortfolioSummaryDTO;
import com.nexora.dto.discovery.RecommendationDTO;

public interface AiExplanationClient {
    AiExplanationDTO generateExplanation(RecommendationDTO candidate, AnalysisRequestDTO req, PortfolioSummaryDTO portfolioSummary);
    String generateChatResponse(String userQuestion, String contextSnapshot);
}
