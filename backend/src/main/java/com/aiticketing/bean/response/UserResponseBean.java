package com.aiticketing.bean.response;

import com.aiticketing.entity.enums.UserRole;

import io.swagger.v3.oas.annotations.media.Schema;

public class UserResponseBean {

	@Schema(example = "1")
    public Long userId;

    @Schema(example = "sana@example.com")
    public String email;

    @Schema(example = "Sana")
    public String name;

    @Schema(example = "USER")
    public UserRole role;
    
    @Schema(example = "TECHNICAL SUPPORT")
	public String department;

	@Override
	public String toString() {
		return "LoginResponseBean [userId=" + userId + ", email=" + email + ", name=" + name + ", role=" + role+ ", department=" +department+ "]";
	}
}
