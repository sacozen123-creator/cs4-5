package com.gym.repository;

import com.gym.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepo extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
    List<User> findByRole(String role);
    List<User> findByUserType(String userType);
    List<User> findAllByOrderByJoinedDateDesc();
}