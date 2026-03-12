package com.wut.screenmsgrx.Service;

import com.wut.screencommonrx.Util.DateParamParseUtil;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

import static com.wut.screencommonrx.Static.MsgModuleStatic.*;

@Component
public class DateTimeParseService {
    @Qualifier("msgTimestampTaskAsyncPool")
    private final Executor msgTimestampTaskAsyncPool;
    private final DynamicStateService dynamicStateService;
    private final RabbitTemplate rabbitTemplate;
    // 发送任务的时间戳日期
    private static String MSG_CURRENT_DATE = null;
    // 发送任务的结束时间戳
    private static long MSG_END_TIMESTAMP = 0L;
    // 发送任务时间参数初始化标志位
    private static boolean MSG_TIME_INIT_FLAG = false;
    // 时间戳日期变动初始化并发锁
    private static final ReentrantLock MSG_DATE_LOCK = new ReentrantLock(true);

    @Autowired
    public DateTimeParseService(DynamicStateService dynamicStateService, RabbitTemplate rabbitTemplate, Executor msgTimestampTaskAsyncPool) {
        this.dynamicStateService = dynamicStateService;
        this.rabbitTemplate = rabbitTemplate;
        this.msgTimestampTaskAsyncPool = msgTimestampTaskAsyncPool;
    }

    @PostConstruct
    public void initDateTimeParam() {
        MSG_CURRENT_DATE = null;
        MSG_END_TIMESTAMP = 0L;
        MSG_TIME_INIT_FLAG = false;
    }

    // 收集发送端传来的原始数据时间戳信息
    public void collectTimestampData(String time) {
        if (checkTableState(time)) {
            sendTimestamp(time, MODEL_FLUSH_WAIT_TIME).thenRunAsync(() -> {});
        }
        if (MSG_END_TIMESTAMP == Long.parseLong(time)) {
            dynamicStateService.flushDynamicContext(MSG_END_TIMESTAMP).thenRunAsync(() -> {});
        }
    }

    // 检查是否需要进行建表等初始化操作
    public boolean checkTableState(String time) {
        if (MSG_TIME_INIT_FLAG) { return true; }
        try {
            MSG_DATE_LOCK.lock();
            if (!MSG_TIME_INIT_FLAG) {
                MSG_CURRENT_DATE = DateParamParseUtil.getDateTimeStr(Long.parseLong(time));
                // 初始化数据表的操作必须要保证时间戳N的发送顺序在时间戳N+200之前,后续时间戳的发送顺序不再保证有序
                dynamicStateService.initDynamicTable(MSG_CURRENT_DATE).thenRunAsync(() -> MessagePrintUtil.printDbState(MSG_CURRENT_DATE));
                // 初始化数据表后立即发送一次时间戳,此时当前日期还未初始化,后续的时间戳都卡在获取并发锁处
                sendTimestamp(time, DATA_SYNC_TIMEOUT).thenRunAsync(() -> {});
                // 初始化时间参数,放行后续的时间戳
                MSG_END_TIMESTAMP = DateParamParseUtil.getEndTimestamp(MSG_CURRENT_DATE);
                MSG_TIME_INIT_FLAG = true;
                return false;
            }
        } catch (Exception e) { MessagePrintUtil.printException(e, "checkDbTableState"); }
        finally { MSG_DATE_LOCK.unlock(); }
        return true;
    }

    // 发送指定时间戳消息,开始轨迹融合相关任务
    public CompletableFuture<Void> sendTimestamp(String time, long syncTime) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 牌照/光纤/激光雷达/微波雷达数据收集任务执行完后再发送消息(预估时间)
                // 发送当前时间戳清洗原始数据的消息
                Thread.sleep(syncTime);
                rabbitTemplate.convertAndSend(QUEUE_DEFAULT_EXCHANGE, QUEUE_NAME_FLUSH, time);
                rabbitTemplate.convertAndSend(QUEUE_DEFAULT_EXCHANGE, QUEUE_NAME_FUSION, time);
            } catch (Exception e) { MessagePrintUtil.printException(e, "sendTimestampMsg"); }
        }, msgTimestampTaskAsyncPool);
    }

}
