package com.wut.screendbredisrx.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class RedisThreadPoolConfig {
    @Bean("redisTaskAsyncPool")
    public Executor redisTaskAsyncPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(300);
        executor.setMaxPoolSize(300);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(10);
        executor.setThreadNamePrefix("REDIS MODULE EXECUTOR-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("redisExpireTaskAsyncPool")
    public Executor redisExpireTaskAsyncPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(10);
        executor.setKeepAliveSeconds(10);
        executor.setThreadNamePrefix("REDIS MODULE EXPIRE EXECUTOR-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

}
