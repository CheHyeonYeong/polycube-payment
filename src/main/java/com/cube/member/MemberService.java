package com.cube.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cube.common.exception.CustomException;
import com.cube.common.exception.DomainError;
import com.cube.member.entity.Member;
import com.cube.member.request.MemberCreateRequest;
import com.cube.member.response.MemberResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public MemberResponse create(MemberCreateRequest request) {
        Member member = memberRepository.save(
                Member.create(request.name(), request.grade(), request.point()));
        return MemberResponse.from(member);
    }

    @Transactional(readOnly = true)
    public MemberResponse read(Long memberId) {
        return MemberResponse.from(getMember(memberId));
    }

    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(new DomainError.MemberNotFound(memberId)));
    }

    @Transactional
    public void deductPoint(Long memberId, long amount) {
        Member member = getMember(memberId);
        if (member.getPoint() < amount) {
            throw new CustomException(new DomainError.InsufficientPoints(memberId, amount, member.getPoint()));
        }
        member.subtractPoint(amount);
    }
}
