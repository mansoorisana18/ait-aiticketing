package com.aiticketing.bean.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PromoteToAgentRequestBean {
	
	@NotBlank(message = "Department is required")
	@Size(max = 80)
	public String department;

	@Override
	public String toString() {
		return "PromoteToAgentRequestBean [department=" + department + "]";
	}

}
