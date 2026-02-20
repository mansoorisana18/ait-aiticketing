package com.aiticketing.bean.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema
public class LoginRequestBean {

	@Schema(example = "sana@example.com", description = "Email Id of the user")
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email cannot be empty")
    public String email;

    @Schema(example = "password123")
    @NotBlank(message = "Password cannot be empty")
    public String password;

	@Override
	public String toString() {
		return "LoginRequestBean [email=" + email + ", password=" + password + "]";
	}
	
}
