package com.Innocent.DevOpsAsistant.Devops.Assistant.Controller.Github;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.CICDconfigDTO;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.CICDConfigEntity;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Repository.CICDConfigRepository;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GithubCommitService;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GithubService;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GithubWorkflowService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/deploy")
public class DeployController {

    private final CICDConfigRepository configRepo;
    private final GithubWorkflowService workflowService;
    private final GithubCommitService commitService;
    private final OAuth2AuthorizedClientService clientService;
    private final GithubService githubService;


    @PostMapping("/{repoId}")
    public ResponseEntity<?> deployRepo(
            @PathVariable Long repoId,
            @Valid @RequestBody CICDconfigDTO configDTO,
            @AuthenticationPrincipal AppUser appuser
    ) {
        CICDConfigEntity config = new CICDConfigEntity();
        config.setProjectType(configDTO.getProjectType());
        config.setBuildTool(configDTO.getBuildTool());
        config.setRuntimeVersion(configDTO.getRuntimeVersion());
        config.setBranchName(configDTO.getBranchName());
        config.setDockerEnabled(configDTO.isDockerEnabled());
        config.setRepo(githubService.getRepoById(repoId));


    String accessToken =
            appuser.getGithub_token();
            System.out.println("Access Token: " + accessToken);


        String workflow =
                workflowService.generateWorkflow(config);
        System.out.println("Workflow:\n"+workflow);
        commitService.commitWorkflow(
                appuser.getGithubId(),
                config.getRepo(), 
                workflow
        );

        return ResponseEntity.ok("CI/CD pipeline triggered");
    }
}

