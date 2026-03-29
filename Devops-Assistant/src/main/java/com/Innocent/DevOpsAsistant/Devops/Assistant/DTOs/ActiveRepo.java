package com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActiveRepo {
    private int TotalCount;
    private Long activeRepo;
}
