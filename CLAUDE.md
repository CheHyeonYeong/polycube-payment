# CLAUDE.md — AI 협업 가이드

## 기술 스택

- Java 17+ (빌드는 JDK 22, `gradle.properties`에서 강제 설정)
- Spring Boot 3.2.5
- Spring Data JPA + H2 In-memory DB
- Lombok
- Gradle 8.13

## 아키텍처 원칙

### DDD + 레이어드 아키텍처

각 도메인은 독립적인 패키지 단위로 구성한다:

```
/{domain}
  ├── entity/       JPA 엔티티
  ├── request/      입력 DTO (@Valid 검증)
  ├── response/     출력 DTO (정적 팩토리 from())
  ├── Controller    REST 컨트롤러
  ├── Service       비즈니스 로직
  └── Repository    데이터 접근
```

`common/` 패키지에는 도메인에 속하지 않는 공통 요소만 위치한다:
- `exception/` — CustomException, ErrorCode, GlobalExceptionHandler
- `response/` — ApiResponse

### 할인 정책 — 전략 패턴

`DiscountPolicy` 인터페이스를 구현한 세 개의 정책 클래스가 있다. 새 등급이 추가될 때 `DiscountPolicyProvider`의 switch만 수정한다.

## 코딩 컨벤션

### 일반

- 주석은 한국어만 사용. WHY가 명확하지 않으면 주석 작성 금지
- `application.yml`만 사용 (`.properties` 파일 절대 사용 금지)
- `System.out.println` 사용 금지
- 기능 구현에 npm 사용 금지

### 엔티티

- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 필수
- `@Setter` 사용 금지 — 상태 변경은 의미있는 메서드로만
- `@Column(nullable = false)` 명시 필수

### DTO (request/response)

- `request/`: `@Valid` + 검증 어노테이션 필수, `@NoArgsConstructor` 필수
- `response/`: `@RequiredArgsConstructor` + 정적 팩토리 `from(Entity)` 패턴 사용

### 서비스

- 쓰기 메서드: `@Transactional`
- 읽기 메서드: `@Transactional(readOnly = true)`
- 조회 실패 시 항상 `CustomException(ErrorCode.XXX_NOT_FOUND)` throw

### 함수형 스타일

```java
// 권장
repository.findById(id)
    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

// 금지
Optional<Member> opt = repository.findById(id);
if (opt.isEmpty()) throw ...;
```

## API 응답 규칙

모든 응답은 `ApiResponse<T>` wrapper 사용:

```json
{ "success": true,  "data": {...}, "message": null }
{ "success": false, "data": null,  "message": "에러 메시지" }
```

- 생성: `201 Created`
- 조회: `200 OK`
- 예외: `GlobalExceptionHandler`가 `ErrorCode`의 HTTP 상태로 변환

## 테스트 작성 원칙

- 테스트 메서드명은 한국어로 작성: `void 회원_가입_성공()`
- `@DisplayName`으로 한국어 설명 추가
- 통합 테스트(`@SpringBootTest`)는 `@Transactional`로 롤백 보장
- 컨트롤러 테스트는 `@WebMvcTest` 슬라이스 테스트만 사용
- `@MockBean`으로 서비스 모킹, `MockMvc`로 HTTP 요청/응답 검증
- `setField()` 헬퍼로 request DTO 필드를 리플렉션으로 설정 (setter 노출 방지)

## 브랜치 전략

- `main`: Step 1 기본 기능 (회원/주문/결제, 기본 할인)
- `feature/step2`: Step 2 고급 기능 (할인 이력, 결제 수단 추가 할인, 상태 관리)
