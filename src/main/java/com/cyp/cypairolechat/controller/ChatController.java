package com.cyp.cypairolechat.controller;

import com.cyp.cypairolechat.baiduyun.ChatPipelineService;
import com.cyp.cypairolechat.common.BaseResponse;
import com.cyp.cypairolechat.common.ResultUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/chat")
public class ChatController {
    @Resource
    private ChatPipelineService chatPipelineService;

    /**
     * 上传音频并进行对话
     */
    @PostMapping("/voice")
    public BaseResponse<String> chatByVoice(@RequestParam("file") MultipartFile file,@RequestParam("roleId") Long roleId,
                                            @RequestParam("userId") Long userId) {
        return ResultUtils.success(chatPipelineService.handleVoiceChat(file, userId, roleId));
    }
}
