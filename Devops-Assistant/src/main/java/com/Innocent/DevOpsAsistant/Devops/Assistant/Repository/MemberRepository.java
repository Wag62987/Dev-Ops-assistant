package com.Innocent.DevOpsAsistant.Devops.Assistant.Repository;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {
}