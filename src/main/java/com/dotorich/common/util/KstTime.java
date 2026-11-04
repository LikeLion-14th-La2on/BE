package com.dotorich.common.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/** DB 의 LocalDateTime(KST) 을 응답용 +09:00 시각으로 바꾼다. */
public final class KstTime {

	public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

	private KstTime() {
	}

	public static OffsetDateTime of(LocalDateTime time) {
		return time == null ? null : time.atZone(ZONE).toOffsetDateTime();
	}
}
