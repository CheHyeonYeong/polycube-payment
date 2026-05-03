package com.cube.member.response;

import com.cube.member.entity.Member;
import com.cube.member.entity.MemberGrade;

public record MemberResponse(
        Long id,
        String name,
        MemberGrade grade,
        long point
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getGrade(),
                member.getPoint());
    }
}
