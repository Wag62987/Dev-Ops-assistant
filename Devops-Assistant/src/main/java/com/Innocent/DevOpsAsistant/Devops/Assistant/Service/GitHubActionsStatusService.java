package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;
import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.CIStatusResponse;
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
    public CIStatusResponse fetchLatestCIStatus(
            String githubId,
            GitRepoEntity repo
    ) {
        AppUser user = appUserService.FindById(githubId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map response = githubClient.get()
            .uri("/repos/{owner}/{repo}/actions/runs?per_page=1",
                    extractOwner(repo.getRepoUrl()),
                    repo.getRepoName())
            .header("Authorization", "Bearer " + user.getGithub_token())
            .retrieve()
            .bodyToMono(Map.class)
            .block();

        var runs = (List<Map>) response.get("workflow_runs");
        if (runs.isEmpty()) {
            return new CIStatusResponse(
                    "NO_RUNS",
                    null,
                    "No CI workflow has run yet",
                    null
            );
        }

        Map run = runs.get(0);
        String conclusion = (String) run.get("conclusion");
        String logsUrl = (String) run.get("logs_url");

        if ("success".equals(conclusion)) {
            return new CIStatusResponse(
                    "SUCCESS",
                    null,
                    null,
                    logsUrl
            );
        }

        // If FAILED → fetch job + step info
        return fetchFailureDetails(
                repo,
                user.getGithub_token(),
                run,
                logsUrl
        );
    }

    private CIStatusResponse fetchFailureDetails(
            GitRepoEntity repo,
            String token,
            Map run,
            String logsUrl
    ) {
        Map jobsResponse = githubClient.get()
            .uri("/repos/{owner}/{repo}/actions/runs/{runId}/jobs",
                    extractOwner(repo.getRepoUrl()),
                    repo.getRepoName(),
                    run.get("id"))
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .bodyToMono(Map.class)
            .block();

        var jobs = (List<Map>) jobsResponse.get("jobs");

        for (Map job : jobs) {
            var steps = (List<Map>) job.get("steps");

            for (Map step : steps) {
                if ("failure".equals(step.get("conclusion"))) {
                    return new CIStatusResponse(
                            "FAILED",
                            (String) step.get("name"),
                            "Step failed during execution",
                            logsUrl
                    );
                }
            }
        }

        return new CIStatusResponse(
                "FAILED",
                "Unknown",
                "Pipeline failed, check logs",
                logsUrl
        );
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

