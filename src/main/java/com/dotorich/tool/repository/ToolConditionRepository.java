package com.dotorich.tool.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.dotorich.tool.domain.ToolCondition;
import com.dotorich.tool.domain.ToolType;

public interface ToolConditionRepository extends JpaRepository<ToolCondition, ToolCondition.Key> {

	Optional<ToolCondition> findByUserIdAndToolType(Long userId, ToolType toolType);

	/** 없으면 넣고 있으면 덮어쓴다. 한 문장이라 같은 저장이 동시에 와도 중복 키 오류가 나지 않는다. MySQL 전용 */
	@Modifying
	@Query(value = """
		insert into saved_conditions (user_id, tool_type, params) values (:userId, :toolType, :params) as new
		on duplicate key update params = new.params
		""", nativeQuery = true)
	int upsert(Long userId, String toolType, String params);

	@Modifying
	@Query("delete from ToolCondition c where c.userId = :userId")
	int deleteAllByUserId(Long userId);
}
