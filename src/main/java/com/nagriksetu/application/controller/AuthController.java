package com.nagriksetu.application.controller;

import com.nagriksetu.application.dto.*;
import com.nagriksetu.application.entity.AppUser;
import com.nagriksetu.application.model.Department;
import com.nagriksetu.application.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }
    @GetMapping({"/login", "/register"}) String access(@RequestParam(required = false, defaultValue = "login") String mode, Model model, HttpSession session) { AppUser user = authService.currentUser(session); if (user != null) return authService.isStaff(user) ? "redirect:/staff/dashboard" : "redirect:/citizen/home"; model.addAttribute("mode", mode); model.addAttribute("departments", Department.values()); return "auth"; }
    @PostMapping("/auth/register") String register(@RequestParam String fullName, @RequestParam String email, @RequestParam String password, @RequestParam String role, @RequestParam(required = false) String department, HttpSession session, Model model) { try { AppUser user = authService.register(new RegisterRequest(fullName, email, password, role, department)); authService.signIn(user, session); return authService.isStaff(user) ? "redirect:/staff/dashboard" : "redirect:/citizen/home"; } catch (IllegalArgumentException exception) { model.addAttribute("error", exception.getMessage()); model.addAttribute("mode", "register"); model.addAttribute("departments", Department.values()); return "auth"; } }
    @PostMapping("/auth/login") String login(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) { try { AppUser user = authService.login(new LoginRequest(email, password)); authService.signIn(user, session); return authService.isStaff(user) ? "redirect:/staff/dashboard" : "redirect:/citizen/home"; } catch (IllegalArgumentException exception) { model.addAttribute("error", exception.getMessage()); model.addAttribute("mode", "login"); model.addAttribute("departments", Department.values()); return "auth"; } }
    @PostMapping("/auth/logout") String logout(HttpSession session) { session.invalidate(); return "redirect:/login"; }
    @GetMapping("/profile") String profile(HttpSession session, Model model) { AppUser user = authService.currentUser(session); if (user == null) return "redirect:/login"; model.addAttribute("user", user); return "profile"; }
    @PostMapping("/profile") String updateProfile(@RequestParam String fullName, @RequestParam(required = false) String newPassword, HttpSession session, Model model) { AppUser user = authService.currentUser(session); if (user == null) return "redirect:/login"; try { model.addAttribute("user", authService.updateProfile(user, fullName, newPassword)); model.addAttribute("message", "Profile settings saved."); } catch (IllegalArgumentException exception) { model.addAttribute("user", user); model.addAttribute("error", exception.getMessage()); } return "profile"; }
}
