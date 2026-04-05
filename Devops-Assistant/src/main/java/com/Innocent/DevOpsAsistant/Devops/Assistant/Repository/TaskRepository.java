package com.Innocent.DevOpsAsistant.Devops.Assistant.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.TaskItem;

public interface TaskRepository extends JpaRepository<TaskItem, Integer> {
}