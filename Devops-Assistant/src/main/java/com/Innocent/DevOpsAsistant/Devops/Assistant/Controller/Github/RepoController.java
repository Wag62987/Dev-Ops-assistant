package com.Innocent.DevOpsAsistant.Devops.Assistant.Controller.Github;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.GitRepo;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GithubService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/repos")
@RequiredArgsConstructor
public class RepoController {

    private final GithubService githubService;

    @GetMapping("/imported")
    public  List<GitRepoEntity> getALlImportedRepos( @AuthenticationPrincipal OAuth2User oauthUser) {
        String githubId = oauthUser.getAttribute("id").toString();
        return githubService.getImportedRepos(githubId);
    }
    

    @PostMapping("/import")
    public ResponseEntity<?> importRepo(
            @AuthenticationPrincipal OAuth2User oauthUser,
            @RequestBody GitRepo repo) {

        String githubId = oauthUser.getAttribute("id").toString();

         GitRepoEntity saved = githubService.importRepo(githubId, repo);

        return ResponseEntity.ok(saved);
    }
}

