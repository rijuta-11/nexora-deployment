package com.nexora.repository;

import com.nexora.entity.UserInvestmentPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInvestmentPreferenceRepository extends JpaRepository<UserInvestmentPreference, Long> {
    Optional<UserInvestmentPreference> findTopByUserIdOrderByUpdatedAtDesc(Long userId);
}
