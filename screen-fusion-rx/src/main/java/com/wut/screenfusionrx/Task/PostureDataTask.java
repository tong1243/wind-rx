package com.wut.screenfusionrx.Task;

import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screenfusionrx.Context.PostureDataContext;
import com.wut.screenfusionrx.Service.PostureDataService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

import static com.wut.screencommonrx.Static.FusionModuleStatic.POSTURE_RECORD_TIME_COND;

@Component
public class PostureDataTask {
    @Qualifier("fusionTaskPostureAsyncPool")
    private final Executor fusionTaskPostureAsyncPool;
    private final PostureDataService postureDataService;
    private final PostureDataContext postureDataContext;
    private static final ReentrantLock POSTURE_DATA_LOCK = new ReentrantLock(true);

    @Autowired
    public PostureDataTask(Executor fusionTaskPostureAsyncPool, PostureDataContext postureDataContext, PostureDataService postureDataService) {
        this.fusionTaskPostureAsyncPool = fusionTaskPostureAsyncPool;
        this.postureDataContext = postureDataContext;
        this.postureDataService = postureDataService;
    }

    @RabbitListener(queues = "posture")
    public void postureDataListener(String timestampStr) {
        startParsePostureData(Long.parseLong(timestampStr)).thenRunAsync(() -> {});
    }

    public CompletableFuture<Void> startParsePostureData(long timestamp) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (postureDataContext.recordPostureTimestamp(timestamp)){
                    try {
                        POSTURE_DATA_LOCK.lock();
                        postureDataService.collectAndStorePostureData(timestamp);
                    } catch (Exception e) { MessagePrintUtil.printException(e, "startParsePostureData->inner"); }
                    finally { POSTURE_DATA_LOCK.unlock(); }
                    MessagePrintUtil.printPostureData(timestamp - POSTURE_RECORD_TIME_COND, timestamp);
                }
            } catch (Exception e) { MessagePrintUtil.printException(e, "startParsePostureData"); }
        }, fusionTaskPostureAsyncPool);
    }

}
