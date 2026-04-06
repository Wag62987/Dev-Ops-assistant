package com.Innocent.DevOpsAsistant.Devops.Assistant.Controller;

import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.AddMemberRequest;
import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.AddTaskRequest;
import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.CreateProjectRequest;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.Member;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.Project;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.TaskItem;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.PlanningService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planning")
@RequiredArgsConstructor
public class PlanningController {

    private final PlanningService service;

    // Create Project
    @PostMapping("/project")
    public ResponseEntity<Project> createProject(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateProjectRequest request) {

        String githubId = userDetails.getUsername(); // assuming username = githubId
        return ResponseEntity.ok(service.createProject(githubId, request));
    }

    // Get all projects for the authenticated user
    @GetMapping("/projects")
    public ResponseEntity<List<Project>> getProjects(
            @AuthenticationPrincipal UserDetails userDetails) {

        String githubId = userDetails.getUsername();
        return ResponseEntity.ok(service.getProjects(githubId));
    }

    // Add Member to a project
    @PostMapping("/project/{projectId}/member")
    public ResponseEntity<Member> addMember(
            @AuthenticationPrincipal  AppUser appuser,
            @PathVariable Integer projectId,
            @RequestBody AddMemberRequest request) {

        String githubId = appuser.getGithubId();
        return ResponseEntity.ok(service.addMember(githubId, projectId, request));
    }

    // Add Task to a project
    @PostMapping("/project/{projectId}/task")
    public ResponseEntity<TaskItem> addTask(
            @AuthenticationPrincipal AppUser appuser,
            @PathVariable Integer projectId,
            @RequestBody AddTaskRequest request) {

        String githubId = appuser.getGithubId();
        return ResponseEntity.ok(service.addTask(githubId, projectId, request));
    }

    // Delete a project
    @DeleteMapping("/project/{projectId}")
    public ResponseEntity<String> deleteProject(
            @AuthenticationPrincipal AppUser appuser,
            @PathVariable Integer projectId) {

        String githubId = appuser.getGithubId();
        service.deleteProject(githubId, projectId);
        return ResponseEntity.ok("Project deleted successfully");
    }
}