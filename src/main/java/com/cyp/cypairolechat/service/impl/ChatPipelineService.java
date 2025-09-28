package com.cyp.cypairolechat.service.impl;

import com.cyp.cypairolechat.baiduyun.AsrService;
import com.cyp.cypairolechat.baiduyun.TtsService;
import com.cyp.cypairolechat.manager.CosManager;
import com.cyp.cypairolechat.model.entity.ChatHistory;
import com.cyp.cypairolechat.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class ChatPipelineService {
    @Resource
    private AsrService asrService;
    @Resource
    private TtsService ttsService;
    @Resource
    private  CosManager cosManager;
    @Resource
    private DynamicAiRoleChatService dynamicAiRoleChatService;
    @Resource
    private ChatHistoryService chatHistoryService;


    public String handleVoiceChat(MultipartFile file, Long userId, Long roleId) {
        try {
            // 1. 保存临时文件
            File tempFile = File.createTempFile("voice_", ".wav");
            file.transferTo(tempFile);
            // 2️. ASR 识别
            String userText = asrService.recognize(tempFile);
            log.info("ASR 识别: {}", userText);

            // 3️. 上传用户语音到 COS
            String userVoiceKey = "voice-user/" + UUID.randomUUID() + ".wav";
            String userVoiceUrl = cosManager.uploadFile(userVoiceKey, tempFile);

            // 4. 写入用户消息到DB
            ChatHistory userChat = new ChatHistory();
            userChat.setMessage(userText);
            userChat.setMessageType("user");
            userChat.setMessageFormat("voice");
            userChat.setVoiceUrl(userVoiceUrl);
            userChat.setRoleId(roleId);
            userChat.setUserId(userId);
            chatHistoryService.save(userChat);

            // 5. 调用 AI 生成回复
            String aiReply = dynamicAiRoleChatService.chatWithRole(roleId, userId, userText);
            // String aiReply = aiRoleChatService.aiChat(userText);
            log.info("AI 回复: {}", aiReply);

            // 4. 调用百度TTS合成
            File ttsFile = ttsService.synthesize(aiReply);

            // 5. 上传到 COS
            String key = "voice-reply/" + UUID.randomUUID() + ".mp3";
            String url = cosManager.uploadFile(key, ttsFile);

            // 6. 写入 DB
            ChatHistory chat = new ChatHistory();
            chat.setMessage(aiReply);
            chat.setMessageType("ai");
            chat.setMessageFormat("voice");
            chat.setVoiceUrl(url);
            chat.setRoleId(roleId);
            chat.setUserId(userId);
            chatHistoryService.save(chat);

            return url;
        } catch (IOException e) {
            log.error("处理语音对话失败", e);
            throw new RuntimeException("处理语音对话失败", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
