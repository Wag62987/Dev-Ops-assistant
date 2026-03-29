package com.Innocent.DevOpsAsistant.Devops.Assistant.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "repos")
@Getter
@Setter
@NoArgsConstructor
public class GitRepoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repo_name")
    private String repoName;

    @Column(name = "repo_url")
    private String repoUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String language;

    @Column(name = "github_repo_id", unique = true, nullable = false)
    private String githubRepoId;

    // MANY Repos → ONE AppUser
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "app_user_id",
        nullable = false
    )
    @JsonIgnore
    private AppUser appUser;
}