package com.dotorich.tool.domain;

import java.io.Serializable;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * saved_conditions (P1, 2순위). 사용자별·도구별 마지막 입력값 하나.
 * params 는 프론트가 보낸 JSON 을 검사 없이 그대로 저장한다 (계산은 프론트에서 하므로 서버는 보관만).
 */
@Getter
@Entity
@Table(name = "saved_conditions")
@IdClass(ToolCondition.Key.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ToolCondition {

	@Id
	@Column(name = "user_id")
	private Long userId;

	@Id
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "tool_type", length = 20)
	private ToolType toolType;

	@Column(nullable = false, columnDefinition = "json")
	private String params;

	@EqualsAndHashCode
	@NoArgsConstructor
	public static class Key implements Serializable {
		private Long userId;
		private ToolType toolType;
	}
}
