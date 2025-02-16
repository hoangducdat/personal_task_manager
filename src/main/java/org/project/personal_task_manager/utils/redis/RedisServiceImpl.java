package org.project.personal_task_manager.utils.redis;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(String key, Object value, long timeout, TimeUnit unit) {
        log.info("Save key: {} value: {} with timeout: {} ms", key, value, timeout);
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    @Override
    public void save(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    @Override
    public void saveExpire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

    @Override
    public Optional<Object> get(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    @Override
    public Optional<Object> get(String key, String hashKey) {
        return Optional.ofNullable(redisTemplate.opsForHash().get(key, hashKey));
    }

    @Override
    public void delete(String key, String hashKey) {
        redisTemplate.opsForHash().delete(key, hashKey);
    }
    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}

