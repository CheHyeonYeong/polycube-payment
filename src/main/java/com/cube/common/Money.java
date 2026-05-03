package com.cube.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.cube.common.exception.InvariantViolation;

public record Money(long amount) {

    public static final Money ZERO = new Money(0);

    public Money {
        if (amount < 0) {
            throw new InvariantViolation("Money 는 음수일 수 없습니다: " + amount);
        }
    }

    public static Money of(long amount) {
        return new Money(amount);
    }

    public Money subtract(Money other) {
        return new Money(this.amount - other.amount);
    }

    public Money min(Money other) {
        return this.amount <= other.amount ? this : other;
    }

    public Money multiply(BigDecimal rate) {
        long result = BigDecimal.valueOf(this.amount)
                .multiply(rate)
                .setScale(0, RoundingMode.FLOOR)
                .longValueExact();
        return new Money(result);
    }
}
