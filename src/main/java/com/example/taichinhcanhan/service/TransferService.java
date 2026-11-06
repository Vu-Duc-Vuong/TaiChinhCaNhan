package com.example.taichinhcanhan.service;

import com.example.taichinhcanhan.dto.TransferRequest;
import com.example.taichinhcanhan.entity.MoneySource;
import com.example.taichinhcanhan.entity.Transfer;
import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.repository.MoneySourceRepository;
import com.example.taichinhcanhan.repository.TransactionRepository;
import com.example.taichinhcanhan.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Service
public class TransferService {

          private final TransferRepository transferRepository;
          private final MoneySourceRepository moneySourceRepository;
          private final TransactionRepository transactionRepository;

          public TransferService(TransferRepository transferRepository,
                              MoneySourceRepository moneySourceRepository,
                              TransactionRepository transactionRepository) {
                    this.transferRepository = transferRepository;
                    this.moneySourceRepository = moneySourceRepository;
                    this.transactionRepository = transactionRepository;
          }

          @Transactional(readOnly = true)
          public List<Transfer> search(UserAccount userAccount,
                              String keyword,
                              Long fromSourceId,
                              Long toSourceId,
                              LocalDate startDate,
                              LocalDate endDate) {
                    String normalizedKeyword = keyword == null ? null : keyword.trim();
                    return transferRepository.searchByUserId(
                                        userAccount.getId(), normalizedKeyword, fromSourceId, toSourceId, startDate,
                                        endDate);
          }

          @Transactional
          public Transfer create(UserAccount userAccount, TransferRequest request) {
                    validateRequest(request);
                    Map<Long, MoneySource> sources = lockOwnedSources(
                                        List.of(request.getFromSourceId(), request.getToSourceId()), userAccount);
                    MoneySource fromSource = sources.get(request.getFromSourceId());
                    MoneySource toSource = sources.get(request.getToSourceId());
                    validateAvailableBalance(fromSource, userAccount, request.getAmount(), null);

                    Transfer transfer = new Transfer();
                    applyRequest(transfer, request, fromSource, toSource);
                    transfer.setUserAccount(userAccount);
                    return transferRepository.save(transfer);
          }

          @Transactional(readOnly = true)
          public Transfer findOwnedById(Long id, UserAccount userAccount) {
                    return transferRepository.findByIdAndUserAccountId(id, userAccount.getId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                            "Không tìm thấy giao dịch chuyển tiền."));
          }

          @Transactional
          public Transfer update(Long id, UserAccount userAccount, TransferRequest request) {
                    Transfer transfer = transferRepository.findOwnedByIdForUpdate(id, userAccount.getId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                            "Không tìm thấy giao dịch chuyển tiền."));
                    validateRequest(request);
                    Map<Long, MoneySource> sources = lockOwnedSources(List.of(
                                        transfer.getFromSource().getId(), transfer.getToSource().getId(),
                                        request.getFromSourceId(), request.getToSourceId()), userAccount);
                    MoneySource fromSource = sources.get(request.getFromSourceId());
                    MoneySource toSource = sources.get(request.getToSourceId());
                    validateAvailableBalance(fromSource, userAccount, request.getAmount(), id);
                    applyRequest(transfer, request, fromSource, toSource);
                    return transferRepository.save(transfer);
          }

          @Transactional
          public void delete(Long id, UserAccount userAccount) {
                    Transfer transfer = transferRepository.findOwnedByIdForUpdate(id, userAccount.getId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                            "Không tìm thấy giao dịch chuyển tiền."));
                    lockOwnedSources(List.of(transfer.getFromSource().getId(), transfer.getToSource().getId()),
                                        userAccount);
                    transferRepository.delete(transfer);
          }

          private void validateRequest(TransferRequest request) {
                    if (request.getAmount() == null || request.getAmount().signum() <= 0) {
                              throw new IllegalArgumentException("Số tiền chuyển phải lớn hơn 0.");
                    }
                    if (request.getFromSourceId() == null || request.getToSourceId() == null) {
                              throw new IllegalArgumentException("Vui lòng chọn nguồn tiền chuyển đi và nhận.");
                    }
                    if (request.getFromSourceId().equals(request.getToSourceId())) {
                              throw new IllegalArgumentException("Nguồn tiền chuyển đi và nhận phải khác nhau.");
                    }
          }

          private Map<Long, MoneySource> lockOwnedSources(List<Long> sourceIds, UserAccount userAccount) {
                    Map<Long, MoneySource> sources = new HashMap<>();
                    for (Long sourceId : new TreeSet<>(sourceIds)) {
                              MoneySource source = moneySourceRepository.findOwnedByIdForUpdate(
                                                  sourceId, userAccount.getId())
                                                  .orElseThrow(() -> new IllegalArgumentException(
                                                                      "Nguồn tiền không thuộc người dùng hiện tại."));
                              sources.put(sourceId, source);
                    }
                    return sources;
          }

          private void validateAvailableBalance(MoneySource source,
                              UserAccount userAccount,
                              BigDecimal amount,
                              Long excludedTransferId) {
                    BigDecimal income = transactionRepository.sumByUserAndSourceAndTypeExcludingTransaction(
                                        userAccount.getId(), source.getId(),
                                        com.example.taichinhcanhan.entity.TransactionType.INCOME, null);
                    BigDecimal expense = transactionRepository.sumByUserAndSourceAndTypeExcludingTransaction(
                                        userAccount.getId(), source.getId(),
                                        com.example.taichinhcanhan.entity.TransactionType.EXPENSE, null);
                    BigDecimal transferIn = transferRepository.sumIncomingByUserAndSourceExcludingTransfer(
                                        userAccount.getId(), source.getId(), excludedTransferId);
                    BigDecimal transferOut = transferRepository.sumOutgoingByUserAndSourceExcludingTransfer(
                                        userAccount.getId(), source.getId(), excludedTransferId);
                    BigDecimal availableBalance = source.getInitialBalance()
                                        .add(zeroIfNull(income))
                                        .subtract(zeroIfNull(expense))
                                        .add(zeroIfNull(transferIn))
                                        .subtract(zeroIfNull(transferOut));
                    if (amount.compareTo(availableBalance) > 0) {
                              throw new IllegalArgumentException("Số dư nguồn tiền chuyển đi không đủ.");
                    }
          }

          private BigDecimal zeroIfNull(BigDecimal amount) {
                    return amount == null ? BigDecimal.ZERO : amount;
          }

          private void applyRequest(Transfer transfer,
                              TransferRequest request,
                              MoneySource fromSource,
                              MoneySource toSource) {
                    transfer.setFromSource(fromSource);
                    transfer.setToSource(toSource);
                    transfer.setAmount(request.getAmount());
                    transfer.setTransferDate(request.getTransferDate());
                    transfer.setDescription(normalizeDescription(request.getDescription()));
          }

          private String normalizeDescription(String description) {
                    if (description == null || description.isBlank()) {
                              return null;
                    }
                    return description.trim();
          }
}