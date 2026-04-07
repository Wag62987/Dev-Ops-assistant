package com.Innocent.DevOpsAsistant.Devops.Assistant.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;

@Repository
public interface GitRepoRepository extends JpaRepository<GitRepoEntity, Long> {

    boolean existsByGithubRepoId(String githubRepoId);

    List<GitRepoEntity> findByAppUser_GithubId(String githubId);

    public Optional<GitRepoEntity> findByGithubRepoId(String repoId);
    @Modifying
@Query("DELETE FROM GitRepoEntity r WHERE r.githubId = :githubId")
void deleteAllByGithubId(@Param("githubId") String githubId);
}

