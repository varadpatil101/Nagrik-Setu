package com.nagriksetu.application.controller;

import com.nagriksetu.application.entity.Issue;
import com.nagriksetu.application.entity.AppUser;
import com.nagriksetu.application.service.AuthService;
import com.nagriksetu.application.service.IssueService;
import com.nagriksetu.application.model.Department;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class WebController {
    private final IssueService issueService; private final AuthService authService;
    public WebController(IssueService issueService, AuthService authService) { this.issueService = issueService; this.authService = authService; }
    @GetMapping("/") String welcome() { return "welcome"; }
    @GetMapping("/citizen/home") String citizenHome(Model model, HttpSession session) { AppUser user = authService.currentUser(session); if (user == null) return "redirect:/login"; if (authService.isStaff(user)) return "redirect:/staff/dashboard"; model.addAttribute("issues", issueService.findAll()); model.addAttribute("user", user); return "index"; }
    @GetMapping("/report") String report(HttpSession session, Model model) { AppUser user = authService.currentUser(session); if (user == null) return "redirect:/login"; if (authService.isStaff(user)) return "redirect:/staff/dashboard"; model.addAttribute("departments", Department.values()); return "report"; }
    @GetMapping("/citizen/dashboard") String citizenDashboard(Model model, HttpSession session) { AppUser user = authService.currentUser(session); if (user == null) return "redirect:/login"; if (authService.isStaff(user)) return "redirect:/staff/dashboard"; model.addAttribute("issues", issueService.findByReporter(user.getId())); model.addAttribute("user", user); return "citizen-dashboard"; }
    @GetMapping({"/dashboard", "/staff/dashboard"}) String staffDashboard(Model model, HttpSession session) { AppUser user = authService.currentUser(session); if (user == null) return "redirect:/login"; if (!authService.isStaff(user)) return "redirect:/citizen/home"; List<Issue> issues = issueService.findByDepartment(user.getDepartment()); model.addAttribute("issues", issues); model.addAttribute("user", user); model.addAttribute("newCount", issues.stream().filter(issue -> "Reported".equals(issue.getStatus())).count()); model.addAttribute("highCount", issues.stream().filter(issue -> "1".equals(issue.getPriority()) || "2".equals(issue.getPriority())).count()); return "dashboard"; }
}
