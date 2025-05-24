package org.example.digital_banking.services;

import org.example.digital_banking.dtos.AccountStatsDTO;
import org.example.digital_banking.dtos.DashboardStatsDTO;
import org.example.digital_banking.dtos.TimeRangeDTO;
import org.example.digital_banking.dtos.TransactionStatsDTO;
import org.example.digital_banking.entities.BankAccount;
import org.example.digital_banking.entities.CurrentAccount;
import org.example.digital_banking.entities.Customer;
import org.example.digital_banking.entities.Operation;
import org.example.digital_banking.enums.AccountStatus;
import org.example.digital_banking.enums.Operation_type;
import org.example.digital_banking.repositories.BankAccountRepo;
import org.example.digital_banking.repositories.OperationRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private BankAccountRepo bankAccountRepo;

    @Mock
    private OperationRepo operationRepo;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private Customer customer;
    private BankAccount account1, account2;
    private Operation operation1, operation2, operation3;
    private Date now, oneMonthAgo, twoMonthsAgo;

    @BeforeEach
    void setUp() {
        // Setup dates
        Calendar cal = Calendar.getInstance();
        now = cal.getTime();
        
        cal.add(Calendar.MONTH, -1);
        oneMonthAgo = cal.getTime();
        
        cal.add(Calendar.MONTH, -1);
        twoMonthsAgo = cal.getTime();

        // Setup customer
        customer = Customer.builder()
                .customer_id(1L)
                .name("Test Customer")
                .email("test@example.com")
                .build();

        // Setup accounts
        account1 = CurrentAccount.builder()
                .idBankAccount(1L)
                .balance(1000)
                .status(AccountStatus.ACTIVE)
                .currency("USD")
                .createdAt(oneMonthAgo)
                .customer(customer)
                .overdraft(500)
                .build();

        account2 = CurrentAccount.builder()
                .idBankAccount(2L)
                .balance(2000)
                .status(AccountStatus.ACTIVE)
                .currency("USD")
                .createdAt(twoMonthsAgo)
                .customer(customer)
                .overdraft(500)
                .build();

        // Setup operations
        operation1 = new Operation(1L, Operation_type.CREDIT, 500, oneMonthAgo, "Deposit", account1);
        operation2 = new Operation(2L, Operation_type.DEBIT, 200, now, "Withdrawal", account1);
        operation3 = new Operation(3L, Operation_type.CREDIT, 1000, now, "Deposit", account2);
    }

    @Test
    void getDashboardStats() {
        // Arrange
        when(bankAccountRepo.findAll()).thenReturn(Arrays.asList(account1, account2));
        when(operationRepo.findAll()).thenReturn(Arrays.asList(operation1, operation2, operation3));

        // Act
        DashboardStatsDTO stats = dashboardService.getDashboardStats(null);

        // Assert
        assertEquals(3000, stats.getTotalBalance());
        assertEquals(1300, stats.getBalanceChange());
        assertEquals(2, stats.getActiveAccounts());
        assertEquals(1, stats.getNewAccounts());
        assertEquals(2, stats.getRecentTransactions());
        assertEquals(0, stats.getPendingTransactions());
    }

    @Test
    void getAccountStats() {
        // Arrange
        when(bankAccountRepo.findAll()).thenReturn(Arrays.asList(account1, account2));

        // Act
        List<AccountStatsDTO> stats = dashboardService.getAccountStats(null);

        // Assert
        assertEquals(1, stats.size());
        AccountStatsDTO currentStats = stats.get(0);
        assertEquals("Current", currentStats.getType());
        assertEquals(2, currentStats.getCount());
        assertEquals(3000, currentStats.getTotalBalance());
    }

    @Test
    void getTransactionStats() {
        // Arrange
        when(operationRepo.findAll()).thenReturn(Arrays.asList(operation1, operation2, operation3));

        // Create time range for the test
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -2);
        Date start = cal.getTime();
        
        cal.add(Calendar.MONTH, 3);
        Date end = cal.getTime();
        
        TimeRangeDTO timeRange = new TimeRangeDTO(start, end);

        // Act
        TransactionStatsDTO stats = dashboardService.getTransactionStats(timeRange, "monthly");

        // Assert
        assertNotNull(stats);
        assertNotNull(stats.getDates());
        assertNotNull(stats.getDeposits());
        assertNotNull(stats.getWithdrawals());
        assertNotNull(stats.getNetChange());
        
        // We should have at least one date entry
        assertTrue(stats.getDates().size() > 0);
        
        // The number of entries in all lists should be the same
        assertEquals(stats.getDates().size(), stats.getDeposits().size());
        assertEquals(stats.getDates().size(), stats.getWithdrawals().size());
        assertEquals(stats.getDates().size(), stats.getNetChange().size());
    }
}