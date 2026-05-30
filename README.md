# spring-gift

선물 거래 플랫폼 백엔드 API 서버입니다.

---

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| ORM | Spring Data JPA / Hibernate |
| DB | MySQL (운영), H2 (테스트) |
| Auth | JWT, Kakao OAuth2 |
| Migration | Flyway |
| Build | Gradle |

---

## 시작하기

### 요구사항

- Java 21
- Docker (MySQL 컨테이너용)

### 실행

```bash
# MySQL 컨테이너 시작
docker-compose up -d

# 애플리케이션 실행
./gradlew bootRun
```

### 환경변수

| 변수 | 기본값 | 설명 |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/spring_gift` | DB 접속 URL |
| `DB_USERNAME` | `root` | DB 사용자명 |
| `DB_PASSWORD` | (없음) | DB 비밀번호 |
| `JWT_SECRET` | `a-string-secret-at-least-256-bits-long` | JWT 서명 키 |
| `JWT_EXPIRATION` | `3600000` | JWT 만료 시간 (ms) |
| `KAKAO_CLIENT_ID` | (없음) | 카카오 앱 클라이언트 ID |
| `KAKAO_CLIENT_SECRET` | (없음) | 카카오 앱 클라이언트 시크릿 |
| `KAKAO_REDIRECT_URI` | `http://localhost:8080/api/auth/kakao/callback` | 카카오 OAuth2 콜백 URI |

### 테스트

```bash
./gradlew test
```

테스트는 H2 in-memory DB를 사용하므로 별도 MySQL 설정 없이 실행됩니다.

---

## API

### 인증 (Auth)

| 메서드 | 경로 | 설명 |
|---|---|---|
| POST | `/api/members/register` | 회원가입 |
| POST | `/api/members/login` | 로그인 |
| GET | `/api/auth/kakao/login` | 카카오 로그인 |
| GET | `/api/auth/kakao/callback` | 카카오 OAuth2 콜백 |

### 카테고리 (Category)

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/api/categories` | 카테고리 목록 조회 |
| POST | `/api/categories` | 카테고리 생성 |
| PUT | `/api/categories/{id}` | 카테고리 수정 |
| DELETE | `/api/categories/{id}` | 카테고리 삭제 |

### 상품 (Product)

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/api/products` | 상품 목록 조회 (페이징) |
| GET | `/api/products/{id}` | 상품 단건 조회 |
| POST | `/api/products` | 상품 생성 |
| PUT | `/api/products/{id}` | 상품 수정 |
| DELETE | `/api/products/{id}` | 상품 삭제 |

### 옵션 (Option)

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/api/products/{productId}/options` | 상품 옵션 목록 조회 |
| POST | `/api/products/{productId}/options` | 옵션 추가 |
| DELETE | `/api/products/{productId}/options/{optionId}` | 옵션 삭제 |

### 주문 (Order)

> `Authorization: Bearer {token}` 헤더 필요

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/api/orders` | 내 주문 목록 조회 (페이징) |
| POST | `/api/orders` | 주문 생성 |

### 위시리스트 (Wish)

> `Authorization: Bearer {token}` 헤더 필요

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/api/wishes` | 위시리스트 조회 (페이징) |
| POST | `/api/wishes` | 위시 추가 |
| DELETE | `/api/wishes/{id}` | 위시 삭제 |

---

## 패키지 구조

```
gift/
├── auth/
│   ├── controller/     카카오 OAuth2 컨트롤러
│   ├── dto/            응답 DTO (TokenResponse)
│   ├── jwt/            JWT 토큰 발급·검증
│   ├── client/         카카오 API 클라이언트
│   └── resolver/       Spring MVC 인증 리졸버
├── category/
│   ├── controller/
│   ├── domain/
│   ├── dto/
│   ├── repository/
│   └── service/
├── member/
│   ├── controller/     REST API + 관리자 화면
│   ├── domain/
│   ├── dto/
│   ├── repository/
│   └── service/
├── option/
│   ├── controller/
│   ├── domain/         엔티티 + 이름 검증
│   ├── dto/
│   ├── repository/
│   └── service/
├── order/
│   ├── controller/
│   ├── domain/
│   ├── dto/
│   ├── repository/
│   └── service/        주문 서비스 + 카카오 메시지 클라이언트
├── product/
│   ├── controller/     REST API + 관리자 화면
│   ├── domain/         엔티티 + 이름 검증
│   ├── dto/
│   ├── repository/
│   └── service/
├── wish/
│   ├── controller/
│   ├── domain/
│   ├── dto/
│   ├── repository/
│   └── service/
└── global/             공통 예외, 핸들러, MVC 설정
```
