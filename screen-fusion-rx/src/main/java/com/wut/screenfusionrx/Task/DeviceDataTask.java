package com.wut.screenfusionrx.Task;

import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screenfusionrx.Context.DeviceDataContext;
import com.wut.screenfusionrx.Service.DeviceDataService;
import com.wut.screenfusionrx.Service.DeviceSecDataService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

import static com.wut.screencommonrx.Static.FusionModuleStatic.DEVICE_RECORD_TIME_COND;
import static com.wut.screencommonrx.Static.FusionModuleStatic.DEVICE_SEC_RECORD_TIME_COND;

@Component
public class DeviceDataTask {
    @Qualifier("fusionTaskDeviceAsyncPool")
    private final Executor fusionTaskDeviceAsyncPool;
    @Qualifier("fusionTaskDeviceSecAsyncPool")
    private final Executor fusionTaskDeviceSecAsyncPool;
    private final DeviceDataService deviceDataService;
    private final DeviceSecDataService deviceSecDataService;
    private final DeviceDataContext deviceDataContext;
    private final ReentrantLock DEVICE_DATA_LOCK = new ReentrantLock(true);
    private final ReentrantLock DEVICE_SEC_DATA_LOCK = new ReentrantLock(true);

    @Autowired
    public DeviceDataTask(Executor fusionTaskDeviceAsyncPool, Executor fusionTaskDeviceSecAsyncPool, DeviceDataService deviceDataService, DeviceSecDataService deviceSecDataService, DeviceDataContext deviceDataContext) {
        this.fusionTaskDeviceAsyncPool = fusionTaskDeviceAsyncPool;
        this.fusionTaskDeviceSecAsyncPool = fusionTaskDeviceSecAsyncPool;
        this.deviceDataService = deviceDataService;
        this.deviceSecDataService = deviceSecDataService;
        this.deviceDataContext = deviceDataContext;
    }

    @RabbitListener(queues = "device")
    public void eventDataListener(String timestampStr) {
//        startParseDeviceData(Long.parseLong(timestampStr)).thenRunAsync(() -> {});
//        startParseDeviceSecData(Long.parseLong(timestampStr)).thenRunAsync(() -> {});
    }

    public CompletableFuture<Void> startParseDeviceData(long timestamp) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (deviceDataContext.recordDeviceTimestamp(timestamp)) {
                    try {
                        DEVICE_DATA_LOCK.lock();
                        deviceDataService.collectAndStoreDeviceData(timestamp);
                    } catch (Exception e) { MessagePrintUtil.printException(e, "startParseDeviceData->inner"); }
                    finally { DEVICE_DATA_LOCK.unlock(); }
                    MessagePrintUtil.printDeviceData(timestamp - DEVICE_RECORD_TIME_COND, timestamp);
                }
            } catch (Exception e) { MessagePrintUtil.printException(e, "startParseDeviceData"); }
        }, fusionTaskDeviceAsyncPool);
    }

    public CompletableFuture<Void> startParseDeviceSecData(long timestamp) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (deviceDataContext.recordDeviceSecTimestamp(timestamp)) {
                    try {
                        DEVICE_SEC_DATA_LOCK.lock();
                        deviceSecDataService.collectAndStoreDeviceSecData(timestamp);
                    } catch (Exception e) { MessagePrintUtil.printException(e, "startParseDeviceSecData->inner"); }
                    finally { DEVICE_SEC_DATA_LOCK.unlock(); }
                    MessagePrintUtil.printDeviceSecData(timestamp - DEVICE_SEC_RECORD_TIME_COND, timestamp);
                }
            } catch (Exception e) { MessagePrintUtil.printException(e, "startParseDeviceSecData"); }
        }, fusionTaskDeviceSecAsyncPool);
    }

}
