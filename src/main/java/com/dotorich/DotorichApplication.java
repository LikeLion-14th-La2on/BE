package com.dotorich;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DotorichApplication {

	public static void main(String[] args) {
		// 하루 기준은 KST 0시 (6차 회의 결정). 서버 기본 시간대를 고정한다.
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		SpringApplication.run(DotorichApplication.class, args);
	}
}
