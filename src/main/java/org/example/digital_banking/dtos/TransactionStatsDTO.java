package org.example.digital_banking.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionStatsDTO {
    private List<String> dates;
    private List<Double> deposits;
    private List<Double> withdrawals;
    private List<Double> netChange;
}