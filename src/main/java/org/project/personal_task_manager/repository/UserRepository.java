package org.project.personal_task_manager.repository;

import java.util.Optional;
import org.project.personal_task_manager.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, String> {
  Optional<UserEntity> findByEmail(String email);
  boolean existsByEmail(String email);
}
