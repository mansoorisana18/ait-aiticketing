package com.aiticketing.service;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aiticketing.bean.request.LoginRequestBean;
import com.aiticketing.bean.request.RegisterUserRequestBean;
import com.aiticketing.bean.response.LoginResponseBean;
import com.aiticketing.bean.response.UserResponseBean;
import com.aiticketing.entity.User;
import com.aiticketing.entity.UserRole;
import com.aiticketing.exception.ConflictException;
import com.aiticketing.exception.NotFoundException;
import com.aiticketing.exception.UnauthorizedException;
import com.aiticketing.repository.UserRepository;

@Service("UserServiceImpl")
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepo;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	private static final Logger USER_SERVICE_LOG = LoggerFactory.getLogger(UserServiceImpl.class);
	
	@Transactional
	public LoginResponseBean registerUser(RegisterUserRequestBean registerUserRequest) {
		USER_SERVICE_LOG.info("UserServiceImpl :: in registerUser() :: registerUserRequest {}", registerUserRequest.toString());
		String email = registerUserRequest.email.trim().toLowerCase();
        if (userRepo.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email is already registered");
        }

        User u = new User();
        u.setEmail(email);
        u.setUsername(registerUserRequest.name.trim());
        u.setRole(UserRole.USER);
        u.setPassword(passwordEncoder.encode(registerUserRequest.password));

        User userSaved = userRepo.save(u);

        LoginResponseBean registerResp = new LoginResponseBean();
        registerResp.userId = userSaved.getUserId();
        registerResp.email = userSaved.getEmail();
        registerResp.name = userSaved.getUsername();
        registerResp.role = userSaved.getRole().name();
        registerResp.sessionToken = UUID.randomUUID().toString();
        
        USER_SERVICE_LOG.info("UserServiceImpl :: exit registerUser() :: registerResp {}", registerResp.toString());
        return registerResp;
	}

	@Transactional
	public LoginResponseBean loginUser(LoginRequestBean loginRequest) {
		USER_SERVICE_LOG.info("UserServiceImpl :: in loginUser() :: loginRequest {}", loginRequest.toString());
		User u = userRepo.findByEmailIgnoreCase(loginRequest.email)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (u.getPassword() == null || !passwordEncoder.matches(loginRequest.password, u.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        LoginResponseBean loginResp = new LoginResponseBean();
        loginResp.userId = u.getUserId();
        loginResp.email = u.getEmail();
        loginResp.name = u.getUsername();
        loginResp.role = u.getRole().name();
        loginResp.sessionToken = UUID.randomUUID().toString();
        USER_SERVICE_LOG.info("UserServiceImpl :: exit loginUser() :: loginResp {}", loginResp.toString());
        return loginResp;
	}

	@Transactional
	public UserResponseBean updateToAgentByAdmin(Long userId) {
		USER_SERVICE_LOG.info("UserServiceImpl :: in updateToAgentByAdmin() :: userId {}", userId);
		User user = userRepo.findById(userId)
	            .orElseThrow(() -> new NotFoundException("User not found"));

	    if (user.getRole() == UserRole.AGENT) {
	        throw new ConflictException("User is already an agent");
	    }

	    user.setRole(UserRole.AGENT);

	    User saved = userRepo.save(user);

	    USER_SERVICE_LOG.info("UserServiceImpl :: exit updateToAgentByAdmin()");

	    return setUsersResponseBean(saved);
	}
	
	public List<UserResponseBean> getUsersForAdmin() {
		USER_SERVICE_LOG.info("UserServiceImpl :: in getUsersForAdmin()");
		List<UserResponseBean> list = userRepo.findAll().stream()
				.filter(u -> u.getRole()!=UserRole.ADMIN)
                .map(this::setUsersResponseBean)
                .toList();;
		return list;
	}
	
	private UserResponseBean setUsersResponseBean(User u) {
		USER_SERVICE_LOG.info("UserServiceImpl :: in setUsersResponseBean()");
		UserResponseBean r = new UserResponseBean();
		r.userId = u.getUserId();
		r.name = u.getUsername();
		r.email = u.getEmail();
		r.role = u.getRole(); 
        
        USER_SERVICE_LOG.info("UserServiceImpl :: exit setUsersResponseBean()");
		return r;
	}

	

}
