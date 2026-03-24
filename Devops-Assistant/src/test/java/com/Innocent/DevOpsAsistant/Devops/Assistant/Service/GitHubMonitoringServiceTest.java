package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GitHubMonitoringService Tests")
class GitHubMonitoringServiceTest {

    // RestTemplate is created internally via `new RestTemplate()`,
    // so we use a subclass spy via reflection to inject a mock.
    // Alternatively we test with a real HTTP mock (MockRestServiceServer).
    // Here we use MockRestServiceServer for clean integration of RestTemplate.

    private GitHubMonitoringService monitoringService;

    private GitRepoEntity mockRepo;

    @BeforeEach
    void setUp() {
        monitoringService = new GitHubMonitoringService();

        mockRepo = new GitRepoEntity();
        mockRepo.setRepoName("my-repo");
        mockRepo.setRepoUrl("https://github.com/john_doe/my-repo");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Builds a fake GitHub workflow run map.
     */
    private Map<String, Object> buildRun(
            String status, String conclusion,
            String headSha, String headBranch, String startedAt) {
        Map<String, Object> run = new HashMap<>();
        run.put("status", status);
        run.put("conclusion", conclusion);
        run.put("head_sha", headSha);
        run.put("head_branch", headBranch);
        run.put("run_started_at", startedAt);
        return run;
    }

    /**
     * Creates a RestTemplate that returns the supplied body when exchanged.
     */
    private RestTemplate mockRestTemplate(Map<String, Object> body) {
        RestTemplate rt = mock(RestTemplate.class);
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(body, HttpStatus.OK);
        when(rt.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);
        return rt;
    }

    // ─── Status mapping ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getWorkflowRuns: completed+success run should map to SUCCESS")
    void getWorkflowRuns_successRun_shouldMapToSuccess() throws Exception {
        Map<String, Object> run = buildRun(
                "completed", "success",
                "abc123", "main", "2024-01-01T10:00:00Z");

        Map<String, Object> apiBody = Map.of("workflow_runs", List.of(run));

        // Inject the mock RestTemplate via reflection (field is private final)
        RestTemplate mockRT = mockRestTemplate(apiBody);
        injectRestTemplate(monitoringService, mockRT);

        List<Map<String, Object>> result =
                monitoringService.getWorkflowRuns("john_doe", mockRepo, "token");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("status")).isEqualTo("SUCCESS");
        assertThat(result.get(0).get("branch")).isEqualTo("main");
        assertThat(result.get(0).get("commit")).isEqualTo("abc123");
    }

    @Test
    @DisplayName("getWorkflowRuns: completed+failure run should map to FAILED")
    void getWorkflowRuns_failedRun_shouldMapToFailed() throws Exception {
        Map<String, Object> run = buildRun(
                "completed", "failure",
                "def456", "develop", "2024-01-02T11:00:00Z");

        Map<String, Object> apiBody = Map.of("workflow_runs", List.of(run));
        RestTemplate mockRT = mockRestTemplate(apiBody);
        injectRestTemplate(monitoringService, mockRT);

        List<Map<String, Object>> result =
                monitoringService.getWorkflowRuns("john_doe", mockRepo, "token");

        assertThat(result.get(0).get("status")).isEqualTo("FAILED");
    }

    @Test
    @DisplayName("getWorkflowRuns: in_progress run should map to RUNNING")
    void getWorkflowRuns_inProgressRun_shouldMapToRunning() throws Exception {
        Map<String, Object> run = buildRun(
                "in_progress", null,
                "ghi789", "feature/x", "2024-01-03T12:00:00Z");

        Map<String, Object> apiBody = Map.of("workflow_runs", List.of(run));
        RestTemplate mockRT = mockRestTemplate(apiBody);
        injectRestTemplate(monitoringService, mockRT);

        List<Map<String, Object>> result =
                monitoringService.getWorkflowRuns("john_doe", mockRepo, "token");

        assertThat(result.get(0).get("status")).isEqualTo("RUNNING");
    }

    // ─── Empty runs ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getWorkflowRuns: should return empty list when workflow_runs is null")
    void getWorkflowRuns_nullRuns_shouldReturnEmptyList() throws Exception {
        Map<String, Object> apiBody = new HashMap<>();
        apiBody.put("workflow_runs", null);

        RestTemplate mockRT = mockRestTemplate(apiBody);
        injectRestTemplate(monitoringService, mockRT);

        List<Map<String, Object>> result =
                monitoringService.getWorkflowRuns("john_doe", mockRepo, "token");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getWorkflowRuns: should return empty list when workflow_runs is empty")
    void getWorkflowRuns_emptyRuns_shouldReturnEmptyList() throws Exception {
        Map<String, Object> apiBody = Map.of("workflow_runs", List.of());

        RestTemplate mockRT = mockRestTemplate(apiBody);
        injectRestTemplate(monitoringService, mockRT);

        List<Map<String, Object>> result =
                monitoringService.getWorkflowRuns("john_doe", mockRepo, "token");

        assertThat(result).isEmpty();
    }

    // ─── Multiple runs ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getWorkflowRuns: should process multiple runs correctly")
    void getWorkflowRuns_multipleRuns_shouldReturnAll() throws Exception {
        List<Map<String, Object>> runs = List.of(
                buildRun("completed", "success", "sha1", "main", "2024-01-01T00:00:00Z"),
                buildRun("completed", "failure", "sha2", "feature/x", "2024-01-02T00:00:00Z"),
                buildRun("in_progress", null, "sha3", "hotfix/y", "2024-01-03T00:00:00Z")
        );

        Map<String, Object> apiBody = Map.of("workflow_runs", runs);
        RestTemplate mockRT = mockRestTemplate(apiBody);
        injectRestTemplate(monitoringService, mockRT);

        List<Map<String, Object>> result =
                monitoringService.getWorkflowRuns("john_doe", mockRepo, "token");

        assertThat(result).hasSize(3);
        assertThat(result.get(0).get("status")).isEqualTo("SUCCESS");
        assertThat(result.get(1).get("status")).isEqualTo("FAILED");
        assertThat(result.get(2).get("status")).isEqualTo("RUNNING");
    }

    // ─── Reflection helper ────────────────────────────────────────────────────

    private void injectRestTemplate(GitHubMonitoringService service, RestTemplate rt)
            throws Exception {
        var field = GitHubMonitoringService.class.getDeclaredField("restTemplate");
        field.setAccessible(true);
        field.set(service, rt);
    }
}