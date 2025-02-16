package org.project.personal_task_manager.service;

import org.project.personal_task_manager.controller.dto.ChangePasswordRequest;
import org.project.personal_task_manager.controller.dto.ForgotPasswordRequest;
import org.project.personal_task_manager.controller.dto.LoginRequest;
import org.project.personal_task_manager.controller.dto.RefreshTokenRequest;
import org.project.personal_task_manager.controller.dto.RegisterRequest;
import org.project.personal_task_manager.controller.dto.ResendOtpRequest;
import org.project.personal_task_manager.controller.dto.ResetPasswordRequest;
import org.project.personal_task_manager.controller.dto.UpdateUserProfileRequest;
import org.project.personal_task_manager.controller.dto.VerifyEmailRequest;
import org.project.personal_task_manager.controller.dto.VerifyOtpResetPasswordRequest;
import org.project.personal_task_manager.controller.response.LoginResponse;
import org.project.personal_task_manager.controller.response.VerifyResetPasswordOtpResponse;

public interface AuthService {
  void registerUser(RegisterRequest registerRequest);
  LoginResponse loginUser(LoginRequest loginRequest);
  void verifyEmail(VerifyEmailRequest verifyEmailRequest);
  void changePassword(ChangePasswordRequest changePasswordRequest);
  void forgotPassword(ForgotPasswordRequest request);
  void resendOtp(ResendOtpRequest request);
  VerifyResetPasswordOtpResponse verifyOtpForResetPassword(VerifyOtpResetPasswordRequest request);
  void resetPassword(ResetPasswordRequest request);
  LoginResponse refreshToken(RefreshTokenRequest request);

}
