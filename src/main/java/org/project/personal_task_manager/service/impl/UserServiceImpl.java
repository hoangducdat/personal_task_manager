package org.project.personal_task_manager.service.impl;

import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
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
import org.project.personal_task_manager.entity.AccountEntity;
import org.project.personal_task_manager.entity.UserEntity;
import org.project.personal_task_manager.exception.AccountNotFoundException;
import org.project.personal_task_manager.exception.OtpException;
import org.project.personal_task_manager.exception.PasswordNotMatchException;
import org.project.personal_task_manager.exception.TokenException;
import org.project.personal_task_manager.exception.UserNotFoundException;
import org.project.personal_task_manager.repository.AccountRepository;
import org.project.personal_task_manager.repository.UserRepository;
import org.project.personal_task_manager.security.AuthTokenService;
import org.project.personal_task_manager.service.UserService;
import org.project.personal_task_manager.utils.SecurityUtil;
import org.project.personal_task_manager.utils.constants.CacheConstants;
import org.project.personal_task_manager.utils.constants.OtpTypeConstants;
import org.project.personal_task_manager.utils.constants.TokenTypeConstants;
import org.project.personal_task_manager.utils.otp.OtpService;
import org.project.personal_task_manager.utils.redis.RedisService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthTokenService authTokenService;
  private final RedisService redisService;
  private final OtpService otpService;

  @Override
  public void verifyEmail(VerifyEmailRequest verifyEmailRequest) {
    String storedOtp = (String) redisService.get(
        CacheConstants.OTP_EMAIL_VERIFICATION_KEY + verifyEmailRequest.getEmail()).orElse(null);

    if (storedOtp == null || !storedOtp.equals(verifyEmailRequest.getOtp())) {
      throw new RuntimeException("Invalid or expired OTP for email verification");
    }

    UserEntity user = userRepository.findByEmail(verifyEmailRequest.getEmail())
        .orElseThrow(() -> new AccountNotFoundException(
            "User not found with email: " + verifyEmailRequest.getEmail()));

    user.setVerifyEmail(true);
    userRepository.save(user);

    redisService.delete(CacheConstants.OTP_EMAIL_VERIFICATION_KEY + verifyEmailRequest.getEmail());
    redisService.delete(CacheConstants.OTP_RESEND_COUNT_KEY + verifyEmailRequest.getEmail() + ":"
        + OtpTypeConstants.EMAIL_VERIFICATION);
    redisService.delete(CacheConstants.OTP_RESEND_LOCK_KEY + verifyEmailRequest.getEmail() + ":"
        + OtpTypeConstants.EMAIL_VERIFICATION);

    log.info("Email {} verified successfully!", verifyEmailRequest.getEmail());
  }

  @Override
  public void changePassword(ChangePasswordRequest request) {

    String userId = SecurityUtil.getUserId();
    log.info("Changing password for user {}", userId);

    AccountEntity account = accountRepository.findByUserId(userId)
        .orElseThrow(() -> new AccountNotFoundException("Account not found with user_id {}" + userId));

    if (!passwordEncoder.matches(request.getOldPassword(), account.getPassword())) {
      log.error("Old password not match");
      throw new PasswordNotMatchException("Old password is incorrect");
    }
    if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
      log.error("New password and Confirm Password not match");
      throw new PasswordNotMatchException("New password and confirm password do not match");
    }
    account.setPassword(passwordEncoder.encode(request.getNewPassword()));
    accountRepository.save(account);
    log.info("Changed password successfully for user {} ", userId);
  }

  @Override
  public void forgotPassword(ForgotPasswordRequest request) {
    AccountEntity account = accountRepository.findAccountByEmail(request.getEmail())
        .orElseThrow(
            () -> new AccountNotFoundException("User not found with email " + request.getEmail()));

    String otp = otpService.generateOtp(request.getEmail());
    redisService.save(CacheConstants.OTP_RESET_PASSWORD_KEY + request.getEmail(), otp,
        CacheConstants.OTP_EXPIRATION_TIME, TimeUnit.MINUTES);
    log.info("Generated OTP: {} for email: {}", otp, request.getEmail());

  }

  @Override
  public void resendOtp(ResendOtpRequest request) {
    String otpKey = request.getOtpType();
    if (OtpTypeConstants.RESET_PASSWORD.equals(otpKey)) {
      log.info("Reset password for user {}", SecurityUtil.getUserId());
      otpKey = CacheConstants.OTP_RESET_PASSWORD_KEY + request.getEmail();
    } else if (OtpTypeConstants.EMAIL_VERIFICATION.equals(otpKey)) {
      otpKey = CacheConstants.OTP_EMAIL_VERIFICATION_KEY + request.getEmail();
    } else {
      throw new OtpException("Invalid OTP type: " + request.getOtpType());
    }
    String lockKey = CacheConstants.OTP_RESEND_LOCK_KEY + request.getEmail() + ":" + otpKey;
    String resendCountKey = CacheConstants.OTP_RESEND_COUNT_KEY + request.getEmail() + ":" + otpKey;
    String resetKey = CacheConstants.OTP_RESEND_RESET_KEY + request.getEmail() + ":" + otpKey;

    if (redisService.get(resetKey).isEmpty()) {
      redisService.delete(resendCountKey);
      redisService.save(resetKey, true, CacheConstants.RESEND_RESET_TTL, TimeUnit.DAYS);
      log.info("Reset resend count for {} after 10 days", request.getEmail());
    }

    if (redisService.get(lockKey).isPresent()) {
      throw new OtpException("Resend OTP is temporarily locked for 24 hours.");
    }
    int resendCount = (int) redisService.get(resendCountKey).orElse(0);

    if (resendCount >= CacheConstants.MAX_RESEND_ATTEMPTS) {
      redisService.save(lockKey, true, CacheConstants.RESEND_LOCK_TTL, TimeUnit.DAYS);
      redisService.delete(resendCountKey);
      throw new OtpException(
          "Maximum OTP resend attempts reached. You cannot request a new OTP for 24 hours.");
    }

    String otp = otpService.generateOtp(request.getEmail());
    redisService.save(otpKey, otp, CacheConstants.OTP_EXPIRATION_TIME, TimeUnit.MINUTES);

    redisService.save(resendCountKey, resendCount + 1, CacheConstants.RESEND_RESET_TTL,
        TimeUnit.DAYS);

    log.info("Resent OTP: {} for {} (Attempt {}/{})", otp, request.getOtpType(), resendCount + 1,
        CacheConstants.MAX_RESEND_ATTEMPTS);
  }

  @Override
  public VerifyResetPasswordOtpResponse verifyOtpForResetPassword(
      VerifyOtpResetPasswordRequest request) {
    String storedOtp = (String) redisService.get(
        CacheConstants.OTP_RESET_PASSWORD_KEY + request.getEmail()).orElse(null);
    log.info("Stored OTP from Redis: {}", storedOtp);

    if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
      log.info("Invalid or expired OTP");
      throw new OtpException("Invalid or expired OTP for reset password");
    }

    String resetPasswordKey = generateResetPasswordKey(request.getEmail());
    redisService.save(CacheConstants.RESET_PASSWORD_KEY, request.getEmail(), resetPasswordKey);
    log.info("Generated Reset Password Key: {} for email: {}", resetPasswordKey,
        request.getEmail());

    return new VerifyResetPasswordOtpResponse(request.getEmail(), resetPasswordKey);
  }

  @Override
  public void resetPassword(ResetPasswordRequest request) {
    log.info("Resetting password using key: {}", request.getEmail());

    Optional<Object> resetPasswordKeyOptional = redisService.get(CacheConstants.RESET_PASSWORD_KEY,
        request.getEmail());
    String resetPasswordKey = (String) resetPasswordKeyOptional.orElse(null);
    if (resetPasswordKey == null || !resetPasswordKey.equals(request.getResetPasswordKey())) {
      log.error("Invalid or expired reset password key for email: {}", request.getEmail());
      throw new IllegalArgumentException("Invalid or expired reset password key");
    }
    AccountEntity account = accountRepository.findAccountByEmail(request.getEmail())
        .orElseThrow(
            () -> new AccountNotFoundException("User not found with email " + request.getEmail()));

    if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
      throw new IllegalArgumentException("New password cannot be null or empty");
    }

    account.setPassword(passwordEncoder.encode(request.getNewPassword()));
    accountRepository.save(account);

    redisService.delete(CacheConstants.RESET_PASSWORD_KEY, request.getEmail());
    redisService.delete(CacheConstants.OTP_RESEND_COUNT_KEY + request.getEmail() + ":"
        + OtpTypeConstants.RESET_PASSWORD);
    redisService.delete(CacheConstants.OTP_RESEND_LOCK_KEY + request.getEmail() + ":"
        + OtpTypeConstants.RESET_PASSWORD);

    log.info("Reset password successfully for {}", request.getEmail());
  }

  @Override
  public LoginResponse refreshToken(RefreshTokenRequest request) {
    String username = authTokenService.getUserIdFromToken(request.getRefreshToken());

    if (username == null) {
      throw new TokenException("Invalid refresh token");
    }

    Optional<Object> storedRefreshToken = redisService.get(
        "TOKEN:REFRESH" + username);
    if (storedRefreshToken.isEmpty() || !storedRefreshToken.get()
        .equals(request.getRefreshToken())) {
      throw new TokenException("Refresh token is expired or invalid");
    }

    String newAccessToken = authTokenService.generateAccessToken(username);
    redisService.save("TOKEN:ACCESS:" + username, newAccessToken,
        authTokenService.getAccessTokenExpiration(), TimeUnit.MILLISECONDS);

    log.info("Issued new access token for {}", username);

    return new LoginResponse(
        newAccessToken,
        request.getRefreshToken(),
        authTokenService.getAccessTokenExpiration(),
        authTokenService.getRefreshTokenExpiration(),
        TokenTypeConstants.TOKEN_TYPE
    );
  }

  @Override
  public void updateUserProfile(UpdateUserProfileRequest request) {
    log.info("Updating user profile");
    String userId = SecurityUtil.getUserId();
    UserEntity userEntity = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

    userEntity.setFullName(request.getFullName());
    userEntity.setAddress(request.getAddress());
    userEntity.setPhoneNumber(request.getPhoneNumber());
    userRepository.save(userEntity);
    log.info("User profile updated successfully");
  }

  @Override
  public UserProfileResponse getUserProfile() {
    String userId = SecurityUtil.getUserId();

    UserEntity user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

    String username = userRepository.findUsernameByUserId(userId);
    if (username == null) {
      throw new UserNotFoundException("Username not found for user id: " + userId);
    }

    return new UserProfileResponse(
        username,
        user.getFullName(),
        user.getEmail(),
        user.getPhoneNumber(),
        user.getAddress(),
        user.isVerifyEmail()
    );
  }

  private String generateResetPasswordKey(String email) {
    return Base64.getEncoder().encodeToString((email + System.currentTimeMillis()).getBytes());
  }
}
