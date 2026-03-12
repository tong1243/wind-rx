package com.wut.screenfusionrx.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class FusionThreadPoolConfig {
    @Bean("fusionTaskModelFlushAsyncPool")
    public Executor fusionTaskModelFlushAsyncPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(20);
        executor.setKeepAliveSeconds(10);
        executor.setThreadNamePrefix("FUSION MODULE MODEL FLUSH EXECUTOR-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("fusionTaskModelPreFlushAsyncPool")
    public Executor fusionTaskModelPreFlushAsyncPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(150);
        executor.setMaxPoolSize(150);
        executor.setQueueCapacity(20);
        executor.setKeepAliveSeconds(10);
        executor.setThreadNamePrefix("FUSION MODULE MODEL PRE FLUSH EXECUTOR-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("fusionTaskModelPreFlushProcessAsyncPool")
    public Executor fusionTaskModelPreFlushProcessAsyncPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(300);
        executor.setMaxPoolSize(300);
        executor.setQueueCapacity(20);
        executor.setKeepAliveSeconds(10);
        executor.setThreadNamePrefix("FUSION MODULE MODEL PRE FLUSH EXECUTOR-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("fusionTaskTrajFusionTimerAsyncPool")
    public Executor fusionTaskTrajFusionTimerAsyncPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(20);
        executor.setKeepAliveSeconds(10);
        executor.setThreadNamePrefix("FUSION MODULE TRAJ FUSION TIMER EXECUTOR-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("fusionTaskEventTimerAsyncPool")
    public Executor fusionTaskEventTimerAsyncPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(20);
        executor.setKeepAliveSeconds(10);
        executor.setThreadNamePrefix("FUSION MODULE EVENT TIMER EXECUTOR-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("fusionTaskTrajFusionAsyncPool")
    public Executor fusionTaskTrajFusionAsyncPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(20);
        executor.setKeepAliveSeconds(10);
        executor.setThreadNamePrefix("FUSION MODULE TRAJ FUSION EXECUTOR-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("fusionTaskSectionAsyncPool")
    public Executor fusionTaskSectionAsyncPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(20);
        executor.setKeepAliveSeconds(7200);
        executor.setThreadNamePrefix("FUSION MODULE SECTION EXECUTOR-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("fusionTaskPostureAsyncPool")
    public Executor fusionTaskPostureAsyncPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(20);
        executor.setKeepAliveSeconds(600);
        executor.setThreadNamePrefix("FUSION MODULE POSTURE EXECUTOR-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    @Bean("fusionTaskRiskAsyncPool")
    public Executor fusionTaskRiskAsyncPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(20);
        executor.setKeepAliveSeconds(10);
        executor.setThreadNamePrefix("FUSION MODULE RISK EXECUTOR-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("fusionTaskEventAsyncPool")
    public Executor fusionTaskEventAsyncPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(20);
        executor.setKeepAliveSeconds(10);
        executor.setThreadNamePrefix("FUSION MODULE EVENT EXECUTOR-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("fusionTaskDeviceAsyncPool")
    public Executor fusionTaskDeviceAsyncPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(20);
        executor.setKeepAliveSeconds(600);
        executor.setThreadNamePrefix("FUSION MODULE DEVICE EXECUTOR-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("fusionTaskDeviceSecAsyncPool")
    public Executor fusionTaskDeviceSecAsyncPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(20);
        executor.setKeepAliveSeconds(7200);
        executor.setThreadNamePrefix("FUSION MODULE DEVICE SECTION EXECUTOR-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

}
