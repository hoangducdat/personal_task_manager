package org.project.personal_task_manager.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
  public String generateAccessToken(String username) {
    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
        .signWith(SignatureAlgorithm.HS256, accessSecret)
        .compact();
  }
  @Override
  public String generateRefreshToken(String username) {
    return Jwts.builder()
        .setSubject(username)
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
  public String getUsernameFromToken(String token) {
    return Jwts.parser()
        .setSigningKey(accessSecret)
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }
  @Override
  public boolean validateToken(String token) {
    try {
      Jwts.parser().setSigningKey(accessSecret).parseClaimsJws(token);
      return true;
    } catch (ExpiredJwtException e) {
      System.out.println("Token hết hạn!");
    } catch (MalformedJwtException e) {
      System.out.println("Token không hợp lệ!");
    } catch (SignatureException e) {
      System.out.println("Token có chữ ký sai!");
    }
    return false;
  }
  @Override
  public String extractToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      return header.substring(7);
    }
    return null;
  }
}
