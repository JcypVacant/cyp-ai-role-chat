package com.cyp.cypairolechat.baiduyun;

import com.cyp.cypairolechat.ai.AiRoleChatService;
import com.cyp.cypairolechat.manager.CosManager;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
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
    private  AsrService asrService;
    @Resource
    private  AiRoleChatService aiRoleChatService;
    @Resource
    private  TtsService ttsService;
    @Resource
    private  CosManager cosManager;

    public String handleVoiceChat(MultipartFile file) {
        try {
            // 1. 保存临时文件
            File tempFile = File.createTempFile("voice_", ".wav");
            file.transferTo(tempFile);

            // 2. 调用百度ASR
            String userText = asrService.recognize(tempFile);
            log.info("ASR识别结果: {}", userText);

            // 3. 调用 AI 生成回复
            String aiReply = aiRoleChatService.aiChat(userText);
            log.info("AI 回复: {}", aiReply);

            // 4. 调用百度TTS合成
            File ttsFile = ttsService.synthesize(aiReply);

            // 5. 上传到 COS
            String key = "voice-reply/" + UUID.randomUUID() + ".mp3";
            String url = cosManager.uploadFile(key, ttsFile);

            // 6. 写入 DB
//            ChatHistory chat = new ChatHistory();
//            chat.setUserMessage(userText);
//            chat.setAiMessage(aiReply);
//            chat.setAudioUrl(url);
//            chatHistoryMapper.insert(chat);

            return url;
        } catch (IOException e) {
            log.error("处理语音对话失败", e);
            throw new RuntimeException("处理语音对话失败", e);
        }
    }
}
