package com.example.taichinhcanhan.service;

import com.example.taichinhcanhan.dto.MoneySourceRequest;
import com.example.taichinhcanhan.entity.MoneySource;
import com.example.taichinhcanhan.entity.MoneySourceType;
import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.repository.MoneySourceRepository;
import com.example.taichinhcanhan.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MoneySourceService {

          private final MoneySourceRepository moneySourceRepository;
          private final TransactionRepository transactionRepository;

          public MoneySourceService(MoneySourceRepository moneySourceRepository,
                              TransactionRepository transactionRepository) {
                    this.moneySourceRepository = moneySourceRepository;
                    this.transactionRepository = transactionRepository;
          }

          @Transactional(readOnly = true)
          public List<MoneySource> search(UserAccount userAccount, String keyword, MoneySourceType type) {
                    String normalizedKeyword = keyword == null ? null : keyword.trim();
                    return moneySourceRepository.searchByUserId(userAccount.getId(), normalizedKeyword, type);
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
                    return moneySourceRepository.findByIdAndUserAccountId(id, userAccount.getId())
                                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nguồn tiền."));
          }

          @Transactional
          public MoneySource update(Long id, UserAccount userAccount, MoneySourceRequest request) {
                    MoneySource moneySource = findOwnedById(id, userAccount);
                    applyRequest(moneySource, request);
                    return moneySourceRepository.save(moneySource);
          }

          @Transactional
          public void delete(Long id, UserAccount userAccount) {
                    MoneySource moneySource = findOwnedById(id, userAccount);
                    if (transactionRepository.existsByMoneySourceId(moneySource.getId())) {
                              throw new IllegalStateException("Không thể xóa nguồn tiền đã có giao dịch liên quan.");
                    }
                    moneySourceRepository.delete(moneySource);
          }

          private void applyRequest(MoneySource moneySource, MoneySourceRequest request) {
                    moneySource.setName(request.getName().trim());
                    moneySource.setType(request.getType());
                    moneySource.setInitialBalance(request.getInitialBalance());
                    moneySource.setDescription(normalizeDescription(request.getDescription()));
          }

          private String normalizeDescription(String description) {
                    if (description == null || description.isBlank()) {
                              return null;
                    }
                    return description.trim();
          }
}