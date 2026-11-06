package com.example.taichinhcanhan.controller;

import com.example.taichinhcanhan.service.DashboardService;
import com.example.taichinhcanhan.service.UserAccountService;
import com.example.taichinhcanhan.entity.UserAccount;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

          private final DashboardService dashboardService;
          private final UserAccountService userAccountService;

          public DashboardController(DashboardService dashboardService, UserAccountService userAccountService) {
                    this.dashboardService = dashboardService;
                    this.userAccountService = userAccountService;
          }

          @GetMapping({ "/", "/dashboard" })
          public String showDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
                    UserAccount userAccount = userAccountService.findByUsername(userDetails.getUsername());
                    model.addAttribute("pageTitle", "Tổng quan");
                    model.addAttribute("currentUserName", userAccount.getUsername());
                    model.addAttribute("dashboard", dashboardService.load(userAccount));
                    return "dashboard/index";
          }
}