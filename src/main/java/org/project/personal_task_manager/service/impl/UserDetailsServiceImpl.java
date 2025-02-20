package org.project.personal_task_manager.service.impl;

import java.util.Collections;
import org.project.personal_task_manager.entity.AccountEntity;
import org.project.personal_task_manager.exception.UsernameNotFoundException;
import org.project.personal_task_manager.repository.AccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
  private final AccountRepository accountRepository;

  public UserDetailsServiceImpl(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String userId) {
    AccountEntity account = accountRepository.findByUserId(userId)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

    return new User(account.getUsername(), account.getPassword(), Collections.emptyList());
  }
}
