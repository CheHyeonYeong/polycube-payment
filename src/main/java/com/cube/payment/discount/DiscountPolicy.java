package com.cube.payment.discount;

import com.cube.payment.order.domain.Order;

/**
 * 할인 정책 전략 인터페이스.
 *
 * 비즈니스 요구사항이 자주 바뀌는 환경을 고려하여 Strategy 패턴으로 설계.
 * 새로운 할인 정책 추가 시 이 인터페이스를 구현하는 클래스만 추가하면 되므로
 * 기존 코드를 수정하지 않고 확장할 수 있다 (OCP).
 */
public interface DiscountPolicy {

    long calculateDiscountAmount(Order order);

    String getPolicyName();
}
