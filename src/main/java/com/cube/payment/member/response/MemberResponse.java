package com.cube.payment.member.response;

import com.cube.payment.member.entity.Member;
import com.cube.payment.member.entity.MemberGrade;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MemberResponse {

    private final Long id;
    private final String name;
    private final MemberGrade grade;

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getName(), member.getGrade());
    }
}
