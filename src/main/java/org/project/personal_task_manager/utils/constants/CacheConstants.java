package org.project.personal_task_manager.utils.constants;

public class CacheConstants {
  public static final int MAX_RESEND_ATTEMPTS = 5;
  public static final long RESEND_RESET_TTL = 10;
  public static final long RESEND_LOCK_TTL = 1;

  public static final long OTP_EXPIRATION_TIME = 3;
  public static final String OTP_RESEND_COUNT_KEY = "OTP_RESEND_COUNT:";
  public static final String OTP_RESEND_LOCK_KEY = "OTP_RESEND_LOCK:";
  public static final String OTP_RESEND_RESET_KEY = "OTP_RESEND_RESET:";

  public static final String OTP_RESET_PASSWORD_KEY = "OTP_RESET_PASSWORD:";
  public static final String OTP_EMAIL_VERIFICATION_KEY = "OTP_EMAIL_VERIFICATION:";
  public static final String RESEND_COUNT_KEY = "OTP_RESEND_COUNT:";
  public static final String RESET_PASSWORD_KEY = "RESET_PASSWORD_KEY:";

}
