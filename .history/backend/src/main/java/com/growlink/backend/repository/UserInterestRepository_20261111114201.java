package com.growlink.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.growlink.backend.entity.UserInterest;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    List<UserInterest> findByUserEmailIgnoreCaseOrderByInterestNameAsc(String email);
    Optional<UserInterest> findByUserEmailIgnoreCaseAndInterestId(String email, Long interestId);
    void deleteByUserEmailIgnoreCase(String email);
}
