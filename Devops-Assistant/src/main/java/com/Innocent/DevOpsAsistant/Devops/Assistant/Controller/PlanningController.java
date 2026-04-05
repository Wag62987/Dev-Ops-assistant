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

    // ✅ Create Project
    @PostMapping("/project")
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        Project savedProject = service.createProject(project);
        return ResponseEntity.ok(savedProject);
    }

    // ✅ Get All Projects
    @GetMapping("/projects")
    public ResponseEntity<?> getProjects() {
        try {
            return ResponseEntity.ok(service.getProjects());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // ✅ Add Member (FIXED: projectId added)
    @PostMapping("/project/{projectId}/member")
    public ResponseEntity<String> addMember(@PathVariable Integer projectId,
                                            @RequestBody Member member) {
        service.addMember(projectId, member);
        return ResponseEntity.ok("Member added successfully");
    }

    // ✅ Add Task (FIXED: projectId added)
    @PostMapping("/project/{projectId}/task")
    public ResponseEntity<String> addTask(@PathVariable Integer projectId,
                                          @RequestBody TaskItem task) {
        service.addTask(projectId, task);
        return ResponseEntity.ok("Task added successfully");
    }

    // ✅ Delete Project
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