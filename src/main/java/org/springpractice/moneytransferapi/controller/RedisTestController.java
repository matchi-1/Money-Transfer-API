package org.springpractice.moneytransferapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/redis")
public class RedisTestController {

    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public RedisTestController(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/ping")
    public String ping() {
        try {
            redisTemplate.opsForValue().set("redis-test-key", "connected");
            String result = redisTemplate.opsForValue().get("redis-test-key");
            return "Redis connected. Value: " + result;
        } catch (Exception e) {
            return "Redis ERROR: " + e.getMessage();
        }
    }
}
