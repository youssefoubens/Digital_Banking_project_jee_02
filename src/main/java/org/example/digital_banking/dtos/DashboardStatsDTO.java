package org.example.digital_banking.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDTO {
    private double totalBalance;
    private double balanceChange;
    private int activeAccounts;
    private int newAccounts;
    private int recentTransactions;
    private int pendingTransactions;
}