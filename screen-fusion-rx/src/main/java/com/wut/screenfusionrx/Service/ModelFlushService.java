package com.wut.screenfusionrx.Service;

import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbredisrx.Service.RedisModelDataService;
import com.wut.screenfusionrx.Service.ModelFlushSubService.FiberModelPreFlushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.wut.screencommonrx.Static.FusionModuleStatic.ASYNC_SERVICE_TIMEOUT;

@Component
public class ModelFlushService {
    private static final int FIBER_COLLECT_RETRY_COUNT = 10;
    private static final long FIBER_COLLECT_RETRY_INTERVAL_MS = 200L;

    private final RedisModelDataService redisModelDataService;
    private final FiberModelPreFlushService fiberModelPreFlushService;

    @Autowired
    public ModelFlushService(RedisModelDataService redisModelDataService, FiberModelPreFlushService fiberModelPreFlushService) {
        this.redisModelDataService = redisModelDataService;
        this.fiberModelPreFlushService = fiberModelPreFlushService;
    }

    public void collectAndStoreTargetModel(long timestamp) {
        try {
            List<VehicleModel> fiberModelList = collectFiberModelWithRetry(timestamp);
            CompletableFuture.allOf(
                    fiberModelPreFlushService.startPreFlush(fiberModelList)
            ).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            MessagePrintUtil.printException(e, "collectTargetModel");
        }
    }

    private List<VehicleModel> collectFiberModelWithRetry(long timestamp) throws Exception {
        List<VehicleModel> fiberModelList = getFiberModelAtTimestamp(timestamp);
        for (int i = 0; i < FIBER_COLLECT_RETRY_COUNT && CollectionEmptyUtil.forList(fiberModelList); i++) {
            Thread.sleep(FIBER_COLLECT_RETRY_INTERVAL_MS);
            fiberModelList = getFiberModelAtTimestamp(timestamp);
        }
        return fiberModelList;
    }

    private List<VehicleModel> getFiberModelAtTimestamp(long timestamp) throws Exception {
        var flushModelTask = redisModelDataService.collectFiberModelData((double) timestamp);
        CompletableFuture.allOf(flushModelTask).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
        return flushModelTask.get();
    }
}

