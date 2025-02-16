package org.project.personal_task_manager.utils.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.personal_task_manager.exception.EmailNotFoundException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {
  private final JavaMailSender mailSender;

  public EmailServiceImpl(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void sendEmail(String email, String subject, String text) {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message);
    try {
      helper.setTo(email);
      helper.setSubject(subject);
      helper.setText(text);
      mailSender.send(message);
      log.info("OTP email sent to {}", email);
    } catch (MessagingException e) {
      log.error("Failed to send OTP email", e);
      throw new EmailNotFoundException("Failed to send email to: " + email);
    }
  }
}
