package com.cube.member;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cube.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
