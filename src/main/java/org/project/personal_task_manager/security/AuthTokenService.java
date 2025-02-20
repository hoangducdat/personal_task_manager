package org.project.personal_task_manager.security;

import jakarta.servlet.http.HttpServletRequest;

public interface AuthTokenService {

  String generateAccessToken(String username);

  String generateRefreshToken(String username);

  String getUserIdFromToken(String token);

  boolean validateToken(String token,String userId);

  String extractToken(HttpServletRequest request);

  long getAccessTokenExpiration();

  long getRefreshTokenExpiration();

}
