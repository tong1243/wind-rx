package com.wut.screenmsgrx.Consumer;

import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screenmsgrx.Service.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.wut.screencommonrx.Static.MsgModuleStatic.*;

@Component
public class MsgDataConsumer {
    private final FiberParseService fiberParseService;
    private final DateTimeParseService dateTimeParseService;

    @Autowired
    public MsgDataConsumer(FiberParseService fiberParseService, DateTimeParseService dateTimeParseService) {
        this.fiberParseService = fiberParseService;
        this.dateTimeParseService = dateTimeParseService;
    }



    @KafkaListener(topics = "fiber", groupId = "group-fiber")
    public void fiberDataListener(List<ConsumerRecord> records, Acknowledgment ack) {
        List<CompletableFuture<Void>> futures = new ArrayList<>(records.size());
        for (ConsumerRecord record : records) {
            String fiberDataStr = record.value().toString();
            futures.add(fiberParseService.collectFiberData(fiberDataStr));
        }
        if (!futures.isEmpty()) {
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        }
        ack.acknowledge();
    }

    @KafkaListener(topics = "timestamp", groupId = "group-timestamp")
    public void timestampDataListener(List<ConsumerRecord> records, Acknowledgment ack) {
        for (ConsumerRecord record : records) {
            String timestampStr = record.value().toString();
//            MessagePrintUtil.printListenerReceive(TOPIC_NAME_TIMESTAMP, timestampStr);
            dateTimeParseService.collectTimestampData(timestampStr);
        }
        ack.acknowledge();
    }

    @KafkaListener(topics = "direct", groupId = "group-direct")
    public void directDataListener(List<ConsumerRecord> records, Acknowledgment ack) {
        for (ConsumerRecord record : records) {}
        ack.acknowledge();
    }

}
