package org.project.personal_task_manager.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Slf4j
public class SecurityUtil {

  public static String getUserId() {
    log.info("(getUserId) Fetching authenticated user ID");

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
      return "SYSTEM_ID";
    }

    Object principal = authentication.getPrincipal();

    if (principal instanceof UserDetails) {
      return ((UserDetails) principal).getUsername();
    }

    return principal.toString();
  }
}