package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.AddMemberRequest;
import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.AddTaskRequest;
import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.CreateProjectRequest;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.Member;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.Project;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.TaskItem;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Repository.AppUserRepository;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Repository.MemberRepository;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Repository.ProjectRepository;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Repository.TaskRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanningService {

    private final AppUserRepository appUserRepository;
    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final TaskRepository taskRepository;

    private AppUser getUserByGithubId(String githubId) {
        return appUserRepository.findByGithubId(githubId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public Project createProject(String githubId, CreateProjectRequest request) {
        AppUser user = getUserByGithubId(githubId);

        String name = request.getName() == null ? "" : request.getName().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Project name is required");
        }

        Project project = Project.builder()
                .name(name)
                .user(user)
                .build();

        return projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public List<Project> getProjects(String githubId) {
        // makes sure only this user's projects are returned
        List<Project> projects = projectRepository.findByUser_GithubIdOrderByIdDesc(githubId);

        // initialize lazy collections inside transaction
        projects.forEach(p -> {
            p.getMembers().size();
            p.getTasks().size();
        });

        log.info("Fetched {} projects for user {}", projects.size(), githubId);
        return projects;
    }

    @Transactional
    public Member addMember(String githubId, Integer projectId, AddMemberRequest request) {
        Project project = projectRepository.findByIdAndUser_GithubId(projectId, githubId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        String name = request.getName() == null ? "" : request.getName().trim();
        String role = request.getRole() == null ? "" : request.getRole().trim();

        if (name.isEmpty() || role.isEmpty()) {
            throw new IllegalArgumentException("Member name and role are required");
        }

        Member member = Member.builder()
                .name(name)
                .role(role)
                .project(project)
                .build();

        Member saved = memberRepository.save(member);

        project.getMembers().add(saved);
        projectRepository.save(project);

        return saved;
    }

    @Transactional
    public TaskItem addTask(String githubId, Integer projectId, AddTaskRequest request) {
        Project project = projectRepository.findByIdAndUser_GithubId(projectId, githubId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        String title = request.getTitle() == null ? "" : request.getTitle().trim();
        if (title.isEmpty()) {
            throw new IllegalArgumentException("Task title is required");
        }

        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        Member member = null;
        if (request.getMemberId() != null) {
            member = memberRepository.findById(request.getMemberId())
                    .orElseThrow(() -> new RuntimeException("Member not found"));

            if (member.getProject() == null || !member.getProject().getId().equals(projectId)) {
                throw new IllegalArgumentException("Selected member does not belong to this project");
            }
        }

        TaskItem task = TaskItem.builder()
                .title(title)
                .status("Pending")
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .project(project)
                .member(member)
                .build();

        TaskItem saved = taskRepository.save(task);

        project.getTasks().add(saved);
        projectRepository.save(project);

        return saved;
    }

    @Transactional
    public void deleteProject(String githubId, Integer projectId) {
        Project project = projectRepository.findByIdAndUser_GithubId(projectId, githubId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        projectRepository.delete(project);
    }
}