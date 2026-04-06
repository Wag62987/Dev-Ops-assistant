package com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddMemberRequest {
    private String name;
    private String role;
}