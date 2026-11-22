package com.nexora.repository;

import com.nexora.entity.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, Long> {
    Optional<Instrument> findBySymbol(String symbol);
    List<Instrument> findByIndustryIgnoreCase(String industry);
    List<Instrument> findByIndustryInIgnoreCase(List<String> industries);
    List<Instrument> findByInstrumentStatus(Instrument.InstrumentStatus status);
}
