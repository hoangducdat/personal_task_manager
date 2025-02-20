package org.project.personal_task_manager.utils.redis;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public interface RedisService {
    void save(String key, Object value, long timeout, TimeUnit unit);

    void save(String key, String hashKey, Object value);

    Optional<Object> get(String key);

    Optional<Object> get(String key, String hashKey);

    void delete(String key,String hashKey);

    void delete(String key);
}
