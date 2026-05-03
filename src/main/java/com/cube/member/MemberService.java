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

    private static final int CAS_MAX_RETRIES = 5;

    private final MemberRepository memberRepository;

    @Transactional
    public MemberResponse create(MemberCreateRequest request) {
        Member member = memberRepository.save(
                Member.create(request.name(), request.grade(), request.point()));
        return MemberResponse.from(member);
    }

    public MemberResponse read(Long memberId) {
        return MemberResponse.from(getMember(memberId));
    }

    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(new DomainError.MemberNotFound(memberId)));
    }

    /** CAS UPDATE 로 포인트 차감. 잔액 부족 → PointNotEnough, retry 초과 → PointVersionConflict. */
    @Transactional
    public void deductPoint(Long memberId, long amount) {
        for (int attempt = 0; attempt < CAS_MAX_RETRIES; attempt++) {
            Member fresh = getMember(memberId);
            if (fresh.getPoint() < amount) {
                throw new CustomException(new DomainError.PointNotEnough(memberId, amount, fresh.getPoint()));
            }
            int updated = memberRepository.casDeductPoint(memberId, fresh.getVersion(), amount);
            if (updated == 1) {
                return;
            }
            Thread.onSpinWait();
        }
        throw new CustomException(new DomainError.PointVersionConflict(memberId));
    }
}
