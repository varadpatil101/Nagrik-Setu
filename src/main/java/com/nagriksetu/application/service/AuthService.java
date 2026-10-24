package com.nagriksetu.application.service;

import com.nagriksetu.application.dto.*;
import com.nagriksetu.application.entity.AppUser;
import com.nagriksetu.application.model.UserRole;
import com.nagriksetu.application.repository.AppUserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    public static final String USER_ID = "nagrikSetuUserId";
    private final AppUserRepository users; private final PasswordEncoder passwordEncoder;
    public AuthService(AppUserRepository users, PasswordEncoder passwordEncoder) { this.users = users; this.passwordEncoder = passwordEncoder; }
    public AppUser register(RegisterRequest request) {
        if (users.findByEmail(request.email().trim().toLowerCase()).isPresent()) throw new IllegalArgumentException("An account already exists for this email.");
        UserRole role; try { role = UserRole.valueOf(request.role().toUpperCase()); } catch (Exception exception) { throw new IllegalArgumentException("Select a valid role."); }
        if (role == UserRole.STAFF && (request.department() == null || request.department().isBlank())) throw new IllegalArgumentException("Staff members must select a department.");
        AppUser user = new AppUser(); user.setFullName(request.fullName().trim()); user.setEmail(request.email().trim().toLowerCase()); user.setPasswordHash(passwordEncoder.encode(request.password())); user.setRole(role.name()); user.setDepartment(role == UserRole.STAFF ? request.department() : null); return users.save(user);
    }
    public AppUser login(LoginRequest request) { AppUser user = users.findByEmail(request.email().trim().toLowerCase()).orElseThrow(() -> new IllegalArgumentException("Incorrect email or password.")); if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) throw new IllegalArgumentException("Incorrect email or password."); return user; }
    public AppUser currentUser(HttpSession session) { Object userId = session.getAttribute(USER_ID); if (!(userId instanceof Long id)) return null; return users.findById(id).orElse(null); }
    public void signIn(AppUser user, HttpSession session) { session.setAttribute(USER_ID, user.getId()); }
    public boolean isStaff(AppUser user) { return user != null && UserRole.STAFF.name().equals(user.getRole()); }
    public AppUser updateProfile(AppUser user, String fullName, String newPassword) { if (fullName != null && !fullName.isBlank()) user.setFullName(fullName.trim()); if (newPassword != null && !newPassword.isBlank()) { if (newPassword.length() < 6) throw new IllegalArgumentException("New password must contain at least 6 characters."); user.setPasswordHash(passwordEncoder.encode(newPassword)); } return users.save(user); }
}
