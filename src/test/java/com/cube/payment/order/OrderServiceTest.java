package com.cube.payment.order;

import com.cube.payment.common.exception.CustomException;
import com.cube.payment.common.exception.ErrorCode;
import com.cube.payment.member.MemberRepository;
import com.cube.payment.member.entity.Member;
import com.cube.payment.member.entity.MemberGrade;
import com.cube.payment.order.request.OrderCreateRequest;
import com.cube.payment.order.response.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("주문 서비스 통합 테스트")
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private MemberRepository memberRepository;

    private Long memberId;

    @BeforeEach
    void setUp() {
        memberId = memberRepository.save(new Member("테스트회원", MemberGrade.VIP)).getId();
    }

    @Test
    @DisplayName("주문 생성 시 상품명과 원가가 정상 저장된다")
    void 주문_생성_성공() {
        OrderCreateRequest request = createRequest(memberId, "노트북", 1_500_000L);

        OrderResponse response = orderService.create(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getProductName()).isEqualTo("노트북");
        assertThat(response.getOriginalPrice()).isEqualTo(1_500_000L);
        assertThat(response.getMemberId()).isEqualTo(memberId);
    }

    @Test
    @DisplayName("주문 금액이 0원인 주문도 생성 가능하다")
    void 주문_금액_0원_생성_성공() {
        OrderCreateRequest request = createRequest(memberId, "무료상품", 0L);

        OrderResponse response = orderService.create(request);

        assertThat(response.getOriginalPrice()).isZero();
    }

    @Test
    @DisplayName("존재하지 않는 회원으로 주문 생성 시 MEMBER_NOT_FOUND 예외가 발생한다")
    void 존재하지_않는_회원_주문_생성_예외() {
        OrderCreateRequest request = createRequest(999L, "상품", 10_000L);

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Test
    @DisplayName("주문을 ID로 조회할 수 있다")
    void 주문_단건_조회_성공() {
        OrderResponse saved = orderService.create(createRequest(memberId, "마우스", 50_000L));

        OrderResponse found = orderService.findById(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getProductName()).isEqualTo("마우스");
    }

    @Test
    @DisplayName("존재하지 않는 주문 ID 조회 시 ORDER_NOT_FOUND 예외가 발생한다")
    void 존재하지_않는_주문_조회_예외() {
        assertThatThrownBy(() -> orderService.findById(999L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ORDER_NOT_FOUND));
    }

    private OrderCreateRequest createRequest(Long memberId, String productName, long price) {
        OrderCreateRequest request = new OrderCreateRequest();
        setField(request, "memberId", memberId);
        setField(request, "productName", productName);
        setField(request, "originalPrice", price);
        return request;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
