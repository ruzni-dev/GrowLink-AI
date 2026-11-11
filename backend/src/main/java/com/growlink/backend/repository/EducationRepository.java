package com.growlink.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import com.growlink.backend.entity.Education;

public interface EducationRepository extends JpaRepository<Education, Long> {
    List<Education> findByUserEmailIgnoreCaseOrderByStartYearDesc(String email);
    Optional<Education> findByIdAndUserEmailIgnoreCase(Long id, String email);
    List<Education> findByUser_IdInOrderByStartYearDesc(Collection<Long> userIds);
}
