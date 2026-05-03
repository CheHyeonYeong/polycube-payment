package com.cube.payment;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cube.common.Money;
import com.cube.common.PaymentMethod;
import com.cube.common.exception.CustomException;
import com.cube.common.exception.DomainError;
import com.cube.discount.DiscountService;
import com.cube.discount.strategy.DiscountResult;
import com.cube.discount.strategy.DiscountSummary;
import com.cube.member.entity.Member;
import com.cube.order.entity.Order;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentChargeCalculator {

    private final DiscountService discountService;

    public PaymentChargePlan plan(Order order, Member member, Map<PaymentMethod, Long> grossByMethod) {
        DiscountSummary gradeSummary = discountService.calculateGradeDiscount(
                Money.of(order.getOriginalPrice()), member.getGrade());
        verifyMeansSum(grossByMethod, gradeSummary.finalPrice());

        List<MethodCharge> charges = grossByMethod.entrySet().stream()
                .map(e -> chargeFor(e.getKey(), Money.of(e.getValue()), member))
                .toList();

        long totalCharged = charges.stream().mapToLong(c -> c.charged().amount()).sum();
        long totalDiscount = order.getOriginalPrice() - totalCharged;
        long pointsToDeduct = sumChargedBy(charges, PaymentMethod.POINT);

        return new PaymentChargePlan(gradeSummary, charges, totalCharged, totalDiscount, pointsToDeduct);
    }

    private MethodCharge chargeFor(PaymentMethod method, Money gross, Member member) {
        DiscountSummary summary = discountService.calculatePaymentMethodDiscount(gross, method, member.getGrade());
        return new MethodCharge(method, gross, summary.finalPrice(), summary.results());
    }

    private void verifyMeansSum(Map<PaymentMethod, Long> grossByMethod, Money expected) {
        long meansSum = grossByMethod.values().stream().mapToLong(Long::longValue).sum();
        if (meansSum != expected.amount()) {
            throw new CustomException(new DomainError.PaymentAmountMismatch(expected.amount(), meansSum));
        }
    }

    private long sumChargedBy(List<MethodCharge> charges, PaymentMethod method) {
        return charges.stream()
                .filter(c -> c.method() == method)
                .mapToLong(c -> c.charged().amount())
                .sum();
    }

    public record MethodCharge(PaymentMethod method, Money gross, Money charged, List<DiscountResult> discounts) {
        public MethodCharge {
            discounts = List.copyOf(discounts);
        }
    }

    public record PaymentChargePlan(
            DiscountSummary gradeSummary,
            List<MethodCharge> charges,
            long totalCharged,
            long totalDiscount,
            long pointsToDeduct
    ) {
        public PaymentChargePlan {
            charges = List.copyOf(charges);
        }
    }
}
