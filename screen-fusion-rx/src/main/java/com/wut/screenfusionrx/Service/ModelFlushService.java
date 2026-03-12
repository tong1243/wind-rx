package com.wut.screenfusionrx.Service;

import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbredisrx.Service.RedisModelDataService;
import com.wut.screenfusionrx.Service.ModelFlushSubService.FiberModelPreFlushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.wut.screencommonrx.Static.FusionModuleStatic.ASYNC_SERVICE_TIMEOUT;

@Component
public class ModelFlushService {
    private final RedisModelDataService redisModelDataService;
    private final FiberModelPreFlushService fiberModelPreFlushService;


    @Autowired
    public ModelFlushService(RedisModelDataService redisModelDataService, FiberModelPreFlushService fiberModelPreFlushService) {
        this.redisModelDataService = redisModelDataService;
        this.fiberModelPreFlushService = fiberModelPreFlushService;

    }

    public void collectAndStoreTargetModel(long timestamp) {
        // 内存数据库取光纤/激光雷达/微波雷达原始数据
        var flushModelTask = redisModelDataService.collectFiberModelData((double)timestamp);

        try {
            CompletableFuture.allOf(flushModelTask).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
            // 分别对光纤/激光雷达/微波雷达数据作单设备去重
            CompletableFuture.allOf(
                    fiberModelPreFlushService.startPreFlush(flushModelTask.get())

            ).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
        } catch(Exception e) { MessagePrintUtil.printException(e, "collectTargetModel"); }
    }

}
