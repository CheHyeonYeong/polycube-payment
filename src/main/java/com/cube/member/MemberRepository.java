package com.cube.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cube.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Member m
               SET m.point = m.point - :amount,
                   m.version = m.version + 1
             WHERE m.id = :id
               AND m.version = :expectedVersion
               AND m.point >= :amount
            """)
    int casDeductPoint(@Param("id") Long id,
                       @Param("expectedVersion") Long expectedVersion,
                       @Param("amount") long amount);
}
