package com.nexora.service;

import com.nexora.dto.discovery.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiExplanationService implements AiExplanationClient {

    private static final String MANDATORY_DISCLAIMER =
            "Nexora AI Stock Discovery provides information and research suggestions based on selected preferences and available data. " +
            "Scores reflect Nexora's stated criteria and are not a probability of return or a guarantee of performance. " +
            "Stocks can lose value. Data may be delayed, incomplete or revised. Review the underlying information and consider your own " +
            "circumstances before making an investment decision.";

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Override
    public AiExplanationDTO generateExplanation(RecommendationDTO candidate, AnalysisRequestDTO req, PortfolioSummaryDTO portfolioSummary) {
        String company = candidate.getCompanyName() != null ? candidate.getCompanyName() : candidate.getSymbol();
        int score = candidate.getScore();
        ScoreBreakdownDTO b = candidate.getScoreBreakdown();
        Map<String, Object> km = candidate.getKeyMetrics();

        double roe = km.containsKey("ROE") ? ((Number) km.get("ROE")).doubleValue() : 0.0;
        double pe = km.containsKey("PE_RATIO") ? ((Number) km.get("PE_RATIO")).doubleValue() : 0.0;
        double de = km.containsKey("DEBT_TO_EQUITY") ? ((Number) km.get("DEBT_TO_EQUITY")).doubleValue() : 0.0;
        double revGrowth = km.containsKey("REV_GROWTH_YOY") ? ((Number) km.get("REV_GROWTH_YOY")).doubleValue() : 0.0;

        String horizon = req.getInvestmentHorizon() != null ? req.getInvestmentHorizon().replace("_", " ").toLowerCase() : "medium term";
        String risk = req.getRiskPreference() != null ? req.getRiskPreference().toLowerCase() : "moderate";

        // Plain language summary
        String summary = String.format(
                "%s scores %d/100 under Nexora's research model. It aligns with your %s risk profile and %s investment horizon, " +
                "supported by a business quality score of %d/25 and valuation context score of %d/20.",
                company, score, risk, horizon, b.getQuality(), b.getValuation()
        );

        // Why it matches
        List<String> matches = new ArrayList<>();
        matches.add(String.format("Strong financial quality: Reported ROE of %.1f%% reflects solid capital productivity.", roe));
        if (de <= 0.5) {
            matches.add(String.format("Low leverage profile: Debt-to-Equity ratio of %.2fx provides balance sheet resilience.", de));
        }
        if (revGrowth > 0) {
            matches.add(String.format("Revenue growth of %.1f%% YoY meets expansion consistency criteria.", revGrowth));
        }
        matches.add(String.format("Fits targeted sector: %s in %s market universe.", candidate.getIndustry(), candidate.getExchange()));

        // Risks to consider
        List<String> risks = new ArrayList<>();
        if (pe >= 35.0) {
            risks.add(String.format("Valuation risk: P/E multiple of %.1fx indicates premium pricing where market expectations are already high.", pe));
        } else if (pe <= 10.0 && pe > 0) {
            risks.add(String.format("Value trap awareness: Low P/E multiple of %.1fx requires checking whether industry growth is slowing.", pe));
        }
        if (b.getPortfolioFit() <= 6) {
            risks.add(String.format("Concentration warning: Your existing exposure in %s is elevated. Adding more shares reduces diversification.", candidate.getIndustry()));
        } else {
            risks.add("Subject to general macroeconomic, monetary and regulatory shifts in the sector.");
        }

        // Portfolio impact
        String portfolioImpact;
        if (portfolioSummary == null || portfolioSummary.getTotalHoldings() == 0) {
            portfolioImpact = "Portfolio impact not assessed: No current portfolio holdings connected to check sector concentration.";
        } else {
            portfolioImpact = candidate.getPortfolioFit();
        }

        // Data limitations
        List<String> limitations = List.of(
                String.format("Verified fundamentals as of %s (quarterly/annual disclosures).", candidate.getDataAsOf()),
                "Market prices reflect recent closing/feed values. Intraday volatility is not real-time tick data.",
                "Research score is algorithmic and does not constitute SEBI/FINRA regulated financial advice."
        );

        return AiExplanationDTO.builder()
                .plainLanguageSummary(summary)
                .whyItMatches(matches)
                .risksToConsider(risks)
                .portfolioImpact(portfolioImpact)
                .dataLimitations(limitations)
                .disclaimer(MANDATORY_DISCLAIMER)
                .build();
    }

    @Override
    public String generateChatResponse(String userQuestion, String contextSnapshot) {
        if (userQuestion == null || userQuestion.trim().isEmpty()) {
            return "Please ask a specific research question regarding the candidates in this analysis, such as: 'Why is stock A ranked higher than stock B?' or 'What are the main risks?'";
        }

        String q = userQuestion.toLowerCase();

        if (q.contains("predict") || q.contains("future price") || q.contains("guaranteed") || q.contains("target price")) {
            return "Nexora AI Stock Discovery is an explainable research assistant, not a price-forecasting oracle. " +
                   "Under our compliance guidelines, we do not predict exact future prices or promise investment returns. " +
                   "Our scores strictly assess financial quality, valuation multiples, revenue consistency, and portfolio concentration fit based on verified historical disclosures.";
        }

        if (q.contains("why") && (q.contains("rank") || q.contains("higher") || q.contains("above") || q.contains("first"))) {
            return "Ranking in Nexora is determined by a transparent 100-point composite score: " +
                   "Business Quality (25 pts), Valuation Context (20 pts), Growth & Consistency (15 pts), " +
                   "Risk & Volatility (15 pts), Industry Fit (10 pts), and Portfolio Fit / Diversification (15 pts). " +
                   "Higher-ranked candidates demonstrated superior balance sheet resilience (e.g. higher ROE and lower Debt-to-Equity) " +
                   "or provided better diversification benefit compared to your existing portfolio exposures.";
        }

        if (q.contains("risk") || q.contains("downside") || q.contains("danger")) {
            return "Key risks to evaluate for the candidates in this analysis include: " +
                   "1) Valuation compression: Stocks trading at elevated P/E multiples are sensitive to earnings misses. " +
                   "2) Portfolio concentration: If you already hold heavy exposure in this sector, adding more shares amplifies single-sector drawdowns. " +
                   "3) Leverage: Check Debt-to-Equity ratios on candidate cards before committing capital.";
        }

        if (q.contains("portfolio") || q.contains("diversif") || q.contains("exposure") || q.contains("concentration")) {
            return "Nexora evaluates how candidate stocks interact with your existing holdings. " +
                   "If your portfolio has less than 20% allocation in the candidate's sector, the system awards full diversification points (+15 pts). " +
                   "Conversely, if sector exposure exceeds 50%, a concentration warning is triggered and portfolio fit points are reduced to protect against over-allocation.";
        }

        if (q.contains("buy") || q.contains("sell") || q.contains("trade") || q.contains("execute")) {
            return "This research tool is strictly informational. No trades are placed automatically, and recommendations " +
                   "do not constitute an execution instruction. You can add candidates of interest to your Watchlist to monitor their performance over time.";
        }

        return String.format(
                "Regarding your inquiry ('%s'): All candidates displayed have passed Stage A eligibility checks (active listing, verified fundamental disclosures) " +
                "and have been evaluated using Nexora's 100-point weighted scoring model. " +
                "You can click 'AI Deep Dive' on any candidate card to inspect the complete financial quality, valuation metrics, and risk trade-offs.",
                userQuestion.replaceAll("[<>]", "")
        );
    }
}
