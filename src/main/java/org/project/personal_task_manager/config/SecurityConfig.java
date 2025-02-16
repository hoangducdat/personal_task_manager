package org.project.personal_task_manager.config;

import org.project.personal_task_manager.security.AuthEntryPointJwt;
import org.project.personal_task_manager.security.AuthTokenService;
import org.project.personal_task_manager.security.JwtAuthFilter;
import org.project.personal_task_manager.utils.constants.SecurityConstants;
import org.project.personal_task_manager.utils.redis.RedisService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final AuthEntryPointJwt authEntryPointJwt;

  public SecurityConfig(AuthEntryPointJwt authEntryPointJwt) {
    this.authEntryPointJwt = authEntryPointJwt;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
  @Bean
  public UserDetailsService userDetailsService() {
    return new InMemoryUserDetailsManager(); // Hoặc custom UserDetailsService nếu bạn có
  }

  @Bean
  public JwtAuthFilter jwtAuthFilter(AuthTokenService authTokenService, UserDetailsService userDetailsService,
      RedisService redisService) {
    return new JwtAuthFilter(authTokenService, userDetailsService,redisService);
  }
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,JwtAuthFilter jwtAuthFilter) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(handling -> handling.authenticationEntryPoint(authEntryPointJwt))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(SecurityConstants.PUBLIC_ENDPOINTS).permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

}
