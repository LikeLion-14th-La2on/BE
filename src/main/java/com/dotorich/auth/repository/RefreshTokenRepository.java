package com.dotorich.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.dotorich.auth.domain.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByToken(String token);

	void deleteAllByUserId(Long userId);

	/** 삭제 건수 반환. 같은 토큰으로 동시에 재발급하면 한 요청만 1을 받는다 */
	@Modifying
	@Query("delete from RefreshToken r where r.token = :token")
	int deleteByToken(String token);
}
