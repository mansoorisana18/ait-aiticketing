package com.aiticketing.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiClientConfig {
	
    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}