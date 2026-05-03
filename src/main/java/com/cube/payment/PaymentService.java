package com.cube.payment;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.cube.common.PaymentMethod;
import com.cube.common.exception.CustomException;
import com.cube.common.exception.DomainError;
import com.cube.member.MemberService;
import com.cube.member.entity.Member;
import com.cube.order.OrderService;
import com.cube.order.entity.Order;
import com.cube.payment.entity.Payment;
import com.cube.payment.entity.PaymentDetail;
import com.cube.payment.entity.PaymentDiscount;
import com.cube.payment.request.PaymentMeansRequest;
import com.cube.payment.request.PaymentRequest;
import com.cube.payment.response.AppliedDiscountResponse;
import com.cube.payment.response.PaymentDetailResponse;
import com.cube.payment.response.PaymentResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderService orderService;
    private final MemberService memberService;
    private final PaymentChargeCalculator chargeCalculator;
    private final PaymentProcessor paymentProcessor;
    private final PaymentRepository paymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final PaymentDiscountRepository paymentDiscountRepository;

    /** 의도적으로 비-트랜잭션 — race fallback 이 rollback-only 에 묶이지 않도록. @Transactional 추가 금지. */
    public PayResult pay(String idempotencyKey, PaymentRequest request) {
        return paymentRepository.findByIdempotencyKey(idempotencyKey)
                .map(p -> new PayResult(loadExisting(p), false))
                .orElseGet(() -> new PayResult(processWithIdempotency(idempotencyKey, request), true));
    }

    public PaymentResponse read(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(new DomainError.PaymentNotFound(paymentId)));
        return loadExisting(payment);
    }

    private PaymentResponse processWithIdempotency(String idempotencyKey, PaymentRequest request) {
        Order order = orderService.getOrder(request.orderId());
        Member member = memberService.getMember(order.getMemberId());
        Map<PaymentMethod, Long> grossByMethod = aggregateByMethod(request.means());

        PaymentChargeCalculator.PaymentChargePlan plan =
                chargeCalculator.plan(order, member, grossByMethod);

        try {
            PaymentProcessor.PersistedPayment persisted =
                    paymentProcessor.process(idempotencyKey, order, member, plan);
            return toResponse(persisted.payment(), persisted.details(), persisted.snapshots());
        } catch (DataIntegrityViolationException race) {
            // 동시 요청에서 한 쪽이 INSERT 성공 → 충돌한 쪽은 기존 결제를 다시 조회해 동일 응답.
            return paymentRepository.findByIdempotencyKey(idempotencyKey)
                    .map(this::loadExisting)
                    .orElseThrow(() -> race);
        }
    }

    private PaymentResponse loadExisting(Payment payment) {
        List<PaymentDetail> details = paymentDetailRepository.findByPaymentId(payment.getId());
        List<PaymentDiscount> snapshots = paymentDiscountRepository.findByPaymentId(payment.getId());
        return toResponse(payment, details, snapshots);
    }

    private Map<PaymentMethod, Long> aggregateByMethod(List<PaymentMeansRequest> means) {
        return means.stream().collect(Collectors.toMap(
                PaymentMeansRequest::method,
                PaymentMeansRequest::amount,
                Long::sum,
                () -> new EnumMap<>(PaymentMethod.class)));
    }

    private PaymentResponse toResponse(Payment payment,
                                       List<PaymentDetail> details,
                                       List<PaymentDiscount> snapshots) {
        return PaymentResponse.of(
                payment,
                details.stream().map(PaymentDetailResponse::from).toList(),
                snapshots.stream().map(AppliedDiscountResponse::from).toList());
    }

    public record PayResult(PaymentResponse response, boolean newlyCreated) {
    }
}
