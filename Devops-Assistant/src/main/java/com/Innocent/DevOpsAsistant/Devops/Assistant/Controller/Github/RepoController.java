package com.Innocent.DevOpsAsistant.Devops.Assistant.Controller.Github;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.GitRepo;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GithubService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/repos")
@RequiredArgsConstructor
public class RepoController {

    private final GithubService githubService;

    @GetMapping("/imported")
    public  List<GitRepoEntity> getALlImportedRepos( @AuthenticationPrincipal AppUser appuser) {
        String githubId = appuser.getGithubId();
        return githubService.getImportedRepos(githubId);
    }
    

    @PostMapping("/import")
    public ResponseEntity<?> importRepo(
            @AuthenticationPrincipal AppUser appuser,
            @RequestBody GitRepo repo) {

        String githubId = appuser.getGithubId();

         GitRepoEntity saved = githubService.importRepo(githubId, repo);

        return ResponseEntity.ok(saved);
    }
}

