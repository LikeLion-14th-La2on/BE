package com.dotorich.home.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * ponytail: Part 2 구현 전 임시값. FE 가 홈·마이페이지 연동을 먼저 시작할 수 있게 빈 값만 내려준다.
 * Part 2 가 LearningStatusReader 를 구현하면 이 파일을 삭제한다 (빈이 두 개면 서버가 안 뜬다).
 */
@Component
public class TempLearningStatusReader implements LearningStatusReader {

	@Override
	public Home read(Long userId) {
		return new Home(null, false, null, false, 0, 0, Collections.nCopies(7, false), 0, 0, 0, 0, false);
	}

	@Override
	public Stats stats(Long userId) {
		return new Stats(0, 0, List.of());
	}
}
