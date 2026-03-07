package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;

@Service
public class GitHubMonitoringService {

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Map<String, Object>> getWorkflowRuns(
            String githubUser,
            GitRepoEntity repo,
            String githubToken) {

        String url = "https://api.github.com/repos/"
                + githubUser + "/" + repo.getRepoName()
                + "/actions/runs";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(githubToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        Map body = response.getBody();

        List<Map> runs = (List<Map>) body.get("workflow_runs");

        List<Map<String, Object>> result = new ArrayList<>();

        if (runs == null) return result;

        for (Map run : runs) {

            String status = (String) run.get("status");
            String conclusion = (String) run.get("conclusion");

            String finalStatus;

            if ("completed".equals(status) && "success".equals(conclusion)) {
                finalStatus = "SUCCESS";
            }
            else if ("completed".equals(status) && "failure".equals(conclusion)) {
                finalStatus = "FAILED";
            }
            else {
                finalStatus = "RUNNING";
            }

            result.add(
                Map.of(
                    "commit", run.get("head_sha"),
                    "branch", run.get("head_branch"),
                    "status", finalStatus
                )
            );
        }

        return result;
    }
}