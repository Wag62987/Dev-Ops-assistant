package com.Innocent.DevOpsAsistant.Devops.Assistant.Controller.Github;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.CICDConfigEntity;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Repository.CICDConfigRepository;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GithubCommitService;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GithubWorkflowService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/deploy")
public class DeployController {

    private final CICDConfigRepository configRepo;
    private final GithubWorkflowService workflowService;
    private final GithubCommitService commitService;
    private final OAuth2AuthorizedClientService clientService;


    @PostMapping("/{repoId}")
    public ResponseEntity<?> deployRepo(
            @PathVariable Long repoId,
            @AuthenticationPrincipal AppUser appuser
    ) {

        CICDConfigEntity config =
                configRepo.findByRepoId(repoId)
                .orElseThrow(() ->
                    new RuntimeException("CI config not found"));

         OAuth2AuthorizedClient client =
            clientService.loadAuthorizedClient(
                    "github",
                    appuser.getName()
            );
             if (client == null) {
        throw new RuntimeException("GitHub OAuth client not found");
    }

    String accessToken =
            client.getAccessToken().getTokenValue();


        String workflow =
                workflowService.generateWorkflow(config);

        commitService.commitWorkflow(
                accessToken,
                config.getRepo(),
                workflow
        );

        return ResponseEntity.ok("CI/CD pipeline triggered");
    }
}

