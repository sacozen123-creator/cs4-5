package com.gym.service;

import com.gym.model.TransactionHistory;
import com.gym.repository.TransactionHistoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TransactionHistoryService {

    @Autowired
    private TransactionHistoryRepo transactionHistoryRepo;

    public void recordTransaction(String username, String action, String details) {
        TransactionHistory transaction = new TransactionHistory(username, action, details);
        transactionHistoryRepo.save(transaction);
    }
    
    public List<TransactionHistory> getUserTransactions(String username) {
        return transactionHistoryRepo.findByUsernameOrderByTimestampDesc(username);
    }
    
    public List<TransactionHistory> getAllTransactions() {
        return transactionHistoryRepo.findAllByOrderByTimestampDesc();
    }
}