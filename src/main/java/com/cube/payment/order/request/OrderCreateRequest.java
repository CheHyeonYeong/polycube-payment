package com.cube.payment.order.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCreateRequest {

    @NotNull(message = "회원 ID는 필수입니다.")
    private Long memberId;

    @NotBlank(message = "상품명은 필수입니다.")
    private String productName;

    @PositiveOrZero(message = "주문 금액은 0 이상이어야 합니다.")
    private long originalPrice;
}
