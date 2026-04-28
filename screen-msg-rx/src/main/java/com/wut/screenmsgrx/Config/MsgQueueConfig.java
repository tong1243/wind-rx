package com.wut.screenmsgrx.Config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.wut.screencommonrx.Static.MsgModuleStatic.*;

@Configuration
public class MsgQueueConfig {
    @Bean("queueFlush")
    public Queue queueFlush() {
        return new Queue(QUEUE_NAME_FLUSH);
    }

    @Bean("queueFusion")
    public Queue queueFusion() {
        return new Queue(QUEUE_NAME_FUSION);
    }

}
