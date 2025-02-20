package org.project.personal_task_manager.repository;

import java.util.Optional;
import org.project.personal_task_manager.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, String> {
  Optional<UserEntity> findByEmail(String email);
  boolean existsByEmail(String email);

  @Query("SELECT a.username FROM AccountEntity a WHERE a.userId = :userId")
  String findUsernameByUserId(@Param("userId") String userId);
}
