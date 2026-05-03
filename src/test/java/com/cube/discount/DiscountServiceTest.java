package com.cube.discount;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import com.cube.common.Money;
import com.cube.common.PaymentMethod;
import com.cube.common.exception.CustomException;
import com.cube.common.exception.DomainError;
import com.cube.discount.entity.DiscountPolicy;
import com.cube.discount.entity.DiscountType;
import com.cube.discount.request.DiscountPolicyUpdateRequest;
import com.cube.discount.strategy.DiscountSummary;
import com.cube.member.entity.MemberGrade;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DiscountServiceTest {

    @Autowired
    DiscountService discountService;

    @Autowired
    DiscountPolicyRepository policyRepository;

    private DiscountPolicy saveFixed(MemberGrade grade, long value, int priority) {
        return policyRepository.save(DiscountPolicy.create(
                "FIXED_" + grade + "_" + value + "_" + priority,
                grade, null, DiscountType.FIXED, BigDecimal.valueOf(value), priority));
    }

    private DiscountPolicy saveRate(MemberGrade grade, BigDecimal value, int priority) {
        return policyRepository.save(DiscountPolicy.create(
                "RATE_" + grade + "_" + value + "_" + priority,
                grade, null, DiscountType.RATE, value, priority));
    }

    @Test
    @DisplayName("NORMAL 등급은 할인이 없다")
    void normalHasNoDiscount() {
        DiscountSummary summary = discountService.calculateGradeDiscount(Money.of(10000), MemberGrade.NORMAL);
        assertEquals(0L, summary.totalDiscount().amount());
        assertEquals(10000L, summary.finalPrice().amount());
    }

    @Test
    @DisplayName("VIP 등급은 1,000원 정액 할인을 받는다")
    void vipReceivesFixedDiscount() {
        DiscountSummary summary = discountService.calculateGradeDiscount(Money.of(10000), MemberGrade.VIP);
        assertEquals(1000L, summary.totalDiscount().amount());
        assertEquals(9000L, summary.finalPrice().amount());
    }

    @Test
    @DisplayName("VVIP 등급은 10% 정률 할인을 받는다")
    void vvipReceivesRateDiscount() {
        DiscountSummary summary = discountService.calculateGradeDiscount(Money.of(10000), MemberGrade.VVIP);
        assertEquals(1000L, summary.totalDiscount().amount());
        assertEquals(9000L, summary.finalPrice().amount());
    }

    @Test
    @DisplayName("포인트 결제 시 5% 추가 할인이 조회된다")
    void pointMethodHasExtraDiscount() {
        DiscountSummary summary = discountService.calculatePaymentMethodDiscount(
                Money.of(10000), PaymentMethod.POINT, MemberGrade.NORMAL);
        assertEquals(500L, summary.totalDiscount().amount());
    }

    @Test
    @DisplayName("CAS 업데이트는 버전이 일치하면 성공하고 버전이 증가한다")
    void casUpdateSucceedsAndBumpsVersion() {
        DiscountPolicy policy = saveFixed(MemberGrade.NORMAL, 1000, 10);
        Long original = policy.getVersion();

        discountService.updateValue(policy.getId(),
                new DiscountPolicyUpdateRequest(original, BigDecimal.valueOf(1500)));

        DiscountPolicy reloaded = policyRepository.findById(policy.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(reloaded.getDiscountValue())
                .isEqualByComparingTo(BigDecimal.valueOf(1500));
        assertTrue(reloaded.getVersion() > original);
    }

    @Test
    @DisplayName("CAS 업데이트는 버전이 다르면 충돌 예외를 던진다")
    void casUpdateFailsOnVersionMismatch() {
        DiscountPolicy policy = saveFixed(MemberGrade.NORMAL, 1000, 10);

        DiscountPolicyUpdateRequest request =
                new DiscountPolicyUpdateRequest(policy.getVersion() + 999, BigDecimal.valueOf(2000));

        CustomException ex = assertThrows(CustomException.class,
                () -> discountService.updateValue(policy.getId(), request));
        assertTrue(ex.getError() instanceof DomainError.PolicyVersionConflict conflict
                && conflict.policyId().equals(policy.getId()));
    }

    @Test
    @DisplayName("CAS 업데이트 후 후속 할인 계산이 새로운 값으로 반영된다")
    void casUpdateAffectsSubsequentCalculation() {
        DiscountPolicy policy = policyRepository.findGradeApplicable(MemberGrade.VIP).get(0);
        Long version = policy.getVersion();

        discountService.updateValue(policy.getId(),
                new DiscountPolicyUpdateRequest(version, BigDecimal.valueOf(2000)));

        DiscountSummary summary = discountService.calculateGradeDiscount(Money.of(10000), MemberGrade.VIP);
        assertEquals(2000L, summary.totalDiscount().amount());
        assertEquals(8000L, summary.finalPrice().amount());
    }

    @Test
    @DisplayName("정책을 비활성화하면 할인 계산에서 제외된다")
    void deactivatedPolicyIsExcluded() {
        DiscountPolicy policy = policyRepository.findGradeApplicable(MemberGrade.VIP).get(0);

        discountService.deactivate(policy.getId(), policy.getVersion());

        DiscountSummary summary = discountService.calculateGradeDiscount(Money.of(10000), MemberGrade.VIP);
        assertEquals(0L, summary.totalDiscount().amount());
        assertEquals(10000L, summary.finalPrice().amount());
    }

    @Test
    @DisplayName("우선순위가 다른 두 정책이 priority 낮은 순서로 누적 적용된다 (시드 비의존)")
    void multiplePoliciesAppliedByPriority() {
        // 의도: 시드 정책 영향 배제. data.sql 의 NORMAL 등급에는 grade 정책이 없는 것을 전제로 한다.
        // data.sql 에 NORMAL 정책이 추가되면 이 테스트는 깨지므로 함께 갱신해야 한다.
        DiscountPolicy first = saveFixed(MemberGrade.NORMAL, 1000, 10);
        DiscountPolicy second = saveRate(MemberGrade.NORMAL, new BigDecimal("0.10"), 20);

        DiscountSummary summary = discountService.calculateGradeDiscount(Money.of(10000), MemberGrade.NORMAL);

        assertEquals(2, summary.results().size());
        assertEquals(first.getId(), summary.results().get(0).policyId());
        assertEquals(second.getId(), summary.results().get(1).policyId());
        // 10000 - 1000 = 9000, 9000 - 900(10%) = 8100
        assertEquals(1900L, summary.totalDiscount().amount());
        assertEquals(8100L, summary.finalPrice().amount());
    }

    @Test
    @DisplayName("priority 동률이면 id 오름차순으로 결정적으로 적용된다 (정렬 안정성)")
    void priorityTieBreaksByIdAscending() {
        // 의도: data.sql 의 NORMAL 등급에는 grade 정책이 없는 것을 전제로 한다.
        // data.sql 에 NORMAL 정책이 추가되면 정렬 결과 첫 위치가 바뀌므로 이 테스트도 함께 갱신해야 한다.
        DiscountPolicy first = saveFixed(MemberGrade.NORMAL, 500, 50);
        DiscountPolicy second = saveFixed(MemberGrade.NORMAL, 300, 50);

        DiscountSummary summary = discountService.calculateGradeDiscount(Money.of(10000), MemberGrade.NORMAL);

        assertEquals(first.getId(), summary.results().get(0).policyId());
        assertEquals(second.getId(), summary.results().get(1).policyId());
    }

    @Test
    @DisplayName("update: FIXED 정책에 소수 newValue 가 들어오면 INVALID_DISCOUNT_VALUE")
    void updateFixedRejectsFractional() {
        DiscountPolicy fixedPolicy = saveFixed(MemberGrade.NORMAL, 1000, 99);

        DiscountPolicyUpdateRequest request =
                new DiscountPolicyUpdateRequest(fixedPolicy.getVersion(), new BigDecimal("1500.5"));

        CustomException ex = assertThrows(CustomException.class,
                () -> discountService.updateValue(fixedPolicy.getId(), request));
        assertTrue(ex.getError() instanceof DomainError.InvalidDiscountValue v
                && v.policyId().equals(fixedPolicy.getId()));
    }

    @Test
    @DisplayName("update: RATE 정책에 1.0 초과 newValue 가 들어오면 INVALID_DISCOUNT_VALUE")
    void updateRateRejectsOutOfRange() {
        DiscountPolicy ratePolicy = saveRate(MemberGrade.NORMAL, new BigDecimal("0.10"), 99);

        DiscountPolicyUpdateRequest request =
                new DiscountPolicyUpdateRequest(ratePolicy.getVersion(), new BigDecimal("2.0"));

        CustomException ex = assertThrows(CustomException.class,
                () -> discountService.updateValue(ratePolicy.getId(), request));
        assertTrue(ex.getError() instanceof DomainError.InvalidDiscountValue v
                && v.policyId().equals(ratePolicy.getId()));
    }

    @Test
    @DisplayName("FIXED 정책 값이 현재가보다 큰 경우 현재가를 초과 차감하지 않는다 (통합)")
    void fixedDiscountCappedToCurrentPrice() {
        saveFixed(MemberGrade.VVIP, 500_000L, 30);

        DiscountSummary summary = discountService.calculateGradeDiscount(Money.of(10000), MemberGrade.VVIP);
        // VVIP 시드 10% 가 먼저 적용되어 9000, 그 후 큰 FIXED(500000) 가 9000 으로 cap → 0
        assertEquals(10000L, summary.totalDiscount().amount());
        assertEquals(0L, summary.finalPrice().amount());
    }
}
