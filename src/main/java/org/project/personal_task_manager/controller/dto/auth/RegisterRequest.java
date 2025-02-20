package org.project.personal_task_manager.controller.dto.auth;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RegisterRequest {
  private String fullName;
  @NotBlank(message = "Email is required")
  @Pattern(regexp ="^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",
      message = "Must match email to format" )
  private String email;
  @NotBlank(message = "Username is required")
  private String username;
  @NotBlank(message = "Password is required")
  @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$",
      message = "Password must be 8-20 chars, with 1 number, 1 uppercase letter, and 1 special character")
  private String password;
  @NotBlank(message = "Confirm Password is required")
  private String confirmPassword;
}
