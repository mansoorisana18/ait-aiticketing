package com.aiticketing.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aiticketing.bean.request.LoginRequestBean;
import com.aiticketing.bean.request.RegisterUserRequestBean;
import com.aiticketing.bean.response.ApiResponseBean;
import com.aiticketing.bean.response.LoginResponseBean;
import com.aiticketing.bean.response.UserResponseBean;
import com.aiticketing.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
	
	@Autowired
	UserService userService;
	
	private static final Logger USER_CONTROLLER_LOG = LoggerFactory.getLogger(UserController.class);
	
	@Operation(summary = "Register user", description = "Creates a new user account")
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
	
	@Operation(summary = "Login user", description = "Validates credentials and returns a placeholder session token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid credentials / validation error")
    })
	@PostMapping(value= "/login")
	public ResponseEntity<ApiResponseBean<LoginResponseBean>> loginUser(@Valid @RequestBody LoginRequestBean loginRequest){
		USER_CONTROLLER_LOG.info("UserController :: in loginUser()");
		LoginResponseBean loginUserResp = userService.loginUser(loginRequest);
		USER_CONTROLLER_LOG.info("UserController :: exit loginUser()");
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponseBean.success(loginUserResp));
	}
	
	@Operation(summary = "Get users", description = "Fetches all users & agents for admin")
	@GetMapping("/admin")
	public ResponseEntity<ApiResponseBean<List<UserResponseBean>>> getUsersForAdmin(){
		USER_CONTROLLER_LOG.info("UserController :: in getUsersForAdmin()");
		List<UserResponseBean> getUsersResp = userService.getUsersForAdmin();
		USER_CONTROLLER_LOG.info("UserController :: exit getUsersForAdmin()");
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponseBean.success(getUsersResp));
	}
	
	@Operation(summary = "Make Agent", description = "Updates role to an AGENT")
	@PatchMapping("/admin/update-role/{userId}")
	public ResponseEntity<ApiResponseBean<UserResponseBean>> updateToAgentByAdmin(@PathVariable Long userId){
		USER_CONTROLLER_LOG.info("UserController :: in updateToAgentByAdmin()");
		UserResponseBean updateToAgentResp = userService.updateToAgentByAdmin(userId);
		USER_CONTROLLER_LOG.info("UserController :: exit updateToAgentByAdmin()");
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponseBean.success(updateToAgentResp));
	}
	
}
