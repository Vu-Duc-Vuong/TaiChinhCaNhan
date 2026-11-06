package com.example.taichinhcanhan.service;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

                    List<MoneySource> result = moneySourceService.search(userAccount, "cash", null);

                    assertEquals(new BigDecimal("125.50"), result.get(0).getCurrentBalance());
          }
}