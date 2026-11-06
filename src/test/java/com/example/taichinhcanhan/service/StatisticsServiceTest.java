package com.example.taichinhcanhan.service;

import com.example.taichinhcanhan.dto.MoneySourceStatistics;
import com.example.taichinhcanhan.dto.StatisticsReport;
import com.example.taichinhcanhan.entity.MoneySource;
import com.example.taichinhcanhan.entity.MoneySourceType;
import com.example.taichinhcanhan.entity.TransactionType;
import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.repository.MoneySourceRepository;
import com.example.taichinhcanhan.repository.TransactionRepository;
import com.example.taichinhcanhan.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

          private static final LocalDate START_DATE = LocalDate.of(2026, 2, 1);
          private static final LocalDate END_DATE = LocalDate.of(2026, 2, 28);

          @Mock
          private TransactionRepository transactionRepository;

          @Mock
          private TransferRepository transferRepository;

          @Mock
          private MoneySourceRepository moneySourceRepository;

          @Mock
          private BudgetService budgetService;

          private StatisticsService statisticsService;
          private UserAccount userAccount;

          @BeforeEach
          void setUp() {
                    statisticsService = new StatisticsService(transactionRepository, transferRepository,
                                        moneySourceRepository, budgetService);
                    userAccount = new UserAccount();
                    userAccount.setId(71L);
          }

          @Test
          void groupsDuplicateSourceNamesByIdAndKeepsTransfersOutsideIncomeAndExpense() {
                    when(transactionRepository.summarizeByType(71L, START_DATE, END_DATE))
                                        .thenReturn(List.<Object[]>of(
                                                            new Object[] { TransactionType.INCOME,
                                                                                new BigDecimal("1500.00"), 3L },
                                                            new Object[] { TransactionType.EXPENSE,
                                                                                new BigDecimal("350.00"), 2L }));
                    when(transactionRepository.summarizeByMonth(71L, START_DATE, END_DATE))
                                        .thenReturn(List.<Object[]>of(
                                                            new Object[] { 2026, 2, TransactionType.INCOME,
                                                                                new BigDecimal("1500.00") },
                                                            new Object[] { 2026, 2, TransactionType.EXPENSE,
                                                                                new BigDecimal("350.00") }));
                    when(transactionRepository.summarizeByYear(71L, START_DATE, END_DATE))
                                        .thenReturn(List.<Object[]>of(
                                                            new Object[] { 2026, TransactionType.INCOME,
                                                                                new BigDecimal("1500.00") },
                                                            new Object[] { 2026, TransactionType.EXPENSE,
                                                                                new BigDecimal("350.00") }));
                    when(transactionRepository.summarizeExpenseByCategory(71L, START_DATE, END_DATE))
                                        .thenReturn(List.<Object[]>of(
                                                            new Object[] { "Ăn uống", new BigDecimal("250.00") }));
                    when(transactionRepository.summarizeByMoneySource(71L, START_DATE, END_DATE))
                                        .thenReturn(List.<Object[]>of(
                                                            new Object[] { 11L, "Ví trùng tên", MoneySourceType.CASH,
                                                                                TransactionType.INCOME,
                                                                                new BigDecimal("1000.00") },
                                                            new Object[] { 11L, "Ví trùng tên", MoneySourceType.CASH,
                                                                                TransactionType.EXPENSE,
                                                                                new BigDecimal("250.00") },
                                                            new Object[] { 12L, "Ví trùng tên", MoneySourceType.BANK,
                                                                                TransactionType.INCOME,
                                                                                new BigDecimal("500.00") },
                                                            new Object[] { 13L, "Nguồn chỉ chi", MoneySourceType.OTHER,
                                                                                TransactionType.EXPENSE,
                                                                                new BigDecimal("100.00") }));

                    when(moneySourceRepository.searchByUserId(71L, null, null)).thenReturn(List.of(
                                        source(11L, "Ví trùng tên", MoneySourceType.CASH),
                                        source(12L, "Ví trùng tên", MoneySourceType.BANK),
                                        source(13L, "Nguồn chỉ chi", MoneySourceType.OTHER),
                                        source(14L, "Nguồn rỗng", MoneySourceType.E_WALLET)));
                    when(transferRepository.summarizeIncomingBySource(71L, START_DATE, END_DATE))
                                        .thenReturn(List.<Object[]>of(
                                                            new Object[] { 11L, "Ví trùng tên", MoneySourceType.CASH,
                                                                                new BigDecimal("900.00") },
                                                            new Object[] { 12L, "Ví trùng tên", MoneySourceType.BANK,
                                                                                new BigDecimal("50.00") }));
                    when(transferRepository.summarizeOutgoingBySource(71L, START_DATE, END_DATE))
                                        .thenReturn(List.<Object[]>of(
                                                            new Object[] { 11L, "Ví trùng tên", MoneySourceType.CASH,
                                                                                new BigDecimal("300.00") },
                                                            new Object[] { 12L, "Ví trùng tên", MoneySourceType.BANK,
                                                                                new BigDecimal("50.00") }));
                    when(budgetService.search(userAccount, null, null, null, null, null)).thenReturn(List.of());

                    StatisticsReport report = statisticsService.buildReport(userAccount, START_DATE, END_DATE);

                    assertEquals(new BigDecimal("1500.00"), report.getSummary().getIncome());
                    assertEquals(new BigDecimal("350.00"), report.getSummary().getExpense());
                    assertEquals(new BigDecimal("1150.00"), report.getSummary().getDifference());
                    List<MoneySourceStatistics> sources = report.getMoneySourceStatistics();
                    assertEquals(4, sources.size());
                    assertEquals(11L, sources.get(0).getSourceId());
                    assertEquals(12L, sources.get(1).getSourceId());
                    assertEquals(sources.get(0).getSourceName(), sources.get(1).getSourceName());
                    assertEquals(MoneySourceType.CASH, sources.get(0).getSourceType());
                    assertEquals(MoneySourceType.BANK, sources.get(1).getSourceType());
                    assertEquals(new BigDecimal("1000.00"), sources.get(0).getIncome());
                    assertEquals(new BigDecimal("250.00"), sources.get(0).getExpense());
                    assertEquals(new BigDecimal("900.00"), sources.get(0).getTransferIn());
                    assertEquals(new BigDecimal("300.00"), sources.get(0).getTransferOut());
                    assertEquals(new BigDecimal("500.00"), sources.get(1).getIncome());
                    assertEquals(BigDecimal.ZERO, sources.get(1).getExpense());
                    assertEquals(new BigDecimal("100.00"), sources.get(2).getExpense());
                    assertEquals(BigDecimal.ZERO, sources.get(2).getIncome());
                    assertEquals(BigDecimal.ZERO, sources.get(3).getIncome());
                    assertEquals(BigDecimal.ZERO, sources.get(3).getExpense());
                    assertEquals(1, report.getMonthlyStatistics().size());
                    assertEquals(1, report.getYearlyStatistics().size());
                    assertEquals("Ăn uống", report.getCategoryStatistics().get(0).getCategoryName());

                    verify(transactionRepository).summarizeByMoneySource(71L, START_DATE, END_DATE);
                    verify(transferRepository).summarizeIncomingBySource(71L, START_DATE, END_DATE);
                    verify(transferRepository).summarizeOutgoingBySource(71L, START_DATE, END_DATE);
                    verify(moneySourceRepository).searchByUserId(71L, null, null);
          }

          @Test
          void returnsZeroSourceRowsWhenThereAreNoTransactionsOrTransfers() {
                    when(transactionRepository.summarizeByType(71L, START_DATE, END_DATE)).thenReturn(List.of());
                    when(transactionRepository.summarizeByMonth(71L, START_DATE, END_DATE)).thenReturn(List.of());
                    when(transactionRepository.summarizeByYear(71L, START_DATE, END_DATE)).thenReturn(List.of());
                    when(transactionRepository.summarizeExpenseByCategory(71L, START_DATE, END_DATE))
                                        .thenReturn(List.of());
                    when(transactionRepository.summarizeByMoneySource(71L, START_DATE, END_DATE))
                                        .thenReturn(List.of());
                    when(moneySourceRepository.searchByUserId(71L, null, null))
                                        .thenReturn(List.of(source(14L, "Nguồn rỗng", MoneySourceType.E_WALLET)));
                    when(transferRepository.summarizeIncomingBySource(71L, START_DATE, END_DATE))
                                        .thenReturn(List.of());
                    when(transferRepository.summarizeOutgoingBySource(71L, START_DATE, END_DATE))
                                        .thenReturn(List.of());
                    when(budgetService.search(userAccount, null, null, null, null, null)).thenReturn(List.of());

                    StatisticsReport report = statisticsService.buildReport(userAccount, START_DATE, END_DATE);

                    assertEquals(BigDecimal.ZERO, report.getSummary().getIncome());
                    assertEquals(BigDecimal.ZERO, report.getSummary().getExpense());
                    assertEquals(1, report.getMoneySourceStatistics().size());
                    assertEquals(BigDecimal.ZERO, report.getMoneySourceStatistics().get(0).getIncome());
                    assertEquals(BigDecimal.ZERO, report.getMoneySourceStatistics().get(0).getExpense());
          }

          @Test
          void rejectsInvalidDateRangeBeforeQueryingRepositories() {
                    assertThrows(IllegalArgumentException.class,
                                        () -> statisticsService.buildReport(userAccount, END_DATE, START_DATE));

                    verify(transactionRepository, org.mockito.Mockito.never())
                                        .summarizeByMoneySource(71L, END_DATE, START_DATE);
          }

          private MoneySource source(Long id, String name, MoneySourceType type) {
                    MoneySource source = new MoneySource();
                    source.setId(id);
                    source.setName(name);
                    source.setType(type);
                    return source;
          }
}