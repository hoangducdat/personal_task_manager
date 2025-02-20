package org.project.personal_task_manager.service;

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

public interface UserService {

  void verifyEmail(VerifyEmailRequest verifyEmailRequest);

  void changePassword(ChangePasswordRequest changePasswordRequest);

  void forgotPassword(ForgotPasswordRequest request);

  void resendOtp(ResendOtpRequest request);

  VerifyResetPasswordOtpResponse verifyOtpForResetPassword(VerifyOtpResetPasswordRequest request);

  void resetPassword(ResetPasswordRequest request);

  LoginResponse refreshToken(RefreshTokenRequest request);

  UserProfileResponse getUserProfile();

  void updateUserProfile(UpdateUserProfileRequest updateUserProfileRequest);
}
