package org.project.personal_task_manager.repository;

import org.project.personal_task_manager.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<TaskEntity, String> {

}
