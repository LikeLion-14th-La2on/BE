# 도토리치 Backend

멋사 14기 2팀(라2온) 금융 학습 게임 **도토리치**의 백엔드 (Spring Boot)

## Tech Stack

| 항목 | 값 |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 4.0.7 |
| Build | Gradle (Wrapper) |
| DB | MySQL 8.4 |
| Docs | Springdoc OpenAPI (Swagger UI) |

## 로컬 실행

1. MySQL 실행 (Docker)
   ```
   docker compose up -d
   ```
2. 설정 파일 복사
   ```
   cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
   ```
3. 실행
   ```
   ./gradlew bootRun
   ```
4. 확인
   - 헬스 체크: http://localhost:8080/api/v1/health
   - Swagger: http://localhost:8080/swagger-ui.html

`application-dev.yml`, `.env`는 커밋하지 않는다. 새 설정 키가 생기면 `*.example`에도 추가한다.

## 패키지 구조

```
com.dotorich
 ├─ auth        # 카카오 로그인, 토큰
 ├─ user        # 온보딩, 마이페이지
 ├─ home        # 홈 조회
 ├─ reward      # 도토리 잔액·내역, 캐릭터, 배지, 스트릭 보호권
 ├─ learning    # 섹터, 단계, 학습 세션, 답안 제출
 ├─ streak      # 연속 학습, 도토리나무, 내 숲
 ├─ review      # 카드뉴스 복습, 오답노트
 ├─ leveltest   # 레벨 테스트
 ├─ question    # 문제 (학습, 레벨 테스트, 시사 퀴즈 공용)
 ├─ term        # 용어사전
 ├─ tutor       # AI 튜터
 ├─ newsquiz    # 시사 퀴즈
 └─ common      # 공통 응답, 예외, 설정, JWT, 유틸
```

각 도메인은 `controller / service / repository / domain / dto` 구조를 따른다. 빈 폴더의 `.gitkeep`은 파일을 추가하면 지워도 된다.

## 담당

| 파트 | 담당 | 패키지 |
| --- | --- | --- |
| Part 1 | 원종윤 | auth, user, home, reward |
| Part 2 | 정예원 | learning, streak, review |
| Part 3 | 김민지 | leveltest, question, term, tutor, newsquiz |
| 공통 | 전체 | common (수정 시 단체방 공유) |

파트 간 규칙
- 도토리 증감은 Part 1 `reward`의 함수만 호출한다. 다른 파트에서 잔액을 직접 수정하지 않는다.
- 단계 상태는 Part 2 `learning`에만 저장한다. 다른 파트는 읽기만 한다.
- 하루 기준은 KST 0시. 서버 시간대는 `Asia/Seoul`로 고정되어 있다.

## 파트 간에 호출하는 것

| 무엇 | 어디 | 누가 쓰나 |
| --- | --- | --- |
| 도토리 적립 `earn(userId, amount, reason, targetId)` | `reward.service.AcornService` | Part 2 하루 학습(+1 `DAILY_STUDY_REWARD`, DAILY_STUDY, 학습 날짜 yyyymmdd), 나무 완성(+5 `TREE_COMPLETE_BONUS`, TREE_COMPLETE, 나무 번호) |
| 도토리 사용 `spend(...)` 잔액 부족 시 `ACORN_NOT_ENOUGH` | `reward.service.AcornService` | Part 3 AI 튜터(AI_TUTOR, targetId=null), 스트릭 보호권(STREAK_SHIELD) |
| 끊김 차감 `penalizeStreakBreak(userId, yyyymmdd)` 잔액 0이면 차감 없음 | `reward.service.AcornService` | Part 2 접속 시 끊김 판정 |
| 배지 지급 `grant(userId, badgeId)` | `reward.service.BadgeService` | 보류 (6차 회의 이후 배지 미사용) |
| 레벨 테스트 완료 처리 `completeLevelTest(userId, DONE/SKIPPED)` 이미 했으면 `LEVEL_TEST_ALREADY_DONE`(409), NONE이면 `INVALID_INPUT`(400) | `user.service.UserService` | Part 3 결과 제출·건너뛰기 맨 처음 |
| 섹터·단계 조회 `getAllSectorIds()`, `getStageInfo(stageId)` → `StageInfo(stageId, sectorId, stepNo)` | `learning.service.StageReader` | Part 3 레벨 테스트 채점 |
| 홈·학습 현황에 들어갈 학습 데이터 | `home.service.LearningStatusReader` 구현 | Part 2 (구현 후 `TempLearningStatusReader` 삭제) |
| 회원탈퇴 시 데이터 삭제 | `user.domain.UserDeletedEvent`를 `@EventListener`로 받아 `deleteByUserId` | Part 2, Part 3 |

같은 (reason, targetId)는 한 번만 반영되고 두 번째부터는 `false`를 돌려준다. 하루 학습 보상을 중복 지급하지 않으려고 따로 확인할 필요는 없다.

로그인 사용자 id는 컨트롤러 파라미터에 `@LoginUser Long userId`로 받는다. 로그인 없이 부를 API는 `common.config.WebConfig`의 제외 목록에 추가한다.

## API 규칙

- 경로: `/api/v1/{기능}/...`
- 인증: `Authorization: Bearer {accessToken}`
- 응답: 모든 API는 `ApiResponse`로 감싼다.

```json
{
  "code": "S200",
  "message": "요청 성공",
  "data": { },
  "errors": null,
  "timestamp": "2026-11-05T10:00:00"
}
```

- 성공: `return ApiResponse.ok(data);`
- 실패: `throw new BusinessException(ErrorCode.NOT_FOUND);`
- 파트별 에러 코드는 `ErrorCode`의 파트 구역에 추가한다.
- 엔티티는 `BaseTimeEntity`를 상속하면 `created_at`, `updated_at`이 자동으로 채워진다.
- ERD: `docs/ERD_도토리치.sql`, API 명세: 노션 API 명세서

## 협업 규칙

**브랜치**
- `main`: 배포
- `develop`: 개발 통합. 기능 브랜치는 여기로 PR
- `feature/{기능}-{이름}` 예) `feature/kakao-login-jongyoon`

**커밋 메시지**

| prefix | 용도 |
| --- | --- |
| `feat:` | 기능 추가 |
| `fix:` | 버그 수정 |
| `refactor:` | 동작 변화 없는 코드 수정 |
| `docs:` | 문서 |
| `chore:` | 설정, 빌드 |

예) `feat: 카카오 로그인 API 추가`

**PR**
- PR 템플릿에 맞춰 작성하고, 다른 파트 1명 이상 리뷰 후 머지
- `git add .` 대신 바꾼 파일만 골라서 add
- 머지 전 `./gradlew build` 통과 확인
