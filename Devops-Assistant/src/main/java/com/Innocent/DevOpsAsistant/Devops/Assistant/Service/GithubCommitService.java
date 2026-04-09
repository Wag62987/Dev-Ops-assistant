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
            String workflowContent,
            String branch
    ) {

        AppUser user = appUserService.FindById(githubId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = user.getGithub_token();

        String owner = extractOwner(repo.getRepoUrl());
        String repoName = repo.getRepoName();

        final String finalBranch = (branch == null || branch.isEmpty()) ? "main" : branch;

        String path = ".github/workflows/ci.yml";

        String encodedContent = Base64.getEncoder()
                .encodeToString(workflowContent.getBytes(StandardCharsets.UTF_8));

        String sha = null;

        try {
            Map<?, ?> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/repos/{owner}/{repo}/contents/{path}")
                            .queryParam("ref", finalBranch)   // ✅ FIXED
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
            System.out.println("File not found → creating new file");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("message", sha == null ? "Add CI pipeline" : "Update CI pipeline");
        body.put("content", encodedContent);
        body.put("branch", finalBranch);   // ✅ FIXED

        if (sha != null) {
            body.put("sha", sha);
        }

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
                        response.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new RuntimeException(error)))
                )
                .toBodilessEntity()
                .block();
    }

    private String extractOwner(String repoUrl) {
        String[] parts = repoUrl.replace(".git", "").split("/");
        return parts[parts.length - 2];
    }
}
