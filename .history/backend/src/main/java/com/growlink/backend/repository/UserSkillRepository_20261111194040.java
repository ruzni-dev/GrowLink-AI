package com.growlink.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import com.growlink.backend.entity.UserSkill;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
    List<UserSkill> findByUserEmailIgnoreCaseOrderBySkillNameAsc(String email);
    Optional<UserSkill> findByIdAndUserEmailIgnoreCase(Long id, String email);
    boolean existsByUserEmailIgnoreCaseAndSkillNameIgnoreCase(String email, String name);
    List<UserSkill> findByUser_IdInOrderBySkillNameAsc(Collection<Long> userIds);
}
