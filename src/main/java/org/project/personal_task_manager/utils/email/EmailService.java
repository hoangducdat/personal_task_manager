package org.project.personal_task_manager.utils.email;

public interface EmailService {
  void sendEmail(String email, String subject, String text);
}
