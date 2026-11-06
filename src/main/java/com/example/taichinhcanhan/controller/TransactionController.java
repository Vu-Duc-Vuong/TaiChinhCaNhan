package com.example.taichinhcanhan.controller;

import com.example.taichinhcanhan.dto.TransactionRequest;
import com.example.taichinhcanhan.entity.Category;
import com.example.taichinhcanhan.entity.MoneySource;
import com.example.taichinhcanhan.entity.Transaction;
import com.example.taichinhcanhan.entity.TransactionType;
import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.service.CategoryService;
import com.example.taichinhcanhan.service.MoneySourceService;
import com.example.taichinhcanhan.service.TransactionService;
import com.example.taichinhcanhan.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class TransactionController {

          private final TransactionService transactionService;
          private final UserAccountService userAccountService;
          private final MoneySourceService moneySourceService;
          private final CategoryService categoryService;

          public TransactionController(TransactionService transactionService,
                              UserAccountService userAccountService,
                              MoneySourceService moneySourceService,
                              CategoryService categoryService) {
                    this.transactionService = transactionService;
                    this.userAccountService = userAccountService;
                    this.moneySourceService = moneySourceService;
                    this.categoryService = categoryService;
          }

          @GetMapping("/transactions")
          public String list(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) TransactionType type,
                              @RequestParam(required = false) Long moneySourceId,
                              @RequestParam(required = false) Long categoryId,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              Model model) {
                    UserAccount userAccount = currentUser(userDetails);
                    prepareListModel(model, userAccount, keyword, type, moneySourceId, categoryId, startDate, endDate);
                    return "transaction/transactions";
          }

          @GetMapping("/transactions/new")
          public String showCreateForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
                    prepareFormModel(model, currentUser(userDetails), new TransactionRequest(), false, null);
                    return "transaction/transaction-form";
          }

          @PostMapping("/transactions")
          public String create(@AuthenticationPrincipal UserDetails userDetails,
                              @Valid @ModelAttribute("transactionRequest") TransactionRequest request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    UserAccount userAccount = currentUser(userDetails);
                    if (bindingResult.hasErrors()) {
                              prepareFormModel(model, userAccount, request, false, null);
                              return "transaction/transaction-form";
                    }

                    try {
                              transactionService.create(userAccount, request);
                              redirectAttributes.addFlashAttribute("successMessage", "Đã thêm giao dịch.");
                    } catch (IllegalArgumentException exception) {
                              bindingResult.reject("transactionError", exception.getMessage());
                              prepareFormModel(model, userAccount, request, false, null);
                              return "transaction/transaction-form";
                    }
                    return "redirect:/transactions";
          }

          @GetMapping("/transactions/{id}")
          public String detail(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    UserAccount userAccount = currentUser(userDetails);
                    try {
                              Transaction transaction = transactionService.findOwnedById(id, userAccount);
                              model.addAttribute("pageTitle", "Chi tiết giao dịch");
                              model.addAttribute("currentUserName", userAccount.getUsername());
                              model.addAttribute("transaction", transaction);
                              return "transaction/transaction-detail";
                    } catch (IllegalArgumentException exception) {
                              redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy giao dịch.");
                              return "redirect:/transactions";
                    }
          }

          @GetMapping("/transactions/{id}/edit")
          public String showEditForm(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    UserAccount userAccount = currentUser(userDetails);
                    try {
                              Transaction transaction = transactionService.findOwnedById(id, userAccount);
                              prepareFormModel(model, userAccount, toRequest(transaction), true, id);
                              return "transaction/transaction-form";
                    } catch (IllegalArgumentException exception) {
                              redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy giao dịch.");
                              return "redirect:/transactions";
                    }
          }

          @PostMapping("/transactions/{id}/edit")
          public String update(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              @Valid @ModelAttribute("transactionRequest") TransactionRequest request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    UserAccount userAccount = currentUser(userDetails);
                    if (bindingResult.hasErrors()) {
                              prepareFormModel(model, userAccount, request, true, id);
                              return "transaction/transaction-form";
                    }

                    try {
                              transactionService.update(id, userAccount, request);
                              redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật giao dịch.");
                    } catch (IllegalArgumentException exception) {
                              redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
                    }
                    return "redirect:/transactions";
          }

          @PostMapping("/transactions/{id}/delete")
          public String delete(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
                    try {
                              transactionService.delete(id, currentUser(userDetails));
                              redirectAttributes.addFlashAttribute("successMessage", "Đã xóa giao dịch.");
                    } catch (IllegalArgumentException exception) {
                              redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy giao dịch.");
                    }
                    return "redirect:/transactions";
          }

          private UserAccount currentUser(UserDetails userDetails) {
                    return userAccountService.findByUsername(userDetails.getUsername());
          }

          private void prepareListModel(Model model,
                              UserAccount userAccount,
                              String keyword,
                              TransactionType type,
                              Long moneySourceId,
                              Long categoryId,
                              LocalDate startDate,
                              LocalDate endDate) {
                    model.addAttribute("pageTitle", "Giao dịch");
                    model.addAttribute("currentUserName", userAccount.getUsername());
                    model.addAttribute("transactions", transactionService.search(
                                        userAccount, keyword, type, moneySourceId, categoryId, startDate, endDate));
                    model.addAttribute("moneySources", moneySourceService.search(userAccount, null, null));
                    model.addAttribute("categories", categoryService.search(userAccount, null, null));
                    model.addAttribute("transactionTypes", TransactionType.values());
                    model.addAttribute("keyword", keyword == null ? "" : keyword);
                    model.addAttribute("selectedType", type);
                    model.addAttribute("selectedMoneySourceId", moneySourceId);
                    model.addAttribute("selectedCategoryId", categoryId);
                    model.addAttribute("startDate", startDate);
                    model.addAttribute("endDate", endDate);
          }

          private void prepareFormModel(Model model,
                              UserAccount userAccount,
                              TransactionRequest request,
                              boolean editMode,
                              Long id) {
                    model.addAttribute("pageTitle", editMode ? "Chỉnh sửa giao dịch" : "Thêm giao dịch");
                    model.addAttribute("currentUserName", userAccount.getUsername());
                    model.addAttribute("transactionRequest", request);
                    model.addAttribute("transactionTypes", TransactionType.values());
                    model.addAttribute("moneySources", moneySourceService.search(userAccount, null, null));
                    model.addAttribute("categories", categoryService.search(userAccount, null, null));
                    model.addAttribute("editMode", editMode);
                    model.addAttribute("transactionId", id);
          }

          private TransactionRequest toRequest(Transaction transaction) {
                    TransactionRequest request = new TransactionRequest();
                    request.setType(transaction.getType());
                    request.setAmount(transaction.getAmount());
                    request.setCategoryId(transaction.getCategory().getId());
                    request.setMoneySourceId(transaction.getMoneySource().getId());
                    request.setTransactionDate(transaction.getTransactionDate());
                    request.setDescription(transaction.getDescription());
                    return request;
          }
}