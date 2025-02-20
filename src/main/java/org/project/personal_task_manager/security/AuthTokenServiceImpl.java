package org.project.personal_task_manager.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.project.personal_task_manager.exception.InvalidTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthTokenServiceImpl implements AuthTokenService {

  @Value("${jwt.access.secret}")
  private String accessSecret;

  @Value("${jwt.refresh.secret}")
  private String refreshSecret;

  @Value("${jwt.access.token.time.to.live}")
  private long accessTokenExpirationMs;

  @Value("${jwt.refresh.token.time.to.live}")
  private long refreshTokenExpirationMs;


  @Override
  public String generateAccessToken(String userId) {
    return Jwts.builder()
        .setSubject(userId)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
        .signWith(SignatureAlgorithm.HS256, accessSecret)
        .compact();
  }
  @Override
  public String generateRefreshToken(String userId) {
    return Jwts.builder()
        .setSubject(userId)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
        .signWith(SignatureAlgorithm.HS256, refreshSecret)
        .compact();
  }
  @Override
  public long getAccessTokenExpiration() {
    return accessTokenExpirationMs;
  }
  @Override
  public long getRefreshTokenExpiration() {
    return refreshTokenExpirationMs;
  }
  @Override
  public String getUserIdFromToken(String token) {
    return Jwts.parser()
        .setSigningKey(accessSecret)
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }
  @Override
  public boolean validateToken(String accessToken, String userId) {
    try {
      String subject = getUserIdFromToken(accessToken);
      if (!subject.equals(userId)) {
        throw new InvalidTokenException("Token does not match the provided userId: " + userId);
      }
      return true;
    } catch (InvalidTokenException e) {
      throw e;
    } catch (Exception e) {
      throw new InvalidTokenException("Failed to validate token for userId: " + userId);
    }
  }
  @Override
  public String extractToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      log.info("header: {}", header);
      return header.substring(7);
    }
    return null;
  }
}
