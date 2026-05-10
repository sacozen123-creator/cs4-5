package com.gym.repository;

import com.gym.model.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionHistoryRepo extends JpaRepository<TransactionHistory, Long> {
    List<TransactionHistory> findByUsernameOrderByTimestampDesc(String username);
    List<TransactionHistory> findAllByOrderByTimestampDesc();
}