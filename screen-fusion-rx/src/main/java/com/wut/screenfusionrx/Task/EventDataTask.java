package com.wut.screenfusionrx.Task;

import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screenfusionrx.Service.EventDataService;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import static com.wut.screencommonrx.Static.FusionModuleStatic.FUSION_TIME_INTER;

@Component
public class EventDataTask {
    @Qualifier("eventDataTaskScheduler")
    private final ThreadPoolTaskScheduler eventDataTaskScheduler;
    @Qualifier("fusionTaskEventTimerAsyncPool")
    private final Executor fusionTaskEventTimerAsyncPool;
    private final EventDataService eventDataService;
    private static final AtomicLong EVENT_LATEST_TIME = new AtomicLong(0L);
    private static Boolean EVENT_TASK_FLAG = false;
    private static Long EVENT_TIME = 0L;
    private static final ReentrantLock EVENT_DATA_LOCK = new ReentrantLock(true);

    @Autowired
    public EventDataTask(ThreadPoolTaskScheduler eventDataTaskScheduler, Executor fusionTaskEventTimerAsyncPool, EventDataService eventDataService) {
        this.eventDataTaskScheduler = eventDataTaskScheduler;
        this.fusionTaskEventTimerAsyncPool = fusionTaskEventTimerAsyncPool;
        this.eventDataService = eventDataService;
    }

    @PostConstruct
    public void initEventDataParam() {
        EVENT_LATEST_TIME.set(0L);
        EVENT_TASK_FLAG = false;
        EVENT_TIME = 0L;
    }

    @RabbitListener(queues = "event")
    public void eventDataListener(String timestampStr) {
        storeEventTimestamp(Long.parseLong(timestampStr)).thenRunAsync(() -> {});
    }

    public CompletableFuture<Void> storeEventTimestamp(long timestamp) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (!EVENT_TASK_FLAG) {
                    EVENT_TASK_FLAG = true;
                    while (!EVENT_LATEST_TIME.compareAndSet(0L, timestamp)) {}
                    EVENT_TIME = timestamp - FUSION_TIME_INTER;
                    eventDataTaskScheduler.scheduleAtFixedRate(this::startCollectEventData, Duration.ofMillis(FUSION_TIME_INTER));
                    return;
                }
                while (!EVENT_LATEST_TIME.compareAndSet(timestamp - FUSION_TIME_INTER, timestamp)) {}
            } catch (Exception e) { MessagePrintUtil.printException(e, "storeEventTimestamp"); }
        }, fusionTaskEventTimerAsyncPool);
    }

    public void startCollectEventData() {
        if (EVENT_TIME < EVENT_LATEST_TIME.get()) {
            EVENT_TIME += FUSION_TIME_INTER;
            try {
                EVENT_DATA_LOCK.lock();
                eventDataService.collectAndStoreEventModelData(EVENT_TIME);
                MessagePrintUtil.printEventData(EVENT_TIME);
            } catch (Exception e) { MessagePrintUtil.printException(e, "startCollectEventData"); }
            finally { EVENT_DATA_LOCK.unlock(); }
        }
    }

}
