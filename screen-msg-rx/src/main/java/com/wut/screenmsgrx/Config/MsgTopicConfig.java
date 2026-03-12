package com.wut.screenmsgrx.Config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import static com.wut.screencommonrx.Static.MsgModuleStatic.*;

@Configuration
public class MsgTopicConfig {
    @Bean("topicPlate")
    public NewTopic topicPlate() {
        return TopicBuilder.name(TOPIC_NAME_PLATE).partitions(TOPIC_DEFAULT_PARTITIONS).replicas(TOPIC_DEFAULT_REPLICAS).build();
    }

    @Bean("topicFiber")
    public NewTopic topicFiber() {
        return TopicBuilder.name(TOPIC_NAME_FIBER).partitions(TOPIC_DEFAULT_PARTITIONS).replicas(TOPIC_DEFAULT_REPLICAS).build();
    }

    @Bean("topicLaser")
    public NewTopic topicLaser() {
        return TopicBuilder.name(TOPIC_NAME_LASER).partitions(TOPIC_DEFAULT_PARTITIONS).replicas(TOPIC_DEFAULT_REPLICAS).build();
    }

    @Bean("topicWave")
    public NewTopic topicWave() {
        return TopicBuilder.name(TOPIC_NAME_WAVE).partitions(TOPIC_DEFAULT_PARTITIONS).replicas(TOPIC_DEFAULT_REPLICAS).build();
    }

    @Bean("topicTimestamp")
    public NewTopic topicTimestamp() {
        return TopicBuilder.name(TOPIC_NAME_TIMESTAMP).partitions(1).replicas(TOPIC_DEFAULT_REPLICAS).build();
    }

    @Bean("topicTraj")
    public NewTopic topicTraj() {
        return TopicBuilder.name(TOPIC_NAME_TRAJ).partitions(1).replicas(TOPIC_DEFAULT_REPLICAS).build();
    }

    @Bean("topicDirect")
    public NewTopic topicDirect() {
        return TopicBuilder.name(TOPIC_NAME_DIRECT).partitions(TOPIC_DEFAULT_PARTITIONS).replicas(TOPIC_DEFAULT_REPLICAS).build();
    }

}
