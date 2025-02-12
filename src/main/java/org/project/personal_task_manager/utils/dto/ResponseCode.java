package org.project.personal_task_manager.utils.dto;

public enum ResponseCode {
    SUCCESS(200),
    BAD_REQUEST(400),
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500),
    CREATED(201),
    CONFLICT(409);

    private final int value;

    ResponseCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

