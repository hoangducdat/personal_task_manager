package org.project.personal_task_manager.utils.constants;


public class SecurityConstants {

  public static final String LOGIN_ENDPOINT = "/api/v1/auth/login";
  public static final String REGISTER_ENDPOINT = "/api/v1/auth/register";
  private static final String FORGOT_PASSWORD_ENDPOINT = "/api/v1/user/forgot-password";
  private static final String VERIFY_OTP_RESET_PASSWORD_ENDPOINT = "/api/v1/user/reset-password:verify-otp";
  private static final String RESET_PASSWORD_ENDPOINT = "/api/v1/user/reset-password";
  public static final String REFRESH_TOKEN_ENDPOINT = "/api/v1/user/refresh-token";
  public static final String VERIFY_EMAIL_ENDPOINT = "/api/v1/user/verify-email";
  public static final String RESEND_OTP_ENDPOINT = "/api/v1/user/resend-otp";
  public static final String[] PUBLIC_ENDPOINTS = {
      LOGIN_ENDPOINT,
      REGISTER_ENDPOINT,
      REFRESH_TOKEN_ENDPOINT,
      FORGOT_PASSWORD_ENDPOINT,
      VERIFY_OTP_RESET_PASSWORD_ENDPOINT,
      RESET_PASSWORD_ENDPOINT,
      VERIFY_EMAIL_ENDPOINT,
      RESEND_OTP_ENDPOINT
  };
}
