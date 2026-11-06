package com.example.taichinhcanhan.controller;

import com.example.taichinhcanhan.dto.CategoryRequest;
import com.example.taichinhcanhan.entity.Category;
import com.example.taichinhcanhan.entity.CategoryType;
import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.service.CategoryService;
import com.example.taichinhcanhan.service.UserAccountService;
import jakarta.validation.Valid;
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

@Controller
public class CategoryController {

          private final CategoryService categoryService;
          private final UserAccountService userAccountService;

          public CategoryController(CategoryService categoryService,
                              UserAccountService userAccountService) {
                    this.categoryService = categoryService;
                    this.userAccountService = userAccountService;
          }

          @GetMapping("/categories")
          public String list(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) CategoryType type,
                              Model model) {
                    UserAccount userAccount = currentUser(userDetails);
                    model.addAttribute("pageTitle", "Danh mục");
                    model.addAttribute("currentUserName", userAccount.getUsername());
                    model.addAttribute("categories", categoryService.search(userAccount, keyword, type));
                    model.addAttribute("categoryTypes", CategoryType.values());
                    model.addAttribute("keyword", keyword == null ? "" : keyword);
                    model.addAttribute("selectedType", type);
                    return "category/categories";
          }

          @GetMapping("/categories/new")
          public String showCreateForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
                    prepareFormModel(model, currentUser(userDetails), new CategoryRequest(), false, null);
                    return "category/category-form";
          }

          @PostMapping("/categories")
          public String create(@AuthenticationPrincipal UserDetails userDetails,
                              @Valid @ModelAttribute("categoryRequest") CategoryRequest request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    UserAccount userAccount = currentUser(userDetails);
                    if (bindingResult.hasErrors()) {
                              prepareFormModel(model, userAccount, request, false, null);
                              return "category/category-form";
                    }

                    try {
                              categoryService.create(userAccount, request);
                              redirectAttributes.addFlashAttribute("successMessage", "Đã thêm danh mục.");
                    } catch (IllegalArgumentException exception) {
                              bindingResult.reject("categoryError", exception.getMessage());
                              prepareFormModel(model, userAccount, request, false, null);
                              return "category/category-form";
                    }
                    return "redirect:/categories";
          }

          @GetMapping("/categories/{id}")
          public String detail(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    UserAccount userAccount = currentUser(userDetails);
                    try {
                              Category category = categoryService.findOwnedById(id, userAccount);
                              model.addAttribute("pageTitle", "Chi tiết danh mục");
                              model.addAttribute("currentUserName", userAccount.getUsername());
                              model.addAttribute("category", category);
                              return "category/category-detail";
                    } catch (IllegalArgumentException exception) {
                              redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy danh mục.");
                              return "redirect:/categories";
                    }
          }

          @GetMapping("/categories/{id}/edit")
          public String showEditForm(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    UserAccount userAccount = currentUser(userDetails);
                    try {
                              Category category = categoryService.findOwnedById(id, userAccount);
                              prepareFormModel(model, userAccount, toRequest(category), true, id);
                              return "category/category-form";
                    } catch (IllegalArgumentException exception) {
                              redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy danh mục.");
                              return "redirect:/categories";
                    }
          }

          @PostMapping("/categories/{id}/edit")
          public String update(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              @Valid @ModelAttribute("categoryRequest") CategoryRequest request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    UserAccount userAccount = currentUser(userDetails);
                    if (bindingResult.hasErrors()) {
                              prepareFormModel(model, userAccount, request, true, id);
                              return "category/category-form";
                    }

                    try {
                              categoryService.update(id, userAccount, request);
                              redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật danh mục.");
                    } catch (IllegalArgumentException exception) {
                              redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
                    }
                    return "redirect:/categories";
          }

          @PostMapping("/categories/{id}/delete")
          public String delete(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
                    try {
                              categoryService.delete(id, currentUser(userDetails));
                              redirectAttributes.addFlashAttribute("successMessage", "Đã xóa danh mục.");
                    } catch (IllegalStateException exception) {
                              redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
                    } catch (IllegalArgumentException exception) {
                              redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy danh mục.");
                    }
                    return "redirect:/categories";
          }

          private UserAccount currentUser(UserDetails userDetails) {
                    return userAccountService.findByUsername(userDetails.getUsername());
          }

          private void prepareFormModel(Model model,
                              UserAccount userAccount,
                              CategoryRequest request,
                              boolean editMode,
                              Long id) {
                    model.addAttribute("pageTitle", editMode ? "Chỉnh sửa danh mục" : "Thêm danh mục");
                    model.addAttribute("currentUserName", userAccount.getUsername());
                    model.addAttribute("categoryRequest", request);
                    model.addAttribute("categoryTypes", CategoryType.values());
                    model.addAttribute("editMode", editMode);
                    model.addAttribute("categoryId", id);
          }

          private CategoryRequest toRequest(Category category) {
                    CategoryRequest request = new CategoryRequest();
                    request.setName(category.getName());
                    request.setType(category.getType());
                    request.setDescription(category.getDescription());
                    return request;
          }
}