package com.aiticketing.bean.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema
public class RegisterUserRequestBean {

	@Schema(example = "sana@example.com", description = "Email Id of the user")
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email cannot be empty")
    public String email;

    @Schema(example = "Sana Mansoori", description = "Users full name")
    @NotBlank
    @Size(max = 35)
    public String name;

    @Schema(example = "password123")
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 100)
    public String password;

	@Override
	public String toString() {
		return "RegisterUserRequestBean [email=" + email + ", name=" + name + ", password=" + password + "]";
	}
}
