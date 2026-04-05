package com.Innocent.DevOpsAsistant.Devops.Assistant.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.Project;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
public interface ProjectRepository extends JpaRepository<Project, Integer> {


@EntityGraph(attributePaths = {"members", "tasks"})
List<Project> findAll();
@Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.members LEFT JOIN FETCH p.tasks")
    List<Project> findAllWithMembersAndTasks();
}