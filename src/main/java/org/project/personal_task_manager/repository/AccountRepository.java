package org.project.personal_task_manager.repository;

import java.util.Optional;
import org.project.personal_task_manager.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<AccountEntity,String> {
  boolean existsByUsername(String username);

  Optional<AccountEntity> findByUsername(String username);
}
