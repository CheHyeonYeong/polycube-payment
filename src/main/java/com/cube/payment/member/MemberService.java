package com.cube.payment.member;

import com.cube.payment.common.exception.CustomException;
import com.cube.payment.common.exception.ErrorCode;
import com.cube.payment.member.entity.Member;
import com.cube.payment.member.request.MemberCreateRequest;
import com.cube.payment.member.response.MemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public MemberResponse create(MemberCreateRequest request) {
        Member member = new Member(request.getName(), request.getGrade());
        return MemberResponse.from(memberRepository.save(member));
    }

    public MemberResponse findById(Long memberId) {
        return memberRepository.findById(memberId)
                .map(MemberResponse::from)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
