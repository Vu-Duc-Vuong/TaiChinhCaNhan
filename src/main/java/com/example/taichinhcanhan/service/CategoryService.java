package com.example.taichinhcanhan.service;

import com.example.taichinhcanhan.dto.CategoryRequest;
import com.example.taichinhcanhan.entity.Category;
import com.example.taichinhcanhan.entity.CategoryType;
import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.repository.BudgetRepository;
import com.example.taichinhcanhan.repository.CategoryRepository;
import com.example.taichinhcanhan.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

          private final CategoryRepository categoryRepository;
          private final TransactionRepository transactionRepository;
          private final BudgetRepository budgetRepository;

          public CategoryService(CategoryRepository categoryRepository,
                              TransactionRepository transactionRepository,
                              BudgetRepository budgetRepository) {
                    this.categoryRepository = categoryRepository;
                    this.transactionRepository = transactionRepository;
                    this.budgetRepository = budgetRepository;
          }

          @Transactional(readOnly = true)
          public List<Category> search(UserAccount userAccount, String keyword, CategoryType type) {
                    String normalizedKeyword = keyword == null ? null : keyword.trim();
                    return categoryRepository.searchByUserId(userAccount.getId(), normalizedKeyword, type);
          }

          @Transactional
          public Category create(UserAccount userAccount, CategoryRequest request) {
                    String name = normalizeName(request.getName());
                    ensureNameAvailable(userAccount, name, request.getType(), null);

                    Category category = new Category();
                    applyRequest(category, request, name);
                    category.setUserAccount(userAccount);
                    return categoryRepository.save(category);
          }

          @Transactional(readOnly = true)
          public Category findOwnedById(Long id, UserAccount userAccount) {
                    return categoryRepository.findByIdAndUserAccountId(id, userAccount.getId())
                                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục."));
          }

          @Transactional
          public Category update(Long id, UserAccount userAccount, CategoryRequest request) {
                    Category category = findOwnedById(id, userAccount);
                    String name = normalizeName(request.getName());
                    ensureNameAvailable(userAccount, name, request.getType(), id);
                    applyRequest(category, request, name);
                    return categoryRepository.save(category);
          }

          @Transactional
          public void delete(Long id, UserAccount userAccount) {
                    Category category = findOwnedById(id, userAccount);
                    if (transactionRepository.existsByCategoryId(category.getId())) {
                              throw new IllegalStateException(
                                                  "Không thể xóa danh mục đang được sử dụng trong giao dịch.");
                    }
                    if (budgetRepository.existsByCategoryId(category.getId())) {
                              throw new IllegalStateException(
                                                  "Không thể xóa danh mục đang được sử dụng trong ngân sách.");
                    }
                    categoryRepository.delete(category);
          }

          private void ensureNameAvailable(UserAccount userAccount,
                              String name,
                              CategoryType type,
                              Long categoryId) {
                    boolean exists = categoryId == null
                                        ? categoryRepository.existsByUserAccountIdAndNameIgnoreCaseAndType(
                                                            userAccount.getId(), name, type)
                                        : categoryRepository.existsByUserAccountIdAndNameIgnoreCaseAndTypeAndIdNot(
                                                            userAccount.getId(), name, type, categoryId);
                    if (exists) {
                              throw new IllegalArgumentException("Tên danh mục đã tồn tại trong cùng loại.");
                    }
          }

          private void applyRequest(Category category, CategoryRequest request, String name) {
                    category.setName(name);
                    category.setType(request.getType());
                    category.setDescription(normalizeDescription(request.getDescription()));
          }

          private String normalizeName(String name) {
                    return name.trim();
          }

          private String normalizeDescription(String description) {
                    if (description == null || description.isBlank()) {
                              return null;
                    }
                    return description.trim();
          }
}