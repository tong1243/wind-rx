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

    @Bean("queueSection")
    public Queue queueSection() {
        return new Queue(QUEUE_NAME_SECTION);
    }

    @Bean("queueEvent")
    public Queue queueEvent() {
        return new Queue(QUEUE_NAME_EVENT);
    }

    @Bean("queuePosture")
    public Queue queuePosture() {
        return new Queue(QUEUE_NAME_POSTURE);
    }

    @Bean("queueDevice")
    public Queue queueDevice() {
        return new Queue(QUEUE_NAME_DEVICE);
    }
    @Bean("risk")
    public Queue queueRisk() {
        return new Queue(QUEUE_NAME_RISK);
    }
}
