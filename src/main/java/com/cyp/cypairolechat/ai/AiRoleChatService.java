package com.cyp.cypairolechat.ai;

import dev.langchain4j.service.SystemMessage;

public interface AiRoleChatService {
    //@SystemMessage(fromResource = "prompt/sugeladi-role-system-prompt.txt")
    String aiChat(String message);
}
