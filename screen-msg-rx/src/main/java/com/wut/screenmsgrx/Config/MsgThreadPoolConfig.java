package com.wut.screenmsgrx.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class MsgThreadPoolConfig {
    @Bean("msgTaskAsyncPool")
    public Executor msgTaskAsyncPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(100);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(10);
        executor.setKeepAliveSeconds(200);
        executor.setThreadNamePrefix("MESSAGE MODULE EXECUTOR-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("msgTimestampTaskAsyncPool")
    public Executor msgTimestampTaskAsyncPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(60);
        executor.setMaxPoolSize(60);
        executor.setQueueCapacity(10);
        executor.setKeepAliveSeconds(200);
        executor.setThreadNamePrefix("MESSAGE MODULE TIMESTAMP EXECUTOR-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

}
