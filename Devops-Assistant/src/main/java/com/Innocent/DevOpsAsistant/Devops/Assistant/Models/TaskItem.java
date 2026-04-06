package com.Innocent.DevOpsAsistant.Devops.Assistant.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 50)
    private String status = "Pending";

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Project relation (CASCADE DELETE like EF)
    
@ManyToOne
@JoinColumn(name = "project_id")
@JsonIgnoreProperties({"tasks", "members"})
private Project project;;

    // Member relation (RESTRICT DELETE like EF)
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
}