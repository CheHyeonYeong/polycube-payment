package com.cube.order.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record OrderCreateRequest(
        @NotBlank(message = "상품명은 비어 있을 수 없습니다.")
        String productName,
        @PositiveOrZero(message = "주문 원가는 0 이상이어야 합니다.")
        long originalPrice,
        @NotNull(message = "회원 식별자는 필수입니다.")
        Long memberId
) {
}
