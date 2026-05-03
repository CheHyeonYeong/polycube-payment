package com.cube.discount;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cube.common.PaymentMethod;
import com.cube.discount.entity.DiscountPolicy;
import com.cube.member.entity.MemberGrade;

public interface DiscountPolicyRepository extends JpaRepository<DiscountPolicy, Long> {

    @Query("""
            SELECT p FROM DiscountPolicy p
             WHERE p.active = true
               AND (p.targetGrade = :grade OR p.targetGrade IS NULL)
               AND p.targetPaymentMethod IS NULL
            """)
    List<DiscountPolicy> findGradeApplicable(@Param("grade") MemberGrade grade);

    @Query("""
            SELECT p FROM DiscountPolicy p
             WHERE p.active = true
               AND p.targetPaymentMethod = :method
            """)
    List<DiscountPolicy> findPaymentMethodApplicable(@Param("method") PaymentMethod method);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE DiscountPolicy p
               SET p.discountValue = :newValue,
                   p.version = p.version + 1
             WHERE p.id = :id
               AND p.version = :expectedVersion
            """)
    int casUpdateValue(@Param("id") Long id,
                       @Param("expectedVersion") Long expectedVersion,
                       @Param("newValue") BigDecimal newValue);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE DiscountPolicy p
               SET p.active = false,
                   p.version = p.version + 1
             WHERE p.id = :id
               AND p.version = :expectedVersion
            """)
    int casDeactivate(@Param("id") Long id,
                      @Param("expectedVersion") Long expectedVersion);
}
