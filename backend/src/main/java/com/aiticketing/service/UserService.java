package com.aiticketing.service;

import java.util.List;

import com.aiticketing.bean.request.LoginRequestBean;
import com.aiticketing.bean.request.RegisterUserRequestBean;
import com.aiticketing.bean.response.LoginResponseBean;
import com.aiticketing.bean.response.UserResponseBean;

public interface UserService {
	
	LoginResponseBean registerUser(RegisterUserRequestBean registerUserRequest);
	LoginResponseBean loginUser(LoginRequestBean loginRequest);
	List<UserResponseBean> getUsersForAdmin();
	UserResponseBean updateToAgentByAdmin(Long userId);

}