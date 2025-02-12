package org.project.personal_task_manager.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.project.personal_task_manager.controller.dto.LoginRequest;
import org.project.personal_task_manager.controller.dto.RegisterRequest;
import org.project.personal_task_manager.controller.response.LoginResponse;
import org.project.personal_task_manager.service.AuthService;
import org.project.personal_task_manager.utils.dto.ApplicationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthService authService;
  public AuthController(AuthService authService) {
    this.authService = authService;
  }
  @PostMapping("/register")
  public ApplicationResponse<String> register(@Valid @RequestBody RegisterRequest registerRequest) {
    log.info("Register request: {}", registerRequest);
    authService.registerUser(registerRequest);
    return ApplicationResponse.of(HttpStatus.CREATED.value(),"null");
  }
  @PostMapping("/login")
  public ApplicationResponse<LoginResponse> login(@RequestBody @Validated LoginRequest request) {
    log.info("Login request: {}", request);
    LoginResponse loginResponse = authService.loginUser(request);
    return ApplicationResponse.of(HttpStatus.OK.value(),loginResponse);
  }
}
