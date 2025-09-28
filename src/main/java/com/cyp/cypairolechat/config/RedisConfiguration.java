package com.cyp.cypairolechat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始创建Redis模板对象，并配置JSON序列化...");

        // 1. 创建 RedisTemplate 实例
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // 2. 设置连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 3. 创建 JSON 序列化器
        // GenericJackson2JsonRedisSerializer 会在 JSON 中加入 @class 字段，方便反序列化
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        // 4. 设置 Key 和 Value 的序列化器
        // Key 使用 StringRedisSerializer，保证 key 是可读的字符串
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // Value 使用 JSON 序列化器，将对象转为 JSON 字符串存储
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);

        // 5. 初始化 RedisTemplate
        redisTemplate.afterPropertiesSet();

        log.info("Redis模板对象创建完成，已配置为JSON序列化。");
        return redisTemplate;
    }
}
