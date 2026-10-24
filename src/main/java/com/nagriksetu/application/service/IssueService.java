package com.nagriksetu.application.service;

import com.nagriksetu.application.dto.*;
import com.nagriksetu.application.entity.Issue;
import com.nagriksetu.application.entity.AppUser;
import com.nagriksetu.application.mapper.IssueMapper;
import com.nagriksetu.application.model.IssueStatus;
import com.nagriksetu.application.repository.IssueRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@Service
public class IssueService {
    private final IssueRepository repository; private final IssueMapper mapper; private final GroqPriorityService groqPriorityService; private final FileStorageService fileStorageService;
    public IssueService(IssueRepository repository, IssueMapper mapper, GroqPriorityService groqPriorityService, FileStorageService fileStorageService) { this.repository = repository; this.mapper = mapper; this.groqPriorityService = groqPriorityService; this.fileStorageService = fileStorageService; }
    public List<Issue> findAll() { return repository.findAllByOrderByPriorityAscCreatedAtDesc(); }
    public List<IssueResponse> findAllResponses() { return findAll().stream().map(mapper::toResponse).toList(); }
    public List<Issue> findByReporter(Long reporterId) { return repository.findAllByReporterIdOrderByPriorityAscCreatedAtDesc(reporterId); }
    public List<Issue> findByDepartment(String department) { return repository.findAllByDepartmentOrderByPriorityAscCreatedAtDesc(department); }
    public void assignUnroutedIssues() { List<Issue> issues = repository.findAll(); issues.forEach(issue -> { if (issue.getDepartment() == null || issue.getDepartment().isBlank()) routeDepartment(issue); if (!issue.getPriority().matches("[1-5]")) issue.setPriority("High".equalsIgnoreCase(issue.getPriority()) ? "1" : "3"); }); repository.saveAll(issues); }
    public IssueResponse create(IssueCreateRequest request, AppUser reporter) { return create(request, reporter, null); }
    public IssueResponse create(IssueCreateRequest request, AppUser reporter, MultipartFile image) { Issue issue = mapper.toEntity(request); issue.setReporterId(reporter.getId()); route(issue, image); return mapper.toResponse(repository.save(issue)); }
    public IssueResponse update(Long id, IssueUpdateRequest request, String staffDepartment) { Issue issue = repository.findById(id).orElseThrow(() -> new NoSuchElementException("Issue not found")); if (!staffDepartment.equals(issue.getDepartment())) throw new SecurityException("You can manage only issues assigned to your department."); if (request.status() != null) issue.setStatus(request.status()); if (request.priority() != null) issue.setPriority(request.priority()); if (request.department() != null && staffDepartment.equals(request.department())) issue.setDepartment(request.department()); return mapper.toResponse(repository.save(issue)); }
    public IssueResponse updateWithEvidence(Long id, String evidenceUrl, String staffDepartment) { Issue issue = repository.findById(id).orElseThrow(() -> new NoSuchElementException("Issue not found")); if (!staffDepartment.equals(issue.getDepartment())) throw new SecurityException("You can manage only issues assigned to your department."); if (evidenceUrl == null || evidenceUrl.isBlank()) throw new IllegalArgumentException("Attach a resolution evidence image before submitting."); String oldEvidence = issue.getResolutionEvidenceUrl(); issue.setResolutionEvidenceUrl(evidenceUrl); issue.setStatus(IssueStatus.RESOLVED.value()); IssueResponse response = mapper.toResponse(repository.save(issue)); if (oldEvidence != null && !oldEvidence.equals(evidenceUrl)) fileStorageService.deleteImage(oldEvidence); return response; }
    public IssueResponse clearResolutionEvidence(Long id, String staffDepartment) { Issue issue = repository.findById(id).orElseThrow(() -> new NoSuchElementException("Issue not found")); if (!staffDepartment.equals(issue.getDepartment())) throw new SecurityException("You can manage only issues assigned to your department."); String oldEvidence = issue.getResolutionEvidenceUrl(); if (oldEvidence == null || oldEvidence.isBlank()) throw new IllegalArgumentException("There is no saved evidence image to clear."); issue.setResolutionEvidenceUrl(null); if (IssueStatus.RESOLVED.value().equals(issue.getStatus())) issue.setStatus(IssueStatus.IN_PROGRESS.value()); IssueResponse response = mapper.toResponse(repository.save(issue)); fileStorageService.deleteImage(oldEvidence); return response; }
    private void route(Issue issue, MultipartFile image) { if (issue.getDepartment() == null || issue.getDepartment().isBlank()) routeDepartment(issue); issue.setPriority(groqPriorityService.suggestPriority(issue.getTitle(), issue.getDescription(), issue.getCategory(), issue.getDepartment(), issue.getLocation(), issue.getLatitude(), issue.getLongitude(), image)); issue.setStatus(IssueStatus.REPORTED.value()); }
    private void routeDepartment(Issue issue) { String category = issue.getCategory().toLowerCase(); if (category.contains("waste") || category.contains("garbage")) issue.setDepartment("Urban Development Department"); else if (category.contains("road") || category.contains("pothole")) issue.setDepartment("Public Works Department"); else if (category.contains("light") || category.contains("electric")) issue.setDepartment("Energy Department"); else if (category.contains("water")) issue.setDepartment("Water Supply and Sanitation Department"); else issue.setDepartment("Urban Development Department"); }
}
