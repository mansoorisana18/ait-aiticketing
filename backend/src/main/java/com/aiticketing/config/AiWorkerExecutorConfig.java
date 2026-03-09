package com.aiticketing.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiWorkerExecutorConfig {
	
	@Value("${aiticketing.ai.worker.thread-pool-size}")
	int threadPoolSize;
	
    @Bean(destroyMethod = "shutdown")
    ExecutorService aiWorkerExecutor() {
        return Executors.newFixedThreadPool(threadPoolSize);
    }
}