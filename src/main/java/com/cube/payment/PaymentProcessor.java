package com.cube.payment;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.cube.discount.strategy.DiscountResult;
import com.cube.member.MemberService;
import com.cube.member.entity.Member;
import com.cube.order.entity.Order;
import com.cube.payment.PaymentChargeCalculator.PaymentChargePlan;
import com.cube.payment.entity.AppliedScope;
import com.cube.payment.entity.Payment;
import com.cube.payment.entity.PaymentDetail;
import com.cube.payment.entity.PaymentDiscount;
import com.cube.payment.entity.PaymentDiscount.DiscountSnapshotData;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentProcessor {

    private final MemberService memberService;
    private final PaymentRepository paymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final PaymentDiscountRepository paymentDiscountRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PersistedPayment process(String idempotencyKey, Order order, Member member, PaymentChargePlan plan) {
        if (plan.pointsToDeduct() > 0) {
            memberService.deductPoint(member.getId(), plan.pointsToDeduct());
        }

        Instant now = Instant.now();

        Payment payment = paymentRepository.save(Payment.create(
                idempotencyKey, order.getId(), order.getOriginalPrice(),
                plan.totalDiscount(), plan.totalCharged(), now));

        List<PaymentDetail> details = paymentDetailRepository.saveAll(
                plan.charges().stream()
                        .map(c -> PaymentDetail.create(
                                payment.getId(), c.method(), c.gross().amount(), c.charged().amount()))
                        .toList());

        List<PaymentDiscount> snapshots = paymentDiscountRepository.saveAll(
                buildSnapshots(payment.getId(), plan, now));

        order.markPaid();

        return new PersistedPayment(payment, details, snapshots);
    }

    private List<PaymentDiscount> buildSnapshots(Long paymentId, PaymentChargePlan plan, Instant now) {
        Stream<PaymentDiscount> gradeSnapshots = plan.gradeSummary().results().stream()
                .map(r -> PaymentDiscount.snapshot(paymentId, toData(r), AppliedScope.GRADE, null, now));
        Stream<PaymentDiscount> methodSnapshots = plan.charges().stream()
                .flatMap(c -> c.discounts().stream()
                        .map(r -> PaymentDiscount.snapshot(
                                paymentId, toData(r), AppliedScope.PAYMENT_METHOD, c.method(), now)));
        return Stream.concat(gradeSnapshots, methodSnapshots).toList();
    }

    private DiscountSnapshotData toData(DiscountResult r) {
        return new DiscountSnapshotData(
                r.policyId(), r.policyName(), r.policyVersion(),
                r.type(), r.value(), r.appliedAmount().amount());
    }

    public record PersistedPayment(
            Payment payment,
            List<PaymentDetail> details,
            List<PaymentDiscount> snapshots
    ) {
        public PersistedPayment {
            details = List.copyOf(details);
            snapshots = List.copyOf(snapshots);
        }
    }
}
