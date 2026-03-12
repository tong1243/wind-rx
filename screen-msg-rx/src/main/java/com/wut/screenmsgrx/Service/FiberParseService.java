package com.wut.screenmsgrx.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wut.screencommonrx.Model.MsgSendData.Fiber;
import com.wut.screencommonrx.Model.MsgSendDataModel;
import com.wut.screencommonrx.Util.ModelTransformUtil;
import com.wut.screendbredisrx.Service.RedisModelDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.function.SupplierUtils;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
public class FiberParseService {
    @Qualifier("msgTaskAsyncPool")
    private final Executor msgTaskAsyncPool;
    private final RedisModelDataService redisModelDataService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public FiberParseService(RedisModelDataService redisModelDataService, Executor msgTaskAsyncPool) {
        this.redisModelDataService = redisModelDataService;
        this.msgTaskAsyncPool = msgTaskAsyncPool;
    }

    // 收集发送端传来的光纤原始数据
    public CompletableFuture<Void> collectFiberData(String fiberDataStr){
        return CompletableFuture.runAsync(() -> {
            Optional.ofNullable(SupplierUtils.resolve(() -> {
                try { return objectMapper.readValue(fiberDataStr, MsgSendDataModel.class); }
                catch (JsonProcessingException e) { return null; }
            })).ifPresent(msgSendDataModel -> {
                Fiber fiberData = objectMapper.convertValue(msgSendDataModel.getData(), Fiber.class);
                redisModelDataService.storeFiberModelData(ModelTransformUtil.fiberToVehicle(fiberData, msgSendDataModel.getTimestamp())).thenRunAsync(() -> {});
            });
        }, msgTaskAsyncPool);
    }

}
