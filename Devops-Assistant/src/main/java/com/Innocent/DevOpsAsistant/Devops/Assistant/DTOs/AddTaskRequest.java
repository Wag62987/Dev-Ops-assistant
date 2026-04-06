package com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddTaskRequest {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer memberId;
}