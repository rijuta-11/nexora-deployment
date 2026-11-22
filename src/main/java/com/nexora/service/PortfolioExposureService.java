package com.nexora.service;

import com.nexora.dto.discovery.PortfolioSummaryDTO;
import com.nexora.dto.discovery.SectorExposureDTO;
import com.nexora.entity.Holding;
import com.nexora.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioExposureService {

    private final HoldingRepository holdingRepository;
    private final MarketDataService marketDataService;

    public PortfolioSummaryDTO calculatePortfolioExposure(Long userId) {
        List<Holding> holdings = holdingRepository.findByUserId(userId);

        if (holdings == null || holdings.isEmpty()) {
            return PortfolioSummaryDTO.builder()
                    .totalPortfolioValue(BigDecimal.ZERO)
                    .totalHoldings(0)
                    .sectorExposure(Collections.emptyList())
                    .warnings(List.of("No existing Nexora holdings detected. Recommendations will be industry-focused without portfolio concentration constraints."))
                    .topSector("None")
                    .topSectorPct(0.0)
                    .build();
        }

        BigDecimal totalVal = BigDecimal.ZERO;
        Map<String, BigDecimal> sectorValues = new HashMap<>();
        Map<String, Integer> sectorCounts = new HashMap<>();

        for (Holding h : holdings) {
            BigDecimal price = h.getStock() != null && h.getStock().getCurrentPrice() != null
                    ? h.getStock().getCurrentPrice()
                    : BigDecimal.ZERO;
            BigDecimal val = price.multiply(BigDecimal.valueOf(h.getQuantity()));
            totalVal = totalVal.add(val);

            String startupIndustry = h.getStock() != null && h.getStock().getStartup() != null
                    ? h.getStock().getStartup().getIndustry()
                    : "Diversified";
            String normalizedSector = marketDataService.mapStartupIndustry(startupIndustry);

            sectorValues.put(normalizedSector, sectorValues.getOrDefault(normalizedSector, BigDecimal.ZERO).add(val));
            sectorCounts.put(normalizedSector, sectorCounts.getOrDefault(normalizedSector, 0) + 1);
        }

        List<SectorExposureDTO> exposures = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        String topSector = "None";
        double maxPct = 0.0;

        for (Map.Entry<String, BigDecimal> entry : sectorValues.entrySet()) {
            String sector = entry.getKey();
            BigDecimal sVal = entry.getValue();

            double pct = totalVal.compareTo(BigDecimal.ZERO) > 0
                    ? sVal.divide(totalVal, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0.0;

            exposures.add(SectorExposureDTO.builder()
                    .sector(sector)
                    .weightPct(Math.round(pct * 10.0) / 10.0)
                    .totalValue(sVal)
                    .holdingCount(sectorCounts.getOrDefault(sector, 1))
                    .build());

            if (pct > maxPct) {
                maxPct = pct;
                topSector = sector;
            }

            if (pct >= 50.0) {
                warnings.add(String.format("Heavy concentration warning: %s accounts for %.1f%% of your equity portfolio.", sector, pct));
            } else if (pct >= 35.0) {
                warnings.add(String.format("Moderate sector concentration: %s accounts for %.1f%% of your portfolio.", sector, pct));
            }
        }

        exposures.sort((a, b) -> Double.compare(b.getWeightPct(), a.getWeightPct()));

        return PortfolioSummaryDTO.builder()
                .totalPortfolioValue(totalVal)
                .totalHoldings(holdings.size())
                .sectorExposure(exposures)
                .warnings(warnings)
                .topSector(topSector)
                .topSectorPct(Math.round(maxPct * 10.0) / 10.0)
                .build();
    }

    public double getSectorWeight(Long userId, String industry) {
        if (userId == null || industry == null) return 0.0;
        PortfolioSummaryDTO summary = calculatePortfolioExposure(userId);
        if (summary.getSectorExposure() == null) return 0.0;

        for (SectorExposureDTO s : summary.getSectorExposure()) {
            if (s.getSector().equalsIgnoreCase(industry)) {
                return s.getWeightPct();
            }
        }
        return 0.0;
    }
}
