package org.springpractice.moneytransferapi.util;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisConnectionTest implements CommandLineRunner {

    private final StringRedisTemplate redisTemplate;

    public RedisConnectionTest(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            String key = "ping";
            String value = "pong";
            redisTemplate.opsForValue().set(key, value);
            System.out.println("successfully set key '" + key + "' in Redis.");

            String retrievedValue = redisTemplate.opsForValue().get(key);
            System.out.println("successfully retrieved value: '" + retrievedValue + "'");

            if (value.equals(retrievedValue)) {
                System.out.println(">>>>> REDIS CONNECTION SUCCESS!!!");
            } else {
                System.out.println(">>>>> REDIS MISMATCHED VALUES!!!");
            }
        } catch (Exception e) {
            System.err.println(">>>>> REDIS CONNECTION FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}