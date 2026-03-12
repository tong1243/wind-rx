package com.wut.screenmsgrx.Consumer;

import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screenmsgrx.Service.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

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
        for (ConsumerRecord record : records) {
            String fiberDataStr = record.value().toString();
            fiberParseService.collectFiberData(fiberDataStr).thenRunAsync(() -> {
//                MessagePrintUtil.printListenerReceive(TOPIC_NAME_FIBER, fiberDataStr);
            });
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
