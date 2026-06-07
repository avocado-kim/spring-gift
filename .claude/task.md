# Refactoring Task List

> 단계는 순서대로 수행한다. 이전 단계가 완료되어야 다음 단계를 시작할 수 있다.
> 5번 TDD 루프는 3단계부터 모든 변경에 적용되는 프로세스 원칙이다.

---

## Phase 1 — 모든 환경에서 동일한 실행 보장 (테스트 가능한 시스템)

테스트를 작성하기 전에 시스템이 테스트 가능한 구조를 갖추어야 한다.
외부 의존성과 시간에 묶인 코드를 격리 가능한 구조로 바꾼다.

### 1-1. `KakaoLoginClient` / `KakaoMessageClient` — 인터페이스 추출
- **파일**: `gift/auth/KakaoLoginClient.java`, `gift/order/KakaoMessageClient.java`
- **현재**: 구체 클래스를 직접 주입받으므로 테스트에서 카카오 서버 없이 실행할 수 없다.
- **수행**: 각각 인터페이스(`KakaoLoginPort`, `KakaoMessagePort`)를 추출하고 기존 구현체가 implement하도록 변경한다. 호출하는 쪽(`KakaoAuthController`, `OrderController`)은 인터페이스에만 의존하게 변경한다.

### 1-2. `Order.orderDateTime` — 생성자 내 `LocalDateTime.now()` 제거
- **파일**: `gift/order/Order.java:38`
- **현재**: 생성자에서 `LocalDateTime.now()`를 직접 호출하므로 테스트에서 주문 시간을 고정할 수 없다.
- **수행**: `@CreationTimestamp` 어노테이션으로 대체해 JPA에 시간 설정을 위임한다.

### 1-3. 도메인 단위 테스트 초기 작성
- **대상**: 현재 의존성 없이 테스트 가능한 순수 도메인 로직
  - `Member.chargePoint()`, `Member.deductPoint()`
  - `Option.subtractQuantity()`
  - `ProductNameValidator.validate()`
  - `OptionNameValidator.validate()`
- **목적**: 이후 구조 변경 시 동작이 깨지지 않았음을 증명하는 최소 안전망을 확보한다.

---

## Phase 2 — 개인 로컬 설정에 의존하지 않는 반복 실행 가능한 테스트 환경

로컬 MySQL 없이도 테스트가 실행되어야 한다.

### 2-1. `application.properties` — MySQL datasource 환경변수 바인딩 추가
- **파일**: `src/main/resources/application.properties`
- **현재**: datasource 설정이 없어 애플리케이션이 시작되지 않는다.
- **수행**: 환경변수 기본값을 포함한 설정을 추가한다.
  ```properties
  spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/spring_gift}
  spring.datasource.username=${DB_USERNAME:root}
  spring.datasource.password=${DB_PASSWORD:}
  spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
  ```

### 2-2. 테스트 전용 `application.properties` 작성
- **파일**: `src/test/resources/application.properties` (신규)
- **현재**: 테스트 프로파일이 없어 테스트 실행 시 MySQL 연결을 시도한다.
- **수행**: H2 in-memory DB를 사용하는 테스트 전용 설정을 추가한다.
  ```properties
  spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1
  spring.datasource.driver-class-name=org.h2.Driver
  spring.flyway.enabled=true
  ```

### 2-3. `build.gradle.kts` — H2 의존성 스코프 변경
- **파일**: `build.gradle.kts:38`
- **현재**: `runtimeOnly("com.h2database:h2")`로 선언되어 있어 테스트 전용 의존성임이 불명확하다.
- **수행**: `testRuntimeOnly("com.h2database:h2")`로 변경한다.

### 2-4. `docker-compose.yml` 추가
- **현재**: MySQL을 수동으로 설치하고 DB를 생성해야 한다. 신규 환경에서 재현이 불가능하다.
- **수행**: MySQL 컨테이너를 포함하는 `docker-compose.yml`을 프로젝트 루트에 추가한다. 컨테이너 포트와 환경변수는 `application.properties` 기본값과 일치시킨다.

---

## Phase 3 — 구조 변경 (작동 변경 없음)

Phase 1~2 완료 후 테스트가 통과하는 상태에서 시작한다.
각 변경 후 전체 테스트가 통과해야 다음으로 넘어간다.

### 3-1. 스타일 정리

#### 3-1-a. `@Autowired` 제거
- **파일**: `AuthenticationResolver.java:20`, `JwtProvider.java:24`, `MemberController.java:28`, `AdminMemberController.java:24`
- 단일 생성자에 `@Autowired`는 불필요하다. 제거한다.

#### 3-1-b. `Collectors.toList()` → `.toList()`
- **파일**: `gift/option/OptionController.java:43`
- Java 16+에서 `.toList()`를 사용할 수 있다. 동일 코드베이스 내 다른 스트림과 일관성을 맞춘다.

### 3-2. 불필요한 코드 제거

#### 3-2-a. `@ExceptionHandler` 중복 → `@RestControllerAdvice` 통합
- **파일**: `OptionController.java:100`, `ProductController.java:97`, `MemberController.java:57`
- 동일한 `handleIllegalArgument` 메서드가 세 컨트롤러에 중복 정의되어 있다.
- `GlobalExceptionHandler`를 `@RestControllerAdvice`로 만들어 통합하고 각 컨트롤러에서 제거한다.

#### 3-2-b. `ProductNameValidator` / `OptionNameValidator` — 정규식 상수 중복 제거
- **파일**: `ProductNameValidator.java:9`, `OptionNameValidator.java:15`
- `ALLOWED_PATTERN`이 두 클래스에 동일하게 정의되어 있다.
- 공통 상수를 `NameAllowedPattern` 공유 클래스로 추출하고 양쪽에서 참조한다.

#### 3-2-c. `OrderController` 미사용 `wishRepository` 제거
- **파일**: `gift/order/OrderController.java:28,42`
- `wishRepository`가 생성자에 주입되지만 코드에서 사용되지 않는다. 주석의 step 6("cleanup wish")은 Phase 4에서 구현한다.
- 지금은 dead dependency를 생성자와 필드에서 제거하고 주석도 함께 정리한다.

### 3-3. 서비스 계층 추출

Controller가 Repository를 직접 의존하고 유효성 검증, 도메인 로직, 외부 API 호출까지 담당한다.
Service를 추출해 Controller는 HTTP 변환만, Service는 비즈니스 로직만 담당하게 분리한다.
이 단계는 동작을 바꾸지 않는다. Controller가 하던 일을 Service로 옮기는 것이다.

#### 3-3-a. `MemberService` 추출
- **대상**: `MemberController`의 register, login 로직

#### 3-3-b. `WishService` 추출
- **대상**: `WishController`의 조회, 추가, 삭제 로직

#### 3-3-c. `OrderService` 추출
- **대상**: `OrderController`의 주문 생성 플로우 전체

#### 3-3-d. `CategoryService` 추출
- **대상**: `CategoryController`의 생성, 수정, 삭제 로직
- **누락 이유 기록**: 최초 작성 시 CRUD가 단순하다는 이유로 암묵적으로 제외했으나, "Controller가 Repository를 직접 의존하며 비즈니스 로직을 담는가"라는 적용 기준에는 동일하게 해당한다.

#### 3-3-e. `ProductService` 추출
- **대상**: `ProductController`의 이름 검증, 카테고리 존재 확인, FK 체크, 생성/수정/삭제 로직

#### 3-3-f. `OptionService` 추출
- **대상**: `OptionController`의 이름 검증, 중복 체크, 최소 옵션 수 체크, 생성/삭제 로직

#### 3-3-g. `AuthenticationResolver` → `HandlerMethodArgumentResolver` 통합
- **파일**: `gift/auth/AuthenticationResolver.java`
- **현재**: 컨트롤러에서 `@RequestHeader("Authorization")`를 받고 null 체크를 반복한다 (`WishController`, `OrderController`).
- **수행**: `HandlerMethodArgumentResolver`를 구현하고 `WebMvcConfigurer`에 등록해 컨트롤러 파라미터에서 `Member`를 직접 받도록 변경한다. null 반환 대신 인증 실패 시 예외를 발생시킨다.

---

## Phase 4 — 작동 변경 (TDD, 결과를 테스트로 증명)

Phase 3 완료 후 테스트가 통과하는 상태에서 시작한다.
각 항목은 red → green 순서로 진행한다. 테스트 없이 코드를 변경하지 않는다.

### 4-1. 트랜잭션 경계 세우기

#### 4-1-a. `OrderService.createOrder()` — `@Transactional` 추가
- **파일**: Phase 3에서 추출된 `OrderService`
- **현재 문제**: 재고 차감 → 포인트 차감 → 주문 저장이 별도 `save()` 호출로 분리되어 있고 `@Transactional`이 없다. 포인트 부족 예외 발생 시 재고만 줄어든 채 주문이 저장되지 않아 DB 불일치가 발생한다.
- **red**: 포인트 잔액보다 비싼 상품을 주문하면 재고가 그대로인지 확인하는 테스트 → 현재는 재고만 줄어든 채 실패한다.
- **green**: `@Transactional` 추가.

### 4-2. 누락된 작동 구현

#### 4-2-a. `Option.subtractQuantity()` — 0 이하 입력 거부
- **파일**: `gift/option/Option.java:39`
- **red**: `subtractQuantity(0)`, `subtractQuantity(-1)` 호출 시 예외가 발생하지 않는다는 테스트.
- **green**: `amount <= 0` 일 때 `IllegalArgumentException` 발생.

#### 4-2-b. `OrderService.createOrder()` — 주문 완료 후 위시리스트 삭제
- **파일**: Phase 3에서 추출된 `OrderService`
- **현재 문제**: 주문 완료 후 해당 상품의 위시리스트가 삭제되지 않는다. (Phase 3-2-c에서 dead dependency만 제거했으므로 구현이 없는 상태)
- **red**: 위시에 등록된 상품을 주문한 뒤 해당 위시가 사라지는지 확인하는 테스트 → 현재는 남아있다.
- **green**: 주문 저장 후 `WishRepository`로 해당 멤버+상품 위시를 삭제.

#### 4-2-c. `WishService.addWish()` — 중복 추가 시 409 반환
- **파일**: Phase 3에서 추출된 `WishService`
- **현재 문제**: 이미 존재하는 위시를 POST하면 200을 반환한다. 클라이언트가 생성과 조회를 구분할 수 없다.
- **red**: 같은 상품을 두 번 위시 추가하면 409를 반환하는 테스트.
- **green**: `wishRepository.findByMemberIdAndProductId` 결과가 존재하면 409 예외 또는 응답 반환.

#### 4-2-d. 상품 삭제 시 연관 데이터 존재 여부 검증
- **파일**: `gift/product/ProductController.java:85`
- **현재 문제**: 위시리스트나 주문이 있는 상품을 삭제하면 FK 제약으로 DB 에러가 발생한다.
- **red**: 위시/주문이 연결된 상품 삭제 요청이 400을 반환하는 테스트.
- **green**: 삭제 전 연관 위시/주문 존재 여부를 확인해 있으면 `400 Bad Request` 반환.

#### 4-2-e. 카테고리 삭제 시 연관 상품 존재 여부 검증
- **파일**: `gift/category/CategoryController.java:57`
- **현재 문제**: 상품이 연결된 카테고리를 삭제하면 FK 제약으로 DB 에러가 발생한다.
- **red**: 상품이 있는 카테고리 삭제 요청이 400을 반환하는 테스트.
- **green**: 삭제 전 연관 상품 존재 여부를 확인해 있으면 `400 Bad Request` 반환.

### 4-3. 도메인 책임 되찾기

#### 4-3-a. `KakaoMessageClient.buildTemplate()` — Jackson으로 JSON 생성
- **파일**: `gift/order/KakaoMessageClient.java:31`
- **현재 문제**: JSON을 텍스트 블록과 `\\n` 이스케이프로 직접 구성한다. 상품명에 따옴표 등 특수문자가 포함되면 JSON이 깨진다.
- **red**: 상품명에 `"`가 포함된 주문 알림이 유효한 JSON인지 확인하는 테스트.
- **green**: `ObjectMapper`로 DTO를 직렬화해 JSON을 생성.

#### 4-3-b. `Order` / `Wish` — memberId primitive FK → `@ManyToOne Member` 통일
- **파일**: `gift/order/Order.java:25`, `gift/wish/Wish.java:16`
- **현재 문제**: `Option`은 `Product`를 `@ManyToOne` entity 참조로 가지는데 `Order`와 `Wish`는 `member`를 `Long memberId`로 저장한다. 설계가 일관되지 않고 연관 조회 시 추가 쿼리가 발생한다.
- **수행**: `Long memberId` 필드를 `@ManyToOne Member member`로 변경하고, 참조하는 쪽을 모두 수정한다. DB 스키마는 이미 `member_id` FK로 되어 있으므로 매핑만 변경된다.

---

## Phase 5 — TDD 루프 (3단계부터 모든 변경에 적용)

> 이 단계는 별도 작업이 아니라 Phase 3, 4 전체에 걸쳐 유지하는 프로세스다.

- **구조 변경(Phase 3)**: 변경 전 테스트가 통과하는 상태에서 시작 → 변경 후 전체 테스트 통과 확인 → 통과하면 다음으로
- **작동 변경(Phase 4)**: red(실패하는 테스트 작성) → green(최소한의 코드로 통과) → refactor(정리) 순서를 반드시 지킨다
- **최소 기준**: 각 태스크 완료 후 `./gradlew test`가 전체 통과해야 커밋한다
