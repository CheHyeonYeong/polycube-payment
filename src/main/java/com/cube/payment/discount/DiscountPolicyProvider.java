package com.cube.payment.discount;

import com.cube.payment.member.domain.MemberGrade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 회원 등급에 따른 할인 정책을 반환하는 Provider.
 *
 * 등급-정책 매핑 책임을 별도 클래스로 분리하여 PaymentService가
 * 매핑 로직에 의존하지 않도록 설계.
 * 새로운 등급이나 정책이 추가될 때 이 클래스만 수정하면 된다.
 */
@Component
@RequiredArgsConstructor
public class DiscountPolicyProvider {

    private final NormalDiscountPolicy normalDiscountPolicy;
    private final VipDiscountPolicy vipDiscountPolicy;
    private final VvipDiscountPolicy vvipDiscountPolicy;

    public DiscountPolicy getPolicy(MemberGrade grade) {
        return switch (grade) {
            case VIP -> vipDiscountPolicy;
            case VVIP -> vvipDiscountPolicy;
            default -> normalDiscountPolicy;
        };
    }
}
