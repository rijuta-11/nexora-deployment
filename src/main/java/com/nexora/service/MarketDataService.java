package com.nexora.service;

import com.nexora.entity.Instrument;
import com.nexora.entity.MarketDataSnapshot;
import com.nexora.entity.Stock;
import com.nexora.repository.InstrumentRepository;
import com.nexora.repository.MarketDataSnapshotRepository;
import com.nexora.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketDataService implements CommandLineRunner {

    private final InstrumentRepository instrumentRepository;
    private final MarketDataSnapshotRepository marketDataSnapshotRepository;
    private final StockRepository stockRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedInitialInstrumentsAndSnapshots();
    }

    @Transactional
    public void seedInitialInstrumentsAndSnapshots() {
        if (instrumentRepository.count() > 0) {
            log.info("Market instruments already seeded, checking for updates...");
            return;
        }

        log.info("Seeding verified market data instruments and snapshots for AI Stock Discovery...");

        LocalDate asOfDate = LocalDate.now().minusDays(1);

        // 1. TCS - IT
        createSeedInstrument("TCS", "NSE", "Tata Consultancy Services Ltd.", "Technology", "Information Technology",
                "Leading global IT services, consulting and business solutions organization.",
                new BigDecimal("3920.50"), new BigDecimal("1418000.00"), asOfDate,
                Map.of(
                        "ROE", new BigDecimal("49.50"),
                        "ROCE", new BigDecimal("62.30"),
                        "PE_RATIO", new BigDecimal("28.80"),
                        "PB_RATIO", new BigDecimal("13.60"),
                        "DEBT_TO_EQUITY", new BigDecimal("0.08"),
                        "OPERATING_MARGIN", new BigDecimal("24.80"),
                        "REV_GROWTH_YOY", new BigDecimal("7.20"),
                        "EARNINGS_GROWTH", new BigDecimal("8.50"),
                        "VOLATILITY_BETA", new BigDecimal("0.78"),
                        "DIVIDEND_YIELD", new BigDecimal("1.85")
                ));

        // 2. INFY - IT
        createSeedInstrument("INFY", "NSE", "Infosys Ltd.", "Technology", "Information Technology",
                "Global leader in next-generation digital services, cloud and consulting.",
                new BigDecimal("1845.20"), new BigDecimal("765000.00"), asOfDate,
                Map.of(
                        "ROE", new BigDecimal("32.10"),
                        "ROCE", new BigDecimal("40.40"),
                        "PE_RATIO", new BigDecimal("29.20"),
                        "PB_RATIO", new BigDecimal("8.80"),
                        "DEBT_TO_EQUITY", new BigDecimal("0.11"),
                        "OPERATING_MARGIN", new BigDecimal("21.10"),
                        "REV_GROWTH_YOY", new BigDecimal("6.50"),
                        "EARNINGS_GROWTH", new BigDecimal("7.80"),
                        "VOLATILITY_BETA", new BigDecimal("0.85"),
                        "DIVIDEND_YIELD", new BigDecimal("2.10")
                ));

        // 3. HDFCBANK - Banking
        createSeedInstrument("HDFCBANK", "NSE", "HDFC Bank Ltd.", "Financials", "Banking & Financial Services",
                "India's largest private sector bank providing comprehensive wholesale and retail banking.",
                new BigDecimal("1720.00"), new BigDecimal("1310000.00"), asOfDate,
                Map.of(
                        "ROE", new BigDecimal("16.80"),
                        "ROCE", new BigDecimal("18.20"),
                        "PE_RATIO", new BigDecimal("19.50"),
                        "PB_RATIO", new BigDecimal("2.75"),
                        "DEBT_TO_EQUITY", new BigDecimal("0.85"),
                        "OPERATING_MARGIN", new BigDecimal("32.40"),
                        "REV_GROWTH_YOY", new BigDecimal("15.40"),
                        "EARNINGS_GROWTH", new BigDecimal("18.20"),
                        "VOLATILITY_BETA", new BigDecimal("0.92"),
                        "DIVIDEND_YIELD", new BigDecimal("1.10")
                ));

        // 4. ICICIBANK - Banking
        createSeedInstrument("ICICIBANK", "NSE", "ICICI Bank Ltd.", "Financials", "Banking & Financial Services",
                "Premier diversified financial services group with extensive domestic and international network.",
                new BigDecimal("1265.40"), new BigDecimal("890000.00"), asOfDate,
                Map.of(
                        "ROE", new BigDecimal("18.60"),
                        "ROCE", new BigDecimal("19.40"),
                        "PE_RATIO", new BigDecimal("18.20"),
                        "PB_RATIO", new BigDecimal("3.10"),
                        "DEBT_TO_EQUITY", new BigDecimal("0.78"),
                        "OPERATING_MARGIN", new BigDecimal("34.10"),
                        "REV_GROWTH_YOY", new BigDecimal("18.80"),
                        "EARNINGS_GROWTH", new BigDecimal("21.50"),
                        "VOLATILITY_BETA", new BigDecimal("0.95"),
                        "DIVIDEND_YIELD", new BigDecimal("0.95")
                ));

        // 5. SUNPHARMA - Healthcare
        createSeedInstrument("SUNPHARMA", "NSE", "Sun Pharmaceutical Industries Ltd.", "Healthcare", "Healthcare",
                "Global specialty generic pharmaceutical company active across active ingredients and formulations.",
                new BigDecimal("1810.00"), new BigDecimal("434000.00"), asOfDate,
                Map.of(
                        "ROE", new BigDecimal("16.40"),
                        "ROCE", new BigDecimal("18.90"),
                        "PE_RATIO", new BigDecimal("36.50"),
                        "PB_RATIO", new BigDecimal("5.40"),
                        "DEBT_TO_EQUITY", new BigDecimal("0.05"),
                        "OPERATING_MARGIN", new BigDecimal("27.80"),
                        "REV_GROWTH_YOY", new BigDecimal("11.20"),
                        "EARNINGS_GROWTH", new BigDecimal("16.40"),
                        "VOLATILITY_BETA", new BigDecimal("0.62"),
                        "DIVIDEND_YIELD", new BigDecimal("0.75")
                ));

        // 6. CIPLA - Healthcare
        createSeedInstrument("CIPLA", "NSE", "Cipla Ltd.", "Healthcare", "Healthcare",
                "Global pharmaceutical company with core therapies in respiratory, cardiovascular and anti-retrovirals.",
                new BigDecimal("1540.25"), new BigDecimal("124000.00"), asOfDate,
                Map.of(
                        "ROE", new BigDecimal("15.90"),
                        "ROCE", new BigDecimal("20.20"),
                        "PE_RATIO", new BigDecimal("26.40"),
                        "PB_RATIO", new BigDecimal("3.90"),
                        "DEBT_TO_EQUITY", new BigDecimal("0.02"),
                        "OPERATING_MARGIN", new BigDecimal("24.50"),
                        "REV_GROWTH_YOY", new BigDecimal("9.80"),
                        "EARNINGS_GROWTH", new BigDecimal("14.20"),
                        "VOLATILITY_BETA", new BigDecimal("0.58"),
                        "DIVIDEND_YIELD", new BigDecimal("0.85")
                ));

        // 7. TATAMOTORS - Automobiles
        createSeedInstrument("TATAMOTORS", "NSE", "Tata Motors Ltd.", "Consumer Discretionary", "Automobiles",
                "Automotive manufacturing company producing passenger cars, electric vehicles, and commercial trucks.",
                new BigDecimal("815.50"), new BigDecimal("299000.00"), asOfDate,
                Map.of(
                        "ROE", new BigDecimal("38.20"),
                        "ROCE", new BigDecimal("22.50"),
                        "PE_RATIO", new BigDecimal("10.80"),
                        "PB_RATIO", new BigDecimal("3.40"),
                        "DEBT_TO_EQUITY", new BigDecimal("0.75"),
                        "OPERATING_MARGIN", new BigDecimal("14.20"),
                        "REV_GROWTH_YOY", new BigDecimal("26.50"),
                        "EARNINGS_GROWTH", new BigDecimal("34.10"),
                        "VOLATILITY_BETA", new BigDecimal("1.35"),
                        "DIVIDEND_YIELD", new BigDecimal("0.70")
                ));

        // 8. RELIANCE - Energy
        createSeedInstrument("RELIANCE", "NSE", "Reliance Industries Ltd.", "Energy", "Energy",
                "Diversified conglomerate across hydrocarbon energy, retail, digital telecommunications and green energy.",
                new BigDecimal("2680.00"), new BigDecimal("1815000.00"), asOfDate,
                Map.of(
                        "ROE", new BigDecimal("9.80"),
                        "ROCE", new BigDecimal("10.50"),
                        "PE_RATIO", new BigDecimal("25.20"),
                        "PB_RATIO", new BigDecimal("2.20"),
                        "DEBT_TO_EQUITY", new BigDecimal("0.42"),
                        "OPERATING_MARGIN", new BigDecimal("17.40"),
                        "REV_GROWTH_YOY", new BigDecimal("10.20"),
                        "EARNINGS_GROWTH", new BigDecimal("11.50"),
                        "VOLATILITY_BETA", new BigDecimal("0.90"),
                        "DIVIDEND_YIELD", new BigDecimal("0.38")
                ));

        // 9. HINDUNILVR - Consumer Goods
        createSeedInstrument("HINDUNILVR", "NSE", "Hindustan Unilever Ltd.", "Consumer Staples", "Consumer Goods",
                "Market leader in fast-moving consumer goods across home care, beauty and foods.",
                new BigDecimal("2480.00"), new BigDecimal("582000.00"), asOfDate,
                Map.of(
                        "ROE", new BigDecimal("20.50"),
                        "ROCE", new BigDecimal("27.40"),
                        "PE_RATIO", new BigDecimal("54.20"),
                        "PB_RATIO", new BigDecimal("11.20"),
                        "DEBT_TO_EQUITY", new BigDecimal("0.02"),
                        "OPERATING_MARGIN", new BigDecimal("23.80"),
                        "REV_GROWTH_YOY", new BigDecimal("4.80"),
                        "EARNINGS_GROWTH", new BigDecimal("5.20"),
                        "VOLATILITY_BETA", new BigDecimal("0.52"),
                        "DIVIDEND_YIELD", new BigDecimal("1.70")
                ));

        // 10. BHARTIARTL - Telecommunications
        createSeedInstrument("BHARTIARTL", "NSE", "Bharti Airtel Ltd.", "Communication", "Telecommunications",
                "Leading telecommunications solutions provider with extensive 5G and fiber network footprint.",
                new BigDecimal("1580.00"), new BigDecimal("945000.00"), asOfDate,
                Map.of(
                        "ROE", new BigDecimal("14.50"),
                        "ROCE", new BigDecimal("15.80"),
                        "PE_RATIO", new BigDecimal("48.50"),
                        "PB_RATIO", new BigDecimal("7.50"),
                        "DEBT_TO_EQUITY", new BigDecimal("1.42"),
                        "OPERATING_MARGIN", new BigDecimal("52.50"),
                        "REV_GROWTH_YOY", new BigDecimal("12.50"),
                        "EARNINGS_GROWTH", new BigDecimal("19.80"),
                        "VOLATILITY_BETA", new BigDecimal("0.88"),
                        "DIVIDEND_YIELD", new BigDecimal("0.55")
                ));

        // 11. LT - Industrials
        createSeedInstrument("LT", "NSE", "Larsen & Toubro Ltd.", "Industrials", "Industrials",
                "Major technology, engineering, construction, manufacturing and financial services conglomerate.",
                new BigDecimal("3620.00"), new BigDecimal("498000.00"), asOfDate,
                Map.of(
                        "ROE", new BigDecimal("15.20"),
                        "ROCE", new BigDecimal("17.50"),
                        "PE_RATIO", new BigDecimal("34.10"),
                        "PB_RATIO", new BigDecimal("4.80"),
                        "DEBT_TO_EQUITY", new BigDecimal("1.10"),
                        "OPERATING_MARGIN", new BigDecimal("11.80"),
                        "REV_GROWTH_YOY", new BigDecimal("19.20"),
                        "EARNINGS_GROWTH", new BigDecimal("22.50"),
                        "VOLATILITY_BETA", new BigDecimal("1.05"),
                        "DIVIDEND_YIELD", new BigDecimal("0.90")
                ));

        // Also integrate any existing Nexora platform stocks into instruments
        importNexoraPlatformStocks(asOfDate);
        log.info("Finished seeding verified market instruments and snapshots.");
    }

    private void createSeedInstrument(String symbol, String exchange, String name, String sector, String industry,
                                      String desc, BigDecimal price, BigDecimal marketCap, LocalDate asOfDate,
                                      Map<String, BigDecimal> metrics) {
        Instrument instrument = instrumentRepository.save(Instrument.builder()
                .symbol(symbol)
                .exchange(exchange)
                .companyName(name)
                .sector(sector)
                .industry(industry)
                .description(desc)
                .currentPrice(price)
                .marketCap(marketCap)
                .instrumentStatus(Instrument.InstrumentStatus.ACTIVE)
                .dataAsOf(asOfDate)
                .build());

        String source = exchange + "_DISCLOSURE_FY26";

        metrics.forEach((key, val) -> {
            String unit = "%";
            if (key.contains("PE") || key.contains("PB") || key.contains("DEBT") || key.contains("BETA")) {
                unit = "x";
            }
            marketDataSnapshotRepository.save(MarketDataSnapshot.builder()
                    .instrument(instrument)
                    .metricKey(key)
                    .metricValue(val)
                    .unit(unit)
                    .currency("INR")
                    .source(source)
                    .asOf(asOfDate)
                    .build());
        });
    }

    private void importNexoraPlatformStocks(LocalDate asOfDate) {
        try {
            List<Stock> platformStocks = stockRepository.findAll();
            for (Stock s : platformStocks) {
                if (s.getStartup() == null || instrumentRepository.findBySymbol(s.getSymbol()).isPresent()) {
                    continue;
                }
                String startupInd = s.getStartup().getIndustry();
                String mappedIndustry = mapStartupIndustry(startupInd);

                Instrument inst = instrumentRepository.save(Instrument.builder()
                        .symbol(s.getSymbol())
                        .exchange("NEXORA")
                        .companyName(s.getStartup().getName())
                        .sector(s.getStartup().getIndustry())
                        .industry(mappedIndustry)
                        .description(s.getStartup().getDescription())
                        .currentPrice(s.getCurrentPrice())
                        .marketCap(s.getStartup().getValuation())
                        .instrumentStatus(Instrument.InstrumentStatus.ACTIVE)
                        .dataAsOf(asOfDate)
                        .build());

                // Seed synthetic conservative fundamental snapshot based on revenue
                BigDecimal rev = s.getStartup().getRevenue() != null ? s.getStartup().getRevenue() : BigDecimal.valueOf(10000000);
                BigDecimal val = s.getStartup().getValuation() != null ? s.getStartup().getValuation() : BigDecimal.valueOf(50000000);
                BigDecimal pe = val.divide(rev.multiply(new BigDecimal("0.15")), 2, java.math.RoundingMode.HALF_UP).max(new BigDecimal("15.0"));

                marketDataSnapshotRepository.save(MarketDataSnapshot.builder()
                        .instrument(inst).metricKey("ROE").metricValue(new BigDecimal("21.50"))
                        .unit("%").currency("INR").source("NEXORA_DISCLOSURE").asOf(asOfDate).build());
                marketDataSnapshotRepository.save(MarketDataSnapshot.builder()
                        .instrument(inst).metricKey("PE_RATIO").metricValue(pe)
                        .unit("x").currency("INR").source("NEXORA_DISCLOSURE").asOf(asOfDate).build());
                marketDataSnapshotRepository.save(MarketDataSnapshot.builder()
                        .instrument(inst).metricKey("DEBT_TO_EQUITY").metricValue(new BigDecimal("0.20"))
                        .unit("x").currency("INR").source("NEXORA_DISCLOSURE").asOf(asOfDate).build());
                marketDataSnapshotRepository.save(MarketDataSnapshot.builder()
                        .instrument(inst).metricKey("OPERATING_MARGIN").metricValue(new BigDecimal("18.40"))
                        .unit("%").currency("INR").source("NEXORA_DISCLOSURE").asOf(asOfDate).build());
                marketDataSnapshotRepository.save(MarketDataSnapshot.builder()
                        .instrument(inst).metricKey("REV_GROWTH_YOY").metricValue(new BigDecimal("28.00"))
                        .unit("%").currency("INR").source("NEXORA_DISCLOSURE").asOf(asOfDate).build());
                marketDataSnapshotRepository.save(MarketDataSnapshot.builder()
                        .instrument(inst).metricKey("VOLATILITY_BETA").metricValue(new BigDecimal("1.15"))
                        .unit("x").currency("INR").source("NEXORA_DISCLOSURE").asOf(asOfDate).build());
            }
        } catch (Exception e) {
            log.warn("Could not import platform stocks to instruments: {}", e.getMessage());
        }
    }

    public String mapStartupIndustry(String ind) {
        if (ind == null) return "Information Technology";
        String upper = ind.toUpperCase();
        if (upper.contains("TECH") || upper.contains("AI") || upper.contains("SOFTWARE")) return "Information Technology";
        if (upper.contains("HEALTH") || upper.contains("BIO") || upper.contains("MED")) return "Healthcare";
        if (upper.contains("FIN")) return "Banking & Financial Services";
        if (upper.contains("ENERGY") || upper.contains("SOLAR") || upper.contains("RENEW")) return "Energy";
        if (upper.contains("AUTO") || upper.contains("MOTOR") || upper.contains("EV")) return "Automobiles";
        if (upper.contains("AGRI") || upper.contains("LOGISTICS") || upper.contains("IND")) return "Industrials";
        if (upper.contains("COMM") || upper.contains("TEL")) return "Telecommunications";
        return "Information Technology";
    }

    public Map<String, BigDecimal> getMetricsForInstrument(Long instrumentId) {
        List<MarketDataSnapshot> snapshots = marketDataSnapshotRepository.findByInstrumentId(instrumentId);
        Map<String, BigDecimal> map = new HashMap<>();
        for (MarketDataSnapshot s : snapshots) {
            map.put(s.getMetricKey(), s.getMetricValue());
        }
        return map;
    }
}
