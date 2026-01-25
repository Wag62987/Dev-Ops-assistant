package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;

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
         Optional<AppUser> existingUser = appUserService.FindById(githubId);
        if (existingUser.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        AppUser user = existingUser.get();
        String accessToken =user.getGithub_token();
        String path = ".github/workflows/ci.yml";
        String encodedContent =
                Base64.getEncoder()
                      .encodeToString(workflowContent.getBytes());

        Map<String, Object> body = Map.of(
            "message", "Add CI pipeline",
            "content", encodedContent
        );

        webClient.put()
            .uri("/repos/{owner}/{repo}/contents/{path}",
                    extractOwner(repo.getRepoUrl()),
                    repo.getRepoName(),
                    path)
            .header("Authorization", "Bearer " + accessToken)
            .bodyValue(body)
            .retrieve()
            .toBodilessEntity()
            .block();
    }

    private String extractOwner(String repoUrl) {
        return repoUrl.split("/")[3];
    }
}

