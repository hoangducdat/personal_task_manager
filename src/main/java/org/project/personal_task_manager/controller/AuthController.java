package org.project.personal_task_manager.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.project.personal_task_manager.controller.dto.ChangePasswordRequest;
import org.project.personal_task_manager.controller.dto.ForgotPasswordRequest;
import org.project.personal_task_manager.controller.dto.LoginRequest;
import org.project.personal_task_manager.controller.dto.RefreshTokenRequest;
import org.project.personal_task_manager.controller.dto.RegisterRequest;
import org.project.personal_task_manager.controller.dto.ResendOtpRequest;
import org.project.personal_task_manager.controller.dto.ResetPasswordRequest;
import org.project.personal_task_manager.controller.dto.VerifyEmailRequest;
import org.project.personal_task_manager.controller.dto.VerifyOtpResetPasswordRequest;
import org.project.personal_task_manager.controller.response.LoginResponse;
import org.project.personal_task_manager.controller.response.VerifyResetPasswordOtpResponse;
import org.project.personal_task_manager.service.AuthService;
import org.project.personal_task_manager.utils.dto.ApplicationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthService authService;
  public AuthController(AuthService authService) {
    this.authService = authService;
  }
  @PostMapping("/register")
  public ApplicationResponse<String> register(@Valid @RequestBody RegisterRequest registerRequest) {
    log.info("Register request: {}", registerRequest);
    authService.registerUser(registerRequest);
    return ApplicationResponse.of(HttpStatus.CREATED.value(),"null");
  }
  @PostMapping("/login")
  public ApplicationResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
    log.info("Login request: {}", request);
    LoginResponse loginResponse = authService.loginUser(request);
    return ApplicationResponse.of(HttpStatus.OK.value(),loginResponse);
  }
  @PostMapping("/verify-email")
  public ApplicationResponse<String> verifyEmail(@RequestBody VerifyEmailRequest request) {
    authService.verifyEmail(request);
    return ApplicationResponse.of(HttpStatus.OK.value(), "Email verified successfully!");
  }
  @PostMapping("/resend-otp")
  public ApplicationResponse<String> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
    log.info("Resending OTP for email: {}, type: {}", request.getEmail(), request.getOtpType());
    authService.resendOtp(request);

    return ApplicationResponse.of(HttpStatus.OK.value(),"Resend OTP successfully");
  }
  @PatchMapping("/change-password")
  public ApplicationResponse<String> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
    log.info("User attempting to change password");
    authService.changePassword(request);
    return ApplicationResponse.of(HttpStatus.OK.value(), "Password changed successfully");
  }

  @PostMapping("/forgot-password")
  public ApplicationResponse<String> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
    log.info("User requested OTP for password reset: {}", request.getEmail());
    authService.forgotPassword(request);
    return ApplicationResponse.of(HttpStatus.OK.value(), "OTP sent to email");
  }

  @PostMapping("/reset-password:verify-otp")
  public ApplicationResponse<VerifyResetPasswordOtpResponse> verifyOtpForResetPassword(@RequestBody @Valid VerifyOtpResetPasswordRequest request) {
    log.info("User verifying OTP for password reset: {}", request.getEmail());
    VerifyResetPasswordOtpResponse response = authService.verifyOtpForResetPassword(request);
    return ApplicationResponse.of(HttpStatus.OK.value(), response);
  }

  @PostMapping("/reset-password")
  public ApplicationResponse<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
    log.info("User resetting password with reset key");
    authService.resetPassword(request);
    return ApplicationResponse.of(HttpStatus.OK.value(), "Password reset successfully");
  }
  @PostMapping("/refresh-token")
  public ApplicationResponse<LoginResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
    log.info("User attempting to refresh token");
    LoginResponse response = authService.refreshToken(request);
    return ApplicationResponse.of(HttpStatus.OK.value(),response);
  }
}

