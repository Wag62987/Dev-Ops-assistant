package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.Member;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.Project;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.TaskItem;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Repository.MemberRepository;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Repository.ProjectRepository;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Repository.TaskRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // ✅ Get All Projects (FIXED: Lazy loading + fetch collections)
    @Transactional
    public List<Project> getProjects() {
        List<Project> projects = projectRepository.findAll();

        projects.forEach(p -> {
            p.getMembers().size(); // force load members
            p.getTasks().size();   // force load tasks
        });

        return projects;
    }

    // ✅ Add Member (FIXED: link with project)
    public Member addMember(Integer projectId, Member member) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        member.setProject(project); // 🔥 important
        return memberRepository.save(member);
    }

    // ✅ Add Task (FIXED: link with project)
    public TaskItem addTask(Integer projectId, TaskItem task) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        task.setProject(project); // 🔥 important
        return taskRepository.save(task);
    }

    // ✅ Delete Project (Cascade handles Members + Tasks)
    public void deleteProject(Integer id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        projectRepository.delete(project);
    }
}