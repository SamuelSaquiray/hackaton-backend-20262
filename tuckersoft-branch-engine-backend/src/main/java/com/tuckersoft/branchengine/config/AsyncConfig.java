package com.tuckersoft.branchengine.config;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {
    @Bean(name="branchExecutor")
    public Executor branchExecutor(){
        ThreadPoolTaskExecutor e=new ThreadPoolTaskExecutor();
        e.setCorePoolSize(2);e.setMaxPoolSize(4);e.setQueueCapacity(50);e.setThreadNamePrefix("branch-worker-");e.initialize();return e;
    }
}
