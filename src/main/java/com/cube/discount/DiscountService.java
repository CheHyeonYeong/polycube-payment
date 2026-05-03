package com.cube.discount;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cube.common.Money;
import com.cube.common.PaymentMethod;
import com.cube.common.exception.CustomException;
import com.cube.common.exception.DomainError;
import com.cube.discount.entity.DiscountPolicy;
import com.cube.discount.request.DiscountPolicyCreateRequest;
import com.cube.discount.request.DiscountPolicyUpdateRequest;
import com.cube.discount.response.DiscountPolicyResponse;
import com.cube.discount.strategy.DiscountContext;
import com.cube.discount.strategy.DiscountResult;
import com.cube.discount.strategy.DiscountSummary;
import com.cube.member.entity.MemberGrade;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountPolicyRepository policyRepository;

    public DiscountSummary calculateGradeDiscount(Money originalPrice, MemberGrade grade) {
        return foldDiscounts(
                policyRepository.findGradeApplicable(grade),
                DiscountContext.of(originalPrice, grade));
    }

    public DiscountSummary calculatePaymentMethodDiscount(Money methodGrossAmount, PaymentMethod method, MemberGrade grade) {
        return foldDiscounts(
                policyRepository.findPaymentMethodApplicable(method),
                DiscountContext.of(methodGrossAmount, grade).withPaymentMethod(method));
    }

    private DiscountSummary foldDiscounts(List<DiscountPolicy> policies, DiscountContext baseContext) {
        List<DiscountResult> results = new ArrayList<>();
        Money currentPrice = baseContext.currentPrice();

        List<DiscountPolicy> ordered = policies.stream()
                .sorted(Comparator.comparingInt(DiscountPolicy::getPriority)
                        .thenComparing(DiscountPolicy::getId))
                .toList();
        for (DiscountPolicy policy : ordered) {
            DiscountResult result = policy.toStrategy().apply(baseContext.withCurrentPrice(currentPrice));
            results.add(result);
            currentPrice = currentPrice.subtract(result.appliedAmount());
        }

        Money totalDiscount = baseContext.currentPrice().subtract(currentPrice);
        return new DiscountSummary(results, totalDiscount, currentPrice);
    }

    @Transactional
    public DiscountPolicyResponse create(DiscountPolicyCreateRequest request) {
        DiscountPolicy policy = policyRepository.save(DiscountPolicy.create(
                request.name(),
                request.targetGrade(),
                request.targetPaymentMethod(),
                request.discountType(),
                request.discountValue(),
                request.priority()));
        return DiscountPolicyResponse.from(policy);
    }

    @Transactional
    public DiscountPolicyResponse updateValue(Long policyId, DiscountPolicyUpdateRequest request) {
        DiscountPolicy policy = getPolicy(policyId);
        policy.getDiscountType().validate(request.newValue())
                .ifPresent(reason -> {
                    throw new CustomException(new DomainError.InvalidDiscountValue(policyId, reason));
                });
        int updated = policyRepository.casUpdateValue(policyId, request.expectedVersion(), request.newValue());
        if (updated == 0) {
            throw new CustomException(new DomainError.PolicyVersionConflict(policyId, request.expectedVersion()));
        }
        return DiscountPolicyResponse.from(getPolicy(policyId));
    }

    @Transactional
    public void deactivate(Long policyId, Long expectedVersion) {
        int updated = policyRepository.casDeactivate(policyId, expectedVersion);
        if (updated == 0) {
            throw new CustomException(new DomainError.PolicyVersionConflict(policyId, expectedVersion));
        }
    }

    public List<DiscountPolicyResponse> readAll() {
        return policyRepository.findAll().stream()
                .map(DiscountPolicyResponse::from)
                .toList();
    }

    private DiscountPolicy getPolicy(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new CustomException(new DomainError.DiscountPolicyNotFound(id)));
    }
}
