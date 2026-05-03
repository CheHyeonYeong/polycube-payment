# 결제 시스템

Spring Boot 기반 결제 시스템 과제입니다.

- **main**: Step 1 — 기본 결제 기능 (회원/주문/결제, 코드 기반 할인 전략)
- **feature/step2**: Step 2 — 고급 기능 (DB 기반 할인 정책, 복합 결제, 멱등성 키, 동시성 제어)

## 기술 스택

- Java 21, Spring Boot 3.2.5
- Spring Data JPA, H2 In-memory DB
- Lombok, Gradle 8.13

## 패키지 구조

```
com.cube
├── common/
│   ├── Money.java                      # BigDecimal 기반 금액 값 객체
│   ├── PaymentMethod.java              # 결제 수단 enum (CREDIT_CARD, POINT)
│   └── exception/
│       ├── CustomException.java
│       ├── DomainError.java            # sealed interface — 비즈니스 에러 타입 정의
│       ├── GlobalExceptionHandler.java
│       └── InvariantViolation.java
├── member/                             # 회원 도메인 (@Version CAS 동시성 제어)
├── order/                              # 주문 도메인
├── discount/
│   ├── entity/
│   │   ├── DiscountPolicy.java         # JPA 엔티티 — DB에서 정책 관리
│   │   └── DiscountType.java           # FIXED(정액), RATE(정률)
│   ├── strategy/
│   │   ├── DiscountStrategy.java       # 계산 전략 인터페이스
│   │   ├── DiscountStrategies.java     # 전략 구현체 (FIXED/RATE)
│   │   ├── DiscountContext.java        # 할인 계산 컨텍스트
│   │   ├── DiscountResult.java         # 개별 할인 결과
│   │   └── DiscountSummary.java        # 전체 할인 요약
│   ├── request/, response/
│   ├── DiscountController.java
│   ├── DiscountPolicyRepository.java
│   └── DiscountService.java
└── payment/
    ├── entity/
    │   ├── Payment.java                # idempotency_key 포함
    │   ├── PaymentDetail.java          # 수단별 금액 명세
    │   ├── PaymentDiscount.java        # 결제 시점 할인 스냅샷
    │   └── AppliedScope.java           # 할인 적용 범위 (GRADE, PAYMENT_METHOD)
    ├── request/
    │   ├── PaymentRequest.java         # orderId + means 배열
    │   └── PaymentMeansRequest.java    # method + amount
    ├── response/
    │   ├── PaymentResponse.java
    │   ├── PaymentDetailResponse.java
    │   └── AppliedDiscountResponse.java
    ├── PaymentChargeCalculator.java    # 등급·수단 할인 계획 수립
    ├── PaymentProcessor.java           # 결제·명세·스냅샷 트랜잭션 저장
    ├── PaymentController.java
    ├── PaymentRepository.java
    ├── PaymentDetailRepository.java
    ├── PaymentDiscountRepository.java
    └── PaymentService.java             # 멱등성 체크 + race fallback
```

## API

| Method | URL | 헤더 | 설명 |
|--------|-----|------|------|
| POST | `/api/v1/members` | | 회원 생성 |
| GET | `/api/v1/members/{id}` | | 회원 조회 |
| POST | `/api/v1/orders` | | 주문 생성 |
| GET | `/api/v1/orders/{id}` | | 주문 조회 |
| POST | `/api/v1/payments` | `Idempotency-Key` 필수 | 결제 |
| GET | `/api/v1/payments/{id}` | | 결제 조회 |
| GET | `/api/v1/discount-policies` | | 할인 정책 목록 조회 |
| POST | `/api/v1/discount-policies` | | 할인 정책 생성 |
| PATCH | `/api/v1/discount-policies/{id}` | | 할인 정책 값 수정 (CAS) |
| POST | `/api/v1/discount-policies/{id}/deactivate` | | 할인 정책 비활성화 (CAS) |

### 결제 요청 예시

```json
POST /api/v1/payments
Idempotency-Key: <uuid>

{
  "orderId": 1,
  "means": [
    { "method": "POINT",       "amount": 5000 },
    { "method": "CREDIT_CARD", "amount": 3550 }
  ]
}
```

## 실행

```bash
./gradlew bootRun
./gradlew test
```

---

## 설계 포인트 (Step 2)

### 1. 할인 정책 DB 관리 전환

```java
// Step 1 — 코드에 하드코딩
return switch (grade) {
    case VIP  -> new VipDiscountPolicy();   // 1,000원 고정
    case VVIP -> new VvipDiscountPolicy();  // 10% 고정
    ...
};

// Step 2 — DB에서 조회
policyRepository.findGradeApplicable(grade)  // discount_policy 테이블
```

정책 값을 바꾸려면 Step 1은 코드 수정 후 재배포가 필요하다.
Step 2에서는 `PATCH /api/v1/discount-policies/{id}` 한 번으로 즉시 반영된다.
정책 추가·삭제도 배포 없이 API로 처리할 수 있다.

### 2. PaymentDiscount 스냅샷

```java
// 결제 시점의 정책 이름·타입·값을 그대로 저장
PaymentDiscount.of(payment, policy, appliedAmount)
```

DB 정책은 언제든 수정·삭제될 수 있다.
변경 후에도 "당시 어떤 조건으로 얼마를 할인받았는지"를 복원할 수 있도록
결제 시점의 정책 스냅샷을 `payment_discount` 테이블에 별도 저장한다.

### 3. 복합 결제 수단 + 포인트 5% 중복 할인

```json
"means": [
  { "method": "POINT",       "amount": 5000 },
  { "method": "CREDIT_CARD", "amount": 3550 }
]
```

Step 1은 결제 수단을 하나만 선택한다.
Step 2는 수단별 금액을 배열로 받아 조합할 수 있다.
포인트로 결제한 금액에는 등급 할인과 별개로 5% 추가 할인이 적용되고,
그 차액은 카드에서 차감해 최종 합계가 항상 주문 금액 - 등급 할인과 일치한다.

### 4. 멱등성 키 + 동시성 제어

```
동일 키 N개 동시 요청 흐름
요청 1 → INSERT 성공 → 201 Created
요청 2 → INSERT 실패(UNIQUE 충돌) → findByIdempotencyKey() → 200 OK (동일 응답)
```

`Idempotency-Key` 헤더를 필수로 받는다.
같은 키의 두 번째 요청은 새 결제를 만들지 않고 첫 번째 결제 응답을 그대로 반환한다.
`DataIntegrityViolationException` race fallback으로 동시 N개 요청에도 DB에 단 한 건만 저장된다.

포인트 동시 차감은 `Member`의 `@Version` 낙관적 락 + CAS 재시도로 직렬화한다.
재시도 횟수를 초과하면 `PointVersionConflict` 에러를 반환해 무한 루프를 방지한다.

---

## main → feature/step2 설계 변화와 근거

| 항목 | main (Step 1) | feature/step2 (Step 2) | 변화 근거 |
|------|--------------|----------------------|----------|
| 할인 정책 | 코드 하드코딩 | DB 엔티티 | 배포 없이 운영 중 정책 수정 |
| 할인 이력 | 없음 | PaymentDiscount 스냅샷 | 정책 변경 후에도 과거 결제 조건 보존 |
| 결제 수단 | 단일 선택 | 복합 조합 | 포인트 잔액 부족 시 혼합 결제 지원 |
| 포인트 추가 할인 | 없음 | 5% 중복 할인 | 포인트 결제 유도 |
| 중복 결제 방지 | 없음 | 멱등성 키 | 네트워크 재시도로 인한 이중 결제 방지 |
| 동시성 제어 | 없음 | @Version CAS 재시도 | 동시 포인트 차감 시 잔액 정합성 보장 |
