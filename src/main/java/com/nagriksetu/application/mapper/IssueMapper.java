package com.nagriksetu.application.mapper;

import com.nagriksetu.application.dto.*;
import com.nagriksetu.application.entity.Issue;
import org.springframework.stereotype.Component;

@Component
public class IssueMapper {
    public Issue toEntity(IssueCreateRequest request) { Issue issue = new Issue(); issue.setTitle(request.title()); issue.setDescription(request.description()); issue.setCategory(request.category()); issue.setDepartment(request.department()); issue.setLocation(request.location()); issue.setLatitude(request.latitude()); issue.setLongitude(request.longitude()); issue.setImageUrl(request.imageUrl()); return issue; }
    public IssueResponse toResponse(Issue issue) { return new IssueResponse(issue.getId(), issue.getTitle(), issue.getDescription(), issue.getCategory(), issue.getLocation(), issue.getLatitude(), issue.getLongitude(), issue.getImageUrl(), issue.getResolutionEvidenceUrl(), issue.getDepartment(), issue.getPriority(), issue.getStatus(), issue.getCreatedAt()); }
}
