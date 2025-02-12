package org.project.personal_task_manager.service.impl;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.project.personal_task_manager.controller.dto.LoginRequest;
import org.project.personal_task_manager.controller.dto.RegisterRequest;
import org.project.personal_task_manager.controller.response.LoginResponse;
import org.project.personal_task_manager.entity.AccountEntity;
import org.project.personal_task_manager.entity.UserEntity;
import org.project.personal_task_manager.exception.EmailAlreadyExistsException;
import org.project.personal_task_manager.exception.PasswordNotMatchException;
import org.project.personal_task_manager.exception.UsernameAlreadyExistsException;
import org.project.personal_task_manager.exception.UsernameNotFoundException;
import org.project.personal_task_manager.repository.AccountRepository;
import org.project.personal_task_manager.repository.UserRepository;
import org.project.personal_task_manager.security.AuthTokenService;
import org.project.personal_task_manager.security.AuthTokenServiceImpl;
import org.project.personal_task_manager.service.AuthService;
import org.project.personal_task_manager.utils.constants.TokenTypeConstants;
import org.project.personal_task_manager.utils.helper.RedisService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthTokenService authTokenService;
  private final RedisService redisService;
  private final AuthTokenServiceImpl authTokenServiceImpl;


  public AuthServiceImpl(UserRepository userRepository, AccountRepository accountRepository,
      PasswordEncoder passwordEncoder, AuthTokenService authTokenService,
      RedisService redisService, AuthTokenServiceImpl authTokenServiceImpl) {
    this.userRepository = userRepository;
    this.accountRepository = accountRepository;
    this.passwordEncoder = passwordEncoder;
    this.authTokenService = authTokenService;
    this.redisService = redisService;
    this.authTokenServiceImpl = authTokenServiceImpl;
  }

  @Override
  public void registerUser(RegisterRequest registerRequest) {
    if (userRepository.existsByEmail(registerRequest.getEmail())) {
      log.error("Email already exists");
      throw new EmailAlreadyExistsException("Email already exists" + registerRequest.getEmail());
    }
    if (accountRepository.existsByUsername(registerRequest.getUsername())) {
      log.error("Username already exists");
      throw new UsernameAlreadyExistsException(
          "Username already exists" + registerRequest.getUsername());
    }
    if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
      log.error("Password and Confirm Password doesn't match");
      throw new PasswordNotMatchException("Password and Confirm Password doesn't match");
    }
    String password = passwordEncoder.encode(registerRequest.getPassword());

    UserEntity user = new UserEntity();
    user.setEmail(registerRequest.getEmail());
    user.setFullname(registerRequest.getFullName());
    user.setCreatedAt(LocalDateTime.now());
    userRepository.save(user);

    AccountEntity account = new AccountEntity();
    account.setUserId(user.getId());
    account.setUsername(registerRequest.getUsername());
    account.setPassword(password);
    accountRepository.save(account);
  }

  @Override
  public LoginResponse loginUser(LoginRequest loginRequest) {
    Optional<AccountEntity> account = accountRepository.findByUsername(loginRequest.getUsername());
    if (account.isEmpty()) {
      log.error("Username not found");
      throw new UsernameNotFoundException("Username not found" + loginRequest.getUsername());
    }
    AccountEntity accountEntity = account.get();
    if (!passwordEncoder.matches(loginRequest.getPassword(), accountEntity.getPassword())) {
      log.error("Incorrect password");
      throw new PasswordNotMatchException("Incorrect password");
    }

    String accessToken = authTokenService.generateAccessToken(accountEntity.getUsername());
    String refreshToken = authTokenService.generateRefreshToken(accountEntity.getUsername());

    log.info("TOKEN:ACCESS:{} Access token: {}", accountEntity.getUsername(), accessToken);
    redisService.save("TOKEN:ACCESS:" + accountEntity.getUsername(), accessToken,
        authTokenServiceImpl.getAccessTokenExpiration(), TimeUnit.DAYS);
    log.info("TOKEN:REFRESH: {} Refresh token: {}",accountEntity.getUsername(), refreshToken);
    redisService.save("TOKEN:REFRESH:" + accountEntity.getUsername(), refreshToken,
        authTokenServiceImpl.getRefreshTokenExpiration(), TimeUnit.DAYS);

    return new LoginResponse(
        accessToken,
        refreshToken,
        authTokenServiceImpl.getAccessTokenExpiration(),
        authTokenServiceImpl.getRefreshTokenExpiration(),
        TokenTypeConstants.TOKEN_TYPE
    );
  }
}
