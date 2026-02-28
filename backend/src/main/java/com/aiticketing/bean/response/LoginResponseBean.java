package com.aiticketing.bean.response;

import io.swagger.v3.oas.annotations.media.Schema;

public class LoginResponseBean {

	@Schema(example = "1")
    public Long userId;

    @Schema(example = "sana@example.com")
    public String email;

    @Schema(example = "Sana")
    public String name;

    @Schema(example = "USER")
    public String role;

    @Schema(example = "accessToken")
    public String token;
	
    @Override
	public String toString() {
		return "LoginResponseBean [userId=" + userId + ", email=" + email + ", name=" + name + ", role=" + role
				+ ", token=" + token + "]";
	}
}
