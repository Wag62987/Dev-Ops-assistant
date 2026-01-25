package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import org.springframework.stereotype.Service;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.CICDConfigEntity;

@Service
public class GithubWorkflowService {

    public String generateWorkflow(CICDConfigEntity config) {

        return switch (config.getProjectType()) {

            case "SPRING_BOOT" -> springBootWorkflow(config);
            case "NODE"       -> nodeWorkflow(config);
            case "REACT"      -> reactWorkflow(config);
            case "PYTHON"     -> pythonWorkflow(config);

            default -> throw new IllegalArgumentException(
                    "Unsupported project type");
        };
    }

    // ---------------- SPRING BOOT ----------------

    private String springBootWorkflow(CICDConfigEntity c) {

        String buildCmd = c.getBuildTool().equals("GRADLE")
                ? "./gradlew test"
                : "mvn clean test";

        String dockerStep = c.isDockerEnabled() ? dockerSteps() : "";

        return """
        name: CI Pipeline

        on:
          push:
            branches: [%s]
          pull_request:

        jobs:
          build:
            runs-on: ubuntu-latest

            steps:
              - uses: actions/checkout@v4

              - name: Setup Java
                uses: actions/setup-java@v4
                with:
                  java-version: '%s'
                  distribution: 'temurin'

              - name: Build & Test
                run: %s
        %s
        """.formatted(
                c.getBranchName(),
                c.getRuntimeVersion(),
                buildCmd,
                dockerStep
        );
    }

    // ---------------- NODE ----------------

    private String nodeWorkflow(CICDConfigEntity c) {

        String installCmd =
                c.getBuildTool().equals("YARN") ? "yarn install" : "npm install";

        String buildCmd =
                c.getBuildTool().equals("YARN") ? "yarn test" : "npm test";

        return """
        name: CI Pipeline

        on:
          push:
            branches: [%s]

        jobs:
          build:
            runs-on: ubuntu-latest

            steps:
              - uses: actions/checkout@v4

              - name: Setup Node
                uses: actions/setup-node@v4
                with:
                  node-version: '%s'

              - name: Install Dependencies
                run: %s

              - name: Run Tests
                run: %s
        """.formatted(
                c.getBranchName(),
                c.getRuntimeVersion(),
                installCmd,
                buildCmd
        );
    }

    // ---------------- REACT ----------------

    private String reactWorkflow(CICDConfigEntity c) {
        return nodeWorkflow(c); // same logic
    }

    // ---------------- PYTHON ----------------

    private String pythonWorkflow(CICDConfigEntity c) {

        return """
        name: CI Pipeline

        on:
          push:
            branches: [%s]

        jobs:
          build:
            runs-on: ubuntu-latest

            steps:
              - uses: actions/checkout@v4

              - name: Setup Python
                uses: actions/setup-python@v5
                with:
                  python-version: '%s'

              - name: Install dependencies
                run: pip install -r requirements.txt

              - name: Run Tests
                run: pytest
        """.formatted(
                c.getBranchName(),
                c.getRuntimeVersion()
        );
    }

    // ---------------- DOCKER ----------------

    private String dockerSteps() {
        return """
              - name: Build Docker Image
                run: docker build -t app:latest .
        """;
    }
}

