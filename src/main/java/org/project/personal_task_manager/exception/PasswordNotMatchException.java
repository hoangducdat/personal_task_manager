package org.project.personal_task_manager.exception;

import org.springframework.http.HttpStatus;

public class PasswordNotMatchException extends RuntimeException {
    public PasswordNotMatchException(String message) {
        super(message);
    }
}
