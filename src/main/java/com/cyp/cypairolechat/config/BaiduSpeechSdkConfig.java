package com.cyp.cypairolechat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.baidu.aip.speech.AipSpeech;
@Data
@Configuration
@ConfigurationProperties(prefix = "baidu.speech")
public class BaiduSpeechSdkConfig {

    private String appId;
    private String apiKey;
    private String secretKey;

    @Bean
    public AipSpeech aipSpeechClient() {
        // 1. 初始化一个AipSpeech客户端
        AipSpeech client = new AipSpeech(appId, apiKey, secretKey);

        // 2. 可选：设置网络连接参数（建议设置）
        client.setConnectionTimeoutInMillis(20000);
        client.setSocketTimeoutInMillis(60000);

        // 3. 可选：设置代理（如果您的服务器需要代理访问外网）
        // client.setHttpProxy("proxy_host", proxy_port);

        return client;
    }
}
