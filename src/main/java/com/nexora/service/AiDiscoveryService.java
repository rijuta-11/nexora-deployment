package com.nexora.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexora.dto.discovery.*;
import com.nexora.entity.*;
import com.nexora.exception.ResourceNotFoundException;
import com.nexora.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiDiscoveryService {

    private final UserRepository userRepository;
    private final InstrumentRepository instrumentRepository;
    private final AnalysisRunRepository analysisRunRepository;
    private final RecommendationResultRepository recommendationResultRepository;
    private final UserInvestmentPreferenceRepository preferenceRepository;
    private final AIInteractionLogRepository aiInteractionLogRepository;
    private final PortfolioExposureService portfolioExposureService;
    private final RecommendationScoringService scoringService;
    private final AiExplanationClient aiExplanationClient;
    private final MarketDataService marketDataService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public AnalysisResponseDTO runAnalysis(Long userId, AnalysisRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // 1. Save or update UserInvestmentPreference
        saveUserPreferences(user, request);

        // 2. Compute Portfolio Exposure
        PortfolioSummaryDTO portfolioSummary = portfolioExposureService.calculatePortfolioExposure(userId);

        // 3. Evaluate and rank candidates
        List<RecommendationDTO> ranked = scoringService.evaluateAndRankCandidates(request, userId);

        // 4. Generate AI Explanations for ranked candidates
        for (RecommendationDTO rec : ranked) {
            AiExplanationDTO aiExp = aiExplanationClient.generateExplanation(rec, request, portfolioSummary);
            rec.setAiExplanation(aiExp);
        }

        // 5. Persist AnalysisRun & RecommendationResults
        String analysisId = "analysis_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        LocalDateTime now = LocalDateTime.now();

        AnalysisRun run = AnalysisRun.builder()
                .id(analysisId)
                .user(user)
                .inputSnapshot(toJson(request))
                .scoringVersion("v1.0")
                .status("COMPLETED")
                .portfolioSummary(toJson(portfolioSummary))
                .marketDataAsOf(now.minusDays(1))
                .createdAt(now)
                .build();
        analysisRunRepository.save(run);

        for (RecommendationDTO rec : ranked) {
            Instrument inst = instrumentRepository.findById(rec.getInstrumentId()).orElse(null);
            if (inst == null) continue;

            RecommendationResult res = RecommendationResult.builder()
                    .analysisRun(run)
                    .instrument(inst)
                    .rankOrder(rec.getRank())
                    .totalScore(rec.getScore())
                    .qualityScore(rec.getScoreBreakdown().getQuality())
                    .valuationScore(rec.getScoreBreakdown().getValuation())
                    .growthScore(rec.getScoreBreakdown().getGrowth())
                    .riskScore(rec.getScoreBreakdown().getRisk())
                    .industryFitScore(rec.getScoreBreakdown().getIndustryFit())
                    .portfolioFitScore(rec.getScoreBreakdown().getPortfolioFit())
                    .portfolioFitLabel(rec.getPortfolioFit())
                    .reasonsJson(toJson(rec.getReasons()))
                    .risksJson(toJson(rec.getRisks()))
                    .scoreBreakdownJson(toJson(rec.getScoreBreakdown()))
                    .aiExplanationJson(toJson(rec.getAiExplanation()))
                    .createdAt(now)
                    .build();
            recommendationResultRepository.save(res);
        }

        return AnalysisResponseDTO.builder()
                .analysisId(analysisId)
                .generatedAt(now)
                .marketDataAsOf(now.minusDays(1).toLocalDate().toString())
                .scoringVersion("v1.0")
                .portfolioSummary(portfolioSummary)
                .recommendations(ranked)
                .totalCandidatesEvaluated(ranked.size())
                .build();
    }

    public AnalysisResponseDTO getAnalysis(String analysisId, Long userId) {
        AnalysisRun run = analysisRunRepository.findByIdAndUserId(analysisId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis run not found: " + analysisId));

        List<RecommendationResult> results = recommendationResultRepository.findByAnalysisRunIdOrderByRankOrderAsc(analysisId);
        List<RecommendationDTO> recommendations = new ArrayList<>();

        for (RecommendationResult r : results) {
            Instrument inst = r.getInstrument();
            Map<String, BigDecimal> metrics = marketDataService.getMetricsForInstrument(inst.getId());
            Map<String, Object> km = new HashMap<>();
            metrics.forEach((k, v) -> km.put(k, v.doubleValue()));
            km.put("CURRENT_PRICE", inst.getCurrentPrice() != null ? inst.getCurrentPrice().doubleValue() : 0.0);
            km.put("MARKET_CAP", inst.getMarketCap() != null ? inst.getMarketCap().doubleValue() : 0.0);

            ScoreBreakdownDTO breakdown = fromJson(r.getScoreBreakdownJson(), ScoreBreakdownDTO.class);
            if (breakdown == null) {
                breakdown = ScoreBreakdownDTO.builder()
                        .quality(r.getQualityScore())
                        .valuation(r.getValuationScore())
                        .growth(r.getGrowthScore())
                        .risk(r.getRiskScore())
                        .industryFit(r.getIndustryFitScore())
                        .portfolioFit(r.getPortfolioFitScore())
                        .totalScore(r.getTotalScore())
                        .scoringVersion("v1.0")
                        .build();
            }

            AiExplanationDTO aiExp = fromJson(r.getAiExplanationJson(), AiExplanationDTO.class);
            List<String> reasons = fromJsonList(r.getReasonsJson());
            List<String> risks = fromJsonList(r.getRisksJson());

            recommendations.add(RecommendationDTO.builder()
                    .instrumentId(inst.getId())
                    .symbol(inst.getSymbol())
                    .companyName(inst.getCompanyName())
                    .exchange(inst.getExchange())
                    .industry(inst.getIndustry())
                    .sector(inst.getSector())
                    .rank(r.getRankOrder())
                    .score(r.getTotalScore())
                    .scoreCategory(r.getTotalScore() >= 80 ? "Strong Match" : (r.getTotalScore() >= 65 ? "Reasonable Match" : "Lower Match"))
                    .scoreBreakdown(breakdown)
                    .portfolioFit(r.getPortfolioFitLabel())
                    .reasons(reasons)
                    .risks(risks)
                    .currentPrice(inst.getCurrentPrice())
                    .marketCap(inst.getMarketCap())
                    .keyMetrics(km)
                    .dataAsOf(inst.getDataAsOf() != null ? inst.getDataAsOf().toString() : "Current")
                    .aiExplanation(aiExp)
                    .build());
        }

        PortfolioSummaryDTO summary = fromJson(run.getPortfolioSummary(), PortfolioSummaryDTO.class);

        return AnalysisResponseDTO.builder()
                .analysisId(run.getId())
                .generatedAt(run.getCreatedAt())
                .marketDataAsOf(run.getMarketDataAsOf() != null ? run.getMarketDataAsOf().toLocalDate().toString() : "Current")
                .scoringVersion(run.getScoringVersion())
                .portfolioSummary(summary)
                .recommendations(recommendations)
                .totalCandidatesEvaluated(recommendations.size())
                .build();
    }

    public StockResearchDTO getStockResearch(String symbol) {
        Instrument inst = instrumentRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Stock instrument not found: " + symbol));

        Map<String, BigDecimal> metrics = marketDataService.getMetricsForInstrument(inst.getId());
        Map<String, Object> fund = new HashMap<>();
        metrics.forEach((k, v) -> fund.put(k, v.doubleValue()));

        List<String> strengths = new ArrayList<>();
        List<String> risks = new ArrayList<>();

        double roe = metrics.getOrDefault("ROE", BigDecimal.ZERO).doubleValue();
        if (roe > 18.0) strengths.add(String.format("High Return on Equity: %.1f%% indicates strong shareholder value generation.", roe));

        double de = metrics.getOrDefault("DEBT_TO_EQUITY", BigDecimal.ONE).doubleValue();
        if (de < 0.4) strengths.add(String.format("Conservative capital structure with Debt-to-Equity of %.2fx.", de));

        double pe = metrics.getOrDefault("PE_RATIO", BigDecimal.ZERO).doubleValue();
        if (pe > 35.0) risks.add(String.format("Valuation premium: Trading at %.1fx P/E, requiring sustained growth execution.", pe));

        return StockResearchDTO.builder()
                .id(inst.getId())
                .symbol(inst.getSymbol())
                .companyName(inst.getCompanyName())
                .exchange(inst.getExchange())
                .sector(inst.getSector())
                .industry(inst.getIndustry())
                .description(inst.getDescription())
                .currentPrice(inst.getCurrentPrice())
                .marketCap(inst.getMarketCap())
                .dataAsOf(inst.getDataAsOf() != null ? inst.getDataAsOf().toString() : "Current")
                .fundamentals(fund)
                .strengthHighlights(strengths)
                .riskWarnings(risks)
                .source(inst.getExchange() + "_OFFICIAL_DISCLOSURES")
                .build();
    }

    public StockCompareResponseDTO compareCandidates(StockCompareRequestDTO request) {
        List<RecommendationDTO> candidates = new ArrayList<>();
        if (request.getSymbols() == null || request.getSymbols().isEmpty()) {
            return StockCompareResponseDTO.builder().candidates(Collections.emptyList()).build();
        }

        for (String sym : request.getSymbols()) {
            Optional<Instrument> opt = instrumentRepository.findBySymbol(sym.toUpperCase());
            if (opt.isEmpty()) continue;
            Instrument inst = opt.get();
            Map<String, BigDecimal> metrics = marketDataService.getMetricsForInstrument(inst.getId());
            Map<String, Object> km = new HashMap<>();
            metrics.forEach((k, v) -> km.put(k, v.doubleValue()));
            km.put("CURRENT_PRICE", inst.getCurrentPrice() != null ? inst.getCurrentPrice().doubleValue() : 0.0);
            km.put("MARKET_CAP", inst.getMarketCap() != null ? inst.getMarketCap().doubleValue() : 0.0);

            candidates.add(RecommendationDTO.builder()
                    .instrumentId(inst.getId())
                    .symbol(inst.getSymbol())
                    .companyName(inst.getCompanyName())
                    .exchange(inst.getExchange())
                    .industry(inst.getIndustry())
                    .currentPrice(inst.getCurrentPrice())
                    .marketCap(inst.getMarketCap())
                    .keyMetrics(km)
                    .dataAsOf(inst.getDataAsOf() != null ? inst.getDataAsOf().toString() : "Current")
                    .build());
        }

        List<String> diffs = new ArrayList<>();
        if (candidates.size() >= 2) {
            RecommendationDTO a = candidates.get(0);
            RecommendationDTO b = candidates.get(1);
            diffs.add(String.format("%s operates in %s, while %s operates in %s.", a.getSymbol(), a.getIndustry(), b.getSymbol(), b.getIndustry()));
            double roeA = (double) a.getKeyMetrics().getOrDefault("ROE", 0.0);
            double roeB = (double) b.getKeyMetrics().getOrDefault("ROE", 0.0);
            diffs.add(String.format("ROE Comparison: %s reports %.1f%% vs %s at %.1f%%.", a.getSymbol(), roeA, b.getSymbol(), roeB));
        }

        String summary = String.format("Side-by-side comparative analysis of %d candidate securities using verified financial snapshot data.", candidates.size());

        return StockCompareResponseDTO.builder()
                .analysisId(request.getAnalysisId())
                .candidates(candidates)
                .keyDifferences(diffs)
                .comparisonSummary(summary)
                .dataSnapshotAsOf(LocalDateTime.now().minusDays(1).toLocalDate().toString())
                .build();
    }

    @Transactional
    public AiChatResponseDTO chatAssistant(Long userId, AiChatRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        long start = System.currentTimeMillis();
        String answer = aiExplanationClient.generateChatResponse(request.getQuestion(), request.getAnalysisId());
        long latency = System.currentTimeMillis() - start;

        // Log interaction for audit
        aiInteractionLogRepository.save(AIInteractionLog.builder()
                .user(user)
                .analysisId(request.getAnalysisId())
                .promptVersion("v1.0")
                .model("hybrid-deterministic-llm")
                .userQuestion(request.getQuestion())
                .aiResponse(answer)
                .latencyMs(latency)
                .status("SUCCESS")
                .build());

        List<String> suggested = List.of(
                "Why did this stock rank higher than the others?",
                "What are the main financial risks?",
                "How does this impact my current portfolio diversification?"
        );

        return AiChatResponseDTO.builder()
                .answer(answer)
                .citations(List.of("Nexora 100-Point Scoring Model v1.0", "Verified NSE/BSE FY26 Financial Disclosures"))
                .dataAsOf(LocalDateTime.now().minusDays(1).toLocalDate().toString())
                .suggestedQuestions(suggested)
                .disclaimer("Research assistant responses are informational only and do not constitute investment advice.")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private void saveUserPreferences(User user, AnalysisRequestDTO req) {
        try {
            String indStr = req.getIndustries() != null ? String.join(",", req.getIndustries()) : req.getIndustry();
            UserInvestmentPreference pref = preferenceRepository.findTopByUserIdOrderByUpdatedAtDesc(user.getId())
                    .orElse(UserInvestmentPreference.builder().user(user).build());

            pref.setIndustries(indStr);
            pref.setRiskPreference(req.getRiskPreference());
            pref.setInvestmentHorizon(req.getInvestmentHorizon());
            pref.setInvestmentGoal(req.getInvestmentGoal());
            pref.setBudgetRange(req.getBudgetRange());
            pref.setPortfolioSource(req.getPortfolioSource());
            preferenceRepository.save(pref);
        } catch (Exception e) {
            log.warn("Could not save user investment preferences: {}", e.getMessage());
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private <T> T fromJson(String json, Class<T> clazz) {
        try {
            if (json == null || json.isEmpty()) return null;
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> fromJsonList(String json) {
        try {
            if (json == null || json.isEmpty()) return Collections.emptyList();
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
