package com.Innocent.DevOpsAsistant.Devops.Assistant.Controller.Github;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Repository.GitRepoRepository;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GitHubActionsStatusService;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GithubService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ci-status")
@RequiredArgsConstructor
public class CIStatusController {

    private final GithubService githubService;
    private final GitHubActionsStatusService statusService;


    @GetMapping("/{repoId}")
    public ResponseEntity<?> getStatus(
            @PathVariable Long repoId,
            @AuthenticationPrincipal AppUser appuser) {
        String githubId = appuser.getGithubId();


        GitRepoEntity repo = githubService.getRepoById(repoId);

        String status = statusService.fetchLatestCIStatus(
                githubId,
                repo
        );

        return ResponseEntity.ok(
                Map.of("status", status)
        );
    }
}
