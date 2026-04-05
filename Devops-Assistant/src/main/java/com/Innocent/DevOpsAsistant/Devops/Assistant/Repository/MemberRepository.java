package com.Innocent.DevOpsAsistant.Devops.Assistant.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.Member;

public interface MemberRepository extends JpaRepository<Member, Integer> {
}