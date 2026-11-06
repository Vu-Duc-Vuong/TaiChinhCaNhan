package com.example.taichinhcanhan.controller;

import com.example.taichinhcanhan.dto.FinancialSummary;
import com.example.taichinhcanhan.dto.StatisticsReport;
import com.example.taichinhcanhan.entity.UserAccount;
import com.example.taichinhcanhan.service.StatisticsService;
import com.example.taichinhcanhan.service.UserAccountService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class StatisticsController {

          private final StatisticsService statisticsService;
          private final UserAccountService userAccountService;

          public StatisticsController(StatisticsService statisticsService,
                              UserAccountService userAccountService) {
                    this.statisticsService = statisticsService;
                    this.userAccountService = userAccountService;
          }

          @GetMapping("/statistics")
          public String statistics(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              Model model) {
                    UserAccount userAccount = userAccountService.findByUsername(userDetails.getUsername());
                    LocalDate effectiveEnd = endDate == null ? LocalDate.now() : endDate;
                    LocalDate effectiveStart = startDate == null
                                        ? effectiveEnd.withDayOfYear(1)
                                        : startDate;

                    model.addAttribute("pageTitle", "Thống kê và báo cáo");
                    model.addAttribute("currentUserName", userAccount.getUsername());
                    model.addAttribute("startDate", effectiveStart);
                    model.addAttribute("endDate", effectiveEnd);

                    try {
                              model.addAttribute("report", statisticsService.buildReport(userAccount, effectiveStart,
                                                  effectiveEnd));
                    } catch (IllegalArgumentException exception) {
                              model.addAttribute("errorMessage", exception.getMessage());
                              model.addAttribute("report", emptyReport(effectiveStart, effectiveEnd));
                    }
                    return "statistics/statistics";
          }

          private StatisticsReport emptyReport(LocalDate startDate, LocalDate endDate) {
                    return new StatisticsReport(
                                        startDate,
                                        endDate,
                                        new FinancialSummary(BigDecimal.ZERO, BigDecimal.ZERO, 0, 0),
                                        List.of(),
                                        List.of(),
                                        List.of(),
                                        List.of(),
                                        List.of());
          }
}