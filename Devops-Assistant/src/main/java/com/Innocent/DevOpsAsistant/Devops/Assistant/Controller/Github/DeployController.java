package com.Innocent.DevOpsAsistant.Devops.Assistant.Controller.Github;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.CICDconfigDTO;
import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.CIStatusResponse;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.CICDConfigEntity;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GitHubActionsStatusService;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GithubCommitService;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GithubService;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GithubWorkflowService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/deploy")
public class DeployController {

    private final GithubWorkflowService workflowService;
    private final GithubCommitService commitService;
    private final GitHubActionsStatusService statusService;
    private final GithubService githubService;

    @PostMapping("/{repoId}")
    public ResponseEntity<Map<String, Object>> deployRepo(
            @PathVariable String repoId,
            @Valid @RequestBody CICDconfigDTO configDTO,
            @AuthenticationPrincipal AppUser appUser
    ) {

        System.out.println("==== DEPLOY REQUEST STARTED ====");

        CICDConfigEntity config = new CICDConfigEntity();
        config.setProjectType(configDTO.getProjectType());
        config.setBuildTool(configDTO.getBuildTool());
        config.setRuntimeVersion(configDTO.getRuntimeVersion());
        config.setBranchName(configDTO.getBranchName());
        config.setDockerEnabled(configDTO.getDockerEnabled());
        config.setCdEnabled(configDTO.getCdEnabled());
        config.setDeployHookUrl(configDTO.getDeployHookUrl());

        System.out.println("Config received: " + configDTO);

        GitRepoEntity repo = githubService.getRepoById(repoId);
        System.out.println("Repository: " + repo.getRepoName());

        String branch = config.getBranchName();
        if (branch == null || branch.trim().isEmpty()) {
            branch = "main";
        }

        System.out.println("Using Branch: " + branch);

        String workflowContent = workflowService.generateWorkflow(config);
        System.out.println("Workflow generated successfully");

        commitService.commitWorkflow(
                appUser.getGithubId(),
                repo,
                workflowContent,
                branch
        );

        System.out.println("Workflow committed to GitHub");

        CIStatusResponse ciStatus = statusService.fetchLatestCIStatus(
                appUser.getGithubId(),
                repo
        );

        System.out.println("CI Status fetched: " + ciStatus.getStatus());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "CI/CD pipeline triggered successfully");
        response.put("repository", repo.getRepoName());
        response.put("branch", branch);
        response.put("ciStatus", ciStatus.getStatus());
        response.put("failedStep", ciStatus.getFailedStep());
        response.put("reason", ciStatus.getReason());
        response.put("logsUrl", ciStatus.getLogsUrl());
        response.put("monitoring", "/ci-status/" + repoId);

        System.out.println("==== DEPLOY REQUEST COMPLETED ====");

        return ResponseEntity.ok(response);
    }
}