package com.cube.common;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.cube.common.exception.InvariantViolation;

class MoneyTest {

    @Test
    @DisplayName("음수로 생성하면 InvariantViolation")
    void negativeIsRejected() {
        assertThrows(InvariantViolation.class, () -> new Money(-1));
    }

    @Test
    @DisplayName("subtract 결과가 음수면 InvariantViolation — invariant 강제")
    void subtractRejectsNegativeResult() {
        assertThrows(InvariantViolation.class, () -> Money.of(100).subtract(Money.of(500)));
    }

    @Test
    @DisplayName("multiply 는 BigDecimal floor 라운딩으로 정확히 계산")
    void multiplyFloors() {
        assertAll(
                () -> assertEquals(950L, Money.of(1000).multiply(new BigDecimal("0.95")).amount()),
                () -> assertEquals(199L, Money.of(2000).multiply(new BigDecimal("0.0999")).amount()));
    }

    @Test
    @DisplayName("min 은 더 작은 쪽을 반환한다")
    void minReturnsSmaller() {
        assertEquals(100L, Money.of(100).min(Money.of(500)).amount());
        assertEquals(100L, Money.of(500).min(Money.of(100)).amount());
    }
}
