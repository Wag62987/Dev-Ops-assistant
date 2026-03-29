package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.CIStatusResponse;
import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.PipelineCount;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GitHubActionsStatusService Tests")
class GithubActionStatusServiceTest {

    @Mock private AppUserService appUserService;
    @Mock private WebClient githubClient;

    @Mock private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;
    @Mock private WebClient.RequestHeadersSpec<?> requestHeadersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private GitHubActionsStatusService service;

    private AppUser mockUser;
    private GitRepoEntity mockRepo;

    @BeforeEach
    void setUp() {
        mockUser = new AppUser();
        mockUser.setGithub_token("ghp_test_token");
        mockUser.setRepos(new ArrayList<>());

        mockRepo = new GitRepoEntity();
        mockRepo.setRepoName("test-repo");
        mockRepo.setRepoUrl("https://github.com/john/test-repo");
    }

    // ─────────────────────────────────────────────────────────
    // fetchLatestCIStatus - SUCCESS
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("fetchLatestCIStatus: should return SUCCESS")
    void fetchLatestCIStatus_shouldReturnSuccess() {

        when(appUserService.FindById("gh_123"))
                .thenReturn(Optional.of(mockUser));

        Map<String, Object> run = new HashMap<>();
        run.put("conclusion", "success");
        run.put("status", "completed");
        run.put("logs_url", "logs-url");

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("workflow_runs", List.of(run));

        mockWebClient(responseMap);

        CIStatusResponse result =
                service.fetchLatestCIStatus("gh_123", mockRepo);

        assertThat(result.getStatus()).isEqualTo("SUCCESS");
    }

    // ─────────────────────────────────────────────────────────
    // fetchLatestCIStatus - IN PROGRESS
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("fetchLatestCIStatus: should return IN_PROGRESS")
    void fetchLatestCIStatus_shouldReturnInProgress() {

        when(appUserService.FindById("gh_123"))
                .thenReturn(Optional.of(mockUser));

        Map<String, Object> run = new HashMap<>();
        run.put("conclusion", null);
        run.put("status", "in_progress");
        run.put("logs_url", "logs-url");

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("workflow_runs", List.of(run));

        mockWebClient(responseMap);

        CIStatusResponse result =
                service.fetchLatestCIStatus("gh_123", mockRepo);

        assertThat(result.getStatus()).isEqualTo("IN_PROGRESS");
    }

    // ─────────────────────────────────────────────────────────
    // fetchLatestCIStatus - NO RUNS
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("fetchLatestCIStatus: should return NO_RUNS")
    void fetchLatestCIStatus_shouldReturnNoRuns() {

        when(appUserService.FindById("gh_123"))
                .thenReturn(Optional.of(mockUser));

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("workflow_runs", new ArrayList<>());

        mockWebClient(responseMap);

        CIStatusResponse result =
                service.fetchLatestCIStatus("gh_123", mockRepo);

        assertThat(result.getStatus()).isEqualTo("NO_RUNS");
    }

    // ─────────────────────────────────────────────────────────
    // getRunningPipelineStats
    // ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getRunningPipelineStats: should count running pipelines")
    void getRunningPipelineStats_shouldReturnCount() {

        mockUser.setRepos(List.of(mockRepo));

        when(appUserService.FindById("gh_123"))
                .thenReturn(Optional.of(mockUser));

        Map<String, Object> run1 = new HashMap<>();
        run1.put("status", "in_progress");

        Map<String, Object> run2 = new HashMap<>();
        run2.put("status", "completed");

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("workflow_runs", List.of(run1, run2));

        mockWebClient(responseMap);

        PipelineCount result =
                service.getRunningPipelineStats("gh_123");
    assertThat(result.getRunningPipelines()).isEqualTo(1);
    }

    // ─────────────────────────────────────────────────────────
    // Helper: Mock WebClient Chain
    // ─────────────────────────────────────────────────────────
@SuppressWarnings({"unchecked", "rawtypes"})
private void mockWebClient(Map<String, Object> responseMap) {

    when(githubClient.get())
            .thenReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);

   
    when(requestHeadersUriSpec.uri(anyString(), any(), any()))
            .thenAnswer(inv -> requestHeadersSpec);

 
    when(requestHeadersSpec.header(anyString(), anyString()))
            .thenAnswer(inv -> requestHeadersSpec);

    when(requestHeadersSpec.retrieve())
            .thenReturn(responseSpec);

    when(responseSpec.bodyToMono(Map.class))
            .thenReturn(Mono.just(responseMap));
}
}