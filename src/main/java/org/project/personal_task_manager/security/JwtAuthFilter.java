package org.project.personal_task_manager.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.personal_task_manager.service.impl.UserDetailsServiceImpl;
import org.project.personal_task_manager.utils.redis.RedisService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final AuthTokenService authTokenService;
  private final UserDetailsServiceImpl userDetailsService;
  private final RedisService redisService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    String token = authTokenService.extractToken(request);
    log.info("Extracted token: {}", token);
    if (token != null) {
      try {
        String userId = authTokenService.getUserIdFromToken(token);

        if (!authTokenService.validateToken(token, userId)) {
          log.error("Token không hợp lệ hoặc không khớp với Redis!");
          response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
          return;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userId, null,
                null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("User {} đã được xác thực thành công!", userId);

      } catch (Exception e) {
        log.error("Lỗi xác thực token: {}", e.getMessage());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token authentication failed");
        return;
      }
    }

    filterChain.doFilter(request, response);
  }
}
