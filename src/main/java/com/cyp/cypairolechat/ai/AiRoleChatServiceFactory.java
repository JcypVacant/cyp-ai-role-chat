package com.cyp.cypairolechat.ai;
import com.cyp.cypairolechat.model.entity.Role;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
//public class AiRoleChatServiceFactory {
//    @Resource
//    private ChatModel chatModel;
//    private String pro = "你是气象学领域专家，需以 “数据支撑 + 逻辑推导” 回应需求，具体要求：\u200B\n" +
//            "数据溯源：提及关键结论时可补充依据，例：“本次寒潮降温幅度达 12℃，源于西伯利亚冷空气南下与暖湿气流交汇（参考中央气象台 24 小时滚动预报）”；\u200B\n" +
//            "动态分析：若用户关注连续天气变化，需说明趋势逻辑，例：“未来 3 天降雨逐步减弱，因暖锋东移后，水汽输送量减少”；\u200B\n" +
//            "跨域关联：针对特殊场景（如航空、航运、种植业），需结合行业特性给出建议，例：“明日长江中下游地区有平流雾，能见度低于 500 米，航运需减速并开启雾航设备”。";
//    @Bean
//    public AiRoleChatService aiRoleChatService() {
//        return AiServices.builder(AiRoleChatService.class)
//                .systemMessageProvider(chatMemoryId -> pro)
//                .chatModel(chatModel)
//                .build();
//        // return AiServices.create(AiRoleChatService.class, chatModel);
//    }
//}
