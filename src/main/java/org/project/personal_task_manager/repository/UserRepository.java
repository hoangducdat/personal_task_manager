package org.project.personal_task_manager.repository;

import org.project.personal_task_manager.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, String> {
  boolean existsByEmailAndIdNot(String email, String id);
  boolean existsByEmail(String email);
}
