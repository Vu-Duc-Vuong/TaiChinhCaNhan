package com.example.taichinhcanhan.controller;

import com.example.taichinhcanhan.dto.BudgetRequest;
import com.example.taichinhcanhan.entity.Budget;
import com.example.taichinhcanhan.entity.Category;
import com.example.taichinhcanhan.entity.CategoryType;
import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.dto.BudgetSummary;
import com.example.taichinhcanhan.service.BudgetService;
import com.example.taichinhcanhan.service.CategoryService;
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
public class BudgetController {

          private final BudgetService budgetService;
          private final UserAccountService userAccountService;
          private final CategoryService categoryService;

          public BudgetController(BudgetService budgetService,
                              UserAccountService userAccountService,
                              CategoryService categoryService) {
                    this.budgetService = budgetService;
                    this.userAccountService = userAccountService;
                    this.categoryService = categoryService;
          }

          @GetMapping("/budgets")
          public String list(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Long categoryId,
                              @RequestParam(required = false) Boolean overall,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              @RequestParam(required = false) String status,
                              Model model) {
                    UserAccount userAccount = currentUser(userDetails);
                    prepareListModel(model, userAccount, keyword, categoryId, overall, startDate, endDate, status);
                    return "budget/budgets";
          }

          @GetMapping("/budgets/new")
          public String showCreateForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
                    prepareFormModel(model, currentUser(userDetails), new BudgetRequest(), false, null);
                    return "budget/budget-form";
          }

          @PostMapping("/budgets")
          public String create(@AuthenticationPrincipal UserDetails userDetails,
                              @Valid @ModelAttribute("budgetRequest") BudgetRequest request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    UserAccount userAccount = currentUser(userDetails);
                    if (bindingResult.hasErrors()) {
                              prepareFormModel(model, userAccount, request, false, null);
                              return "budget/budget-form";
                    }

                    try {
                              budgetService.create(userAccount, request);
                              redirectAttributes.addFlashAttribute("successMessage", "Đã thêm ngân sách.");
                    } catch (IllegalArgumentException exception) {
                              bindingResult.reject("budgetError", exception.getMessage());
                              prepareFormModel(model, userAccount, request, false, null);
                              return "budget/budget-form";
                    }
                    return "redirect:/budgets";
          }

          @GetMapping("/budgets/{id}")
          public String detail(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    UserAccount userAccount = currentUser(userDetails);
                    try {
                              BudgetSummary summary = budgetService.getSummary(id, userAccount);
                              model.addAttribute("pageTitle", "Chi tiết ngân sách");
                              model.addAttribute("currentUserName", userAccount.getUsername());
                              model.addAttribute("summary", summary);
                              return "budget/budget-detail";
                    } catch (IllegalArgumentException exception) {
                              redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy ngân sách.");
                              return "redirect:/budgets";
                    }
          }

          @GetMapping("/budgets/{id}/edit")
          public String showEditForm(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    UserAccount userAccount = currentUser(userDetails);
                    try {
                              Budget budget = budgetService.findOwnedById(id, userAccount);
                              prepareFormModel(model, userAccount, toRequest(budget), true, id);
                              return "budget/budget-form";
                    } catch (IllegalArgumentException exception) {
                              redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy ngân sách.");
                              return "redirect:/budgets";
                    }
          }

          @PostMapping("/budgets/{id}/edit")
          public String update(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              @Valid @ModelAttribute("budgetRequest") BudgetRequest request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    UserAccount userAccount = currentUser(userDetails);
                    if (bindingResult.hasErrors()) {
                              prepareFormModel(model, userAccount, request, true, id);
                              return "budget/budget-form";
                    }

                    try {
                              budgetService.update(id, userAccount, request);
                              redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật ngân sách.");
                    } catch (IllegalArgumentException exception) {
                              redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
                    }
                    return "redirect:/budgets";
          }

          @PostMapping("/budgets/{id}/delete")
          public String delete(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
                    try {
                              budgetService.delete(id, currentUser(userDetails));
                              redirectAttributes.addFlashAttribute("successMessage", "Đã xóa ngân sách.");
                    } catch (IllegalArgumentException exception) {
                              redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy ngân sách.");
                    }
                    return "redirect:/budgets";
          }

          private UserAccount currentUser(UserDetails userDetails) {
                    return userAccountService.findByUsername(userDetails.getUsername());
          }

          private void prepareListModel(Model model,
                              UserAccount userAccount,
                              String keyword,
                              Long categoryId,
                              Boolean overall,
                              LocalDate startDate,
                              LocalDate endDate,
                              String status) {
                    model.addAttribute("pageTitle", "Ngân sách");
                    model.addAttribute("currentUserName", userAccount.getUsername());
                    model.addAttribute("summaries", budgetService.search(
                                        userAccount, keyword, categoryId, overall, startDate, endDate, status));
                    model.addAttribute("categories", expenseCategories(userAccount));
                    model.addAttribute("keyword", keyword == null ? "" : keyword);
                    model.addAttribute("selectedCategoryId", categoryId);
                    model.addAttribute("selectedOverall", overall);
                    model.addAttribute("startDate", startDate);
                    model.addAttribute("endDate", endDate);
                    model.addAttribute("selectedStatus", status);
          }

          private void prepareFormModel(Model model,
                              UserAccount userAccount,
                              BudgetRequest request,
                              boolean editMode,
                              Long id) {
                    model.addAttribute("pageTitle", editMode ? "Chỉnh sửa ngân sách" : "Thêm ngân sách");
                    model.addAttribute("currentUserName", userAccount.getUsername());
                    model.addAttribute("budgetRequest", request);
                    model.addAttribute("categories", expenseCategories(userAccount));
                    model.addAttribute("editMode", editMode);
                    model.addAttribute("budgetId", id);
          }

          private java.util.List<Category> expenseCategories(UserAccount userAccount) {
                    return categoryService.search(userAccount, null, CategoryType.EXPENSE);
          }

          private BudgetRequest toRequest(Budget budget) {
                    BudgetRequest request = new BudgetRequest();
                    request.setName(budget.getName());
                    request.setAmount(budget.getAmount());
                    request.setCategoryId(budget.getCategory() == null ? null : budget.getCategory().getId());
                    request.setStartDate(budget.getStartDate());
                    request.setEndDate(budget.getEndDate());
                    request.setDescription(budget.getDescription());
                    return request;
          }
}