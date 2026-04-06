package com.Innocent.DevOpsAsistant.Devops.Assistant.Models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private AppUser user;

    // ✅ ORDER COLUMN FIX
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "member_order")
    @Builder.Default
    private List<Member> members = new ArrayList<>();

    // ✅ ORDER COLUMN FIX
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "task_order")
    @Builder.Default
    private List<TaskItem> tasks = new ArrayList<>();
}