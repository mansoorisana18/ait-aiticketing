package com.aiticketing.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aiticketing.bean.request.LoginRequestBean;
import com.aiticketing.bean.request.PromoteToAgentRequestBean;
import com.aiticketing.bean.request.RegisterUserRequestBean;
import com.aiticketing.bean.response.ApiResponseBean;
import com.aiticketing.bean.response.LoginResponseBean;
import com.aiticketing.bean.response.UserResponseBean;
import com.aiticketing.security.AuthUserPrincipal;
import com.aiticketing.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
	
	@Autowired
	UserService userService;
	
	private static final Logger USER_CONTROLLER_LOG = LoggerFactory.getLogger(UserController.class);
	
	/**
	 * AUTH ENDPOINTS
	 */
	
	@Operation(summary = "Login user", description = "Returns access JWT + sets HttpOnly refresh cookie", security = @SecurityRequirement(name = ""))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
	@PostMapping(value= "/login")
	public ResponseEntity<ApiResponseBean<LoginResponseBean>> loginUser(@Valid @RequestBody LoginRequestBean loginRequest, HttpServletResponse response){
		USER_CONTROLLER_LOG.info("UserController :: in loginUser()");
		LoginResponseBean loginUserResp = userService.loginUser(loginRequest, response);
		USER_CONTROLLER_LOG.info("UserController :: exit loginUser()");
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponseBean.success(loginUserResp));
	}
	
	@Operation(summary = "Logout user", description = "Revokes refresh token and clears cookie")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseBean<Object>> logout(
            @AuthenticationPrincipal AuthUserPrincipal principal,
            HttpServletResponse response) {
        USER_CONTROLLER_LOG.info("UserController :: in logout()");
        userService.logout(principal.getUserId(), response);
        USER_CONTROLLER_LOG.info("UserController :: exit logout()");
        return ResponseEntity.ok(ApiResponseBean.success("Logged out", null));
    }
	
	@Operation(summary = "Refresh access token", description = "Uses HttpOnly refresh cookie to issue new access token", security = @SecurityRequirement(name = ""))
    @PostMapping("/refresh")
	@ApiResponses({
        @ApiResponse(responseCode = "200", description = "New access token in response"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
	})
    public ResponseEntity<ApiResponseBean<LoginResponseBean>> refreshAccessToken(
            @CookieValue(name = "refresh_token", required = false) String refreshTokenCookie) {
        USER_CONTROLLER_LOG.info("UserController :: in refreshAccessToken()");
        LoginResponseBean resp = userService.refreshAccessToken(refreshTokenCookie);
        USER_CONTROLLER_LOG.info("UserController :: exit refreshAccessToken()");
        return ResponseEntity.ok(ApiResponseBean.success(resp));
    }
	
	/**
	 * USER MANAGEMENT ENDPOINTS
	 */
	
	@Operation(summary = "Register user", description = "Creates a new user account", security = @SecurityRequirement(name = ""))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registered successfully"),
            @ApiResponse(responseCode = "409", description = "User already exists"),
            @ApiResponse(responseCode = "400", description = "Validation error / email already registered")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponseBean<LoginResponseBean>> registerUser(@Valid @RequestBody RegisterUserRequestBean registerUserReq) {
		USER_CONTROLLER_LOG.info("UserController :: in registerUser()");
		LoginResponseBean registerUserResp = userService.registerUser(registerUserReq);
		USER_CONTROLLER_LOG.info("UserController :: exit registerUser()");
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseBean.success("Registered successfully", registerUserResp));
    }
	
	@Operation(summary = "Get users", description = "Fetches all users & agents for admin")
	@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Provides all users list"),
        @ApiResponse(responseCode = "401", description = "Unauthorized/ Invalid or expired token"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
	})
	@GetMapping("/admin")
	public ResponseEntity<ApiResponseBean<List<UserResponseBean>>> getUsersForAdmin(){
		USER_CONTROLLER_LOG.info("UserController :: in getUsersForAdmin()");
		List<UserResponseBean> getUsersResp = userService.getUsersForAdmin();
		USER_CONTROLLER_LOG.info("UserController :: exit getUsersForAdmin()");
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponseBean.success(getUsersResp));
	}
	
	@Operation(summary = "Make Agent", description = "Updates role to an AGENT with department")
	@ApiResponses({
        @ApiResponse(responseCode = "200", description = "User updated to Agent"),
        @ApiResponse(responseCode = "401", description = "Unauthorized/ Invalid or expired token"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
	})
	@PatchMapping("/admin/update-role/{userId}")
	public ResponseEntity<ApiResponseBean<UserResponseBean>> updateToAgentByAdmin(@PathVariable Long userId, @Valid @RequestBody PromoteToAgentRequestBean toAgentReq){
		USER_CONTROLLER_LOG.info("UserController :: in updateToAgentByAdmin()");
		UserResponseBean updateToAgentResp = userService.updateToAgentByAdmin(userId, toAgentReq);
		USER_CONTROLLER_LOG.info("UserController :: exit updateToAgentByAdmin()");
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponseBean.success(updateToAgentResp));
	}
	
}
