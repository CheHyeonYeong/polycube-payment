package com.cube.payment.discount;

import com.cube.payment.order.entity.Order;

/**
 * 회원 등급 기반 할인 정책 인터페이스.
 *
 * Strategy 패턴으로 설계. 새로운 할인 정책 추가 시 이 인터페이스를 구현하는
 * 클래스만 추가하면 기존 코드 수정 없이 확장 가능 (OCP).
 */
public interface DiscountPolicy {

    long calculateDiscountAmount(Order order);

    String getPolicyName();
}
