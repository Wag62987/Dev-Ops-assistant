package com.Innocent.DevOpsAsistant.Devops.Assistant.Repository;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    List<Project> findByUser_GithubIdOrderByIdDesc(String githubId);
    Optional<Project> findByIdAndUser_GithubId(Integer id, String githubId);
}