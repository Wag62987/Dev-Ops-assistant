package com.Innocent.DevOpsAsistant.Devops.Assistant.Models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="AppUser")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String name;
    private String email;
    private String githubId;
    private String password;

}
