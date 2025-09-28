package com.cyp.cypairolechat.baiduyun;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyp.cypairolechat.ai.AiRoleChatService;
import com.cyp.cypairolechat.model.entity.ChatHistory;
import com.cyp.cypairolechat.model.entity.Role;
import com.cyp.cypairolechat.service.ChatHistoryService;
import com.cyp.cypairolechat.service.RoleService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DynamicAiRoleChatService {

    @Resource
    private ChatModel chatModel;
    @Resource
    private RoleService roleService;
    @Resource
    private ChatHistoryService chatHistoryService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    // 使用一个固定的前缀，方便管理和清理缓存
    private static final String ROLE_SYSTEM_PROMPT_KEY_PREFIX = "role:system_prompt:";

    /**
     * 角色对话
     * @param roleId
     * @param userId
     * @param userText
     * @return
     */
    public String chatWithRole(Long roleId, Long userId, String userText) {
        // 1.获取角色 systemPrompt
        // 1. 从 Redis 获取 systemPrompt
        String systemPrompt = getSystemPromptFromCache(roleId);

        // 2.查询用户-角色历史消息（最近 10 条）通过 chatHistoryService
        List<ChatHistory> historyList = chatHistoryService.list(
                new LambdaQueryWrapper<ChatHistory>()
                        .eq(ChatHistory::getRoleId, roleId)   // 过滤当前角色
                        .eq(ChatHistory::getUserId, userId)   // 过滤当前用户
                        .orderByDesc(ChatHistory::getCreateTime) // 按时间倒序
                        .last("LIMIT 10")                      // 取最近 10 条
        );

        // 3.构建历史上下文（按时间升序排列，User/AI前缀）
        List<String> historyMessages = historyList.stream()
                .sorted(Comparator.comparing(ChatHistory::getCreateTime)) // 时间升序
                .map(chat -> {
                    // 根据消息来源添加前缀
                    if ("user".equals(chat.getMessageType())) {
                        return "User: " + chat.getMessage();
                    } else {
                        return "AI: " + chat.getMessage();
                    }
                })
                .collect(Collectors.toList());

        // 4.构建 LangChain4j 动态 systemMessageProvider
        AiRoleChatService chatService = AiServices.builder(AiRoleChatService.class)
                .chatModel(chatModel)
                .systemMessageProvider(chatMemoryId -> systemPrompt)
                .build();
        // 5.拼接 prompt + 历史消息 + 当前用户输入
        String prompt = String.join("\n", historyMessages) + "\nUser: " + userText + "\nAI: ";
        String aiReply = chatService.aiChat(prompt);

        log.info("AI 回复: {}", aiReply);
        return aiReply;
    }
    /**
     * 从缓存获取 systemPrompt，如果缓存不存在，则从数据库加载并写入缓存
     * @param roleId 角色ID
     * @return systemPrompt 字符串
     */
    private String getSystemPromptFromCache(Long roleId) {
        String cacheKey = ROLE_SYSTEM_PROMPT_KEY_PREFIX + roleId;

        // 1. 尝试从 Redis 缓存中获取
        Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
        if (cachedValue != null) {
            log.info("缓存命中，直接从 Redis 获取 systemPrompt, roleId: {}", roleId);
            return (String) cachedValue;
        }

        log.info("缓存未命中，将从数据库加载 systemPrompt, roleId: {}", roleId);
        // 2. 缓存不存在，从数据库获取
        Role role = roleService.getById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在: " + roleId);
        }
        String systemPrompt = role.getSystemPrompt();

        // 3. 将从数据库获取到的 systemPrompt 写入 Redis 缓存
        try {
            redisTemplate.opsForValue().set(cacheKey, systemPrompt, 24, TimeUnit.HOURS);
            log.info("成功将 systemPrompt 写入 Redis 缓存, roleId: {}", roleId);
        } catch (Exception e) {
            // 缓存写入失败不应影响主业务流程
            log.error("将 systemPrompt 写入 Redis 缓存失败, roleId: {}", roleId, e);
        }

        return systemPrompt;
    }
}
