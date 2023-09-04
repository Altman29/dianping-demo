package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {
    /**
     * 开始时间戳
     */
    private static final long BEGIN_TIMESTAMP = 1640995200L;
    /**
     * 序列号的位数
     */
    private static final int COUNT_BITS = 32;

    private StringRedisTemplate stringRedisTemplate;

    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @SuppressWarnings("all")
    public long nextId(String keyPrefix) {
        LocalDateTime now = null;
        long timestamp = 0;
        try {
            // 1.生成时间戳
            now = LocalDateTime.now();
            long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
            timestamp = nowSecond - BEGIN_TIMESTAMP;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 2.生成序列号
        // 2.1.获取当前日期，精确到天 易于统计
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        // 2.2.自增长
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);

        // 3.拼接并返回 时间戳左移，和自增的值取或，即可形成 64 位唯一id
        return timestamp << COUNT_BITS | count;
    }
}
