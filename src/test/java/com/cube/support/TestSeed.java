package com.cube.support;

import java.util.Map;
import java.util.Set;

import com.cube.member.entity.MemberGrade;

/** data.sql 시드의 코드측 source of truth. data.sql 변경 시 함께 갱신. */
public final class TestSeed {

    public static final Set<String> MEMBER_NAMES = Set.of("일반회원", "VIP회원", "VVIP회원");

    public static final Set<String> POLICY_NAMES = Set.of(
            "VIP 1,000원 정액 할인",
            "VVIP 10% 정률 할인",
            "포인트 결제 5% 중복 할인");

    /** 시드 멤버 등급별 초기 포인트 잔액 (data.sql 기준). 결제 후 cleanup 으로 복원할 때 사용. */
    public static final Map<MemberGrade, Long> MEMBER_POINTS = Map.of(
            MemberGrade.NORMAL, 0L,
            MemberGrade.VIP, 50_000L,
            MemberGrade.VVIP, 200_000L);

    private TestSeed() {
    }
}
