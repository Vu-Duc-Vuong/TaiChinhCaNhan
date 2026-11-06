package com.example.taichinhcanhan.service;

import com.example.taichinhcanhan.dto.DashboardData;
import com.example.taichinhcanhan.dto.FinancialSummary;
import com.example.taichinhcanhan.dto.MonthlyStatistics;
import com.example.taichinhcanhan.entity.MoneySource;
import com.example.taichinhcanhan.entity.Transaction;
import com.example.taichinhcanhan.entity.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

          @Mock
          private MoneySourceService moneySourceService;

          @Mock
          private TransactionService transactionService;

          @Mock
          private StatisticsService statisticsService;

          private DashboardService dashboardService;
          private UserAccount userAccount;

          @BeforeEach
          void setUp() {
                    dashboardService = new DashboardService(
                                        moneySourceService, transactionService, statisticsService);
                    userAccount = new UserAccount();
                    userAccount.setId(42L);
                    userAccount.setUsername("dashboard-user");
          }

          @Test
          void returnsZeroAssetsAndEmptyListsWhenUserHasNoData() {
                    stubDashboardData(List.of(),
                                        new FinancialSummary(BigDecimal.ZERO, BigDecimal.ZERO, 0, 0),
                                        List.of(), List.of());

                    DashboardData dashboard = dashboardService.load(userAccount);

                    assertEquals(BigDecimal.ZERO, dashboard.getTotalAssets());
                    assertEquals(BigDecimal.ZERO, dashboard.getMonthlySummary().getIncome());
                    assertEquals(BigDecimal.ZERO, dashboard.getMonthlySummary().getExpense());
                    assertEquals(List.of(), dashboard.getMoneySources());
                    assertEquals(List.of(), dashboard.getRecentTransactions());
                    assertEquals(List.of(), dashboard.getMonthlyStatistics());
          }

          @Test
          void totalsCurrentBalancesFromAllSourcesWithoutRecalculatingTransfers() {
                    MoneySource wallet = source(1L, "2000.00", "1500.00");
                    MoneySource bank = source(2L, "3000.00", "3500.00");
                    stubDashboardData(List.of(wallet, bank),
                                        new FinancialSummary(BigDecimal.ZERO, BigDecimal.ZERO, 0, 0),
                                        List.of(), List.of());

                    DashboardData dashboard = dashboardService.load(userAccount);

                    assertEquals(new BigDecimal("5000.00"), dashboard.getTotalAssets());
                    assertSame(wallet, dashboard.getMoneySources().get(0));
                    assertSame(bank, dashboard.getMoneySources().get(1));
                    verify(moneySourceService).search(userAccount, null, null);
          }

          @Test
          void usesCurrentMonthSummaryAndYearToDateMonthlySeries() {
                    LocalDate today = LocalDate.now();
                    FinancialSummary summary = new FinancialSummary(
                                        new BigDecimal("1000.00"), new BigDecimal("350.00"), 2, 1);
                    List<MonthlyStatistics> monthlyStatistics = List.of(
                                        new MonthlyStatistics(today.getYear() + "-01",
                                                            new BigDecimal("1000.00"), new BigDecimal("350.00")));
                    stubDashboardData(List.of(), summary, monthlyStatistics, List.of());

                    DashboardData dashboard = dashboardService.load(userAccount);

                    assertEquals(new BigDecimal("1000.00"), dashboard.getMonthlySummary().getIncome());
                    assertEquals(new BigDecimal("350.00"), dashboard.getMonthlySummary().getExpense());
                    assertEquals(new BigDecimal("650.00"), dashboard.getMonthlySummary().getDifference());
                    assertEquals(monthlyStatistics, dashboard.getMonthlyStatistics());
                    assertEquals("Tháng " + today.getMonthValue() + "/" + today.getYear(),
                                        dashboard.getCurrentMonthLabel());
                    verify(statisticsService).getSummary(userAccount, today.withDayOfMonth(1), today);
                    verify(statisticsService).getMonthlyStatistics(userAccount, today.withDayOfYear(1), today);
          }

          @Test
          void passesTheCurrentUserAndLatestTransactionsToTheSnapshot() {
                    Transaction recentTransaction = new Transaction();
                    recentTransaction.setId(900L);
                    stubDashboardData(List.of(),
                                        new FinancialSummary(BigDecimal.ZERO, BigDecimal.ZERO, 0, 0),
                                        List.of(), List.of(recentTransaction));

                    DashboardData dashboard = dashboardService.load(userAccount);

                    assertEquals(1, dashboard.getRecentTransactions().size());
                    assertSame(recentTransaction, dashboard.getRecentTransactions().get(0));
                    verify(moneySourceService).search(userAccount, null, null);
                    verify(transactionService).recent(userAccount, 8);
          }

          private void stubDashboardData(List<MoneySource> moneySources,
                              FinancialSummary summary,
                              List<MonthlyStatistics> monthlyStatistics,
                              List<Transaction> recentTransactions) {
                    LocalDate today = LocalDate.now();
                    when(moneySourceService.search(userAccount, null, null)).thenReturn(moneySources);
                    when(statisticsService.getSummary(userAccount, today.withDayOfMonth(1), today))
                                        .thenReturn(summary);
                    when(statisticsService.getMonthlyStatistics(userAccount, today.withDayOfYear(1), today))
                                        .thenReturn(monthlyStatistics);
                    when(transactionService.recent(userAccount, 8)).thenReturn(recentTransactions);
          }

          private MoneySource source(Long id, String initialBalance, String currentBalance) {
                    MoneySource source = new MoneySource();
                    source.setId(id);
                    source.setInitialBalance(new BigDecimal(initialBalance));
                    source.setCurrentBalance(new BigDecimal(currentBalance));
                    source.setUserAccount(userAccount);
                    return source;
          }
}