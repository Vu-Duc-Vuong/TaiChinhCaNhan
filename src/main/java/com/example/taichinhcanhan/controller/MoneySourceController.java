package com.example.taichinhcanhan.controller;

import com.example.taichinhcanhan.dto.MoneySourceRequest;
import com.example.taichinhcanhan.entity.MoneySource;
import com.example.taichinhcanhan.entity.MoneySourceType;
import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.service.MoneySourceService;
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
public class MoneySourceController {

          private final MoneySourceService moneySourceService;
          private final UserAccountService userAccountService;

          public MoneySourceController(MoneySourceService moneySourceService,
                              UserAccountService userAccountService) {
                    this.moneySourceService = moneySourceService;
                    this.userAccountService = userAccountService;
          }

          @GetMapping("/money-sources")
          public String list(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) MoneySourceType type,
                              Model model) {
                    UserAccount userAccount = currentUser(userDetails);
                    model.addAttribute("pageTitle", "Nguồn tiền");
                    model.addAttribute("currentUserName", userAccount.getUsername());
                    model.addAttribute("moneySources", moneySourceService.search(userAccount, keyword, type));
                    model.addAttribute("moneySourceTypes", MoneySourceType.values());
                    model.addAttribute("keyword", keyword == null ? "" : keyword);
                    model.addAttribute("selectedType", type);
                    return "money-source/money-sources";
          }

          @GetMapping("/money-sources/new")
          public String showCreateForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
                    prepareFormModel(model, currentUser(userDetails), new MoneySourceRequest(), false, null);
                    return "money-source/money-source-form";
          }

          @PostMapping("/money-sources")
          public String create(@AuthenticationPrincipal UserDetails userDetails,
                              @Valid @ModelAttribute("moneySourceRequest") MoneySourceRequest request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    UserAccount userAccount = currentUser(userDetails);
                    if (bindingResult.hasErrors()) {
                              prepareFormModel(model, userAccount, request, false, null);
                              return "money-source/money-source-form";
                    }

                    moneySourceService.create(userAccount, request);
                    redirectAttributes.addFlashAttribute("successMessage", "Đã thêm nguồn tiền.");
                    return "redirect:/money-sources";
          }

          @GetMapping("/money-sources/{id}")
          public String detail(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    UserAccount userAccount = currentUser(userDetails);
                    try {
                              MoneySource moneySource = moneySourceService.findOwnedById(id, userAccount);
                              model.addAttribute("pageTitle", "Chi tiết nguồn tiền");
                              model.addAttribute("currentUserName", userAccount.getUsername());
                              model.addAttribute("moneySource", moneySource);
                              model.addAttribute("activities", moneySourceService.activityHistory(id, userAccount));
                              return "money-source/money-source-detail";
                    } catch (IllegalArgumentException exception) {
                              redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nguồn tiền.");
                              return "redirect:/money-sources";
                    }
          }

          @GetMapping("/money-sources/{id}/edit")
          public String showEditForm(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    UserAccount userAccount = currentUser(userDetails);
                    try {
                              MoneySource moneySource = moneySourceService.findOwnedById(id, userAccount);
                              MoneySourceRequest request = toRequest(moneySource);
                              prepareFormModel(model, userAccount, request, true, id);
                              return "money-source/money-source-form";
                    } catch (IllegalArgumentException exception) {
                              redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nguồn tiền.");
                              return "redirect:/money-sources";
                    }
          }

          @PostMapping("/money-sources/{id}/edit")
          public String update(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              @Valid @ModelAttribute("moneySourceRequest") MoneySourceRequest request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    UserAccount userAccount = currentUser(userDetails);
                    if (bindingResult.hasErrors()) {
                              prepareFormModel(model, userAccount, request, true, id);
                              return "money-source/money-source-form";
                    }

                    try {
                              moneySourceService.update(id, userAccount, request);
                              redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật nguồn tiền.");
                    } catch (IllegalArgumentException exception) {
                              redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nguồn tiền.");
                    }
                    return "redirect:/money-sources";
          }

          @PostMapping("/money-sources/{id}/delete")
          public String delete(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
                    try {
                              moneySourceService.delete(id, currentUser(userDetails));
                              redirectAttributes.addFlashAttribute("successMessage", "Đã xóa nguồn tiền.");
                    } catch (IllegalStateException exception) {
                              redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
                    } catch (IllegalArgumentException exception) {
                              redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nguồn tiền.");
                    }
                    return "redirect:/money-sources";
          }

          private UserAccount currentUser(UserDetails userDetails) {
                    return userAccountService.findByUsername(userDetails.getUsername());
          }

          private void prepareFormModel(Model model,
                              UserAccount userAccount,
                              MoneySourceRequest request,
                              boolean editMode,
                              Long id) {
                    model.addAttribute("pageTitle", editMode ? "Chỉnh sửa nguồn tiền" : "Thêm nguồn tiền");
                    model.addAttribute("currentUserName", userAccount.getUsername());
                    model.addAttribute("moneySourceRequest", request);
                    model.addAttribute("moneySourceTypes", MoneySourceType.values());
                    model.addAttribute("editMode", editMode);
                    model.addAttribute("moneySourceId", id);
          }

          private MoneySourceRequest toRequest(MoneySource moneySource) {
                    MoneySourceRequest request = new MoneySourceRequest();
                    request.setName(moneySource.getName());
                    request.setType(moneySource.getType());
                    request.setInitialBalance(moneySource.getInitialBalance());
                    request.setDescription(moneySource.getDescription());
                    return request;
          }
}