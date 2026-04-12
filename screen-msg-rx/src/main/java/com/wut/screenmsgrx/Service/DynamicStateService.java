package com.wut.screenmsgrx.Service;

import com.wut.screendbmysqlrx.Service.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.wut.screencommonrx.Static.FusionModuleStatic.FUSION_TIME_INTER;
import static com.wut.screencommonrx.Static.MsgModuleStatic.QUEUE_DEFAULT_EXCHANGE;
import static com.wut.screencommonrx.Static.MsgModuleStatic.QUEUE_NAME_FUSION;

@Component
public class DynamicStateService {
    @Qualifier("msgTimestampTaskAsyncPool")
    private final Executor msgTimestampTaskAsyncPool;
    private final TrajService trajService;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public DynamicStateService(Executor msgTimestampTaskAsyncPool, TrajService trajService, RabbitTemplate rabbitTemplate) {
        this.msgTimestampTaskAsyncPool = msgTimestampTaskAsyncPool;
        this.trajService = trajService;
        this.rabbitTemplate = rabbitTemplate;
    }

    // 初次接收发送端时间戳时,执行初始化表结构操作
    public CompletableFuture<Void> initDynamicTable(String time) {
        return CompletableFuture.allOf(
                initTrajTable(time)
//                initSectionTable(time),
//                initEventTable(time),
//                initPostureTable(time),
//                initFiberMetricTable(time),
//                initRadarMetricTable(time),
//                initFiberSecMetricTable(time),
//                initRadarSecMetricTable(time),
//                initRadarAllSecMetricTable(time),
//                initParametersTable(time),
//                initBottleneckAreaStateTable(time),
//                initTnnelRiskTable(time),
//                initRiskEventTable(time)
        );
    }

    // 接收终止时间的发送端时间戳时,推送剩余数据
    public CompletableFuture<Void> flushDynamicContext(long timestamp) {
        return CompletableFuture.runAsync(() -> {
            rabbitTemplate.convertAndSend(QUEUE_DEFAULT_EXCHANGE, QUEUE_NAME_FUSION, Long.toString(timestamp + FUSION_TIME_INTER));
            rabbitTemplate.convertAndSend(QUEUE_DEFAULT_EXCHANGE, QUEUE_NAME_FUSION, Long.toString(timestamp + (2 * FUSION_TIME_INTER)));
            rabbitTemplate.convertAndSend(QUEUE_DEFAULT_EXCHANGE, QUEUE_NAME_FUSION, Long.toString(timestamp + (3 * FUSION_TIME_INTER)));
        }, msgTimestampTaskAsyncPool);
    }

    public CompletableFuture<Void> initTrajTable(String time) {
        return CompletableFuture.runAsync(() -> {
            trajService.dropTable(time);
            trajService.createTable(time);
        }, msgTimestampTaskAsyncPool);
    }

//    public CompletableFuture<Void> initSectionTable(String time) {
//        return CompletableFuture.runAsync(() -> {
//            sectionService.dropTable(time);
//            sectionService.createTable(time);
//        }, msgTimestampTaskAsyncPool);
//    }

//    public CompletableFuture<Void> initEventTable(String time) {
//        return CompletableFuture.runAsync(() -> {
//            carEventService.dropTable(time);
//            carEventService.createTable(time);
//        }, msgTimestampTaskAsyncPool);
//    }
//
//    public CompletableFuture<Void> initPostureTable(String time) {
//        return CompletableFuture.runAsync(() -> {
//            postureService.dropTable(time);
//            postureService.createTable(time);
//        }, msgTimestampTaskAsyncPool);
//    }
//
//    public CompletableFuture<Void> initFiberMetricTable(String time) {
//        return CompletableFuture.runAsync(() -> {
//            fiberMetricService.dropTable(time);
//            fiberMetricService.createTable(time);
//        }, msgTimestampTaskAsyncPool);
//    }
//
//    public CompletableFuture<Void> initRadarMetricTable(String time) {
//        return CompletableFuture.runAsync(() -> {
//            radarMetricService.dropTable(time);
//            radarMetricService.createTable(time);
//        }, msgTimestampTaskAsyncPool);
//    }
//
//    public CompletableFuture<Void> initFiberSecMetricTable(String time) {
//        return CompletableFuture.runAsync(() -> {
//            fiberSecMetricService.dropTable(time);
//            fiberSecMetricService.createTable(time);
//        }, msgTimestampTaskAsyncPool);
//    }
//
//    public CompletableFuture<Void> initRadarSecMetricTable(String time) {
//        return CompletableFuture.runAsync(() -> {
//            radarSecMetricService.dropTable(time);
//            radarSecMetricService.createTable(time);
//        }, msgTimestampTaskAsyncPool);
//    }
//
//    public CompletableFuture<Void> initRadarAllSecMetricTable(String time) {
//        return CompletableFuture.runAsync(() -> {
//            radarAllSecMetricService.dropTable(time);
//            radarAllSecMetricService.createTable(time);
//        }, msgTimestampTaskAsyncPool);
//    }
////    private CompletableFuture<Void> initRiskTable(String time) {
////        return CompletableFuture.runAsync(() -> {
////            riskService.dropTable(time);
////            riskService.createTable(time);
////        }, msgTimestampTaskAsyncPool);
////    }
//private CompletableFuture<Void> initParametersTable(String time) {
//    return CompletableFuture.runAsync(() -> {
//        parametersService.dropTable(time);
//        parametersService.createTable(time);
//    }, msgTimestampTaskAsyncPool);
//}
//    private CompletableFuture<Void> initBottleneckAreaStateTable(String time) {
//        return CompletableFuture.runAsync(() -> {
//            bottleneckAreaStateService.dropTable(time);
//            bottleneckAreaStateService.createTable(time);
//        }, msgTimestampTaskAsyncPool);
//    }
//    private CompletableFuture<Void> initTnnelRiskTable(String time) {
//        return CompletableFuture.runAsync(() -> {
//            tunnelRiskService.dropTable(time);
//            tunnelRiskService.createTable(time);
//        }, msgTimestampTaskAsyncPool);
//    }
//    private CompletableFuture<Void> initRiskEventTable(String time) {
//        return CompletableFuture.runAsync(() -> {
//            riskEventService.dropTable(time);
//            riskEventService.createTable(time);
//        }, msgTimestampTaskAsyncPool);
//    }
}
