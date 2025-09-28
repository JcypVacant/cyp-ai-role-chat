package com.cyp.cypairolechat.baiduyun;

import com.cyp.cypairolechat.service.impl.DynamicAiRoleChatService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DynamicAiRoleChatServiceTest {

    @Resource
    private DynamicAiRoleChatService dynamicAiRoleChatService;
    @Test
    void chatWithRole() {
        String chat =dynamicAiRoleChatService.chatWithRole(1L, 1970299962884169729L, "最近有什么台风天气吗？");
        System.out.println(chat);
    }
}