package com.Innocent.DevOpsAsistant.Devops.Assistant.Controller.Github;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
public ResponseEntity<?> deployRepo(
        @PathVariable String repoId,
        @RequestBody CICDconfigDTO configDTO,
        @AuthenticationPrincipal AppUser appuser
) {
    try {
        System.out.println("DEPLOY HIT");
        System.out.println("repoId = " + repoId);
        System.out.println("appuser = " + appuser);

        if (appuser == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        GitRepoEntity repo = githubService.getRepoById(repoId);
        System.out.println("repo = " + repo.getRepoName());

        CICDConfigEntity config = new CICDConfigEntity();
        config.setProjectType(configDTO.getProjectType());
        config.setBuildTool(configDTO.getBuildTool());
        config.setRuntimeVersion(configDTO.getRuntimeVersion());
        config.setBranchName(configDTO.getBranchName());
        config.setDockerEnabled(configDTO.getDockerEnabled());
        config.setCdEnabled(configDTO.getCdEnabled());
        config.setDeployHookUrl(configDTO.getDeployHookUrl());

        String workflowContent = workflowService.generateWorkflow(config);
        System.out.println("workflow generated");

        commitService.commitWorkflow(appuser.getGithubId(), repo, workflowContent);
        System.out.println("workflow committed");

        CIStatusResponse ciStatus =
                statusService.fetchLatestCIStatus(appuser.getGithubId(), repo);

        return ResponseEntity.ok(ciStatus);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
}

