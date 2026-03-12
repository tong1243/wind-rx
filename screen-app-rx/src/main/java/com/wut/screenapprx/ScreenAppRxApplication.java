package com.wut.screenapprx;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAsync
@EnableKafka
@EnableRabbit
@EnableScheduling
@EnableTransactionManagement
@EnableMongoRepositories("com.wut.screendbmongorx.Repository")
@MapperScan("com.wut.screendbmysqlrx.Mapper")
@ComponentScan(basePackages = {
        "com.wut.screenapprx",
        "com.wut.screencommonrx",
        "com.wut.screenmsgrx",
        "com.wut.screendbmysqlrx",
        "com.wut.screendbredisrx",
        "com.wut.screendbtdenginerx",
        "com.wut.screendbmongorx",
        "com.wut.screenfusionrx"
})
public class ScreenAppRxApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScreenAppRxApplication.class, args);
    }

}
