package com.aiticketing.service;

import com.aiticketing.entity.User;

public interface RefreshTokenService {

	String issueRefreshToken(User user);
    User validateRefreshTokenOrThrow(String rawRefreshToken);
    void revokeForUser(Long userId);

}