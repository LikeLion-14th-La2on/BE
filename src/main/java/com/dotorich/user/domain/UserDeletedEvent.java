package com.dotorich.user.domain;

/**
 * 회원탈퇴 시 users 삭제 직전에 발행된다 (같은 트랜잭션).
 * 다른 파트는 @EventListener 로 받아 자기 테이블의 user_id 행을 지운다.
 * 예) @EventListener void on(UserDeletedEvent e) { repository.deleteByUserId(e.userId()); }
 */
public record UserDeletedEvent(Long userId) {
}
