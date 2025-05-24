package org.example.digital_banking.web;

import org.example.digital_banking.dtos.AccountStatsDTO;
import org.example.digital_banking.dtos.DashboardStatsDTO;
import org.example.digital_banking.dtos.TimeRangeDTO;
import org.example.digital_banking.dtos.TransactionStatsDTO;
import org.example.digital_banking.services.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    private MockMvc mockMvc;
    private SimpleDateFormat dateFormat;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }

    @Test
    void getDashboardStats() throws Exception {
        // Arrange
        DashboardStatsDTO statsDTO = new DashboardStatsDTO(
                5000.0,
                1000.0,
                10,
                2,
                15,
                0
        );

        when(dashboardService.getDashboardStats(any())).thenReturn(statsDTO);

        // Act & Assert
        mockMvc.perform(get("/dashboard/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBalance", is(5000.0)))
                .andExpect(jsonPath("$.balanceChange", is(1000.0)))
                .andExpect(jsonPath("$.activeAccounts", is(10)))
                .andExpect(jsonPath("$.newAccounts", is(2)))
                .andExpect(jsonPath("$.recentTransactions", is(15)))
                .andExpect(jsonPath("$.pendingTransactions", is(0)));
    }

    @Test
    void getDashboardStatsWithDateRange() throws Exception {
        // Arrange
        DashboardStatsDTO statsDTO = new DashboardStatsDTO(
                3000.0,
                500.0,
                5,
                1,
                10,
                0
        );

        Date startDate = dateFormat.parse("2023-01-01");
        Date endDate = dateFormat.parse("2023-01-31");
        
        when(dashboardService.getDashboardStats(any(TimeRangeDTO.class))).thenReturn(statsDTO);

        // Act & Assert
        mockMvc.perform(get("/dashboard/stats")
                .param("startDate", "2023-01-01")
                .param("endDate", "2023-01-31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBalance", is(3000.0)))
                .andExpect(jsonPath("$.balanceChange", is(500.0)))
                .andExpect(jsonPath("$.activeAccounts", is(5)))
                .andExpect(jsonPath("$.newAccounts", is(1)))
                .andExpect(jsonPath("$.recentTransactions", is(10)))
                .andExpect(jsonPath("$.pendingTransactions", is(0)));
    }

    @Test
    void getAccountStats() throws Exception {
        // Arrange
        List<AccountStatsDTO> accountStats = Arrays.asList(
                new AccountStatsDTO("Current", 5, 3000.0),
                new AccountStatsDTO("Saving", 3, 2000.0)
        );

        when(dashboardService.getAccountStats(any())).thenReturn(accountStats);

        // Act & Assert
        mockMvc.perform(get("/dashboard/account-stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].type", is("Current")))
                .andExpect(jsonPath("$[0].count", is(5)))
                .andExpect(jsonPath("$[0].totalBalance", is(3000.0)))
                .andExpect(jsonPath("$[1].type", is("Saving")))
                .andExpect(jsonPath("$[1].count", is(3)))
                .andExpect(jsonPath("$[1].totalBalance", is(2000.0)));
    }

    @Test
    void getTransactionStats() throws Exception {
        // Arrange
        TransactionStatsDTO transactionStats = new TransactionStatsDTO(
                Arrays.asList("2023-01-01", "2023-01-02", "2023-01-03"),
                Arrays.asList(1000.0, 1500.0, 2000.0),
                Arrays.asList(500.0, 700.0, 900.0),
                Arrays.asList(500.0, 800.0, 1100.0)
        );

        when(dashboardService.getTransactionStats(any(TimeRangeDTO.class), eq("daily"))).thenReturn(transactionStats);

        // Act & Assert
        mockMvc.perform(get("/dashboard/transaction-stats")
                .param("startDate", "2023-01-01")
                .param("endDate", "2023-01-31")
                .param("interval", "daily")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dates", hasSize(3)))
                .andExpect(jsonPath("$.deposits", hasSize(3)))
                .andExpect(jsonPath("$.withdrawals", hasSize(3)))
                .andExpect(jsonPath("$.netChange", hasSize(3)))
                .andExpect(jsonPath("$.dates[0]", is("2023-01-01")))
                .andExpect(jsonPath("$.deposits[0]", is(1000.0)))
                .andExpect(jsonPath("$.withdrawals[0]", is(500.0)))
                .andExpect(jsonPath("$.netChange[0]", is(500.0)));
    }

    @Test
    void getTransactionStatsMissingDates() throws Exception {
        // Act & Assert - Missing required parameters should return 400 Bad Request
        mockMvc.perform(get("/dashboard/transaction-stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}