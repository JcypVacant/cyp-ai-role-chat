package com.cyp.cypairolechat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableCaching
public class CypAiRoleChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(CypAiRoleChatApplication.class, args);
    }

}
