package com.cyp.cypairolechat.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class AiRoleChatServiceTest {
    @Resource
    private AiRoleChatService aiRoleChatService;
    @Test
    void aiChat() {
        String message = aiRoleChatService.aiChat("你是谁");
        Assertions.assertNotNull(message);
    }
}