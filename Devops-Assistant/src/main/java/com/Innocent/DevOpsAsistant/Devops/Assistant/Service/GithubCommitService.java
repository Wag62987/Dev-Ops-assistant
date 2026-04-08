package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GithubCommitService {

    private final AppUserService appUserService;
    private final WebClient webClient;

    public void commitWorkflow(
            String githubId,
            GitRepoEntity repo,
            String workflowContent
    ) {
        System.out.println("STEP 1: Starting commit");

        AppUser user = appUserService.FindById(githubId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = user.getGithub_token();
        String owner = extractOwner(repo.getRepoUrl());
        String repoName = repo.getRepoName();
        String path = ".github/workflows/ci.yml";

        String encodedContent = Base64.getEncoder()
                .encodeToString(workflowContent.getBytes(StandardCharsets.UTF_8));

        System.out.println("STEP 2: Fetching SHA");

        String sha = null;
        try {
            Map response = webClient.get()
                    .uri("/repos/{owner}/{repo}/contents/{path}", owner, repoName, path)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null) {
                sha = (String) response.get("sha");
            }
        } catch (Exception e) {
            System.out.println("File not found, will create new file");
        }

        System.out.println("STEP 3: SHA = " + sha);

        Map<String, Object> body = new HashMap<>();
        body.put("message", sha == null ? "Add CI pipeline" : "Update CI pipeline");
        body.put("content", encodedContent);

        if (sha != null) {
            body.put("sha", sha);
        }

        // ✅ FIX: REQUIRED FOR GITHUB API
        body.put("branch", "main");

        System.out.println("STEP 4: Sending PUT request to GitHub");

        webClient.put()
                .uri("/repos/{owner}/{repo}/contents/{path}", owner, repoName, path)
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/vnd.github.v3+json")
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();

        System.out.println("STEP 5: Commit successful");
    }

    private String extractOwner(String repoUrl) {
        return repoUrl.split("/")[3];
    }
}