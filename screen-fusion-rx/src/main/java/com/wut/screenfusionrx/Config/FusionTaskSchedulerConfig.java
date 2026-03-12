package com.wut.screenfusionrx.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static com.wut.screencommonrx.Static.FusionModuleStatic.TASK_AWAIT;
import static com.wut.screencommonrx.Static.FusionModuleStatic.TASK_POOL_SIZE;

@Configuration
public class FusionTaskSchedulerConfig {
    @Bean("trajFusionTaskScheduler")
    public ThreadPoolTaskScheduler trajFusionTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(TASK_POOL_SIZE);
        scheduler.setThreadNamePrefix("TRAJ FUSION TASK SCHEDULER-");
        scheduler.setAwaitTerminationSeconds(TASK_AWAIT);
        scheduler.initialize();
        return scheduler;
    }

    @Bean("eventDataTaskScheduler")
    public ThreadPoolTaskScheduler eventDataTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(TASK_POOL_SIZE);
        scheduler.setThreadNamePrefix("EVENT DATA TASK SCHEDULER-");
        scheduler.setAwaitTerminationSeconds(TASK_AWAIT);
        scheduler.initialize();
        return scheduler;
    }

}
