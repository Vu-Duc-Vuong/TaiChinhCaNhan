package com.example.taichinhcanhan.service;

import com.example.taichinhcanhan.dto.TransferRequest;
import com.example.taichinhcanhan.entity.MoneySource;
import com.example.taichinhcanhan.entity.Transfer;
import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.repository.MoneySourceRepository;
import com.example.taichinhcanhan.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TransferService {

          private final TransferRepository transferRepository;
          private final MoneySourceRepository moneySourceRepository;

          public TransferService(TransferRepository transferRepository,
                              MoneySourceRepository moneySourceRepository) {
                    this.transferRepository = transferRepository;
                    this.moneySourceRepository = moneySourceRepository;
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
                    MoneySource fromSource = findOwnedSource(request.getFromSourceId(), userAccount);
                    MoneySource toSource = findOwnedSource(request.getToSourceId(), userAccount);
                    validateDifferentSources(fromSource, toSource);

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
                    Transfer transfer = findOwnedById(id, userAccount);
                    MoneySource fromSource = findOwnedSource(request.getFromSourceId(), userAccount);
                    MoneySource toSource = findOwnedSource(request.getToSourceId(), userAccount);
                    validateDifferentSources(fromSource, toSource);
                    applyRequest(transfer, request, fromSource, toSource);
                    return transferRepository.save(transfer);
          }

          @Transactional
          public void delete(Long id, UserAccount userAccount) {
                    Transfer transfer = findOwnedById(id, userAccount);
                    transferRepository.delete(transfer);
          }

          private MoneySource findOwnedSource(Long sourceId, UserAccount userAccount) {
                    return moneySourceRepository.findByIdAndUserAccountId(sourceId, userAccount.getId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                            "Nguồn tiền không thuộc người dùng hiện tại."));
          }

          private void validateDifferentSources(MoneySource fromSource, MoneySource toSource) {
                    if (fromSource.getId().equals(toSource.getId())) {
                              throw new IllegalArgumentException("Nguồn tiền chuyển đi và nhận phải khác nhau.");
                    }
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