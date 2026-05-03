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
│   ├── PaymentMethod.java          # 결제 수단 enum (CREDIT_CARD, POINT)
│   └── exception/
│       ├── CustomException.java
│       ├── DomainError.java        # sealed interface — 비즈니스 에러 타입 정의
│       ├── GlobalExceptionHandler.java
│       └── InvariantViolation.java
├── member/                         # 회원 도메인
├── order/                          # 주문 도메인
├── discount/                       # 할인 전략
│   ├── DiscountPolicy.java         # interface: calculate(long price)
│   ├── NormalDiscountPolicy.java   # 할인 없음
│   ├── VipDiscountPolicy.java      # 1,000원 정액 할인
│   ├── VvipDiscountPolicy.java     # 10% 정률 할인
│   └── DiscountPolicyProvider.java # grade → 구현체 매핑
└── payment/                        # 결제 도메인
```

## API

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/v1/members` | 회원 생성 |
| GET | `/api/v1/members/{id}` | 회원 조회 |
| POST | `/api/v1/orders` | 주문 생성 |
| GET | `/api/v1/orders/{id}` | 주문 조회 |
| POST | `/api/v1/payments` | 결제 |
| GET | `/api/v1/payments/{id}` | 결제 조회 |

## 실행

```bash
./gradlew bootRun
./gradlew test
```

---

## 설계 포인트

### 1. 전략 패턴으로 할인 캡슐화

```java
// DiscountPolicyProvider
return switch (grade) {
    case NORMAL -> normalPolicy;
    case VIP    -> vipPolicy;
    case VVIP   -> vvipPolicy;
};
```

등급별 할인 로직을 `DiscountPolicy` 인터페이스 뒤로 숨겼다.
새 등급이 추가되더라도 `DiscountPolicyProvider`의 switch 하나만 수정하면 되고,
결제 서비스는 손대지 않아도 된다 (OCP).

### 2. DDD 레이어드 아키텍처

각 도메인(`member`, `order`, `payment`)을 독립 패키지로 분리했다.
패키지 안에 `entity / request / response / Service / Controller / Repository`가 모두 모여 있어
도메인 경계가 코드 구조에서 바로 드러난다.

### 3. sealed DomainError

```java
public sealed interface DomainError permits
    DomainError.MemberNotFound,
    DomainError.OrderNotFound,
    DomainError.PaymentNotFound,
    DomainError.InsufficientPoints,
    DomainError.PaymentAmountMismatch { ... }
```

모든 비즈니스 에러를 sealed interface의 하위 레코드로 명시했다.
`GlobalExceptionHandler`에서 `instanceof` 패턴 매칭 시 컴파일러가 누락된 케이스를 잡아줘
런타임 에러 처리 구멍이 생길 수 없다.

### 4. 정적 팩토리 + 불변 엔티티

```java
// 허용 — 의미있는 메서드로만 상태 변경
member.subtractPoint(amount);
order.markPaid();

// 금지 — @Setter 사용 안 함
member.setPoint(newPoint);
```

엔티티는 `@NoArgsConstructor(access = PROTECTED)` + `@Setter` 금지로 캡슐화했다.
객체 생성은 정적 팩토리 `create()`로만, 상태 변경은 도메인 메서드로만 가능하다.

---

## main → feature/step2 설계 변화와 근거

### 코드 전략 → DB 정책 관리

**main**에서는 할인 정책이 코드에 하드코딩돼 있다.
정책 값을 바꾸려면 코드를 수정하고 배포해야 한다.

**feature/step2**에서는 `discount_policy` 테이블로 이동했다.
운영 중에 API 한 번으로 정책 값을 즉시 변경할 수 있어 배포 없이 대응 가능하다.
변경 충돌은 `@Version` 기반 낙관적 락(CAS)으로 방지한다.

### PaymentDiscount 스냅샷

할인 정책을 DB에서 관리하면 나중에 정책이 변경되거나 삭제될 수 있다.
이 경우 과거 결제에 어떤 정책이 적용됐는지 알 수 없게 된다.

**feature/step2**에서는 결제 시점의 정책 이름, 타입, 값을 `payment_discount`에 스냅샷으로 저장한다.
정책이 바뀌더라도 과거 결제 기록의 할인 조건은 불변으로 보존된다.

### 복합 결제 수단

**main**에서는 결제 수단을 하나만 선택한다 (카드 또는 포인트).

**feature/step2**에서는 수단별 금액을 배열로 받아 조합할 수 있다.
포인트 잔액이 부족하면 잔액만 포인트로, 나머지는 카드로 자동 처리한다.
포인트 결제에는 5% 추가 할인이 적용돼 등급 할인과 중복으로 받을 수 있다.

### 멱등성 키 + 동시성 제어

**main**에서는 네트워크 재시도로 같은 요청이 두 번 오면 결제가 두 번 생성된다.

**feature/step2**에서는 `Idempotency-Key` 헤더로 중복 결제를 방지한다.
같은 키의 두 번째 요청은 새 결제를 만들지 않고 첫 번째 결제 응답을 그대로 반환한다.
동시에 같은 키로 N개 요청이 들어와도 `UNIQUE` 제약 + `DataIntegrityViolationException` race fallback으로 단 한 건만 저장된다.
포인트 동시 차감은 `@Version` CAS 재시도로 직렬화해 최종 잔액이 항상 정확하다.
