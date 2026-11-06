package com.example.taichinhcanhan.service;

import com.example.taichinhcanhan.dto.MoneySourceRequest;
import com.example.taichinhcanhan.dto.MoneySourceActivity;
import com.example.taichinhcanhan.entity.MoneySource;
import com.example.taichinhcanhan.entity.MoneySourceType;
import com.example.taichinhcanhan.entity.TransactionType;
import com.example.taichinhcanhan.entity.Transaction;
import com.example.taichinhcanhan.entity.Transfer;
import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.repository.MoneySourceRepository;
import com.example.taichinhcanhan.repository.TransactionRepository;
import com.example.taichinhcanhan.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;

@Service
public class MoneySourceService {

          private final MoneySourceRepository moneySourceRepository;
          private final TransactionRepository transactionRepository;
          private final TransferRepository transferRepository;

          public MoneySourceService(MoneySourceRepository moneySourceRepository,
                              TransactionRepository transactionRepository,
                              TransferRepository transferRepository) {
                    this.moneySourceRepository = moneySourceRepository;
                    this.transactionRepository = transactionRepository;
                    this.transferRepository = transferRepository;
          }

          @Transactional(readOnly = true)
          public List<MoneySource> search(UserAccount userAccount, String keyword, MoneySourceType type) {
                    String normalizedKeyword = keyword == null ? null : keyword.trim();
                    List<MoneySource> moneySources = moneySourceRepository.searchByUserId(
                                        userAccount.getId(), normalizedKeyword, type);
                    populateCurrentBalances(userAccount, moneySources);
                    return moneySources;
          }

          @Transactional
          public MoneySource create(UserAccount userAccount, MoneySourceRequest request) {
                    MoneySource moneySource = new MoneySource();
                    applyRequest(moneySource, request);
                    moneySource.setUserAccount(userAccount);
                    return moneySourceRepository.save(moneySource);
          }

          @Transactional(readOnly = true)
          public MoneySource findOwnedById(Long id, UserAccount userAccount) {
                    MoneySource moneySource = moneySourceRepository.findByIdAndUserAccountId(id, userAccount.getId())
                                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nguồn tiền."));
                    populateCurrentBalances(userAccount, List.of(moneySource));
                    return moneySource;
          }

          @Transactional(readOnly = true)
          public List<MoneySourceActivity> activityHistory(Long sourceId, UserAccount userAccount) {
                    moneySourceRepository.findByIdAndUserAccountId(sourceId, userAccount.getId())
                                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nguồn tiền."));

                    List<MoneySourceActivity> activities = new ArrayList<>();
                    for (Transaction transaction : transactionRepository.findHistoryByUserAndSource(
                                        userAccount.getId(), sourceId)) {
                              boolean income = transaction.getType() == TransactionType.INCOME;
                              String title = hasText(transaction.getDescription())
                                                  ? transaction.getDescription()
                                                  : transaction.getCategory().getName();
                              activities.add(new MoneySourceActivity(
                                                  transaction.getTransactionDate(),
                                                  transaction.getCreatedAt(),
                                                  transaction.getType().getDisplayName(),
                                                  title,
                                                  "Danh mục: " + transaction.getCategory().getName(),
                                                  transaction.getAmount(),
                                                  income));
                    }
                    for (Transfer transfer : transferRepository.findHistoryByUserAndSource(
                                        userAccount.getId(), sourceId)) {
                              boolean incoming = transfer.getToSource().getId().equals(sourceId);
                              String otherSourceName = incoming
                                                  ? transfer.getFromSource().getName()
                                                  : transfer.getToSource().getName();
                              String direction = incoming ? "Chuyển tiền đến" : "Chuyển tiền đi";
                              String title = hasText(transfer.getDescription())
                                                  ? transfer.getDescription()
                                                  : direction + " · " + otherSourceName;
                              String details = incoming
                                                  ? "Từ nguồn: " + otherSourceName
                                                  : "Đến nguồn: " + otherSourceName;
                              activities.add(new MoneySourceActivity(
                                                  transfer.getTransferDate(),
                                                  transfer.getCreatedAt(),
                                                  direction,
                                                  title,
                                                  details,
                                                  transfer.getAmount(),
                                                  incoming));
                    }
                    return activities.stream()
                                        .sorted(Comparator.comparing(MoneySourceActivity::getDate)
                                                            .thenComparing(MoneySourceActivity::getCreatedAt)
                                                            .reversed())
                                        .toList();
          }

          @Transactional
          public MoneySource update(Long id, UserAccount userAccount, MoneySourceRequest request) {
                    MoneySource moneySource = moneySourceRepository.findOwnedByIdForUpdate(id, userAccount.getId())
                                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nguồn tiền."));
                    applyRequest(moneySource, request);
                    return moneySourceRepository.save(moneySource);
          }

          @Transactional
          public void delete(Long id, UserAccount userAccount) {
                    MoneySource moneySource = moneySourceRepository.findOwnedByIdForUpdate(id, userAccount.getId())
                                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nguồn tiền."));
                    if (transactionRepository.existsByMoneySourceId(moneySource.getId())) {
                              throw new IllegalStateException("Không thể xóa nguồn tiền đã có giao dịch liên quan.");
                    }
                    if (transferRepository.existsByFromSourceIdOrToSourceId(moneySource.getId(), moneySource.getId())) {
                              throw new IllegalStateException(
                                                  "Không thể xóa nguồn tiền đang được sử dụng trong chuyển tiền.");
                    }
                    moneySourceRepository.delete(moneySource);
          }

          private void applyRequest(MoneySource moneySource, MoneySourceRequest request) {
                    moneySource.setName(request.getName().trim());
                    moneySource.setType(request.getType());
                    moneySource.setInitialBalance(request.getInitialBalance());
                    moneySource.setDescription(normalizeDescription(request.getDescription()));
          }

          private void populateCurrentBalances(UserAccount userAccount, List<MoneySource> moneySources) {
                    Map<Long, BigDecimal> balances = new HashMap<>();
                    for (MoneySource moneySource : moneySources) {
                              balances.put(moneySource.getId(), moneySource.getInitialBalance());
                    }
                    if (balances.isEmpty()) {
                              return;
                    }

                    for (Object[] row : transactionRepository.summarizeBalancesByUserId(userAccount.getId())) {
                              Long sourceId = ((Number) row[0]).longValue();
                              BigDecimal balance = balances.get(sourceId);
                              if (balance == null) {
                                        continue;
                              }

                              BigDecimal amount = row[2] == null ? BigDecimal.ZERO : (BigDecimal) row[2];
                              balance = row[1] == TransactionType.INCOME
                                                  ? balance.add(amount)
                                                  : balance.subtract(amount);
                              balances.put(sourceId, balance);
                    }
                    for (Object[] row : transferRepository.summarizeIncomingBalancesByUserId(userAccount.getId())) {
                              Long sourceId = ((Number) row[0]).longValue();
                              BigDecimal balance = balances.get(sourceId);
                              if (balance != null) {
                                        balances.put(sourceId, balance.add((BigDecimal) row[1]));
                              }
                    }
                    for (Object[] row : transferRepository.summarizeOutgoingBalancesByUserId(userAccount.getId())) {
                              Long sourceId = ((Number) row[0]).longValue();
                              BigDecimal balance = balances.get(sourceId);
                              if (balance != null) {
                                        balances.put(sourceId, balance.subtract((BigDecimal) row[1]));
                              }
                    }
                    moneySources.forEach(source -> source.setCurrentBalance(balances.get(source.getId())));
          }

          private String normalizeDescription(String description) {
                    if (description == null || description.isBlank()) {
                              return null;
                    }
                    return description.trim();
          }

          private boolean hasText(String value) {
                    return value != null && !value.isBlank();
          }
}