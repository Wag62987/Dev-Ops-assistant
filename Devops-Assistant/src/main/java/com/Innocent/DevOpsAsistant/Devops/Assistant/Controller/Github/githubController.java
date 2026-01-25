package com.Innocent.DevOpsAsistant.Devops.Assistant.Controller.Github;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.GitRepo;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Exception.UserNotFound;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GithubService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/github")
@RequiredArgsConstructor
public class githubController {
    private final GithubService githubService;

    @GetMapping("/userRepos")
    public ResponseEntity<List<GitRepo>> getUserRepos(
        @AuthenticationPrincipal OAuth2User oauthUser) {

    if (oauthUser == null) {
        log.error("OAuth2User is null");
        return ResponseEntity.status(401).build();
    }

    Object githubIdObj = oauthUser.getAttribute("id");

    if (githubIdObj == null) {
        log.error("GitHub ID attribute not found in OAuth user attributes: {}", 
                  oauthUser.getAttributes());
        return ResponseEntity.badRequest().build();
    }

    String githubId = githubIdObj.toString();

        try{

            List<GitRepo> repos=githubService.getUserRepos(githubId);
            
            log.info("Fetched {} repositories for user {}", repos.size(), githubId);
            repos.forEach(repo -> 
                System.out.println("Repo Name: " + repo.getName()));
            return ResponseEntity.ok(repos);
        }
        catch(UserNotFound e){
            log.error("User not found: {}", githubId);
        return ResponseEntity.badRequest().body(null);
    }
}

}
