# Specification

> 현재 코드를 기준으로 작성한다.
> 의도와 실제 동작이 다른 경우 `[구현 불일치]`로 표시한다.

---

## 1. API 기능 목록

### 인증 (Auth)

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| POST | `/api/members/register` | 불필요 | 이메일/비밀번호로 회원가입, JWT 토큰 반환 |
| POST | `/api/members/login` | 불필요 | 이메일/비밀번호 검증 후 JWT 토큰 반환 |
| GET | `/api/auth/kakao/login` | 불필요 | 카카오 인가 페이지로 302 리다이렉트 |
| GET | `/api/auth/kakao/callback` | 불필요 | 카카오 인가 코드를 토큰으로 교환, 미가입 시 자동 가입, JWT 반환 |

**규칙**
- 이미 가입된 이메일로 register 시 400
- 비밀번호 불일치 시 400
- 카카오 로그인 성공 시 카카오 액세스 토큰을 멤버에 저장

---

### 카테고리 (Category)

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| GET | `/api/categories` | 불필요 | 전체 카테고리 목록 조회 |
| POST | `/api/categories` | 불필요 | 카테고리 생성, 생성된 리소스 URI 반환 |
| PUT | `/api/categories/{id}` | 불필요 | 카테고리 수정 |
| DELETE | `/api/categories/{id}` | 불필요 | 카테고리 삭제 |

**규칙**
- name, color, imageUrl은 필수
- `[구현 불일치]` 상품이 연결된 카테고리를 삭제하면 FK 제약으로 500 에러가 발생한다. 400을 반환해야 한다.

---

### 상품 (Product)

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| GET | `/api/products` | 불필요 | 상품 목록 조회 (페이징) |
| GET | `/api/products/{id}` | 불필요 | 상품 단건 조회 |
| POST | `/api/products` | 불필요 | 상품 생성 |
| PUT | `/api/products/{id}` | 불필요 | 상품 수정 |
| DELETE | `/api/products/{id}` | 불필요 | 상품 삭제 |

**규칙**
- 상품 이름: 공백 포함 최대 15자
- 상품 이름: 허용 문자만 사용 가능 — 한글, 영문, 숫자, `( ) [ ] + - & / _`
- 상품 이름에 `카카오` 포함 불가 (관리자 화면은 허용)
- categoryId가 존재하지 않으면 404
- `[구현 불일치]` 위시리스트나 주문이 연결된 상품을 삭제하면 FK 제약으로 500 에러가 발생한다. 400을 반환해야 한다.

---

### 옵션 (Option)

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| GET | `/api/products/{productId}/options` | 불필요 | 상품의 옵션 목록 조회 |
| POST | `/api/products/{productId}/options` | 불필요 | 상품에 옵션 추가 |
| DELETE | `/api/products/{productId}/options/{optionId}` | 불필요 | 옵션 삭제 |

**규칙**
- productId가 존재하지 않으면 404
- 옵션 이름: 공백 포함 최대 50자
- 옵션 이름: 허용 문자만 사용 가능 — 한글, 영문, 숫자, `( ) [ ] + - & / _`
- 같은 상품에 동일한 이름의 옵션 중복 불가 (400)
- 상품당 옵션이 1개인 경우 삭제 불가 (400)
- optionId가 해당 productId에 속하지 않으면 404

---

### 주문 (Order)

> 모든 엔드포인트에 `Authorization: Bearer {token}` 헤더 필요. 없거나 유효하지 않으면 401.

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| GET | `/api/orders` | 필요 | 내 주문 목록 조회 (페이징) |
| POST | `/api/orders` | 필요 | 주문 생성 |

**주문 생성 플로우**
1. 인증 확인
2. optionId 존재 확인 (없으면 404)
3. 재고 차감 (`option.subtractQuantity`)
4. 포인트 차감 (`상품 가격 × 수량`)
5. 주문 저장
6. `[구현 불일치]` 위시리스트 삭제 — 코드에 주석으로 명시되어 있으나 구현이 없다
7. 카카오 메시지 발송 (best-effort, 카카오 토큰 없으면 skip, 실패해도 주문은 성공)

**규칙**
- 재고보다 많은 수량 주문 불가
- 포인트가 부족하면 주문 불가
- `[구현 불일치]` `@Transactional`이 없어 포인트 부족 시 재고만 차감된 채 주문이 실패한다

---

### 위시리스트 (Wish)

> 모든 엔드포인트에 `Authorization: Bearer {token}` 헤더 필요. 없거나 유효하지 않으면 401.

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| GET | `/api/wishes` | 필요 | 내 위시리스트 조회 (페이징) |
| POST | `/api/wishes` | 필요 | 위시 추가 |
| DELETE | `/api/wishes/{id}` | 필요 | 위시 삭제 |

**규칙**
- productId가 존재하지 않으면 404
- `[구현 불일치]` 이미 추가된 상품을 POST하면 200을 반환한다. 409를 반환해야 한다.
- 본인 위시가 아닌 항목 삭제 시 403

---

### 관리자 화면 (Admin — Thymeleaf)

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/admin/members` | 회원 목록 |
| GET | `/admin/members/new` | 회원 추가 폼 |
| POST | `/admin/members` | 회원 생성 |
| GET | `/admin/members/{id}/edit` | 회원 수정 폼 |
| POST | `/admin/members/{id}/edit` | 회원 수정 |
| POST | `/admin/members/{id}/charge-point` | 포인트 충전 |
| POST | `/admin/members/{id}/delete` | 회원 삭제 |
| GET | `/admin/products` | 상품 목록 |
| GET | `/admin/products/new` | 상품 추가 폼 |
| POST | `/admin/products` | 상품 생성 (`카카오` 이름 허용) |
| GET | `/admin/products/{id}/edit` | 상품 수정 폼 |
| POST | `/admin/products/{id}/edit` | 상품 수정 (`카카오` 이름 허용) |
| POST | `/admin/products/{id}/delete` | 상품 삭제 |

---

## 2. 엔티티 책임 목록

### `Member`

| 책임 | 메서드/필드 | 내용 |
|---|---|---|
| 신원 보유 | `email`, `password` | 이메일은 고유, 비밀번호는 평문 저장 |
| 카카오 연동 정보 보유 | `kakaoAccessToken` | 카카오 로그인 시 발급된 액세스 토큰 저장 |
| 포인트 충전 | `chargePoint(amount)` | amount > 0 이어야 함, 위반 시 예외 |
| 포인트 차감 | `deductPoint(amount)` | amount > 0 이어야 하고, 잔액 이상이어야 함, 위반 시 예외 |
| 정보 수정 | `update(email, password)` | 이메일, 비밀번호 변경 |
| 카카오 토큰 갱신 | `updateKakaoAccessToken(token)` | 카카오 로그인마다 최신 토큰으로 덮어씀 |

---

### `Category`

| 책임 | 메서드/필드 | 내용 |
|---|---|---|
| 분류 정보 보유 | `name`, `color`, `imageUrl`, `description` | name은 고유 |
| 수정 | `update(name, color, imageUrl, description)` | 전체 필드 일괄 수정 |

---

### `Product`

| 책임 | 메서드/필드 | 내용 |
|---|---|---|
| 상품 정보 보유 | `name`, `price`, `imageUrl`, `category` | |
| 옵션 목록 소유 | `options` (`OneToMany`, `cascade ALL`) | 옵션 생명주기를 상품이 관리 |
| 수정 | `update(name, price, imageUrl, category)` | 전체 필드 일괄 수정 |

**이름 검증 규칙** (`ProductNameValidator` — 현재 별도 유틸리티)
- 공백 포함 최대 15자
- 허용 문자: 한글, 영문, 숫자, `( ) [ ] + - & / _`
- `카카오` 포함 불가 (허용 플래그로 override 가능)

---

### `Option`

| 책임 | 메서드/필드 | 내용 |
|---|---|---|
| 옵션 정보 보유 | `name`, `quantity`, `product` | |
| 재고 차감 | `subtractQuantity(amount)` | 재고보다 많은 양 차감 시 예외. `[구현 불일치]` amount ≤ 0 검증 없음 |

**이름 검증 규칙** (`OptionNameValidator` — 현재 별도 유틸리티)
- 공백 포함 최대 50자
- 허용 문자: 한글, 영문, 숫자, `( ) [ ] + - & / _`

---

### `Order`

| 책임 | 메서드/필드 | 내용 |
|---|---|---|
| 주문 기록 보유 | `option`, `memberId`, `quantity`, `message`, `orderDateTime` | |
| 주문 시각 설정 | 생성자 내 `LocalDateTime.now()` | `[구현 불일치]` 생성자 직접 호출로 테스트에서 시간 고정 불가 |

> `memberId`는 `Long` primitive FK로 저장 (`Member` 엔티티 참조 없음).
> `option`은 `@ManyToOne` 엔티티 참조. 같은 코드베이스 내 설계 불일치.

---

### `Wish`

| 책임 | 메서드/필드 | 내용 |
|---|---|---|
| 위시 항목 보유 | `memberId`, `product` | |

> `memberId`는 `Long` primitive FK (`Order`와 동일 패턴).
> `product`는 `@ManyToOne` 엔티티 참조.
