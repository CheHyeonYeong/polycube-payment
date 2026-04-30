package com.cube.payment.payment.request;

import com.cube.payment.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentCreateRequest {

    @NotNull(message = "주문 ID는 필수입니다.")
    private Long orderId;

    @NotNull(message = "결제 수단은 필수입니다.")
    private PaymentMethod paymentMethod;
}
