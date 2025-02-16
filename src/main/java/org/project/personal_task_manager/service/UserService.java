package org.project.personal_task_manager.service;

import org.project.personal_task_manager.controller.dto.UpdateUserProfileRequest;

public interface UserService {
  void updateUserProfile(UpdateUserProfileRequest updateUserProfileRequest);
}
