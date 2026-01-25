package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class GitHubActionsStatusService {
    private final AppUserService appUserService;

    private final WebClient githubClient;

   

    /**
     * Fetch latest CI status of a repository
     * @return success | failure | in_progress | cancelled | queued
     */
    public String fetchLatestCIStatus(
            String githubId,
            GitRepoEntity repo
    ) {
        Optional<AppUser> existingUser = appUserService.FindById(githubId);

        if (existingUser.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        AppUser user = existingUser.get();
        String accessToken =user.getGithub_token();

        return githubClient.get()
            .uri("/repos/{owner}/{repo}/actions/runs?per_page=1",
                    extractOwner(repo.getRepoUrl()),
                    repo.getRepoName())
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .map(this::extractConclusion)
            .block();
    }

    private String extractConclusion(JsonNode json) {

        JsonNode runs = json.get("workflow_runs");

        if (runs == null || runs.isEmpty()) {
            return "no_runs";
        }

        JsonNode run = runs.get(0);

        // If still running, conclusion is null
        if (run.get("conclusion").isNull()) {
            return run.get("status").asText(); // queued / in_progress
        }

        return run.get("conclusion").asText(); // success / failure / cancelled
    }

    private String extractOwner(String repoUrl) {
        return repoUrl.split("/")[3];
    }
}

