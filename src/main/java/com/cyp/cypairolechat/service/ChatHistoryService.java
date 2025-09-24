package com.cyp.cypairolechat.service;

import com.cyp.cypairolechat.model.entity.ChatHistory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Jcyp
* @description 针对表【chat_history(角色对话历史表（支持多用户与单角色聊天，适配文本/语音消息）)】的数据库操作Service
* @createDate 2025-09-23 20:32:02
*/
public interface ChatHistoryService extends IService<ChatHistory> {

}
