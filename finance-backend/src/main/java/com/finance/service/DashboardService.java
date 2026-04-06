package com.finance.service;

import com.finance.dto.response.DashboardSummaryResponse;
import com.finance.dto.response.TransactionResponse;
import com.finance.entity.Transaction.TransactionType;
import com.finance.repository.CategoryTotalProjection;
import com.finance.repository.TransactionRepository;
import com.finance.repository.TrendProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;

    public DashboardSummaryResponse getSummary() {
        BigDecimal income   = transactionRepository.sumByType(TransactionType.INCOME);
        BigDecimal expenses = transactionRepository.sumByType(TransactionType.EXPENSE);
        long count          = transactionRepository.countActive();
        return new DashboardSummaryResponse(income, expenses, income.subtract(expenses), count);
    }

    public List<CategoryTotalProjection> getCategoryTotals() {
        return transactionRepository.getCategoryTotals();
    }

    public List<TrendProjection> getTrends(String period) {
        String fmt = "weekly".equalsIgnoreCase(period) ? "%Y-%u" : "%Y-%m";
        return transactionRepository.getTrends(fmt);
    }

    public List<TransactionResponse> getRecent() {
        return transactionRepository.findRecent(PageRequest.of(0, 10))
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }
}
