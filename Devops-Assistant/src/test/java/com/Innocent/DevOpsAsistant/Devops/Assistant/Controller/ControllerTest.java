package com.Innocent.DevOpsAsistant.Devops.Assistant.Controller;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Config.Jwt.JwtUtil;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Controller.AppUser.UserController;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Controller.Github.*;
import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.*;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Exception.UserNotFound;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.CICDConfigEntity;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Controller Tests")
class ControllerTest {

    // ─── Shared fixtures ──────────────────────────────────────────────────────

    private AppUser mockUser;
    private GitRepoEntity mockRepo;

    @BeforeEach
    void setUpFixtures() {
        mockUser = new AppUser();
        mockUser.setUsername("john_doe");
        mockUser.setGithubId("gh_123");
        mockUser.setGithub_token("ghp_test_token");

        mockRepo = new GitRepoEntity();
        mockRepo.setRepoName("my-repo");
        mockRepo.setRepoUrl("https://github.com/john_doe/my-repo");
    }

    // =========================================================================
    // UserController
    // =========================================================================

    @Nested
    @DisplayName("UserController")
    class UserControllerTests {

        @Mock private AppUserService userService;
        @Mock private JwtUtil jwtUtil;
        @InjectMocks private UserController userController;

        @Test
        @DisplayName("getUserInfo: returns 200 with UserDTO when JWT cookie is valid")
        void getUserInfo_validCookie_returns200() {
            when(jwtUtil.getGithubId("valid.jwt.token")).thenReturn("gh_123");
            when(userService.FindById("gh_123")).thenReturn(Optional.of(mockUser));

            UserDTO dto = new UserDTO();
            dto.setUsername("john_doe");
            when(userService.GetUserInfo(any())).thenReturn(dto);

            ResponseEntity<UserDTO> response =
                    userController.getUserInfo("valid.jwt.token", null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getUsername()).isEqualTo("john_doe");
        }

        @Test
        @DisplayName("getUserInfo: falls back to Authorization header when cookie absent")
        void getUserInfo_authHeader_returns200() {
            when(jwtUtil.getGithubId("header.jwt.token")).thenReturn("gh_123");
            when(userService.FindById("gh_123")).thenReturn(Optional.of(mockUser));

            UserDTO dto = new UserDTO();
            dto.setUsername("john_doe");
            when(userService.GetUserInfo(any())).thenReturn(dto);

            ResponseEntity<UserDTO> response =
                    userController.getUserInfo(null, "Bearer header.jwt.token");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("getUserInfo: returns 401 when both cookie and header are absent")
        void getUserInfo_noToken_returns401() {
            ResponseEntity<UserDTO> response = userController.getUserInfo(null, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("getUserInfo: returns 401 when JWT githubId is null")
        void getUserInfo_invalidToken_returns401() {
            when(jwtUtil.getGithubId("bad.token")).thenReturn(null);

            ResponseEntity<UserDTO> response = userController.getUserInfo("bad.token", null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    // =========================================================================
    // githubController
    // =========================================================================

    @Nested
    @DisplayName("githubController")
    class GithubControllerTests {

        @Mock private GithubService githubService;
        @InjectMocks private githubController controller;

        @Test
        @DisplayName("getUserRepos: returns 200 with repo list on success")
        void getUserRepos_success_returns200() throws UserNotFound {
            GitRepo repo = GitRepo.builder().name("my-repo").build();
            when(githubService.getUserRepos("gh_123")).thenReturn(List.of(repo));

            ResponseEntity<List<GitRepo>> response = controller.getUserRepos(mockUser);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("getUserRepos: returns 400 when UserNotFound is thrown")
        void getUserRepos_userNotFound_returns400() throws UserNotFound {
            when(githubService.getUserRepos("gh_123")).thenThrow(new UserNotFound("User not found"));

            ResponseEntity<List<GitRepo>> response = controller.getUserRepos(mockUser);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    // =========================================================================
    // RepoController
    // =========================================================================

    @Nested
    @DisplayName("RepoController")
    class RepoControllerTests {

        @Mock private GithubService githubService;
        @InjectMocks private RepoController repoController;
@Test
@DisplayName("getImportedRepos: returns list of imported repos")
void getImportedRepos_shouldReturnList() {

    when(githubService.getImportedRepos("gh_123"))
            .thenReturn(List.of(mockRepo));

    
    ResponseEntity<?> response =
            repoController.getAllImportedRepos(mockUser);

  
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

   
    @SuppressWarnings("unchecked")
    List<GitRepoEntity> result =
            (List<GitRepoEntity>) response.getBody();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getRepoName()).isEqualTo("my-repo");
}
// @Test
// @DisplayName("importRepo: returns 200 with saved entity")
// void importRepo_shouldReturnSavedRepo() {

//     
//     mockUser.setGithubId("gh_123");

//    
//     GitRepo dto = GitRepo.builder()
//             .name("new-repo")
//             .build();

//     
//     when(githubService.importRepo(anyString(), any(GitRepo.class)))
//             .thenReturn(mockRepo);

//     
//     ResponseEntity<?> response =
//             repoController.importRepo(mockUser, dto);

//     
//     System.out.println("Response: " + response);

// 
//     assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//     assertThat(response.getBody()).isEqualTo(mockRepo);
// }
    // =========================================================================
    // CIStatusController
    // =========================================================================

    @Nested
    @DisplayName("CIStatusController")
    class CIStatusControllerTests {

        @Mock private GithubService githubService;
        @Mock private GitHubActionsStatusService statusService;
        @InjectMocks private CIStatusController ciStatusController;

     @Test
@DisplayName("getStatus: returns 200 with CI status response")
void getStatus_shouldReturnCIStatus() {

    GitRepoEntity repo = new GitRepoEntity();
    repo.setRepoName("test-repo");

    CIStatusResponse mockResponse = new CIStatusResponse(
            "SUCCESS",
            null,
            null,
            "logs-url"
    );

    when(githubService.getRepoById("1")).thenReturn(repo);
    when(statusService.fetchLatestCIStatus("gh_123", repo))
            .thenReturn(mockResponse);

    ResponseEntity<CIStatusResponse> response =
            ciStatusController.getStatus("1", mockUser);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getStatus()).isEqualTo("SUCCESS");
}
    }

    // =========================================================================
    // CIMonitoringController
    // =========================================================================

    @Nested
    @DisplayName("CIMonitoringController")
    class CIMonitoringControllerTests {

        @Mock private GithubService githubService;
        @Mock private GitHubMonitoringService monitoringService;
        @InjectMocks private CIMonitoringController monitoringController;

     @Test
@DisplayName("monitor: returns 200 with workflow runs")
void monitor_shouldReturn200WithRuns() {

    WorkflowRunDTO run = new WorkflowRunDTO(
            1L,
            "build",
            "SUCCESS",
            "main",
            "abc123",
            "2026-01-01"
    );

    WorkflowRunsResponse runsResponse =
            new WorkflowRunsResponse(1, List.of(run));

    when(githubService.getRepoById("1"))
            .thenReturn(mockRepo);

    when(monitoringService.getWorkflowRuns("john_doe", mockRepo, "ghp_test_token"))
            .thenReturn(runsResponse);

    ResponseEntity<WorkflowRunsResponse> response =
            monitoringController.monitor("1", mockUser);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getWorkflowRuns()).hasSize(1);
}
    }

    // =========================================================================
    // DeployController
    // =========================================================================

    @Nested
    @DisplayName("DeployController")
    class DeployControllerTests {

        @Mock private GithubWorkflowService workflowService;
        @Mock private GithubCommitService commitService;
        @Mock private GitHubActionsStatusService statusService;
        @Mock private GithubService githubService;
        @InjectMocks private DeployController deployController;
@Test
@DisplayName("deployRepo: returns 200 with pipeline response map")
void deployRepo_shouldReturn200WithResponseMap() {

    // Prepare CICD config DTO
    CICDconfigDTO dto = new CICDconfigDTO();
    dto.setProjectType("SPRING_BOOT");
    dto.setBuildTool("MAVEN");
    dto.setRuntimeVersion("17");
    dto.setBranchName("main");
    dto.setDockerEnabled(false);
    dto.setCdEnabled(false);

    // Prepare mock CI status response
    CIStatusResponse ciStatus = new CIStatusResponse(
            "SUCCESS", null, null, "https://logs.url");

    // Mock repo fetch
    when(githubService.getRepoById("1L")).thenReturn(mockRepo);

    // Mock workflow generation
    when(workflowService.generateWorkflow(any(CICDConfigEntity.class)))
            .thenReturn("name: CI Pipeline\n");

    // Mock workflow commit (void)
    doNothing().when(commitService)
            .commitWorkflow(anyString(), any(GitRepoEntity.class), anyString());

    // Mock CI status fetch
    when(statusService.fetchLatestCIStatus(anyString(), any(GitRepoEntity.class)))
            .thenReturn(ciStatus);

    // Call controller method
    ResponseEntity<Map<String, Object>> response =
            deployController.deployRepo("1L", dto, mockUser);

    // Assertions
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    Map<String, Object> body = response.getBody();
    assertThat(body).isNotNull();

    assertThat(body)
            .containsEntry("message", "CI/CD pipeline triggered")
            .containsEntry("repository", "my-repo")
            .containsEntry("branch", "main")
            .containsEntry("ciStatus", "SUCCESS");
}
    }}}