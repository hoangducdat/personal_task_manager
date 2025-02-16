package org.project.personal_task_manager.utils.otp;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.project.personal_task_manager.utils.email.EmailService;
import org.project.personal_task_manager.utils.redis.RedisService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
  private final OtpGenerator otpGenerator;
  private final EmailService emailService;
  private final RedisService redisService;

  @Override
  public String generateOtp(String email) {
    String otp = otpGenerator.generateOtp();
    emailService.sendEmail(email, "Your OTP Code", "Your OTP is: " + otp);
    return otp;
  }

}
