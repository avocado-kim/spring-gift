# Commit Strategy

> Angular 커밋 컨벤션(https://gist.github.com/stephenparish/9941e89d80e2bc58a153)을 기반으로
> 이 프로젝트의 task.md 단계와 TDD 루프에 맞게 정의한다.

---

## 메시지 형식

```
<type>(<scope>): <subject>

<body>

<footer>
```

- 모든 줄은 100자 이하
- `<type>(<scope>): <subject>` 한 줄은 필수, body/footer는 필요할 때만 작성

---

## Type

| type | 사용 시점 |
|---|---|
| `feat` | 새 기능 추가 (누락된 동작 구현 포함) |
| `fix` | 버그 수정 (잘못된 동작 교정) |
| `refactor` | 동작 변경 없는 코드 구조 변경 (서비스 추출, 중복 제거 등) |
| `test` | 테스트 추가 또는 수정 (프로덕션 코드 변경 없음) |
| `style` | 포맷, import 정리, `@Autowired` 제거 등 의미 변화 없는 정리 |
| `chore` | 빌드 설정, 의존성, Docker, 환경 설정 등 |
| `docs` | 문서 파일 수정 (spec.md, task.md, README 등) |

---

## Scope

이 프로젝트의 패키지/관심사를 기준으로 정의한다.

| scope | 대상 |
|---|---|
| `auth` | JwtProvider, AuthenticationResolver, KakaoAuthController, KakaoLoginClient |
| `member` | Member, MemberController, MemberService, AdminMemberController |
| `category` | Category, CategoryController |
| `product` | Product, ProductController, AdminProductController, ProductNameValidator |
| `option` | Option, OptionController, OptionNameValidator |
| `order` | Order, OrderController, OrderService, KakaoMessageClient |
| `wish` | Wish, WishController, WishService |
| `infra` | build.gradle.kts, docker-compose.yml, application.properties, Flyway SQL |
| `global` | 여러 패키지에 걸친 변경 (GlobalExceptionHandler, HandlerMethodArgumentResolver 등) |

---

## Subject 규칙

- 명령형 현재형 동사로 시작: `add`, `remove`, `extract`, `fix`, `move`
- 첫 글자 소문자
- 마침표 없음
- 무엇을 했는지가 아니라 무엇이 변하는지를 기술

```
// 나쁜 예
feat(order): OrderService를 추가했습니다
refactor(option): 리팩토링

// 좋은 예
feat(order): extract OrderService from OrderController
refactor(option): replace Collectors.toList() with toList()
```

---

## Body

변경 이유 또는 이전 동작과의 차이를 적는다.
단순한 구조 변경이나 스타일 정리는 생략해도 된다.

```
fix(order): wrap createOrder in @Transactional

재고 차감 후 포인트 부족 예외가 발생하면 재고만 줄어든 채 주문이
저장되지 않는 불일치가 발생한다. 트랜잭션으로 묶어 원자적으로 처리한다.
```

---

## Footer

### Breaking Change

호환성이 깨지는 변경은 footer에 명시한다.

```
BREAKING CHANGE: Order/Wish의 memberId 필드가 @ManyToOne Member로 변경됨.
기존에 memberId로 직접 쿼리하던 코드는 member.id로 수정 필요.
```

### 이슈/태스크 참조

task.md 항목과 연결할 때 footer에 명시한다.

```
Closes task: T1-1
Closes task: T4-1-a
```

---

## TDD 루프와 커밋 단위

task.md Phase 4의 작동 변경은 red → green → refactor 순으로 커밋을 분리한다.

```
// 1. red: 실패하는 테스트 먼저 커밋
test(order): add failing test for transactional rollback on point shortage

// 2. green: 테스트를 통과시키는 최소한의 코드
fix(order): wrap createOrder in @Transactional

// 3. refactor: 필요한 경우에만 (동작 유지 확인 후)
refactor(order): inline unnecessary variable in createOrder
```

Phase 3의 구조 변경(동작 유지)은 red/green 분리 없이 단일 커밋으로 처리한다.
단, 커밋 전 전체 테스트가 통과해야 한다.

---

## 커밋 단위 원칙

- 하나의 커밋은 하나의 이유로만 변경한다
- 스타일 정리와 동작 변경을 같은 커밋에 섞지 않는다
- 테스트와 그 테스트를 통과시키는 코드는 같은 커밋에 넣어도 되고 분리해도 된다 (green 커밋)
- 전체 테스트가 통과하지 않는 상태로 커밋하지 않는다

---

## Phase별 커밋 예시

### Phase 1 — 테스트 가능한 구조

```
refactor(auth): extract KakaoLoginPort interface from KakaoLoginClient
refactor(order): extract KakaoMessagePort interface from KakaoMessageClient
style(order): replace LocalDateTime.now() with @CreationTimestamp
test(member): add unit tests for Member point charge and deduction
test(option): add unit tests for Option subtractQuantity
test(product): add unit tests for ProductNameValidator
test(option): add unit tests for OptionNameValidator
```

### Phase 2 — 환경 독립성

```
chore(infra): add datasource environment variable bindings to application.properties
chore(infra): add H2 test application.properties with MySQL compatibility mode
chore(infra): move H2 dependency to testRuntimeOnly
chore(infra): add docker-compose.yml for local MySQL
```

### Phase 3 — 구조 변경

```
style(global): remove unnecessary @Autowired annotations
style(option): replace Collectors.toList() with toList()
refactor(global): consolidate @ExceptionHandler into GlobalExceptionHandler
refactor(global): extract shared ALLOWED_PATTERN constant from name validators
refactor(order): remove unused wishRepository dependency
refactor(member): extract MemberService from MemberController
refactor(wish): extract WishService from WishController
refactor(order): extract OrderService from OrderController
refactor(auth): register AuthenticationResolver as HandlerMethodArgumentResolver
```

### Phase 4 — 작동 변경

```
test(option): add failing test for subtractQuantity with zero amount
feat(option): reject zero or negative amount in subtractQuantity

test(order): add failing test for transactional rollback on point shortage
fix(order): wrap createOrder in @Transactional

test(order): add failing test for wish cleanup after order
feat(order): delete wish on order completion

test(wish): add failing test for duplicate wish returns 409
fix(wish): return 409 Conflict when wish already exists

test(product): add failing test for delete product with associated wishes
fix(product): return 400 when deleting product with associated wishes or orders

test(category): add failing test for delete category with associated products
fix(category): return 400 when deleting category with associated products

test(order): add failing test for kakao template with special characters in product name
fix(order): use ObjectMapper to build kakao message template JSON

test(global): add failing test for Order and Wish with Member entity reference
refactor(order): replace memberId primitive FK with @ManyToOne Member
refactor(wish): replace memberId primitive FK with @ManyToOne Member
```
