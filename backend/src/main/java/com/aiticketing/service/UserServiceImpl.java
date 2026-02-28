package com.aiticketing.service;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
import com.aiticketing.security.JwtService;

import jakarta.servlet.http.HttpServletResponse;

@Service("UserServiceImpl")
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepo;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	JwtService jwtService;

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	RefreshTokenService refreshTokenService;

	@Value("${app.security.refresh.cookie.secure}")
	private boolean refreshCookieSecure;

	private static final Logger USER_SERVICE_LOG = LoggerFactory.getLogger(UserServiceImpl.class);

	@Transactional
	public LoginResponseBean registerUser(RegisterUserRequestBean registerUserRequest) {
		USER_SERVICE_LOG.info("UserServiceImpl :: in registerUser() :: registerUserRequest {}",
				registerUserRequest.toString());
		String email = registerUserRequest.email.trim();

		// Check if email already exists
		if (userRepo.existsByEmailIgnoreCase(email)) {
			throw new ConflictException("Email already registered");
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
//		registerResp.token = jwtService.generateToken(userSaved.getUserId(), userSaved.getEmail(), userSaved.getRole());

		USER_SERVICE_LOG.info("UserServiceImpl :: exit registerUser() :: registerResp {}", registerResp.toString());
		return registerResp;
	}

	@Transactional
	public LoginResponseBean loginUser(LoginRequestBean loginRequest, HttpServletResponse response) {
		USER_SERVICE_LOG.info("UserServiceImpl :: in loginUser() :: loginRequest {}", loginRequest.toString());
		//Authenticating credentials with Spring security - UserDetailsService with
		//password check
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
				loginRequest.email.trim().toLowerCase(), loginRequest.password));

		//Fetch user from db
		User u = userRepo.findByEmailIgnoreCase(loginRequest.email.trim())
				.orElseThrow(() -> new NotFoundException("User not found"));

//        if (u.getPassword() == null || !passwordEncoder.matches(loginRequest.password, u.getPassword())) {
//            throw new UnauthorizedException("Invalid credentials");
//        }

		//step 1: Generate JWT - access token
		String accessToken = jwtService.generateToken(u.getUserId(), u.getEmail(), u.getRole());

		//step 2: Generate refresh token and store its hash in db
		String refreshTokenRaw = refreshTokenService.issueRefreshToken(u);

        //step 3: Set refresh cookie
        setRefreshCookie(response, refreshTokenRaw);
		
		//Set response JSON with only access token JWT
		LoginResponseBean loginResp = new LoginResponseBean();
		loginResp.userId = u.getUserId();
		loginResp.email = u.getEmail();
		loginResp.name = u.getUsername();
		loginResp.role = u.getRole().name();
		loginResp.token = accessToken;
		USER_SERVICE_LOG.info("UserServiceImpl :: exit loginUser() :: loginResp {}", loginResp.toString());
		return loginResp;
	}
	
	private void setRefreshCookie(HttpServletResponse response, String refreshTokenRaw) {
		USER_SERVICE_LOG.info("UserServiceImpl :: in setRefreshCookie()");
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshTokenRaw)
                .httpOnly(true)
                .secure(refreshCookieSecure) //false on localhost; true on HTTPS
                .sameSite("Lax")
                .path("/api/users") //cookie sent to /api/users/refresh and /api/users/logout
                .maxAge(Duration.ofDays(7))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

	@Transactional(readOnly = true)
	public LoginResponseBean refreshAccessToken(String refreshTokenCookie) {
		USER_SERVICE_LOG.info("UserServiceImpl :: in refreshAccessToken()");

        if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
            throw new UnauthorizedException("Unauthorized");
        }

        User u = refreshTokenService.validateRefreshTokenOrThrow(refreshTokenCookie);

        String newAccessToken = jwtService.generateToken(u.getUserId(), u.getEmail(), u.getRole());

        LoginResponseBean resp = new LoginResponseBean();
        resp.userId = u.getUserId();
        resp.email = u.getEmail();
        resp.name = u.getUsername();
        resp.role = u.getRole().name();
        resp.token = newAccessToken;
        USER_SERVICE_LOG.info("UserServiceImpl :: exit refreshAccessToken()");
        return resp;
	}

	@Transactional
	public void logout(Long userId, HttpServletResponse response) {		
		USER_SERVICE_LOG.info("UserServiceImpl :: in logout() userId={}", userId);
        refreshTokenService.revokeForUser(userId);
        clearRefreshCookie(response);
        USER_SERVICE_LOG.info("UserServiceImpl :: exit logout()");
	}
	
	private void clearRefreshCookie(HttpServletResponse response) {
		USER_SERVICE_LOG.info("UserServiceImpl :: in clearRefreshCookie()");
		ResponseCookie cleared = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite("Lax")
                .path("/api/users")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cleared.toString());
    }
	
	//////////////////Admin endpoints
	
	@Transactional
	public UserResponseBean updateToAgentByAdmin(Long userId) {
		USER_SERVICE_LOG.info("UserServiceImpl :: in updateToAgentByAdmin() :: userId {}", userId);
		User user = userRepo.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

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
		List<UserResponseBean> list = userRepo.findAll().stream().filter(u -> u.getRole() != UserRole.ADMIN)
				.map(this::setUsersResponseBean).toList();
		;
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
