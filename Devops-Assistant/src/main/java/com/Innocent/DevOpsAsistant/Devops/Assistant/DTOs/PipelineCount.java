package com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PipelineCount {
    private long runningPipelines;
}