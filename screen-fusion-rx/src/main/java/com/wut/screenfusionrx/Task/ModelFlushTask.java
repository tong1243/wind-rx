package com.wut.screenfusionrx.Task;

import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screenfusionrx.Service.ModelFlushService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
public class ModelFlushTask {
    @Qualifier("fusionTaskModelFlushAsyncPool")
    private final Executor fusionTaskModelFlushAsyncPool;
    private final ModelFlushService modelFlushService;

    @Autowired
    public ModelFlushTask(ModelFlushService modelFlushService, Executor fusionTaskModelFlushAsyncPool) {
        this.modelFlushService = modelFlushService;
        this.fusionTaskModelFlushAsyncPool = fusionTaskModelFlushAsyncPool;
    }

    @RabbitListener(queues = "flush")
    public void modelFlushListener(String timestampStr) {
        startModelFlush(Long.parseLong(timestampStr)).thenRunAsync(() -> {});
    }

    // 将光纤/激光雷达/微波雷达数据转换成每辆车时间戳唯一的VehicleModel轨迹数据
    // 时间戳到达直接开辟线程处理当前时间戳的光纤/雷达数据
    // 24/06/23更新:该步骤仅作单设备数据去重,多设备去重过程改为同步任务合并到轨迹融合中
    public CompletableFuture<Void> startModelFlush(long timestamp){
        return CompletableFuture.runAsync(() -> {
            modelFlushService.collectAndStoreTargetModel(timestamp);
            MessagePrintUtil.printModelFlush(timestamp);
        }, fusionTaskModelFlushAsyncPool);
    }

}
