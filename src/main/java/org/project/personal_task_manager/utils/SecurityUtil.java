package org.project.personal_task_manager.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class SecurityUtil {

  public static String getUserId() {
    log.info("(getUserId) Fetching authenticated user ID");

    var authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
      return "SYSTEM_ID";
    }
    return authentication.getPrincipal().toString();
  }
}