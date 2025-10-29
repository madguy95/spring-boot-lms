package com.springjwt.core.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Service("redisService")
@Slf4j
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisService {

    private final ObjectMapper mapper;
    private final RedisTemplate<String, Object> jsonTemplate;
    private final StringRedisTemplate stringTemplate;

    public RedisService(ObjectMapper mapper,
                        @Qualifier("redisTemplate") RedisTemplate<String, Object> jsonTemplate,
                        StringRedisTemplate stringTemplate) {
        this.mapper = mapper;
        this.jsonTemplate = jsonTemplate;
        this.stringTemplate = stringTemplate;
    }

    public List<String> getKeys(final String pattern) {
        Set<String> redisKeys = stringTemplate.keys(pattern);
        return redisKeys == null ? List.of() : new ArrayList<>(redisKeys);
    }

    public Optional<String> getFirstKey(final String pattern) {
        List<String> keys = getKeys(pattern);
        return keys.isEmpty() ? Optional.empty() : Optional.of(keys.getFirst());
    }

    public Optional<String> getLastKey(final String pattern) {
        List<String> keys = getKeys(pattern);
        return keys.isEmpty() ? Optional.empty() : Optional.of(keys.getLast());
    }

    public List<String> getKeysReversed(final String pattern) {
        return getKeys(pattern).reversed();
    }

    public Object getValue(final String key) {
        return stringTemplate.opsForValue().get(key);
    }

    public Object getValue(final String key, Class<?> clazz) {
        Object obj = jsonTemplate.opsForValue().get(key);
        return mapper.convertValue(obj, clazz);
    }

    public void setValue(final String key, final Object value) {
        setValue(key, value, TimeUnit.HOURS, 5, false);
    }

    public void setValue(final String key, final Object value, boolean marshal) {
        if (marshal) {
            jsonTemplate.opsForValue().set(key, value);
            jsonTemplate.expire(key, 5, TimeUnit.MINUTES);
        } else {
            stringTemplate.opsForValue().set(key, String.valueOf(value));
            stringTemplate.expire(key, 5, TimeUnit.MINUTES);
        }
    }

    public void setValue(final String key, final Object value, TimeUnit unit, long timeout) {
        setValue(key, value, unit, timeout, false);
    }

    public void setValue(final String key, final Object value, TimeUnit unit, long timeout, boolean marshal) {
        if (marshal) {
            jsonTemplate.opsForValue().set(key, value);
            jsonTemplate.expire(key, timeout, unit);
        } else {
            stringTemplate.opsForValue().set(key, String.valueOf(value));
            stringTemplate.expire(key, timeout, unit);
        }
    }

    public boolean removeKey(String key) {
        return Boolean.TRUE.equals(stringTemplate.delete(key));
    }

    public Map<String, Object> getFirstNValues(String pattern, int n) {
        List<String> keys = getKeys(pattern);
        Map<String, Object> result = new LinkedHashMap<>();

        // Take first N keys and get their values
        keys.stream()
                .limit(n)
                .forEach(key -> result.put(key, getValue(key)));

        return result;
    }

    public Map<String, Object> getLastNValues(String pattern, int n) {
        List<String> keys = getKeys(pattern);
        Map<String, Object> result = new LinkedHashMap<>();

        // Get last N keys using reversed() and take first N
        keys.reversed()
                .stream()
                .limit(n)
                .forEach(key -> result.put(key, getValue(key)));

        return result;
    }
}

