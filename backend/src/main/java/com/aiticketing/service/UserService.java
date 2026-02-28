package com.aiticketing.service;

import java.util.List;

import com.aiticketing.bean.request.LoginRequestBean;
import com.aiticketing.bean.request.RegisterUserRequestBean;
import com.aiticketing.bean.response.LoginResponseBean;
import com.aiticketing.bean.response.UserResponseBean;

import jakarta.servlet.http.HttpServletResponse;

public interface UserService {
	
	LoginResponseBean registerUser(RegisterUserRequestBean registerUserRequest);
	
	//HttpServletResponse for setting refresh cookie
	LoginResponseBean loginUser(LoginRequestBean loginRequest, HttpServletResponse response);
	//For refreshing access token using refresh token
	LoginResponseBean refreshAccessToken(String refreshTokenCookie);
    //Logout endpoint that revokes refresh token for current user
    void logout(Long userId, HttpServletResponse response);
	
	List<UserResponseBean> getUsersForAdmin();
	UserResponseBean updateToAgentByAdmin(Long userId);

}