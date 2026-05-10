package com.gym.repository;

import com.gym.model.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MembershipRepo extends JpaRepository<Membership, Long> {
    Optional<Membership> findByUsernameAndStatus(String username, String status);
    List<Membership> findByUsername(String username);
    List<Membership> findByUsernameOrderByStartDateDesc(String username);
}