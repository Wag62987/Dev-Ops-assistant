package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.Member;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.Project;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.TaskItem;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Repository.MemberRepository;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Repository.ProjectRepository;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Repository.TaskRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanningService {

    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final TaskRepository taskRepository;

    // ✅ Create Project
    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    // ✅ Get All Projects (with Members + Tasks)
    public List<Project> getProjects() {
        return projectRepository.findAll();
    }

    // ✅ Add Member
    public Member addMember(Member member) {
        return memberRepository.save(member);
    }

    // ✅ Add Task
    public TaskItem addTask(TaskItem task) {
        return taskRepository.save(task);
    }

    // ✅ Delete Project (Cascade handles Members + Tasks)
    public void deleteProject(Integer id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        projectRepository.delete(project);
    }
}