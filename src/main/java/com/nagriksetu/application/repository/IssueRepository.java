package com.nagriksetu.application.repository;

import com.nagriksetu.application.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> { List<Issue> findAllByOrderByPriorityAscCreatedAtDesc(); List<Issue> findAllByReporterIdOrderByPriorityAscCreatedAtDesc(Long reporterId); List<Issue> findAllByDepartmentOrderByPriorityAscCreatedAtDesc(String department); List<Issue> findAllByDepartmentIsNull(); }
