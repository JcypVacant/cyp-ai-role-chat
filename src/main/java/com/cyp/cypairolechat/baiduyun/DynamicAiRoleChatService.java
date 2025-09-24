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
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
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

    /**
     * 角色对话
     * @param roleId
     * @param userId
     * @param userText
     * @return
     */
    public String chatWithRole(Long roleId, Long userId, String userText) {
        // 1.获取角色 systemPrompt
        Role role = roleService.getById(roleId);
        if (role == null) throw new RuntimeException("角色不存在: " + roleId);
        String systemPrompt = role.getSystemPrompt();

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
}
