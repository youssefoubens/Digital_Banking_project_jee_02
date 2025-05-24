package org.example.digital_banking.web;

import org.example.digital_banking.dtos.AccountStatsDTO;
import org.example.digital_banking.dtos.DashboardStatsDTO;
import org.example.digital_banking.dtos.TimeRangeDTO;
import org.example.digital_banking.dtos.TransactionStatsDTO;
import org.example.digital_banking.services.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Get overall dashboard statistics
     * @param startDate Optional start date for filtering statistics
     * @param endDate Optional end date for filtering statistics
     * @return Dashboard statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {

        TimeRangeDTO timeRange = null;
        if (startDate != null || endDate != null) {
            timeRange = new TimeRangeDTO(startDate, endDate);
        }

        DashboardStatsDTO stats = dashboardService.getDashboardStats(timeRange);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get account statistics grouped by account type
     * @param startDate Optional start date for filtering statistics
     * @param endDate Optional end date for filtering statistics
     * @return List of account statistics by type
     */
    @GetMapping("/account-stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AccountStatsDTO>> getAccountStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {

        TimeRangeDTO timeRange = null;
        if (startDate != null || endDate != null) {
            timeRange = new TimeRangeDTO(startDate, endDate);
        }

        List<AccountStatsDTO> stats = dashboardService.getAccountStats(timeRange);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get transaction statistics over time
     * @param startDate Start date for the transaction data (required)
     * @param endDate End date for the transaction data (required)
     * @param interval Interval for grouping data (daily, weekly, monthly)
     * @return Transaction statistics
     */
    @GetMapping("/transaction-stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransactionStatsDTO> getTransactionStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate,
            @RequestParam(defaultValue = "daily") String interval) {

        if (startDate == null || endDate == null) {
            return ResponseEntity.badRequest().build();
        }

        TimeRangeDTO timeRange = new TimeRangeDTO(startDate, endDate);
        TransactionStatsDTO stats = dashboardService.getTransactionStats(timeRange, interval);
        return ResponseEntity.ok(stats);
    }
}
