package org.project.personal_task_manager.controller.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VerifyOtpResetPasswordRequest {
  @NotBlank
  private String email;
  @NotBlank
  private String otp;
}
