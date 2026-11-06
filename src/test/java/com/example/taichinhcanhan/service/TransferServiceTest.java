package com.example.taichinhcanhan.service;

import com.example.taichinhcanhan.dto.TransferRequest;
import com.example.taichinhcanhan.entity.MoneySource;
import com.example.taichinhcanhan.entity.Transfer;
import com.example.taichinhcanhan.entity.TransactionType;
import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.repository.MoneySourceRepository;
import com.example.taichinhcanhan.repository.TransactionRepository;
import com.example.taichinhcanhan.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

          @Mock
          private TransferRepository transferRepository;

          @Mock
          private MoneySourceRepository moneySourceRepository;

          @Mock
          private TransactionRepository transactionRepository;

          private TransferService transferService;
          private UserAccount userAccount;
          private MoneySource sourceA;
          private MoneySource sourceB;
          private MoneySource sourceC;

          @BeforeEach
          void setUp() {
                    transferService = new TransferService(transferRepository, moneySourceRepository,
                                        transactionRepository);
                    userAccount = new UserAccount();
                    userAccount.setId(1L);
                    sourceA = source(10L, "2000.00");
                    sourceB = source(20L, "3000.00");
                    sourceC = source(30L, "1000.00");
          }

          @Test
          void createsTransferAndLocksSourcesByAscendingId() {
                    stubSource(sourceA);
                    stubSource(sourceB);
                    stubBalance(sourceB, null, "0.00", "0.00", "0.00", "0.00");
                    stubSave();

                    Transfer created = transferService.create(userAccount, request(20L, 10L, "500.00"));

                    assertEquals(sourceB, created.getFromSource());
                    assertEquals(sourceA, created.getToSource());
                    assertEquals(new BigDecimal("500.00"), created.getAmount());
                    InOrder lockOrder = inOrder(moneySourceRepository);
                    lockOrder.verify(moneySourceRepository).findOwnedByIdForUpdate(10L, 1L);
                    lockOrder.verify(moneySourceRepository).findOwnedByIdForUpdate(20L, 1L);
                    verify(transferRepository).save(created);
          }

          @Test
          void rejectsTransferExceedingSenderBalance() {
                    stubSource(sourceA);
                    stubSource(sourceB);
                    stubBalance(sourceA, null, "0.00", "0.00", "0.00", "1500.00");

                    assertThrows(IllegalArgumentException.class,
                                        () -> transferService.create(userAccount, request(10L, 20L, "501.00")));

                    verify(transferRepository, never()).save(any(Transfer.class));
          }

          @Test
          void rejectsZeroAndNegativeAmounts() {
                    assertThrows(IllegalArgumentException.class,
                                        () -> transferService.create(userAccount, request(10L, 20L, "0.00")));
                    assertThrows(IllegalArgumentException.class,
                                        () -> transferService.create(userAccount, request(10L, 20L, "-1.00")));

                    verify(moneySourceRepository, never()).findOwnedByIdForUpdate(any(), any());
          }

          @Test
          void rejectsSameSource() {
                    assertThrows(IllegalArgumentException.class,
                                        () -> transferService.create(userAccount, request(10L, 10L, "1.00")));

                    verify(moneySourceRepository, never()).findOwnedByIdForUpdate(any(), any());
          }

          @Test
          void rejectsMissingDestination() {
                    stubSource(sourceA);
                    when(moneySourceRepository.findOwnedByIdForUpdate(99L, 1L)).thenReturn(Optional.empty());

                    assertThrows(IllegalArgumentException.class,
                                        () -> transferService.create(userAccount, request(10L, 99L, "1.00")));

                    verify(transferRepository, never()).save(any(Transfer.class));
          }

          @Test
          void rejectsDestinationOwnedByAnotherUser() {
                    stubSource(sourceA);
                    when(moneySourceRepository.findOwnedByIdForUpdate(20L, 1L)).thenReturn(Optional.empty());

                    assertThrows(IllegalArgumentException.class,
                                        () -> transferService.create(userAccount, request(10L, 20L, "1.00")));

                    verify(transferRepository, never()).save(any(Transfer.class));
          }

          @Test
          void updatesTransferAmountWithoutDoubleCountingOldTransfer() {
                    Transfer existing = existingTransfer(100L, sourceA, sourceB, "500.00");
                    stubExisting(existing);
                    stubBalance(sourceA, 100L, "0.00", "0.00", "0.00", "0.00");
                    stubSave();

                    Transfer updated = transferService.update(100L, userAccount, request(10L, 20L, "700.00"));

                    assertEquals(new BigDecimal("700.00"), updated.getAmount());
                    assertEquals(sourceA, updated.getFromSource());
                    assertEquals(sourceB, updated.getToSource());
                    verify(transferRepository).sumOutgoingByUserAndSourceExcludingTransfer(1L, 10L, 100L);
                    verify(transferRepository).save(existing);
          }

          @Test
          void updatesSenderSourceAndChecksItsBalance() {
                    Transfer existing = existingTransfer(101L, sourceA, sourceB, "500.00");
                    stubExisting(existing);
                    stubSource(sourceC);
                    stubBalance(sourceC, 101L, "0.00", "0.00", "0.00", "0.00");
                    stubSave();

                    Transfer updated = transferService.update(101L, userAccount, request(30L, 20L, "700.00"));

                    assertEquals(sourceC, updated.getFromSource());
                    assertEquals(sourceB, updated.getToSource());
          }

          @Test
          void updatesReceiverSource() {
                    Transfer existing = existingTransfer(102L, sourceA, sourceB, "500.00");
                    stubExisting(existing);
                    stubSource(sourceC);
                    stubBalance(sourceA, 102L, "0.00", "0.00", "0.00", "0.00");
                    stubSave();

                    Transfer updated = transferService.update(102L, userAccount, request(10L, 30L, "700.00"));

                    assertEquals(sourceA, updated.getFromSource());
                    assertEquals(sourceC, updated.getToSource());
          }

          @Test
          void rejectsUpdateWhenNewAmountExceedsBalanceAfterRemovingOldTransfer() {
                    Transfer existing = existingTransfer(103L, sourceA, sourceB, "500.00");
                    stubExisting(existing);
                    stubBalance(sourceA, 103L, "0.00", "0.00", "0.00", "1500.00");

                    assertThrows(IllegalArgumentException.class, () -> transferService.update(103L, userAccount,
                                        request(10L, 20L, "501.00")));

                    assertEquals(new BigDecimal("500.00"), existing.getAmount());
                    verify(transferRepository, never()).save(any(Transfer.class));
          }

          @Test
          void deletesOwnedTransferAndLocksBothSources() {
                    Transfer existing = existingTransfer(104L, sourceA, sourceB, "500.00");
                    stubExisting(existing);

                    transferService.delete(104L, userAccount);

                    InOrder lockOrder = inOrder(moneySourceRepository);
                    lockOrder.verify(moneySourceRepository).findOwnedByIdForUpdate(10L, 1L);
                    lockOrder.verify(moneySourceRepository).findOwnedByIdForUpdate(20L, 1L);
                    verify(transferRepository).delete(existing);
          }

          @Test
          void doesNotPersistIncomeOrExpenseTransactions() {
                    stubSource(sourceA);
                    stubSource(sourceB);
                    stubBalance(sourceA, null, "100.00", "50.00", "25.00", "0.00");
                    stubSave();

                    transferService.create(userAccount, request(10L, 20L, "75.00"));

                    verify(transferRepository).save(any(Transfer.class));
                    verify(transactionRepository).sumByUserAndSourceAndTypeExcludingTransaction(
                                        1L, 10L, TransactionType.INCOME, null);
                    verify(transactionRepository).sumByUserAndSourceAndTypeExcludingTransaction(
                                        1L, 10L, TransactionType.EXPENSE, null);
                    verify(transactionRepository, never()).save(any());
          }

          @Test
          void doesNotAllowEditingTransferOwnedByAnotherUser() {
                    when(transferRepository.findOwnedByIdForUpdate(105L, 1L)).thenReturn(Optional.empty());

                    assertThrows(IllegalArgumentException.class, () -> transferService.update(
                                        105L, userAccount, request(10L, 20L, "100.00")));

                    verify(moneySourceRepository, never()).findOwnedByIdForUpdate(any(), any());
          }

          private void stubBalance(MoneySource source,
                              Long excludedTransferId,
                              String income,
                              String expense,
                              String transferIn,
                              String transferOut) {
                    when(transactionRepository.sumByUserAndSourceAndTypeExcludingTransaction(
                                        1L, source.getId(), TransactionType.INCOME, null))
                                        .thenReturn(new BigDecimal(income));
                    when(transactionRepository.sumByUserAndSourceAndTypeExcludingTransaction(
                                        1L, source.getId(), TransactionType.EXPENSE, null))
                                        .thenReturn(new BigDecimal(expense));
                    when(transferRepository.sumIncomingByUserAndSourceExcludingTransfer(
                                        1L, source.getId(), excludedTransferId))
                                        .thenReturn(new BigDecimal(transferIn));
                    when(transferRepository.sumOutgoingByUserAndSourceExcludingTransfer(
                                        1L, source.getId(), excludedTransferId))
                                        .thenReturn(new BigDecimal(transferOut));
          }

          private void stubExisting(Transfer transfer) {
                    when(transferRepository.findOwnedByIdForUpdate(transfer.getId(), 1L))
                                        .thenReturn(Optional.of(transfer));
                    stubSource(transfer.getFromSource());
                    stubSource(transfer.getToSource());
          }

          private void stubSource(MoneySource source) {
                    when(moneySourceRepository.findOwnedByIdForUpdate(source.getId(), 1L))
                                        .thenReturn(Optional.of(source));
          }

          private void stubSave() {
                    when(transferRepository.save(any(Transfer.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));
          }

          private TransferRequest request(Long fromId, Long toId, String amount) {
                    TransferRequest request = new TransferRequest();
                    request.setFromSourceId(fromId);
                    request.setToSourceId(toId);
                    request.setAmount(new BigDecimal(amount));
                    request.setTransferDate(LocalDate.now());
                    return request;
          }

          private Transfer existingTransfer(Long id, MoneySource fromSource, MoneySource toSource, String amount) {
                    Transfer transfer = new Transfer();
                    transfer.setId(id);
                    transfer.setUserAccount(userAccount);
                    transfer.setFromSource(fromSource);
                    transfer.setToSource(toSource);
                    transfer.setAmount(new BigDecimal(amount));
                    transfer.setTransferDate(LocalDate.now());
                    return transfer;
          }

          private MoneySource source(Long id, String initialBalance) {
                    MoneySource source = new MoneySource();
                    source.setId(id);
                    source.setInitialBalance(new BigDecimal(initialBalance));
                    source.setUserAccount(userAccount);
                    return source;
          }
}