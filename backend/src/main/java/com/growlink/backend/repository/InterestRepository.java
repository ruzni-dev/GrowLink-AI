package com.growlink.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.growlink.backend.entity.Interest;

public interface InterestRepository extends JpaRepository<Interest, Long> {
    List<Interest> findAllByOrderByNameAsc();
    Optional<Interest> findByNameIgnoreCase(String name);
}
