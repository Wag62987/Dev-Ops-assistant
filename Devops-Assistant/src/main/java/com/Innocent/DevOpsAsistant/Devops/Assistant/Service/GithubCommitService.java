package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

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

        // 🔥 IMPORTANT: Always use SAME branch everywhere
        String branch = "main"; // or dynamically fetch default branch

        String path = ".github/workflows/ci.yml";

        System.out.println("OWNER = " + owner);
        System.out.println("REPO = " + repoName);
        System.out.println("BRANCH = " + branch);
        System.out.println("FULL URL = " + repo.getRepoUrl());

        String encodedContent = Base64.getEncoder()
                .encodeToString(workflowContent.getBytes(StandardCharsets.UTF_8));

        System.out.println("STEP 2: Fetching SHA");

        String sha = null;

        try {
            Map<?, ?> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/repos/{owner}/{repo}/contents/{path}")
                            .queryParam("ref", branch) // ✅ FIX 1
                            .build(owner, repoName, path))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .header("User-Agent", "DevOps-Assistant")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.get("sha") != null) {
                sha = response.get("sha").toString();
            }

        } catch (WebClientResponseException.NotFound e) {
            System.out.println("File not found -> will create new file");
        } catch (Exception e) {
            System.out.println("ERROR fetching SHA: " + e.getMessage());
        }

        System.out.println("STEP 3: SHA = " + sha);

        Map<String, Object> body = new HashMap<>();
        body.put("message", sha == null ? "Add CI pipeline" : "Update CI pipeline");
        body.put("content", encodedContent);
        body.put("branch", branch); // ✅ FIX 2

        if (sha != null) {
            body.put("sha", sha);
        }

        System.out.println("STEP 4: Sending PUT request");

        try {
            webClient.put()
                    .uri("/repos/{owner}/{repo}/contents/{path}", owner, repoName, path)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .header("User-Agent", "DevOps-Assistant")
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(status -> status.isError(), response ->
                            response.headers().asHttpHeaders().forEach((k, v) ->
                                    System.out.println("HEADER: " + k + " -> " + v)
                            )
                            // 🔥 VERY IMPORTANT DEBUG
                            // This shows required permissions
                    )
                    .bodyToMono(String.class)
                    .doOnNext(res -> System.out.println("GitHub Response: " + res))
                    .onErrorResume(err -> {
                        System.out.println("GITHUB ERROR: " + err.getMessage());
                        return Mono.empty();
                    })
                    .block();

            System.out.println("STEP 5: Commit successful");

        } catch (Exception e) {
            System.out.println("FINAL ERROR: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String extractOwner(String repoUrl) {
        try {
            String[] parts = repoUrl.replace(".git", "").split("/");
            return parts[parts.length - 2];
        } catch (Exception e) {
            throw new RuntimeException("Invalid repo URL: " + repoUrl);
        }
    }
}