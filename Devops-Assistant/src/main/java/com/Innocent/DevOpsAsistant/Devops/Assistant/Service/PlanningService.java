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

    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    @Transactional
    public List<Project> getProjects() {
        List<Project> projects = projectRepository.findAll();

        projects.forEach(p -> {
            p.getMembers().size();
            p.getTasks().size();
        });

        return projects;
    }

    public Member addMember(Integer projectId, Member member) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        member.setProject(project);
        Member saved = memberRepository.save(member);

        project.getMembers().add(saved); 
        projectRepository.save(project);
        return saved;
    }

    public TaskItem addTask(Integer projectId, TaskItem task) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        task.setProject(project);
        TaskItem saved = taskRepository.save(task);

        project.getTasks().add(saved);
        projectRepository.save(project);
        return saved;
    }

    public void deleteProject(Integer id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        projectRepository.delete(project);
    }
}