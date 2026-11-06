package com.example.taichinhcanhan.service;

import com.example.taichinhcanhan.entity.MoneySource;
import com.example.taichinhcanhan.entity.MoneySourceType;
import com.example.taichinhcanhan.entity.Category;
import com.example.taichinhcanhan.entity.Transaction;
import com.example.taichinhcanhan.entity.TransactionType;
import com.example.taichinhcanhan.entity.Transfer;
import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.dto.MoneySourceActivity;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoneySourceServiceTest {

          @Mock
          private MoneySourceRepository moneySourceRepository;

          @Mock
          private TransactionRepository transactionRepository;

          @Mock
          private TransferRepository transferRepository;

          private MoneySourceService moneySourceService;
          private UserAccount userAccount;

          @BeforeEach
          void setUp() {
                    moneySourceService = new MoneySourceService(
                                        moneySourceRepository, transactionRepository, transferRepository);
                    userAccount = new UserAccount();
                    userAccount.setId(1L);
          }

          @Test
          void calculatesCurrentBalanceFromInitialBalanceAndTransactions() {
                    MoneySource source = new MoneySource();
                    source.setId(10L);
                    source.setName("Ví tiền mặt");
                    source.setType(MoneySourceType.CASH);
                    source.setInitialBalance(new BigDecimal("2000000.00"));

                    when(moneySourceRepository.searchByUserId(1L, null, null)).thenReturn(List.of(source));
                    when(transactionRepository.summarizeBalancesByUserId(1L)).thenReturn(List.of(
                                        new Object[] { 10L, TransactionType.INCOME, new BigDecimal("1000000.00") },
                                        new Object[] { 10L, TransactionType.EXPENSE, new BigDecimal("500000.00") }));
                    when(transferRepository.summarizeIncomingBalancesByUserId(1L)).thenReturn(List.of());
                    when(transferRepository.summarizeOutgoingBalancesByUserId(1L)).thenReturn(List.of());

                    List<MoneySource> result = moneySourceService.search(userAccount, null, null);

                    assertEquals(new BigDecimal("2500000.00"), result.get(0).getCurrentBalance());
                    assertEquals(new BigDecimal("2000000.00"), result.get(0).getInitialBalance());
                    verify(transactionRepository).summarizeBalancesByUserId(1L);
          }

          @Test
          void leavesInitialBalanceWhenSourceHasNoTransactions() {
                    MoneySource source = new MoneySource();
                    source.setId(11L);
                    source.setInitialBalance(new BigDecimal("125.50"));

                    when(moneySourceRepository.searchByUserId(1L, "cash", null)).thenReturn(List.of(source));
                    when(transactionRepository.summarizeBalancesByUserId(1L)).thenReturn(List.of());
                    when(transferRepository.summarizeIncomingBalancesByUserId(1L)).thenReturn(List.of());
                    when(transferRepository.summarizeOutgoingBalancesByUserId(1L)).thenReturn(List.of());

                    List<MoneySource> result = moneySourceService.search(userAccount, "cash", null);

                    assertEquals(new BigDecimal("125.50"), result.get(0).getCurrentBalance());
          }

          @Test
          void appliesIncomingAndOutgoingTransfersWithoutChangingTotalAssets() {
                    MoneySource sourceA = source(10L, "2000000.00");
                    MoneySource sourceB = source(11L, "3000000.00");
                    when(moneySourceRepository.searchByUserId(1L, null, null))
                                        .thenReturn(List.of(sourceA, sourceB));
                    when(transactionRepository.summarizeBalancesByUserId(1L)).thenReturn(List.of());
                    when(transferRepository.summarizeIncomingBalancesByUserId(1L)).thenReturn(List.<Object[]>of(
                                        new Object[] { 11L, new BigDecimal("500000.00") }));
                    when(transferRepository.summarizeOutgoingBalancesByUserId(1L)).thenReturn(List.<Object[]>of(
                                        new Object[] { 10L, new BigDecimal("500000.00") }));

                    List<MoneySource> sources = moneySourceService.search(userAccount, null, null);

                    assertEquals(new BigDecimal("1500000.00"), sources.get(0).getCurrentBalance());
                    assertEquals(new BigDecimal("3500000.00"), sources.get(1).getCurrentBalance());
                    assertEquals(new BigDecimal("5000000.00"), sources.get(0).getCurrentBalance()
                                        .add(sources.get(1).getCurrentBalance()));
          }

          @Test
          void combinesIncomeExpenseAndTransfersInCurrentBalance() {
                    MoneySource source = source(10L, "2000000.00");
                    when(moneySourceRepository.searchByUserId(1L, null, null)).thenReturn(List.of(source));
                    when(transactionRepository.summarizeBalancesByUserId(1L)).thenReturn(List.of(
                                        new Object[] { 10L, TransactionType.INCOME, new BigDecimal("1000000.00") },
                                        new Object[] { 10L, TransactionType.EXPENSE, new BigDecimal("500000.00") }));
                    when(transferRepository.summarizeIncomingBalancesByUserId(1L)).thenReturn(List.<Object[]>of(
                                        new Object[] { 10L, new BigDecimal("300000.00") }));
                    when(transferRepository.summarizeOutgoingBalancesByUserId(1L)).thenReturn(List.<Object[]>of(
                                        new Object[] { 10L, new BigDecimal("100000.00") }));

                    MoneySource result = moneySourceService.search(userAccount, null, null).get(0);

                    assertEquals(new BigDecimal("2700000.00"), result.getCurrentBalance());
          }

          @Test
          void deletesSourceOnlyAfterLockingAndCheckingRelations() {
                    MoneySource source = source(15L, "50.00");
                    when(moneySourceRepository.findOwnedByIdForUpdate(15L, 1L))
                                        .thenReturn(java.util.Optional.of(source));
                    when(transactionRepository.existsByMoneySourceId(15L)).thenReturn(false);
                    when(transferRepository.existsByFromSourceIdOrToSourceId(15L, 15L)).thenReturn(false);

                    moneySourceService.delete(15L, userAccount);

                    verify(transactionRepository).existsByMoneySourceId(15L);
                    verify(transferRepository).existsByFromSourceIdOrToSourceId(15L, 15L);
                    verify(moneySourceRepository).delete(source);
          }

          @Test
          void refusesDeletingSourceThatHasTransactionRelations() {
                    MoneySource source = source(16L, "50.00");
                    when(moneySourceRepository.findOwnedByIdForUpdate(16L, 1L))
                                        .thenReturn(java.util.Optional.of(source));
                    when(transactionRepository.existsByMoneySourceId(16L)).thenReturn(true);

                    assertThrows(IllegalStateException.class, () -> moneySourceService.delete(16L, userAccount));

                    verify(transferRepository, never()).existsByFromSourceIdOrToSourceId(16L, 16L);
                    verify(moneySourceRepository, never()).delete(source);
          }

          @Test
          void refusesDeletingSourceThatHasTransferRelations() {
                    MoneySource source = source(17L, "50.00");
                    when(moneySourceRepository.findOwnedByIdForUpdate(17L, 1L))
                                        .thenReturn(java.util.Optional.of(source));
                    when(transactionRepository.existsByMoneySourceId(17L)).thenReturn(false);
                    when(transferRepository.existsByFromSourceIdOrToSourceId(17L, 17L)).thenReturn(true);

                    assertThrows(IllegalStateException.class, () -> moneySourceService.delete(17L, userAccount));

                    verify(moneySourceRepository, never()).delete(source);
          }

          @Test
          void rejectsDeletingSourceOwnedByAnotherUser() {
                    when(moneySourceRepository.findOwnedByIdForUpdate(18L, 1L))
                                        .thenReturn(java.util.Optional.empty());

                    assertThrows(IllegalArgumentException.class, () -> moneySourceService.delete(18L, userAccount));

                    verify(transactionRepository, never()).existsByMoneySourceId(18L);
          }

          @Test
          void activityHistoryExplainsIncomeExpenseAndTransfersInNewestFirstOrder() {
                    MoneySource wallet = source(20L, "2000.00");
                    wallet.setName("Ví tiền mặt");
                    when(moneySourceRepository.findByIdAndUserAccountId(20L, 1L))
                                        .thenReturn(Optional.of(wallet));

                    Category incomeCategory = category(30L, "Lương");
                    Category expenseCategory = category(31L, "Mua sắm");
                    Transaction income = transaction(40L, TransactionType.INCOME, incomeCategory,
                                        "Lương tháng 11", LocalDate.of(2026, 11, 1),
                                        LocalDateTime.of(2026, 11, 1, 8, 0));
                    Transaction expense = transaction(41L, TransactionType.EXPENSE, expenseCategory,
                                        "Thanh toán hóa đơn", LocalDate.of(2026, 11, 4),
                                        LocalDateTime.of(2026, 11, 4, 10, 0));
                    MoneySource bank = source(21L, "1000.00");
                    bank.setName("Tài khoản ngân hàng");
                    MoneySource cash = source(22L, "500.00");
                    cash.setName("Tiền mặt dự phòng");
                    Transfer transferIn = transfer(50L, cash, wallet, "", LocalDate.of(2026, 11, 3));
                    Transfer transferOut = transfer(51L, wallet, bank, "Chuyển tiền tiết kiệm",
                                        LocalDate.of(2026, 11, 2));

                    when(transactionRepository.findHistoryByUserAndSource(1L, 20L))
                                        .thenReturn(List.of(income, expense));
                    when(transferRepository.findHistoryByUserAndSource(1L, 20L))
                                        .thenReturn(List.of(transferIn, transferOut));

                    List<MoneySourceActivity> activities = moneySourceService.activityHistory(20L, userAccount);

                    assertEquals(4, activities.size());
                    assertEquals("Thanh toán hóa đơn", activities.get(0).getTitle());
                    assertEquals("Mua sắm", activities.get(0).getDetails().replace("Danh mục: ", ""));
                    assertEquals("Chi tiêu", activities.get(0).getTypeLabel());
                    assertEquals(false, activities.get(0).isIncreasesBalance());
                    assertEquals("Chuyển tiền đến · Tiền mặt dự phòng", activities.get(1).getTitle());
                    assertEquals("Chuyển tiền đến", activities.get(1).getTypeLabel());
                    assertEquals(true, activities.get(1).isIncreasesBalance());
                    assertEquals("Chuyển tiền tiết kiệm", activities.get(2).getTitle());
                    assertEquals("Chuyển tiền đi", activities.get(2).getTypeLabel());
                    assertEquals(false, activities.get(2).isIncreasesBalance());
                    assertEquals("Lương tháng 11", activities.get(3).getTitle());
                    assertEquals("Lương", activities.get(3).getDetails().replace("Danh mục: ", ""));
                    assertEquals("Thu nhập", activities.get(3).getTypeLabel());
                    assertEquals(true, activities.get(3).isIncreasesBalance());
                    verify(transactionRepository).findHistoryByUserAndSource(1L, 20L);
                    verify(transferRepository).findHistoryByUserAndSource(1L, 20L);
          }

          @Test
          void activityHistoryRejectsSourceOwnedByAnotherUser() {
                    when(moneySourceRepository.findByIdAndUserAccountId(99L, 1L)).thenReturn(Optional.empty());

                    assertThrows(IllegalArgumentException.class,
                                        () -> moneySourceService.activityHistory(99L, userAccount));

                    verify(transactionRepository, never()).findHistoryByUserAndSource(1L, 99L);
                    verify(transferRepository, never()).findHistoryByUserAndSource(1L, 99L);
          }

          private MoneySource source(Long id, String initialBalance) {
                    MoneySource source = new MoneySource();
                    source.setId(id);
                    source.setInitialBalance(new BigDecimal(initialBalance));
                    source.setUserAccount(userAccount);
                    return source;
          }

          private Category category(Long id, String name) {
                    Category category = new Category();
                    category.setId(id);
                    category.setName(name);
                    category.setUserAccount(userAccount);
                    return category;
          }

          private Transaction transaction(Long id,
                              TransactionType type,
                              Category category,
                              String description,
                              LocalDate date,
                              LocalDateTime createdAt) {
                    Transaction transaction = new Transaction();
                    transaction.setId(id);
                    transaction.setType(type);
                    transaction.setCategory(category);
                    transaction.setDescription(description);
                    transaction.setAmount(new BigDecimal("100.00"));
                    transaction.setTransactionDate(date);
                    transaction.setCreatedAt(createdAt);
                    transaction.setUserAccount(userAccount);
                    return transaction;
          }

          private Transfer transfer(Long id,
                              MoneySource from,
                              MoneySource to,
                              String description,
                              LocalDate date) {
                    Transfer transfer = new Transfer();
                    transfer.setId(id);
                    transfer.setFromSource(from);
                    transfer.setToSource(to);
                    transfer.setDescription(description);
                    transfer.setAmount(new BigDecimal("75.00"));
                    transfer.setTransferDate(date);
                    transfer.setCreatedAt(date.atStartOfDay());
                    transfer.setUserAccount(userAccount);
                    return transfer;
          }
}