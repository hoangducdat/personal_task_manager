package org.project.personal_task_manager.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.project.personal_task_manager.controller.dto.user.ChangePasswordRequest;
import org.project.personal_task_manager.controller.dto.user.ForgotPasswordRequest;
import org.project.personal_task_manager.controller.dto.user.RefreshTokenRequest;
import org.project.personal_task_manager.controller.dto.user.ResendOtpRequest;
import org.project.personal_task_manager.controller.dto.user.ResetPasswordRequest;
import org.project.personal_task_manager.controller.dto.user.VerifyEmailRequest;
import org.project.personal_task_manager.controller.dto.user.VerifyOtpResetPasswordRequest;
import org.project.personal_task_manager.controller.dto.user.UpdateUserProfileRequest;
import org.project.personal_task_manager.controller.response.LoginResponse;
import org.project.personal_task_manager.controller.response.UserProfileResponse;
import org.project.personal_task_manager.controller.response.VerifyResetPasswordOtpResponse;
import org.project.personal_task_manager.service.UserService;
import org.project.personal_task_manager.utils.dto.ApplicationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/verify-email")
  public ApplicationResponse<String> verifyEmail(@RequestBody VerifyEmailRequest request) {
    userService.verifyEmail(request);
    return ApplicationResponse.of(HttpStatus.OK.value(), "Email verified successfully!");
  }

  @PostMapping("/resend-otp")
  public ApplicationResponse<String> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
    log.info("Resending OTP for email: {}, type: {}", request.getEmail(), request.getOtpType());
    userService.resendOtp(request);

    return ApplicationResponse.of(HttpStatus.OK.value(), "Resend OTP successfully");
  }

  @PatchMapping("/change-password")
  public ApplicationResponse<String> changePassword(
      @RequestBody @Valid ChangePasswordRequest request) {
    log.info("User attempting to change password");
    userService.changePassword(request);
    return ApplicationResponse.of(HttpStatus.OK.value(), "Password changed successfully");
  }

  @PostMapping("/forgot-password")
  public ApplicationResponse<String> forgotPassword(
      @RequestBody @Valid ForgotPasswordRequest request) {
    log.info("User requested OTP for password reset: {}", request.getEmail());
    userService.forgotPassword(request);
    return ApplicationResponse.of(HttpStatus.OK.value(), "OTP sent to email");
  }

  @PostMapping("/reset-password:verify-otp")
  public ApplicationResponse<VerifyResetPasswordOtpResponse> verifyOtpForResetPassword(
      @RequestBody @Valid VerifyOtpResetPasswordRequest request) {
    log.info("User verifying OTP for password reset: {}", request.getEmail());
    VerifyResetPasswordOtpResponse response = userService.verifyOtpForResetPassword(request);
    return ApplicationResponse.of(HttpStatus.OK.value(), response);
  }

  @PostMapping("/reset-password")
  public ApplicationResponse<String> resetPassword(
      @RequestBody @Valid ResetPasswordRequest request) {
    log.info("User resetting password with reset key");
    userService.resetPassword(request);
    return ApplicationResponse.of(HttpStatus.OK.value(), "Password reset successfully");
  }

  @PostMapping("/refresh-token")
  public ApplicationResponse<LoginResponse> refreshToken(
      @RequestBody @Valid RefreshTokenRequest request) {
    log.info("User attempting to refresh token");
    LoginResponse response = userService.refreshToken(request);
    return ApplicationResponse.of(HttpStatus.OK.value(), response);
  }

  @GetMapping("/profile")
  public ApplicationResponse<UserProfileResponse> getUserProfile() {
    UserProfileResponse profile = userService.getUserProfile();
    return ApplicationResponse.of(HttpStatus.OK.value(), profile);
  }

  @PutMapping("/profile:update")
  public ApplicationResponse<String> updateProfile(
      @Valid @RequestBody UpdateUserProfileRequest request) {
    userService.updateUserProfile(request);
    return ApplicationResponse.of(HttpStatus.OK.value(), "Profile updated successfully");
  }

}
