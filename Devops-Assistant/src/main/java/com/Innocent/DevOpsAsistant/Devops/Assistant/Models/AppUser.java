package com.Innocent.DevOpsAsistant.Devops.Assistant.Models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="AppUser")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String name;
    private String email;
    private String githubId;
    private String password;

    public AppUser(String username, String name, String email, String githubId) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.githubId = githubId;
    }

}
