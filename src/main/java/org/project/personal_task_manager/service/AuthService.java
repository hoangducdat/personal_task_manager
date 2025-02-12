package org.project.personal_task_manager.service;

import org.project.personal_task_manager.controller.dto.LoginRequest;
import org.project.personal_task_manager.controller.dto.RegisterRequest;
import org.project.personal_task_manager.controller.response.LoginResponse;

public interface AuthService {
  void registerUser(RegisterRequest registerRequest);
  LoginResponse loginUser(LoginRequest loginRequest);
}
