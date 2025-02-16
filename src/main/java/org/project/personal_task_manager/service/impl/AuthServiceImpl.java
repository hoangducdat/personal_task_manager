package org.project.personal_task_manager.service.impl;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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
import org.project.personal_task_manager.entity.AccountEntity;
import org.project.personal_task_manager.entity.UserEntity;
import org.project.personal_task_manager.exception.AccountNotFoundException;
import org.project.personal_task_manager.exception.EmailAlreadyExistsException;
import org.project.personal_task_manager.exception.OtpException;
import org.project.personal_task_manager.exception.PasswordNotMatchException;
import org.project.personal_task_manager.exception.TokenException;
import org.project.personal_task_manager.exception.UsernameAlreadyExistsException;
import org.project.personal_task_manager.exception.UsernameNotFoundException;
import org.project.personal_task_manager.repository.AccountRepository;
import org.project.personal_task_manager.repository.UserRepository;
import org.project.personal_task_manager.security.AuthTokenService;
import org.project.personal_task_manager.security.AuthTokenServiceImpl;
import org.project.personal_task_manager.service.AuthService;
import org.project.personal_task_manager.utils.constants.CacheConstants;
import org.project.personal_task_manager.utils.constants.OtpTypeConstants;
import org.project.personal_task_manager.utils.constants.TokenTypeConstants;
import org.project.personal_task_manager.utils.otp.OtpService;
import org.project.personal_task_manager.utils.redis.RedisService;
import org.project.personal_task_manager.utils.SecurityUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthTokenService authTokenService;
  private final RedisService redisService;
  private final OtpService otpService;


  public AuthServiceImpl(UserRepository userRepository, AccountRepository accountRepository,
      PasswordEncoder passwordEncoder, AuthTokenService authTokenService,
      RedisService redisService, OtpService otpService) {
    this.userRepository = userRepository;
    this.accountRepository = accountRepository;
    this.passwordEncoder = passwordEncoder;
    this.authTokenService = authTokenService;
    this.redisService = redisService;
    this.otpService = otpService;
  }

  @Override
  public void registerUser(RegisterRequest registerRequest) {
    if (userRepository.existsByEmail(registerRequest.getEmail())) {
      log.error("Email already exists");
      throw new EmailAlreadyExistsException("Email already exists" + registerRequest.getEmail());
    }
    if (accountRepository.existsByUsername(registerRequest.getUsername())) {
      log.error("Username already exists");
      throw new UsernameAlreadyExistsException(
          "Username already exists" + registerRequest.getUsername());
    }
    if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
      log.error("Password and Confirm Password doesn't match");
      throw new PasswordNotMatchException("Password and Confirm Password doesn't match");
    }
    String password = passwordEncoder.encode(registerRequest.getPassword());

    UserEntity user = new UserEntity();
    user.setEmail(registerRequest.getEmail());
    user.setFullname(registerRequest.getFullName());
    user.setCreatedAt(LocalDateTime.now());
    userRepository.save(user);

    AccountEntity account = new AccountEntity();
    account.setUserId(user.getId());
    account.setUsername(registerRequest.getUsername());
    account.setPassword(password);
    accountRepository.save(account);

    log.info("Registered user successfully with email: {}, username: {} ",
        registerRequest.getEmail(), registerRequest.getUsername());

    String otpVerifyEmail = otpService.generateOtp(registerRequest.getEmail());
    redisService.save(CacheConstants.OTP_EMAIL_VERIFICATION_KEY + registerRequest.getEmail(),
        otpVerifyEmail, CacheConstants.OTP_EXPIRATION_TIME, TimeUnit.MINUTES);
    log.info("Saved otp email verification key: {}", otpVerifyEmail);
  }

  @Override
  public LoginResponse loginUser(LoginRequest loginRequest) {
    Optional<AccountEntity> account = accountRepository.findByUsername(loginRequest.getUsername());
    if (account.isEmpty()) {
      log.error("Username not found");
      throw new UsernameNotFoundException("Username not found" + loginRequest.getUsername());
    }
    AccountEntity accountEntity = account.get();
    if (!passwordEncoder.matches(loginRequest.getPassword(), accountEntity.getPassword())) {
      log.error("Incorrect password");
      throw new PasswordNotMatchException("Incorrect password");
    }

    String accessToken = authTokenService.generateAccessToken(accountEntity.getUsername());
    String refreshToken = authTokenService.generateRefreshToken(accountEntity.getUsername());

    log.info("TOKEN:ACCESS:{} Access token: {}", accountEntity.getUsername(), accessToken);
    redisService.save("TOKEN:ACCESS:" + accountEntity.getUsername(), accessToken,
        authTokenService.getAccessTokenExpiration(), TimeUnit.MILLISECONDS);
    log.info("TOKEN:REFRESH: {} Refresh token: {}", accountEntity.getUsername(), refreshToken);
    redisService.save("TOKEN:REFRESH:" + accountEntity.getUsername(), refreshToken,
        authTokenService.getRefreshTokenExpiration(), TimeUnit.MILLISECONDS);

    return new LoginResponse(
        accessToken,
        refreshToken,
        authTokenService.getAccessTokenExpiration(),
        authTokenService.getRefreshTokenExpiration(),
        TokenTypeConstants.TOKEN_TYPE
    );
  }
  @Override
  public void verifyEmail(VerifyEmailRequest verifyEmailRequest) {
    String storedOtp = (String) redisService.get(CacheConstants.OTP_EMAIL_VERIFICATION_KEY + verifyEmailRequest.getEmail()).orElse(null);

    if (storedOtp == null || !storedOtp.equals(verifyEmailRequest.getOtp())) {
      throw new RuntimeException("Invalid or expired OTP for email verification");
    }

    UserEntity user = userRepository.findByEmail(verifyEmailRequest.getEmail())
        .orElseThrow(() -> new AccountNotFoundException("User not found with email: " + verifyEmailRequest.getEmail()));

    user.setVerifyEmail(true);
    userRepository.save(user);

    redisService.delete(CacheConstants.OTP_EMAIL_VERIFICATION_KEY + verifyEmailRequest.getEmail());
    redisService.delete(CacheConstants.OTP_RESEND_COUNT_KEY + verifyEmailRequest.getEmail() + ":" + OtpTypeConstants.EMAIL_VERIFICATION);
    redisService.delete(CacheConstants.OTP_RESEND_LOCK_KEY + verifyEmailRequest.getEmail() + ":" + OtpTypeConstants.EMAIL_VERIFICATION);


    log.info("Email {} verified successfully!", verifyEmailRequest.getEmail());
  }

  @Override
  public void changePassword(ChangePasswordRequest request) {

    String userId = SecurityUtil.getUserId();
    log.info("Changing password for user {}", userId);

    AccountEntity account = accountRepository.findById(userId)
        .orElseThrow(() -> new AccountNotFoundException("User with id {} not found" + userId));

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
    log.info("Changed password for user {}", userId);
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
      throw new OtpException("Maximum OTP resend attempts reached. You cannot request a new OTP for 24 hours.");
    }

    String otp = otpService.generateOtp(request.getEmail());
    redisService.save(otpKey, otp, CacheConstants.OTP_EXPIRATION_TIME, TimeUnit.MINUTES);

    redisService.save(resendCountKey, resendCount + 1, CacheConstants.RESEND_RESET_TTL, TimeUnit.DAYS);

    log.info("Resent OTP: {} for {} (Attempt {}/{})", otp, request.getOtpType(), resendCount + 1, CacheConstants.MAX_RESEND_ATTEMPTS);
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
    redisService.delete(CacheConstants.OTP_RESEND_COUNT_KEY + request.getEmail() + ":" + OtpTypeConstants.RESET_PASSWORD);
    redisService.delete(CacheConstants.OTP_RESEND_LOCK_KEY + request.getEmail() + ":" + OtpTypeConstants.RESET_PASSWORD);


    log.info("Reset password successfully for {}", request.getEmail());
  }
  @Override
  public LoginResponse refreshToken(RefreshTokenRequest request) {
    String username = authTokenService.getUsernameFromToken(request.getRefreshToken());

    if (username == null) {
      throw new TokenException("Invalid refresh token");
    }

    Optional<Object> storedRefreshToken = redisService.get(
        "TOKEN:REFRESH" + username);
    if (storedRefreshToken.isEmpty() || !storedRefreshToken.get().equals(request.getRefreshToken())) {
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
  private String generateResetPasswordKey(String email) {
    return Base64.getEncoder().encodeToString((email + System.currentTimeMillis()).getBytes());
  }
}

