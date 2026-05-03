package com.cube.member.entity;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.Hibernate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberGrade grade;

    @Column(nullable = false)
    private long point;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static Member create(String name, MemberGrade grade, long point) {
        Member member = new Member();
        member.name = name;
        member.grade = grade;
        member.point = point;
        Instant now = Instant.now();
        member.createdAt = now;
        member.updatedAt = now;
        return member;
    }

    public void subtractPoint(long amount) {
        this.point -= amount;
        this.updatedAt = Instant.now();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Member other = (Member) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(Hibernate.getClass(this), id);
    }

    @Override
    public String toString() {
        return "Member(id=" + id + ", grade=" + grade + ")";
    }
}
