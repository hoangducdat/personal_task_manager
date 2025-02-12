package org.project.personal_task_manager.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
  private String accessToken;
  private String refreshToken;
  private Long accessTokenExpiration;
  private Long refreshTokenExpiration;
  private String tokenType;

}
