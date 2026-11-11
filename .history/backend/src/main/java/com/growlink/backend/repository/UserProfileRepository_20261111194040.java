package com.growlink.backend.repository;

import java.util.Optional;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.growlink.backend.entity.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserEmailIgnoreCase(String email);
    List<UserProfile> findByUser_IdIn(Collection<Long> userIds);
}
