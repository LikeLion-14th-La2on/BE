-- 도토리치 ERD (MySQL) 2026-10-30
-- ERD Cloud: 가져오기(Import) > SQL에 붙여넣기
-- 담당: P1 = Part 1, P2 = Part 2, P3 = Part 3

-- ===== Part 1 =====
CREATE TABLE characters (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '캐릭터 id',
  name VARCHAR(50) NOT NULL COMMENT '캐릭터 이름',
  image_url VARCHAR(255) NOT NULL COMMENT '이미지',
  price INT NOT NULL COMMENT '도토리 가격',
  PRIMARY KEY (id)
) COMMENT = '캐릭터 (P1)';

CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '사용자 id',
  kakao_id VARCHAR(64) NULL COMMENT '카카오 회원번호',
  email VARCHAR(255) NULL COMMENT '이메일 (2순위)',
  password_hash VARCHAR(255) NULL COMMENT '이메일 로그인용 (2순위)',
  nickname VARCHAR(20) NULL COMMENT '닉네임',
  terms_agreed_at DATETIME NULL COMMENT '약관 동의 시각',
  level_test_status VARCHAR(10) NOT NULL DEFAULT 'NONE' COMMENT 'NONE/DONE/SKIPPED',
  acorn_balance INT NOT NULL DEFAULT 0 COMMENT '도토리 잔액',
  equipped_character_id BIGINT NULL COMMENT '장착 캐릭터',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입 시각',
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_kakao (kakao_id),
  UNIQUE KEY uk_users_email (email),
  UNIQUE KEY uk_users_nickname (nickname),
  FOREIGN KEY (equipped_character_id) REFERENCES characters (id)
) COMMENT = '사용자 (P1)';

CREATE TABLE refresh_tokens (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  token VARCHAR(512) NOT NULL COMMENT 'Refresh Token',
  expires_at DATETIME NOT NULL COMMENT '만료 시각 (14일, 사용 시 연장)',
  PRIMARY KEY (id),
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) COMMENT = 'Refresh Token (P1)';

CREATE TABLE acorn_ledger (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  amount INT NOT NULL COMMENT '+적립 / -차감',
  reason VARCHAR(20) NOT NULL COMMENT 'STAGE_COMPLETE/AI_TUTOR/CHARACTER/STREAK_SHIELD/TREE_HARVEST/STREAK_BREAK',
  target_id BIGINT NULL COMMENT '단계 완수=stage_id, 캐릭터=character_id, 수확=나무 번호, 연속 학습 끊김=끊긴 날짜(yyyymmdd), 튜터·보호권=NULL',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_acorn_once (user_id, reason, target_id),
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) COMMENT = '도토리 내역 (P1). 유니크로 단계 최초 완수 1회 지급 보장';

CREATE TABLE user_characters (
  user_id BIGINT NOT NULL,
  character_id BIGINT NOT NULL,
  purchased_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, character_id),
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  FOREIGN KEY (character_id) REFERENCES characters (id)
) COMMENT = '보유 캐릭터 (P1)';

CREATE TABLE badges (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  image_url VARCHAR(255) NULL,
  PRIMARY KEY (id)
) COMMENT = '배지 (P1)';

CREATE TABLE user_badges (
  user_id BIGINT NOT NULL,
  badge_id BIGINT NOT NULL,
  earned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, badge_id),
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  FOREIGN KEY (badge_id) REFERENCES badges (id)
) COMMENT = '받은 배지 (P1, 지급 호출은 P2)';

CREATE TABLE saved_conditions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  tool_type VARCHAR(20) NOT NULL COMMENT 'LOAN/COMPOUND/RATE_CHANGE',
  params JSON NOT NULL COMMENT '입력값',
  PRIMARY KEY (id),
  UNIQUE KEY uk_condition (user_id, tool_type),
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) COMMENT = '금융 도구 내 조건 (P1, 2순위)';

-- ===== Part 2 =====
CREATE TABLE sectors (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(30) NOT NULL COMMENT '주식/생활 금융/금융 기초',
  rotation_order INT NOT NULL COMMENT '요일 순환 순서',
  PRIMARY KEY (id)
) COMMENT = '섹터 (P2)';

CREATE TABLE stages (
  id BIGINT NOT NULL AUTO_INCREMENT,
  sector_id BIGINT NOT NULL,
  step_no INT NOT NULL COMMENT '단계 번호 1~3',
  title VARCHAR(50) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_stage (sector_id, step_no),
  FOREIGN KEY (sector_id) REFERENCES sectors (id)
) COMMENT = '단계 (P2)';

CREATE TABLE user_stage_progress (
  user_id BIGINT NOT NULL,
  stage_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL COMMENT 'UNLOCKED/PASSED_BY_TEST/COMPLETED, 행 없으면 잠김',
  completed_at DATETIME NULL,
  PRIMARY KEY (user_id, stage_id),
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  FOREIGN KEY (stage_id) REFERENCES stages (id)
) COMMENT = '단계 상태 (P2 단일 저장, 레벨 테스트 결과도 P2 함수로)';

CREATE TABLE user_streaks (
  user_id BIGINT NOT NULL,
  current_count INT NOT NULL DEFAULT 0 COMMENT '연속 학습일',
  last_study_date DATE NULL COMMENT '마지막 학습일 (KST)',
  shield_count INT NOT NULL DEFAULT 0 COMMENT '보유 보호권',
  PRIMARY KEY (user_id),
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) COMMENT = '스트릭 (P2)';

-- ===== Part 3 =====
CREATE TABLE terms (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL COMMENT '용어',
  definition TEXT NOT NULL COMMENT '뜻',
  easy_explanation TEXT NOT NULL COMMENT '쉬운 설명',
  example TEXT NULL COMMENT '예시',
  reference VARCHAR(100) NULL COMMENT '출처',
  PRIMARY KEY (id),
  UNIQUE KEY uk_term_name (name)
) COMMENT = '용어 (P3)';

CREATE TABLE questions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  stage_id BIGINT NULL COMMENT '시사 퀴즈는 NULL',
  purpose VARCHAR(20) NOT NULL COMMENT 'LEARNING/LEVEL_TEST/NEWS',
  type VARCHAR(20) NOT NULL COMMENT 'OX/MULTIPLE/MATCHING/SITUATION',
  content TEXT NOT NULL COMMENT '문제',
  choices JSON NULL COMMENT '보기',
  answer JSON NOT NULL COMMENT '정답',
  explanation TEXT NOT NULL COMMENT '해설',
  reference VARCHAR(100) NULL COMMENT '출처',
  PRIMARY KEY (id),
  FOREIGN KEY (stage_id) REFERENCES stages (id)
) COMMENT = '문제 (P3 시드 데이터, 학습·레벨 테스트·시사 퀴즈 공용)';

CREATE TABLE question_terms (
  question_id BIGINT NOT NULL,
  term_id BIGINT NOT NULL,
  PRIMARY KEY (question_id, term_id),
  FOREIGN KEY (question_id) REFERENCES questions (id),
  FOREIGN KEY (term_id) REFERENCES terms (id)
) COMMENT = '문제별 연결 용어 (P3)';

CREATE TABLE bookmarks (
  user_id BIGINT NOT NULL,
  term_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, term_id),
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  FOREIGN KEY (term_id) REFERENCES terms (id)
) COMMENT = '용어 북마크 (P3, 2순위)';

CREATE TABLE tutor_usage (
  user_id BIGINT NOT NULL,
  usage_date DATE NOT NULL COMMENT 'KST 날짜',
  count INT NOT NULL DEFAULT 0 COMMENT '그날 질문 수',
  PRIMARY KEY (user_id, usage_date),
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) COMMENT = 'AI 튜터 사용 횟수 (P3, 무료 횟수 판정)';

CREATE TABLE news_quizzes (
  id BIGINT NOT NULL AUTO_INCREMENT,
  question_id BIGINT NOT NULL,
  publish_date DATE NOT NULL COMMENT '게시일',
  PRIMARY KEY (id),
  UNIQUE KEY uk_news_date (publish_date),
  FOREIGN KEY (question_id) REFERENCES questions (id)
) COMMENT = '시사 퀴즈 (P3, 2순위)';

-- ===== Part 2 (문제 참조) =====
CREATE TABLE wrong_answers (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL COMMENT 'CARD_REVIEW(다음 날 복습 대기)/IN_NOTE(오답노트)/RESOLVED',
  wrong_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '주차별 조회 기준',
  PRIMARY KEY (id),
  UNIQUE KEY uk_wrong (user_id, question_id),
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  FOREIGN KEY (question_id) REFERENCES questions (id)
) COMMENT = '오답 (P2)';

CREATE TABLE question_reports (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  content TEXT NOT NULL COMMENT '신고 내용',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  FOREIGN KEY (question_id) REFERENCES questions (id)
) COMMENT = '문제 신고 (P2, 2순위)';
