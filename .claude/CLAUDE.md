# 우아한 유스방 과제5

## 관련 문서

@.claude/spec.md
@.claude/task.md
@.claude/commit-strategy.md

---

## 코드 작성 규칙

### 메서드

- 모든 public 메서드는 테스트 코드를 작성한다 (red → green → refactor)
- 중첩 깊이: 최대 2단계. private 메서드는 1단계만 허용, private 안에 private 금지
- 책임은 1개. 책임에 맞지 않는 메서드는 다른 객체로 분리한다

### 객체

- **Controller**: `@RestController` 기반. `@Service` 호출과 `ResponseEntity` 반환 외 코드 금지
- **Domain**: JPA 엔티티. 팩토리 메서드(`public static T create()`)로만 생성. 비즈니스 로직 전담
- **Service**: 메서드를 호출하는 facade 형태로만 사용. 판단 로직이 필요하면 개발자에게 먼저 확인

### 패키지 구조

```
src/main/java/gift/
  controller/
    dto/
  domain/
  service/
  repository/
  infrastructure/
  exception/
  config/
  support/
  Application.java
```

dto를 제외한 추가 패키지 생성 금지
