package com.cube.discount.entity;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/** 할인 타입. 각 상수가 자신의 invariant (FIXED=정수, RATE=0~1) 검증을 갖는다. */
public enum DiscountType {

    FIXED {
        @Override
        Optional<String> doValidate(BigDecimal value) {
            return value.stripTrailingZeros().scale() > 0
                    ? Optional.of("FIXED 정책의 discountValue 는 정수여야 합니다.")
                    : Optional.empty();
        }
    },
    RATE {
        @Override
        Optional<String> doValidate(BigDecimal value) {
            return (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.ONE) > 0)
                    ? Optional.of("RATE 정책의 discountValue 는 0 이상 1 이하의 비율이어야 합니다.")
                    : Optional.empty();
        }
    };

    abstract Optional<String> doValidate(BigDecimal value);

    /** value non-null 필수 (null → NPE). 위반 사유 반환, 적합하면 Optional.empty(). */
    public final Optional<String> validate(BigDecimal value) {
        Objects.requireNonNull(value, "discountValue 는 null 일 수 없습니다.");
        return doValidate(value);
    }

    /** null-safe — Bean Validation @AssertTrue 메서드용. */
    public final boolean isValid(BigDecimal value) {
        return value != null && doValidate(value).isEmpty();
    }
}
