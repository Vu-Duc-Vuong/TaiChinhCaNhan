package com.example.taichinhcanhan.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

          @GetMapping({ "/", "/dashboard" })
          public String showDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
                    model.addAttribute("pageTitle", "Tổng quan");
                    model.addAttribute("currentUserName", userDetails.getUsername());
                    return "dashboard/index";
          }
}