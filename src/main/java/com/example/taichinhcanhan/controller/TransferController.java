package com.example.taichinhcanhan.controller;

import com.example.taichinhcanhan.dto.TransferRequest;
import com.example.taichinhcanhan.entity.Transfer;
import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.service.MoneySourceService;
import com.example.taichinhcanhan.service.TransferService;
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
public class TransferController {

          private final TransferService transferService;
          private final UserAccountService userAccountService;
          private final MoneySourceService moneySourceService;

          public TransferController(TransferService transferService,
                              UserAccountService userAccountService,
                              MoneySourceService moneySourceService) {
                    this.transferService = transferService;
                    this.userAccountService = userAccountService;
                    this.moneySourceService = moneySourceService;
          }

          @GetMapping("/transfers")
          public String list(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Long fromSourceId,
                              @RequestParam(required = false) Long toSourceId,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              Model model) {
                    UserAccount userAccount = currentUser(userDetails);
                    prepareListModel(model, userAccount, keyword, fromSourceId, toSourceId, startDate, endDate);
                    return "transfer/transfers";
          }

          @GetMapping("/transfers/new")
          public String showCreateForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
                    prepareFormModel(model, currentUser(userDetails), new TransferRequest(), false, null);
                    return "transfer/transfer-form";
          }

          @PostMapping("/transfers")
          public String create(@AuthenticationPrincipal UserDetails userDetails,
                              @Valid @ModelAttribute("transferRequest") TransferRequest request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    UserAccount userAccount = currentUser(userDetails);
                    if (bindingResult.hasErrors()) {
                              prepareFormModel(model, userAccount, request, false, null);
                              return "transfer/transfer-form";
                    }

                    try {
                              transferService.create(userAccount, request);
                              redirectAttributes.addFlashAttribute("successMessage", "Đã tạo giao dịch chuyển tiền.");
                    } catch (IllegalArgumentException exception) {
                              bindingResult.reject("transferError", exception.getMessage());
                              prepareFormModel(model, userAccount, request, false, null);
                              return "transfer/transfer-form";
                    }
                    return "redirect:/transfers";
          }

          @GetMapping("/transfers/{id}")
          public String detail(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    UserAccount userAccount = currentUser(userDetails);
                    try {
                              Transfer transfer = transferService.findOwnedById(id, userAccount);
                              model.addAttribute("pageTitle", "Chi tiết chuyển tiền");
                              model.addAttribute("currentUserName", userAccount.getUsername());
                              model.addAttribute("transfer", transfer);
                              return "transfer/transfer-detail";
                    } catch (IllegalArgumentException exception) {
                              redirectAttributes.addFlashAttribute("errorMessage",
                                                  "Không tìm thấy giao dịch chuyển tiền.");
                              return "redirect:/transfers";
                    }
          }

          @GetMapping("/transfers/{id}/edit")
          public String showEditForm(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    UserAccount userAccount = currentUser(userDetails);
                    try {
                              Transfer transfer = transferService.findOwnedById(id, userAccount);
                              prepareFormModel(model, userAccount, toRequest(transfer), true, id);
                              return "transfer/transfer-form";
                    } catch (IllegalArgumentException exception) {
                              redirectAttributes.addFlashAttribute("errorMessage",
                                                  "Không tìm thấy giao dịch chuyển tiền.");
                              return "redirect:/transfers";
                    }
          }

          @PostMapping("/transfers/{id}/edit")
          public String update(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              @Valid @ModelAttribute("transferRequest") TransferRequest request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    UserAccount userAccount = currentUser(userDetails);
                    if (bindingResult.hasErrors()) {
                              prepareFormModel(model, userAccount, request, true, id);
                              return "transfer/transfer-form";
                    }

                    try {
                              transferService.update(id, userAccount, request);
                              redirectAttributes.addFlashAttribute("successMessage",
                                                  "Đã cập nhật giao dịch chuyển tiền.");
                    } catch (IllegalArgumentException exception) {
                              redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
                    }
                    return "redirect:/transfers";
          }

          @PostMapping("/transfers/{id}/delete")
          public String delete(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
                    try {
                              transferService.delete(id, currentUser(userDetails));
                              redirectAttributes.addFlashAttribute("successMessage", "Đã xóa giao dịch chuyển tiền.");
                    } catch (IllegalArgumentException exception) {
                              redirectAttributes.addFlashAttribute("errorMessage",
                                                  "Không tìm thấy giao dịch chuyển tiền.");
                    }
                    return "redirect:/transfers";
          }

          private UserAccount currentUser(UserDetails userDetails) {
                    return userAccountService.findByUsername(userDetails.getUsername());
          }

          private void prepareListModel(Model model,
                              UserAccount userAccount,
                              String keyword,
                              Long fromSourceId,
                              Long toSourceId,
                              LocalDate startDate,
                              LocalDate endDate) {
                    model.addAttribute("pageTitle", "Chuyển tiền");
                    model.addAttribute("currentUserName", userAccount.getUsername());
                    model.addAttribute("transfers", transferService.search(
                                        userAccount, keyword, fromSourceId, toSourceId, startDate, endDate));
                    model.addAttribute("moneySources", moneySourceService.search(userAccount, null, null));
                    model.addAttribute("keyword", keyword == null ? "" : keyword);
                    model.addAttribute("selectedFromSourceId", fromSourceId);
                    model.addAttribute("selectedToSourceId", toSourceId);
                    model.addAttribute("startDate", startDate);
                    model.addAttribute("endDate", endDate);
          }

          private void prepareFormModel(Model model,
                              UserAccount userAccount,
                              TransferRequest request,
                              boolean editMode,
                              Long id) {
                    model.addAttribute("pageTitle", editMode ? "Chỉnh sửa chuyển tiền" : "Tạo chuyển tiền");
                    model.addAttribute("currentUserName", userAccount.getUsername());
                    model.addAttribute("transferRequest", request);
                    model.addAttribute("moneySources", moneySourceService.search(userAccount, null, null));
                    model.addAttribute("editMode", editMode);
                    model.addAttribute("transferId", id);
          }

          private TransferRequest toRequest(Transfer transfer) {
                    TransferRequest request = new TransferRequest();
                    request.setFromSourceId(transfer.getFromSource().getId());
                    request.setToSourceId(transfer.getToSource().getId());
                    request.setAmount(transfer.getAmount());
                    request.setTransferDate(transfer.getTransferDate());
                    request.setDescription(transfer.getDescription());
                    return request;
          }
}