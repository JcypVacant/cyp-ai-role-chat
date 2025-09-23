package com.cyp.cypairolechat.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiRoleChatServiceFactory {
    @Resource
    private ChatModel chatModel;
    @Bean
    public AiRoleChatService createAiRoleChatService() {
        return AiServices.create(AiRoleChatService.class, chatModel);
    }
}
