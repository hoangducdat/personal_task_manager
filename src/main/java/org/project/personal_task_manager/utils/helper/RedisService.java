package org.project.personal_task_manager.utils.helper;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public interface RedisService {
    void save(String key, Object value, long timeout, TimeUnit unit);

    Optional<Object> get(String key);
}
