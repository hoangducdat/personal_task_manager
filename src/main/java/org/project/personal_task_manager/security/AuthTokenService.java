package org.project.personal_task_manager.security;

import jakarta.servlet.http.HttpServletRequest;

public interface AuthTokenService {
  String generateAccessToken(String username);
  String generateRefreshToken(String username);
  String getUsernameFromToken(String token);
  boolean validateToken(String token);
  String extractToken(HttpServletRequest request);
}
