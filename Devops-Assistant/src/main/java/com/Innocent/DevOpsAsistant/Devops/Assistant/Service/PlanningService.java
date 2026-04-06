package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.*;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.*;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Repository.*;

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

    private AppUser getUserByGithubId(String githubId) {
        return appUserRepository.findByGithubId(githubId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // =============================
    // CREATE PROJECT
    // =============================
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

    // =============================
    // GET PROJECTS
    // =============================
    @Transactional(readOnly = true)
    public List<Project> getProjects(String githubId) {

        List<Project> projects =
                projectRepository.findByUser_GithubIdOrderByIdDesc(githubId);

        // initialize lazy collections
        projects.forEach(p -> {
            p.getMembers().size();
            p.getTasks().size();
        });

        return projects;
    }

    // =============================
    // ADD MEMBER (FIXED)
    // =============================
    @Transactional
    public Member addMember(String githubId, Integer projectId, AddMemberRequest request) {

        Project project = projectRepository
                .findByIdAndUser_GithubId(projectId, githubId)
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

        // ✅ CRITICAL: ADD TO LIST FIRST
        project.getMembers().add(member);

        // ✅ SAVE PROJECT (Hibernate sets member_order)
        projectRepository.save(project);

        return member;
    }

    // =============================
    // ADD TASK (FIXED)
    // =============================
    @Transactional
    public TaskItem addTask(String githubId, Integer projectId, AddTaskRequest request) {

        Project project = projectRepository
                .findByIdAndUser_GithubId(projectId, githubId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        String title = request.getTitle() == null ? "" : request.getTitle().trim();
        if (title.isEmpty()) {
            throw new IllegalArgumentException("Task title is required");
        }

        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Start & End date required");
        }

        Member member = null;
        if (request.getMemberId() != null) {
            member = memberRepository.findById(request.getMemberId())
                    .orElseThrow(() -> new RuntimeException("Member not found"));

            if (!member.getProject().getId().equals(projectId)) {
                throw new IllegalArgumentException("Member not in project");
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

        // ✅ CRITICAL: ADD TO LIST
        project.getTasks().add(task);

        // ✅ SAVE PROJECT (Hibernate sets task_order)
        projectRepository.save(project);

        return task;
    }

    // =============================
    // DELETE PROJECT
    // =============================
    @Transactional
    public void deleteProject(String githubId, Integer projectId) {

        Project project = projectRepository
                .findByIdAndUser_GithubId(projectId, githubId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        projectRepository.delete(project);
    }
}