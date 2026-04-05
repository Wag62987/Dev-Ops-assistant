package com.Innocent.DevOpsAsistant.Devops.Assistant.Controller;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.Member;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.Project;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.TaskItem;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.PlanningService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planning")
@RequiredArgsConstructor
public class PlanningController {

    private final PlanningService service;

    @PostMapping("/project")
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        Project savedProject = service.createProject(project);
        return ResponseEntity.ok(savedProject);
    }

   @GetMapping("/projects")
public ResponseEntity<?> getProjects() {
    try {
        return ResponseEntity.ok(service.getProjects());
    } catch (Exception e) {
        e.printStackTrace(); // 🔥 IMPORTANT
        return ResponseEntity.status(500).body(e.getMessage());
    }
}

    @PostMapping("/member")
    public ResponseEntity<String> addMember(@RequestBody Member member) {
        System.out.println("REQUEST CHECK!!");
        service.addMember(member);
        return ResponseEntity.ok("Member added successfully");
    }

    @PostMapping("/task")
    public ResponseEntity<String> addTask(@RequestBody TaskItem task) {
        service.addTask(task);
        return ResponseEntity.ok("Task added successfully");
    }

    @DeleteMapping("/project/{id}")
    public ResponseEntity<String> deleteProject(@PathVariable Integer id) {
        try {
            service.deleteProject(id);
            return ResponseEntity.ok("Project deleted successfully");
        } catch (RuntimeException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        }
    }
}