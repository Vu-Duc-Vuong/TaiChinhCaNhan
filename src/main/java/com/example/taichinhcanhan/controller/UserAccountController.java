package com.example.taichinhcanhan.controller;

import com.example.taichinhcanhan.dto.ChangePasswordRequest;
import com.example.taichinhcanhan.dto.UpdateProfileRequest;
import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.service.UserAccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserAccountController {

          private final UserAccountService userAccountService;

          public UserAccountController(UserAccountService userAccountService) {
                    this.userAccountService = userAccountService;
          }

          @GetMapping("/profile")
          public String showProfile(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(defaultValue = "false") boolean updated,
                              Model model) {
                    UserAccount userAccount = getCurrentUser(userDetails);
                    UpdateProfileRequest request = new UpdateProfileRequest();
                    request.setEmail(userAccount.getEmail());
                    request.setFullName(userAccount.getFullName());

                    model.addAttribute("pageTitle", "Thông tin cá nhân");
                    model.addAttribute("currentUserName", userAccount.getUsername());
                    model.addAttribute("userAccount", userAccount);
                    model.addAttribute("updateProfileRequest", request);
                    if (updated) {
                              model.addAttribute("successMessage", "Thông tin cá nhân đã được cập nhật.");
                    }
                    return "user/profile";
          }

          @PostMapping("/profile")
          public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                              @Valid @ModelAttribute("updateProfileRequest") UpdateProfileRequest request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    UserAccount userAccount = getCurrentUser(userDetails);
                    if (bindingResult.hasErrors()) {
                              prepareProfileModel(model, userAccount);
                              return "user/profile";
                    }

                    try {
                              userAccountService.updateProfile(userAccount, request);
                    } catch (IllegalArgumentException exception) {
                              bindingResult.reject("profileError", exception.getMessage());
                              prepareProfileModel(model, userAccount);
                              return "user/profile";
                    }

                    redirectAttributes.addFlashAttribute("successMessage", "Thông tin cá nhân đã được cập nhật.");
                    return "redirect:/profile?updated=true";
          }

          @GetMapping("/change-password")
          public String showChangePassword(@AuthenticationPrincipal UserDetails userDetails, Model model) {
                    model.addAttribute("pageTitle", "Đổi mật khẩu");
                    model.addAttribute("currentUserName", userDetails.getUsername());
                    model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
                    return "user/change-password";
          }

          @PostMapping("/change-password")
          public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
                              @Valid @ModelAttribute("changePasswordRequest") ChangePasswordRequest request,
                              BindingResult bindingResult,
                              Model model,
                              HttpServletRequest httpRequest,
                              HttpServletResponse httpResponse) {
                    UserAccount userAccount = getCurrentUser(userDetails);
                    if (bindingResult.hasErrors()) {
                              preparePasswordModel(model, userAccount);
                              return "user/change-password";
                    }

                    try {
                              userAccountService.changePassword(userAccount, request);
                    } catch (IllegalArgumentException exception) {
                              bindingResult.reject("passwordError", exception.getMessage());
                              preparePasswordModel(model, userAccount);
                              return "user/change-password";
                    }

                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    new SecurityContextLogoutHandler().logout(httpRequest, httpResponse, authentication);
                    return "redirect:/login?passwordChanged=true";
          }

          private UserAccount getCurrentUser(UserDetails userDetails) {
                    return userAccountService.findByUsername(userDetails.getUsername());
          }

          private void prepareProfileModel(Model model, UserAccount userAccount) {
                    model.addAttribute("pageTitle", "Thông tin cá nhân");
                    model.addAttribute("currentUserName", userAccount.getUsername());
                    model.addAttribute("userAccount", userAccount);
          }

          private void preparePasswordModel(Model model, UserAccount userAccount) {
                    model.addAttribute("pageTitle", "Đổi mật khẩu");
                    model.addAttribute("currentUserName", userAccount.getUsername());
          }
}