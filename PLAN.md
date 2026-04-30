# 결제 시스템 설계 계획 (Step 1 - Basic)

## 도메인 유스케이스

### 회원 (Member)
- 회원 가입 (이름, 등급 지정)
- 회원 단건 조회

### 주문 (Order)
- 주문 생성 (회원 ID, 상품명, 가격)
- 주문 단건 조회

### 결제 (Payment)
- 결제 처리 (주문 ID, 결제 수단 선택)
- 결제 단건 조회

---

## API 설계

### 회원 API
| Method | URI | 설명 | 응답 상태 |
|--------|-----|------|-----------|
| POST | /api/v1/members | 회원 가입 | 201 |
| GET | /api/v1/members/{memberId} | 회원 단건 조회 | 200 |

### 주문 API
| Method | URI | 설명 | 응답 상태 |
|--------|-----|------|-----------|
| POST | /api/v1/orders | 주문 생성 | 201 |
| GET | /api/v1/orders/{orderId} | 주문 단건 조회 | 200 |

### 결제 API
| Method | URI | 설명 | 응답 상태 |
|--------|-----|------|-----------|
| POST | /api/v1/payments | 결제 처리 | 201 |
| GET | /api/v1/payments/{paymentId} | 결제 단건 조회 | 200 |

---

## 패키지 구조

```
com.cube.payment
├── common
│   ├── exception
│   │   ├── CustomException.java
│   │   ├── ErrorCode.java
│   │   └── GlobalExceptionHandler.java
│   └── response
│       └── ApiResponse.java
├── member
│   ├── entity
│   │   ├── Member.java
│   │   └── MemberGrade.java          (NORMAL / VIP / VVIP)
│   ├── request
│   │   └── MemberCreateRequest.java
│   ├── response
│   │   └── MemberResponse.java
│   ├── MemberController.java
│   ├── MemberService.java
│   └── MemberRepository.java
├── order
│   ├── entity
│   │   └── Order.java
│   ├── request
│   │   └── OrderCreateRequest.java
│   ├── response
│   │   └── OrderResponse.java
│   ├── OrderController.java
│   ├── OrderService.java
│   └── OrderRepository.java
├── discount
│   ├── DiscountPolicy.java            (인터페이스)
│   ├── NormalDiscountPolicy.java
│   ├── VipDiscountPolicy.java
│   ├── VvipDiscountPolicy.java
│   └── DiscountPolicyProvider.java
└── payment
    ├── entity
    │   ├── Payment.java
    │   └── PaymentMethod.java         (CREDIT_CARD / POINT)
    ├── request
    │   └── PaymentCreateRequest.java
    ├── response
    │   └── PaymentResponse.java
    ├── PaymentController.java
    ├── PaymentService.java
    └── PaymentRepository.java
```

---

## 할인 정책

| 등급 | 할인 방식 | 할인 금액 |
|------|-----------|-----------|
| NORMAL | 할인 없음 | 0원 |
| VIP | 고정 할인 | 1,000원 (주문 금액이 1,000원 미만이면 원가까지만) |
| VVIP | 비율 할인 | 주문 금액의 10% |

- 전략 패턴(Strategy Pattern) 적용 → OCP 준수
- `DiscountPolicyProvider`가 등급에 맞는 정책을 주입

---

## 공통 응답 형식

```json
{
  "success": true,
  "data": { ... },
  "message": null
}
```

오류 응답:
```json
{
  "success": false,
  "data": null,
  "message": "존재하지 않는 회원입니다."
}
```

---

## 에러 코드

| 에러 코드 | HTTP 상태 | 메시지 |
|-----------|-----------|--------|
| MEMBER_NOT_FOUND | 404 | 존재하지 않는 회원입니다. |
| ORDER_NOT_FOUND | 404 | 존재하지 않는 주문입니다. |
| PAYMENT_NOT_FOUND | 404 | 존재하지 않는 결제입니다. |
| INVALID_ORDER_PRICE | 400 | 주문 금액은 0 이상이어야 합니다. |

---

## 테스트 전략

| 테스트 종류 | 대상 | 어노테이션 |
|------------|------|-----------|
| 단위 테스트 | DiscountPolicy 3종 | 순수 JUnit (Spring 없음) |
| 통합 테스트 | MemberService, OrderService, PaymentService | @SpringBootTest + @Transactional |
| 슬라이스 테스트 | MemberController, OrderController, PaymentController | @WebMvcTest + MockMvc |
