package com.example.taichinhcanhan.controller;

import com.example.taichinhcanhan.dto.ForgotPasswordRequest;
import com.example.taichinhcanhan.dto.RegisterRequest;
import com.example.taichinhcanhan.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

          private final UserAccountService userAccountService;

          public AuthController(UserAccountService userAccountService) {
                    this.userAccountService = userAccountService;
          }

          @GetMapping("/login")
          public String showLogin(@RequestParam(defaultValue = "false") boolean error,
                              @RequestParam(defaultValue = "false") boolean logout,
                              @RequestParam(defaultValue = "false") boolean registered,
                              @RequestParam(defaultValue = "false") boolean passwordChanged,
                              Model model) {
                    model.addAttribute("pageTitle", "Đăng nhập");
                    model.addAttribute("loginError", error);
                    model.addAttribute("loggedOut", logout);
                    model.addAttribute("registered", registered);
                    model.addAttribute("passwordChanged", passwordChanged);
                    return "auth/login";
          }

          @GetMapping("/register")
          public String showRegister(Model model) {
                    model.addAttribute("pageTitle", "Đăng ký");
                    model.addAttribute("registerRequest", new RegisterRequest());
                    return "auth/register";
          }

          @GetMapping("/forgot-password")
          public String showForgotPassword(Model model) {
                    model.addAttribute("pageTitle", "Quên mật khẩu");
                    model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
                    return "auth/forgot-password";
          }

          @PostMapping("/forgot-password")
          public String processForgotPassword(
                              @Valid @ModelAttribute("forgotPasswordRequest") ForgotPasswordRequest request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    if (bindingResult.hasErrors()) {
                              model.addAttribute("pageTitle", "Quên mật khẩu");
                              return "auth/forgot-password";
                    }

                    redirectAttributes.addFlashAttribute("resetEmailSent", true);
                    return "redirect:/login";
          }

          @PostMapping("/register")
          public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
                    if (bindingResult.hasErrors()) {
                              model.addAttribute("pageTitle", "Đăng ký");
                              return "auth/register";
                    }

                    try {
                              userAccountService.register(request);
                    } catch (IllegalArgumentException exception) {
                              bindingResult.reject("registerError", exception.getMessage());
                              model.addAttribute("pageTitle", "Đăng ký");
                              return "auth/register";
                    }

                    redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công. Vui lòng đăng nhập.");
                    return "redirect:/login?registered=true";
          }
}