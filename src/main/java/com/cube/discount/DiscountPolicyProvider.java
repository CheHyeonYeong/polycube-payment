package com.cube.discount;

import org.springframework.stereotype.Component;

import com.cube.member.entity.MemberGrade;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiscountPolicyProvider {

    private final NormalDiscountPolicy normalPolicy;
    private final VipDiscountPolicy vipPolicy;
    private final VvipDiscountPolicy vvipPolicy;

    public DiscountPolicy getPolicy(MemberGrade grade) {
        return switch (grade) {
            case NORMAL -> normalPolicy;
            case VIP -> vipPolicy;
            case VVIP -> vvipPolicy;
        };
    }
}
