package com.dotorich.tool.dto;

import com.dotorich.tool.domain.ToolType;
import com.fasterxml.jackson.annotation.JsonRawValue;

/** params 는 저장해 둔 JSON 그대로 내보낸다 */
public record ToolConditionResponse(ToolType toolType, @JsonRawValue String params) {
}
