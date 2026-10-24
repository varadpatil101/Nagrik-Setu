package com.nagriksetu.application.controller;

import com.nagriksetu.application.entity.AppUser;
import com.nagriksetu.application.service.AuthService;
import com.nagriksetu.application.service.GroqPriorityService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class GroqController {
    private final GroqPriorityService groqPriorityService;
    private final AuthService authService;
    public GroqController(GroqPriorityService groqPriorityService, AuthService authService) { this.groqPriorityService = groqPriorityService; this.authService = authService; }

    @PostMapping("/department-suggestion")
    public ResponseEntity<?> suggestDepartment(@RequestBody Map<String, String> body, HttpSession session) {
        AppUser user = authService.currentUser(session);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please sign in to use automatic department selection."));
        if (authService.isStaff(user)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Only citizens can create new reports."));
        try { return ResponseEntity.ok(groqPriorityService.suggestDepartment(body.getOrDefault("title", "").trim())); }
        catch (IllegalArgumentException exception) { return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage())); }
        catch (IllegalStateException exception) { return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", exception.getMessage())); }
    }

    @PostMapping(value = "/image-verification", consumes = "multipart/form-data")
    public ResponseEntity<?> verifyImage(@RequestParam String title, @RequestParam MultipartFile image, HttpSession session) {
        AppUser user = authService.currentUser(session);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Please sign in to verify a report image."));
        if (authService.isStaff(user)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Only citizens can create new reports."));
        try { return ResponseEntity.ok(groqPriorityService.verifyReportImage(title.trim(), image)); }
        catch (IllegalArgumentException exception) { return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage())); }
        catch (IllegalStateException exception) { return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", exception.getMessage())); }
    }

    /** Kept for the existing generic AI experiment endpoint. */
    @PostMapping("/categorize")
    public ResponseEntity<?> categorize(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.GONE).body(Map.of("message", "Use /api/ai/department-suggestion for department routing."));
    }
}
