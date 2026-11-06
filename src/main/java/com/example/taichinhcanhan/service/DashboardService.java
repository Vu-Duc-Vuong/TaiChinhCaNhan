package com.example.taichinhcanhan.service;

import com.example.taichinhcanhan.dto.DashboardData;
import com.example.taichinhcanhan.dto.FinancialSummary;
import com.example.taichinhcanhan.dto.MonthlyStatistics;
import com.example.taichinhcanhan.entity.MoneySource;
import com.example.taichinhcanhan.entity.UserAccount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

          private static final int RECENT_TRANSACTION_LIMIT = 8;

          private final MoneySourceService moneySourceService;
          private final TransactionService transactionService;
          private final StatisticsService statisticsService;

          public DashboardService(MoneySourceService moneySourceService,
                              TransactionService transactionService,
                              StatisticsService statisticsService) {
                    this.moneySourceService = moneySourceService;
                    this.transactionService = transactionService;
                    this.statisticsService = statisticsService;
          }

          @Transactional(readOnly = true)
          public DashboardData load(UserAccount userAccount) {
                    LocalDate today = LocalDate.now();
                    LocalDate monthStart = today.withDayOfMonth(1);
                    LocalDate yearStart = today.withDayOfYear(1);

                    List<MoneySource> moneySources = moneySourceService.search(userAccount, null, null);
                    BigDecimal totalAssets = moneySources.stream()
                                        .map(MoneySource::getCurrentBalance)
                                        .filter(balance -> balance != null)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    FinancialSummary monthlySummary = statisticsService.getSummary(
                                        userAccount, monthStart, today);
                    List<MonthlyStatistics> monthlyStatistics = statisticsService.getMonthlyStatistics(
                                        userAccount, yearStart, today);

                    return new DashboardData(
                                        totalAssets,
                                        monthlySummary,
                                        moneySources,
                                        transactionService.recent(userAccount, RECENT_TRANSACTION_LIMIT),
                                        monthlyStatistics,
                                        "Tháng " + today.getMonthValue() + "/" + today.getYear());
          }
}