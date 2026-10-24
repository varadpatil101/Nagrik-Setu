package com.nagriksetu.application.config;

import org.springframework.context.annotation.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.*;
import java.nio.file.Paths;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.nagriksetu.application.service.IssueService;

@Configuration
public class AppConfig implements WebMvcConfigurer { @Value("${app.upload-dir}") private String uploadDirectory; @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); } @Bean CommandLineRunner routeExistingIssues(IssueService issueService) { return arguments -> issueService.assignUnroutedIssues(); } @Override public void addResourceHandlers(ResourceHandlerRegistry registry) { registry.addResourceHandler("/uploads/**").addResourceLocations(Paths.get(uploadDirectory).toAbsolutePath().toUri().toString()); } }
