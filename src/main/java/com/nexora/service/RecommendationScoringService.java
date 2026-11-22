package com.nexora.service;

import com.nexora.dto.discovery.AnalysisRequestDTO;
import com.nexora.dto.discovery.PortfolioSummaryDTO;
import com.nexora.dto.discovery.RecommendationDTO;
import com.nexora.dto.discovery.ScoreBreakdownDTO;
import com.nexora.entity.Instrument;
import com.nexora.repository.InstrumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationScoringService {

    private final InstrumentRepository instrumentRepository;
    private final MarketDataService marketDataService;
    private final PortfolioExposureService portfolioExposureService;

    public List<RecommendationDTO> evaluateAndRankCandidates(AnalysisRequestDTO request, Long userId) {
        List<Instrument> allInstruments = instrumentRepository.findByInstrumentStatus(Instrument.InstrumentStatus.ACTIVE);
        List<RecommendationDTO> evaluated = new ArrayList<>();

        List<String> targetIndustries = request.getIndustries() != null && !request.getIndustries().isEmpty()
                ? request.getIndustries()
                : (request.getIndustry() != null ? List.of(request.getIndustry()) : Collections.emptyList());

        boolean hasPortfolio = !"NONE".equalsIgnoreCase(request.getPortfolioSource()) && userId != null;
        PortfolioSummaryDTO portfolioSummary = hasPortfolio ? portfolioExposureService.calculatePortfolioExposure(userId) : null;

        for (Instrument inst : allInstruments) {
            // Stage A: Hard Eligibility Filters
            if (!isEligible(inst, targetIndustries)) {
                continue;
            }

            Map<String, BigDecimal> metrics = marketDataService.getMetricsForInstrument(inst.getId());
            if (metrics.isEmpty() || !metrics.containsKey("ROE")) {
                // Must have sufficient data coverage, never fabricate data (Page 5 rule)
                continue;
            }

            // Stage B: 100-Point Weighted Scoring
            ScoreBreakdownDTO breakdown = calculateScoreBreakdown(inst, metrics, request, portfolioSummary, hasPortfolio);
            int totalScore = breakdown.getTotalScore();

            String scoreCategory = totalScore >= 80 ? "Strong Match"
                    : (totalScore >= 65 ? "Reasonable Match" : "Lower Match");

            double sectorWeight = hasPortfolio ? getSectorWeightFromSummary(portfolioSummary, inst.getIndustry()) : 0.0;
            String portfolioFitLabel = buildPortfolioFitLabel(hasPortfolio, sectorWeight, inst.getIndustry());

            List<String> reasons = buildReasons(inst, metrics, breakdown, sectorWeight);
            List<String> risks = buildRisks(inst, metrics, breakdown, sectorWeight);

            Map<String, Object> keyMetrics = new HashMap<>();
            metrics.forEach((k, v) -> keyMetrics.put(k, v.doubleValue()));
            keyMetrics.put("CURRENT_PRICE", inst.getCurrentPrice() != null ? inst.getCurrentPrice().doubleValue() : 0.0);
            keyMetrics.put("MARKET_CAP", inst.getMarketCap() != null ? inst.getMarketCap().doubleValue() : 0.0);

            RecommendationDTO dto = RecommendationDTO.builder()
                    .instrumentId(inst.getId())
                    .symbol(inst.getSymbol())
                    .companyName(inst.getCompanyName())
                    .exchange(inst.getExchange())
                    .industry(inst.getIndustry())
                    .sector(inst.getSector())
                    .score(totalScore)
                    .scoreCategory(scoreCategory)
                    .scoreBreakdown(breakdown)
                    .portfolioFit(portfolioFitLabel)
                    .reasons(reasons)
                    .risks(risks)
                    .currentPrice(inst.getCurrentPrice())
                    .marketCap(inst.getMarketCap())
                    .keyMetrics(keyMetrics)
                    .dataAsOf(inst.getDataAsOf() != null ? inst.getDataAsOf().toString() : "Current")
                    .build();

            evaluated.add(dto);
        }

        // Sort descending by total score
        evaluated.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));

        // Assign ranks (1, 2, 3...)
        for (int i = 0; i < evaluated.size(); i++) {
            evaluated.get(i).setRank(i + 1);
        }

        return evaluated;
    }

    private boolean isEligible(Instrument inst, List<String> targetIndustries) {
        if (inst.getInstrumentStatus() != Instrument.InstrumentStatus.ACTIVE) {
            return false;
        }
        if (targetIndustries.isEmpty()) {
            return true;
        }
        for (String target : targetIndustries) {
            if (inst.getIndustry().equalsIgnoreCase(target.trim()) ||
                inst.getSector().equalsIgnoreCase(target.trim()) ||
                inst.getIndustry().toLowerCase().contains(target.trim().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private ScoreBreakdownDTO calculateScoreBreakdown(Instrument inst, Map<String, BigDecimal> metrics,
                                                     AnalysisRequestDTO req, PortfolioSummaryDTO portfolioSummary,
                                                     boolean hasPortfolio) {
        // 1. Business / Financial Quality (25 pts)
        double roe = getDouble(metrics, "ROE", 12.0);
        double opMargin = getDouble(metrics, "OPERATING_MARGIN", 15.0);
        double roce = getDouble(metrics, "ROCE", 15.0);

        int quality = 0;
        if (roe >= 25.0) quality += 10;
        else if (roe >= 18.0) quality += 8;
        else if (roe >= 12.0) quality += 6;
        else if (roe >= 8.0) quality += 4;
        else quality += 2;

        if (opMargin >= 25.0) quality += 8;
        else if (opMargin >= 18.0) quality += 6;
        else if (opMargin >= 10.0) quality += 4;
        else quality += 2;

        if (roce >= 20.0) quality += 7;
        else if (roce >= 14.0) quality += 5;
        else if (roce >= 8.0) quality += 3;
        else quality += 1;

        // 2. Valuation Context (20 pts)
        double pe = getDouble(metrics, "PE_RATIO", 25.0);
        double pb = getDouble(metrics, "PB_RATIO", 3.0);
        double divYield = getDouble(metrics, "DIVIDEND_YIELD", 1.0);

        int valuation = 0;
        if (pe > 0 && pe <= 24.0) valuation += 12;
        else if (pe > 24.0 && pe <= 35.0) valuation += 8;
        else if (pe > 35.0) valuation += 4;
        else valuation += 2; // loss making or negative PE

        if (pb <= 3.5 || divYield >= 1.5) valuation += 8;
        else if (pb <= 7.0 || divYield >= 0.8) valuation += 5;
        else valuation += 3;

        // 3. Growth and Consistency (15 pts)
        double revGrowth = getDouble(metrics, "REV_GROWTH_YOY", 8.0);
        double earnGrowth = getDouble(metrics, "EARNINGS_GROWTH", 10.0);

        int growth = 0;
        if (revGrowth >= 18.0) growth += 8;
        else if (revGrowth >= 10.0) growth += 6;
        else if (revGrowth >= 5.0) growth += 4;
        else growth += 2;

        if (earnGrowth >= 18.0) growth += 7;
        else if (earnGrowth >= 10.0) growth += 5;
        else if (earnGrowth >= 4.0) growth += 3;
        else growth += 1;

        // 4. Risk and Volatility (15 pts)
        double de = getDouble(metrics, "DEBT_TO_EQUITY", 0.5);
        double beta = getDouble(metrics, "VOLATILITY_BETA", 1.0);
        String riskPref = req.getRiskPreference() != null ? req.getRiskPreference().toUpperCase() : "MODERATE";

        int risk = 0;
        if (de <= 0.2) risk += 8;
        else if (de <= 0.6) risk += 6;
        else if (de <= 1.2) risk += 4;
        else risk += 2;

        if ("LOWER".equalsIgnoreCase(riskPref)) {
            if (beta <= 0.8) risk += 7;
            else if (beta <= 1.05) risk += 5;
            else risk += 2;
        } else if ("HIGHER".equalsIgnoreCase(riskPref)) {
            if (beta >= 1.0) risk += 7;
            else risk += 5;
        } else { // MODERATE
            if (beta >= 0.7 && beta <= 1.15) risk += 7;
            else risk += 4;
        }

        // 5. Industry Fit (10 pts)
        int industryFit = 10;

        // 6. Portfolio Fit / Diversification (15 pts)
        int portfolioFit = 10; // default for no portfolio ("not assessed")
        if (hasPortfolio && portfolioSummary != null) {
            double sectorWeight = getSectorWeightFromSummary(portfolioSummary, inst.getIndustry());
            if (sectorWeight < 15.0) portfolioFit = 15; // strong diversification benefit
            else if (sectorWeight < 35.0) portfolioFit = 11;
            else if (sectorWeight < 50.0) portfolioFit = 6;
            else portfolioFit = 2; // severe concentration warning penalty
        }

        int total = quality + valuation + growth + risk + industryFit + portfolioFit;

        return ScoreBreakdownDTO.builder()
                .quality(quality)
                .valuation(valuation)
                .growth(growth)
                .risk(risk)
                .industryFit(industryFit)
                .portfolioFit(portfolioFit)
                .totalScore(total)
                .scoringVersion("v1.0")
                .build();
    }

    private double getSectorWeightFromSummary(PortfolioSummaryDTO summary, String industry) {
        if (summary == null || summary.getSectorExposure() == null) return 0.0;
        for (var exp : summary.getSectorExposure()) {
            if (exp.getSector().equalsIgnoreCase(industry)) {
                return exp.getWeightPct();
            }
        }
        return 0.0;
    }

    private String buildPortfolioFitLabel(boolean hasPortfolio, double sectorWeight, String industry) {
        if (!hasPortfolio) {
            return "Portfolio fit: Not assessed (no portfolio connected)";
        }
        if (sectorWeight < 15.0) {
            return String.format("High diversification value: current %s exposure is only %.1f%%", industry, sectorWeight);
        } else if (sectorWeight < 35.0) {
            return String.format("Balanced allocation: current %s exposure is %.1f%%", industry, sectorWeight);
        } else if (sectorWeight < 50.0) {
            return String.format("Caution: %s already represents %.1f%% of your portfolio", industry, sectorWeight);
        } else {
            return String.format("⚠️ High concentration warning: %s already accounts for %.1f%% of your holdings", industry, sectorWeight);
        }
    }

    private List<String> buildReasons(Instrument inst, Map<String, BigDecimal> metrics, ScoreBreakdownDTO b, double sectorWeight) {
        List<String> list = new ArrayList<>();
        list.add("Matches your selected research industry: " + inst.getIndustry());

        double roe = getDouble(metrics, "ROE", 0);
        if (roe >= 20.0) {
            list.add(String.format("High capital efficiency with ROE of %.1f%%", roe));
        }

        double de = getDouble(metrics, "DEBT_TO_EQUITY", 1.0);
        if (de <= 0.3) {
            list.add(String.format("Prudent balance sheet with low Debt-to-Equity ratio of %.2fx", de));
        }

        double revGrowth = getDouble(metrics, "REV_GROWTH_YOY", 0);
        if (revGrowth >= 12.0) {
            list.add(String.format("Healthy revenue expansion of %.1f%% YoY", revGrowth));
        }

        if (sectorWeight < 20.0) {
            list.add("Provides fresh sector diversification to your existing asset allocation");
        }
        return list;
    }

    private List<String> buildRisks(Instrument inst, Map<String, BigDecimal> metrics, ScoreBreakdownDTO b, double sectorWeight) {
        List<String> list = new ArrayList<>();

        double pe = getDouble(metrics, "PE_RATIO", 0);
        if (pe >= 35.0) {
            list.add(String.format("Valuation multiple is elevated at P/E of %.1fx relative to broader market", pe));
        }

        double beta = getDouble(metrics, "VOLATILITY_BETA", 1.0);
        if (beta >= 1.2) {
            list.add(String.format("Higher historical price volatility with Beta of %.2fx", beta));
        }

        double de = getDouble(metrics, "DEBT_TO_EQUITY", 0);
        if (de >= 1.0) {
            list.add(String.format("Leverage alert: Debt-to-Equity is %.2fx, requiring monitor on interest coverage", de));
        }

        if (sectorWeight >= 40.0) {
            list.add(String.format("Portfolio concentration risk: Adding this stock increases already heavy exposure (%.1f%%) in %s", sectorWeight, inst.getIndustry()));
        }

        if (list.isEmpty()) {
            list.add("General market risk and sector-specific regulatory shifts apply.");
        }
        return list;
    }

    private double getDouble(Map<String, BigDecimal> m, String key, double defaultVal) {
        BigDecimal val = m.get(key);
        return val != null ? val.doubleValue() : defaultVal;
    }
}
