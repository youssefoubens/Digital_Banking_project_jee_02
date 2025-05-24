package org.example.digital_banking.services;

import org.example.digital_banking.dtos.AccountStatsDTO;
import org.example.digital_banking.dtos.DashboardStatsDTO;
import org.example.digital_banking.dtos.TimeRangeDTO;
import org.example.digital_banking.dtos.TransactionStatsDTO;

import java.util.List;

public interface DashboardService {
    
    /**
     * Get overall dashboard statistics
     * @param timeRange Optional time range for filtering statistics
     * @return Dashboard statistics
     */
    DashboardStatsDTO getDashboardStats(TimeRangeDTO timeRange);
    
    /**
     * Get account statistics grouped by account type
     * @param timeRange Optional time range for filtering statistics
     * @return List of account statistics by type
     */
    List<AccountStatsDTO> getAccountStats(TimeRangeDTO timeRange);
    
    /**
     * Get transaction statistics over time
     * @param timeRange Time range for the transaction data
     * @param interval Interval for grouping data (daily, weekly, monthly)
     * @return Transaction statistics
     */
    TransactionStatsDTO getTransactionStats(TimeRangeDTO timeRange, String interval);
}