package com.cyp.cypairolechat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyp.cypairolechat.model.entity.ChatHistory;
import com.cyp.cypairolechat.service.ChatHistoryService;
import com.cyp.cypairolechat.mapper.ChatHistoryMapper;
import org.springframework.stereotype.Service;

/**
* @author Jcyp
* @description 针对表【chat_history(角色对话历史表（支持多用户与单角色聊天，适配文本/语音消息）)】的数据库操作Service实现
* @createDate 2025-09-23 20:32:02
*/
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>
    implements ChatHistoryService{

}




