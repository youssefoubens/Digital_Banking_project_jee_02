package org.example.digital_banking.services;

import org.example.digital_banking.dtos.AccountStatsDTO;
import org.example.digital_banking.dtos.DashboardStatsDTO;
import org.example.digital_banking.dtos.TimeRangeDTO;
import org.example.digital_banking.dtos.TransactionStatsDTO;
import org.example.digital_banking.entities.BankAccount;
import org.example.digital_banking.entities.Operation;
import org.example.digital_banking.enums.AccountStatus;
import org.example.digital_banking.enums.Operation_type;
import org.example.digital_banking.repositories.BankAccountRepo;
import org.example.digital_banking.repositories.OperationRepo;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final BankAccountRepo bankAccountRepo;
    private final OperationRepo operationRepo;

    public DashboardServiceImpl(BankAccountRepo bankAccountRepo, OperationRepo operationRepo) {
        this.bankAccountRepo = bankAccountRepo;
        this.operationRepo = operationRepo;
    }

    @Override
    public DashboardStatsDTO getDashboardStats(TimeRangeDTO timeRange) {
        List<BankAccount> accounts = bankAccountRepo.findAll();
        
        // Calculate total balance
        double totalBalance = accounts.stream()
                .mapToDouble(BankAccount::getBalance)
                .sum();
        
        // Count active accounts
        long activeAccounts = accounts.stream()
                .filter(account -> account.getStatus() == AccountStatus.ACTIVE)
                .count();
        
        // Count new accounts (created within the time range or last 30 days if no range specified)
        Date startDate = timeRange != null && timeRange.getStart() != null ? 
                timeRange.getStart() : getDateMinusDays(30);
        
        long newAccounts = accounts.stream()
                .filter(account -> account.getCreatedAt() != null && 
                        account.getCreatedAt().after(startDate))
                .count();
        
        // Get all operations
        List<Operation> operations = operationRepo.findAll();
        
        // Filter operations by date if timeRange is provided
        if (timeRange != null) {
            if (timeRange.getStart() != null) {
                operations = operations.stream()
                        .filter(op -> op.getOperationDate().after(timeRange.getStart()))
                        .collect(Collectors.toList());
            }
            if (timeRange.getEnd() != null) {
                operations = operations.stream()
                        .filter(op -> op.getOperationDate().before(timeRange.getEnd()))
                        .collect(Collectors.toList());
            }
        }
        
        // Count recent transactions (last 7 days)
        Date lastWeek = getDateMinusDays(7);
        long recentTransactions = operations.stream()
                .filter(op -> op.getOperationDate().after(lastWeek))
                .count();
        
        // Calculate balance change (simplified - just sum of credits minus debits in the period)
        double credits = operations.stream()
                .filter(op -> op.getOperationType() == Operation_type.CREDIT)
                .mapToDouble(Operation::getAmount)
                .sum();
        
        double debits = operations.stream()
                .filter(op -> op.getOperationType() == Operation_type.DEBIT)
                .mapToDouble(Operation::getAmount)
                .sum();
        
        double balanceChange = credits - debits;
        
        // For this example, we'll assume there are no pending transactions
        int pendingTransactions = 0;
        
        return new DashboardStatsDTO(
                totalBalance,
                balanceChange,
                (int) activeAccounts,
                (int) newAccounts,
                (int) recentTransactions,
                pendingTransactions
        );
    }

    @Override
    public List<AccountStatsDTO> getAccountStats(TimeRangeDTO timeRange) {
        List<BankAccount> accounts = bankAccountRepo.findAll();
        
        // Filter accounts by date if timeRange is provided
        if (timeRange != null) {
            if (timeRange.getStart() != null) {
                accounts = accounts.stream()
                        .filter(account -> account.getCreatedAt() == null || 
                                account.getCreatedAt().after(timeRange.getStart()))
                        .collect(Collectors.toList());
            }
            if (timeRange.getEnd() != null) {
                accounts = accounts.stream()
                        .filter(account -> account.getCreatedAt() == null || 
                                account.getCreatedAt().before(timeRange.getEnd()))
                        .collect(Collectors.toList());
            }
        }
        
        // Group accounts by type and calculate statistics
        Map<String, List<BankAccount>> accountsByType = accounts.stream()
                .collect(Collectors.groupingBy(account -> 
                        account.getClass().getSimpleName().replace("Account", "")));
        
        List<AccountStatsDTO> result = new ArrayList<>();
        
        for (Map.Entry<String, List<BankAccount>> entry : accountsByType.entrySet()) {
            String type = entry.getKey();
            List<BankAccount> accountsOfType = entry.getValue();
            
            double totalBalance = accountsOfType.stream()
                    .mapToDouble(BankAccount::getBalance)
                    .sum();
            
            result.add(new AccountStatsDTO(
                    type,
                    accountsOfType.size(),
                    totalBalance
            ));
        }
        
        return result;
    }

    @Override
    public TransactionStatsDTO getTransactionStats(TimeRangeDTO timeRange, String interval) {
        if (timeRange == null || timeRange.getStart() == null || timeRange.getEnd() == null) {
            throw new IllegalArgumentException("Time range with start and end dates is required");
        }
        
        List<Operation> operations = operationRepo.findAll();
        
        // Filter operations by date range
        operations = operations.stream()
                .filter(op -> op.getOperationDate().after(timeRange.getStart()) && 
                        op.getOperationDate().before(timeRange.getEnd()))
                .collect(Collectors.toList());
        
        // Determine date format and grouping based on interval
        String dateFormat;
        Calendar calendar = Calendar.getInstance();
        int calendarField;
        
        switch (interval.toLowerCase()) {
            case "daily":
                dateFormat = "yyyy-MM-dd";
                calendarField = Calendar.DAY_OF_MONTH;
                break;
            case "weekly":
                dateFormat = "yyyy-'W'ww";
                calendarField = Calendar.WEEK_OF_YEAR;
                break;
            case "monthly":
                dateFormat = "yyyy-MM";
                calendarField = Calendar.MONTH;
                break;
            default:
                dateFormat = "yyyy-MM-dd";
                calendarField = Calendar.DAY_OF_MONTH;
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        
        // Group operations by date
        Map<String, List<Operation>> operationsByDate = operations.stream()
                .collect(Collectors.groupingBy(op -> sdf.format(op.getOperationDate())));
        
        // Sort dates
        List<String> sortedDates = new ArrayList<>(operationsByDate.keySet());
        Collections.sort(sortedDates);
        
        // Calculate statistics for each date
        List<Double> deposits = new ArrayList<>();
        List<Double> withdrawals = new ArrayList<>();
        List<Double> netChanges = new ArrayList<>();
        
        for (String date : sortedDates) {
            List<Operation> opsOnDate = operationsByDate.get(date);
            
            double depositsOnDate = opsOnDate.stream()
                    .filter(op -> op.getOperationType() == Operation_type.CREDIT)
                    .mapToDouble(Operation::getAmount)
                    .sum();
            
            double withdrawalsOnDate = opsOnDate.stream()
                    .filter(op -> op.getOperationType() == Operation_type.DEBIT)
                    .mapToDouble(Operation::getAmount)
                    .sum();
            
            deposits.add(depositsOnDate);
            withdrawals.add(withdrawalsOnDate);
            netChanges.add(depositsOnDate - withdrawalsOnDate);
        }
        
        return new TransactionStatsDTO(sortedDates, deposits, withdrawals, netChanges);
    }
    
    private Date getDateMinusDays(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        return calendar.getTime();
    }
}