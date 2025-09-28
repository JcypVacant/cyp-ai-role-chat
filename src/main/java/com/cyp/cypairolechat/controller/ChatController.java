package com.cyp.cypairolechat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyp.cypairolechat.baiduyun.ChatPipelineService;
import com.cyp.cypairolechat.common.BaseResponse;
import com.cyp.cypairolechat.common.ResultUtils;
import com.cyp.cypairolechat.model.entity.ChatHistory;
import com.cyp.cypairolechat.service.ChatHistoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/chat")
public class ChatController {
    @Resource
    private ChatPipelineService chatPipelineService;
    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * 上传音频并进行对话
     */
    @PostMapping("/voice")
    public BaseResponse<String> chatByVoice(@RequestParam("file") MultipartFile file,@RequestParam("roleId") Long roleId,
                                            @RequestParam("userId") Long userId) {
        return ResultUtils.success(chatPipelineService.handleVoiceChat(file, userId, roleId));
    }
    @GetMapping("/history")
    public BaseResponse<Page<ChatHistory>> getChatHistory(
            @RequestParam("roleId") Long roleId,
            @RequestParam("userId") Long userId,
            @RequestParam(required = false, defaultValue = "1") int current,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        // 创建分页对象
        Page<ChatHistory> page = new Page<>(current, pageSize);

        // 分页查询（按时间升序，最新的在后面）
        Page<ChatHistory> resultPage = chatHistoryService.page(page,
                new LambdaQueryWrapper<ChatHistory>()
                        .eq(ChatHistory::getRoleId, roleId)
                        .eq(ChatHistory::getUserId, userId)
                        .eq(ChatHistory::getIsDelete, 0)
                        .orderByAsc(ChatHistory::getCreateTime)
        );

        return ResultUtils.success(resultPage);
    }
}
