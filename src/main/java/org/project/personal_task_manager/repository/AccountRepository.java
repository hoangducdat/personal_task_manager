package org.project.personal_task_manager.repository;

import java.util.Optional;
import org.project.personal_task_manager.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<AccountEntity,String> {
  boolean existsByUsername(String username);

  Optional<AccountEntity> findByUsername(String username);

  @Query("SELECT a FROM AccountEntity a JOIN UserEntity u ON a.userId = u.id WHERE u.email = :email")
  Optional<AccountEntity> findAccountByEmail(@Param("email") String email);

  Optional<AccountEntity> findByUserId(String userId);
}
