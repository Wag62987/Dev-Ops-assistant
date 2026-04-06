package com.Innocent.DevOpsAsistant.Devops.Assistant.Repository;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.TaskItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<TaskItem, Integer> {
}