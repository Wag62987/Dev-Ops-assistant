package com.Innocent.DevOpsAsistant.Devops.Assistant.Repository;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser,Long> {
    Optional<AppUser> findByGithubId(String id);
}
