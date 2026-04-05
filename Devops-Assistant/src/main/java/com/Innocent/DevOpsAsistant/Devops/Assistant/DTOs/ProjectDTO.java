package com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs;

import java.util.List;

import org.springframework.scheduling.config.Task;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.Member;

class ProjectDTO {
   List<Member> members;
   List<Task> tasks;
}
