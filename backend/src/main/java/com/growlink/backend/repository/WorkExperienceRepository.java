package com.growlink.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import com.growlink.backend.entity.WorkExperience;

public interface WorkExperienceRepository extends JpaRepository<WorkExperience, Long> {
    List<WorkExperience> findByUserEmailIgnoreCaseOrderByStartDateDesc(String email);
    Optional<WorkExperience> findByIdAndUserEmailIgnoreCase(Long id, String email);
    List<WorkExperience> findByUser_IdInOrderByStartDateDesc(Collection<Long> userIds);
}
