package com.wut.screenfusionrx.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wut.screencommonrx.Model.CarPlateModel;
import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screencommonrx.Util.ModelTransformUtil;
import com.wut.screendbmongorx.Repository.VehicleModelDocuRepository;
import com.wut.screendbmongorx.Util.MongoModelTransformUtil;
import com.wut.screendbmysqlrx.Model.Traj;
import com.wut.screendbmysqlrx.Util.DbModelTransformUtil;
import com.wut.screendbredisrx.Service.RedisBatchCacheService;
import com.wut.screendbredisrx.Service.RedisModelDataService;
import com.wut.screendbredisrx.Service.RedisTrajFusionService;
import com.wut.screenfusionrx.Context.TrajDataContext;
import com.wut.screenfusionrx.Model.TrajCarCountModel;
import com.wut.screenfusionrx.Model.TrajFrameModel;
import com.wut.screenfusionrx.Service.TrajFusionSubService.TrajModelDeDuplicateService;
import com.wut.screenfusionrx.Service.TrajFusionSubService.TrajModelLineCollectService;
import com.wut.screenfusionrx.Service.TrajFusionSubService.TrajModelLineConnectService;
import com.wut.screenfusionrx.Util.ModelConvertParamUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;
import static com.wut.screencommonrx.Static.MsgModuleStatic.*;
import static com.wut.screencommonrx.Static.MsgModuleStatic.QUEUE_NAME_RISK;

@Component
public class TrajFusionService {
    @Qualifier("fusionTaskTrajFusionAsyncPool")
    private final Executor fusionTaskTrajFusionAsyncPool;
    private final RedisTrajFusionService redisTrajFusionService;
    private final TrajModelDeDuplicateService trajModelDeDuplicateService;
    private final RedisBatchCacheService redisBatchCacheService;
    private final RedisModelDataService redisModelDataService;
    private final KafkaTemplate kafkaTemplate;
    private final TrajDataContext trajDataContext;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RabbitTemplate rabbitTemplate;
    private final VehicleModelDocuRepository vehicleModelDocuRepository;


    @Autowired
    public TrajFusionService(Executor fusionTaskTrajFusionAsyncPool, RedisModelDataService redisModelDataService, RedisTrajFusionService redisTrajFusionService, TrajModelLineConnectService trajModelLineConnectService, TrajModelDeDuplicateService trajModelDeDuplicateService, TrajModelLineCollectService trajModelLineCollectService, RabbitTemplate rabbitTemplate, RedisBatchCacheService redisBatchCacheService, KafkaTemplate kafkaTemplate, TrajDataContext trajDataContext, VehicleModelDocuRepository vehicleModelDocuRepository) {
        this.fusionTaskTrajFusionAsyncPool = fusionTaskTrajFusionAsyncPool;
        this.redisModelDataService = redisModelDataService;
        this.redisTrajFusionService = redisTrajFusionService;
        this.trajModelDeDuplicateService = trajModelDeDuplicateService;
        this.rabbitTemplate = rabbitTemplate;
        this.redisBatchCacheService = redisBatchCacheService;
        this.kafkaTemplate = kafkaTemplate;
        this.trajDataContext = trajDataContext;
        this.vehicleModelDocuRepository = vehicleModelDocuRepository;
    }

//    public List<Traj> collectFusionTraj(long timestamp) {
//        try {
//            // 轨迹融合第一步:同步多设备去重
//            List<VehicleModel> flushModelList = trajModelDeDuplicateService.deDuplicate((double)timestamp);
//            storeTrajModelDeDuplicateTask(flushModelList).thenRunAsync(() -> {});
//            // 轨迹融合第二步:填充历史轨迹队列
//            trajModelLineConnectService.connect(flushModelList.stream().map(model -> {
//                int carType = ModelConvertParamUtil.carLengthToType(model.getLength());
//                return ModelTransformUtil.vehicleToTraj(model, carType);
//            }).toList(), timestamp);
//        } catch (Exception e) { MessagePrintUtil.printException(e, "collectFusionTraj"); }
//        if (CollectionEmptyUtil.forList(trajDataContext.getTrajModelLineList())) { return List.of(); }
//        // 轨迹融合第三步:绑定轨迹牌照信息
//        trajCarPlateConnectService.connect((double)timestamp);
//        // 轨迹融合第四步:取出满容量连续轨迹队列的首个轨迹,组成时间戳下的轨迹数据
//        return trajModelLineCollectService.collect(timestamp);
//    }
public List<Traj> collectFusionTraj(long timestamp) {
    try {
        // 轨迹融合第一步:同步多设备去重
        List<VehicleModel> flushModelList = trajModelDeDuplicateService.deDuplicate((double)timestamp);
//        MessagePrintUtil.printTrajModelLineListToLogger(objectMapper.writeValueAsString(flushModelList));
//        storeTrajModelDeDuplicateTask(flushModelList).thenRunAsync(() -> {});
        // 轨迹融合第二步:填充历史轨迹队列
        List<Traj> trajList = flushModelList.stream().map(model -> {
            int carType = ModelConvertParamUtil.carLengthToType(model.getLength());
            return DbModelTransformUtil.trajModelToTraj(ModelTransformUtil.vehicleToTraj(model, carType));
        }).toList();

        // 当前累计的车辆数信息
        AtomicInteger carNumCountToWH = trajDataContext.getCarNumCountToWH();
        AtomicInteger carNumCountToEZ = trajDataContext.getCarNumCountToEZ();

        trajList.stream().forEach(traj -> {
            if (trajDataContext.getTrajRawIdRecordMap().containsKey(traj.getRawId())) {
                traj.setTrajId(trajDataContext.getTrajRawIdRecordMap().get(traj.getRawId()));
            }
            else {
                trajDataContext.pushNewTrajId(traj);
                switch (traj.getRoadDirect())
                {
                    case ROAD_DIRECT_TO_WH -> carNumCountToWH.incrementAndGet();
                    case ROAD_DIRECT_TO_EZ -> carNumCountToEZ.incrementAndGet();
                }
                trajDataContext.pushNewRecordCarNum(timestamp);
            }
            traj.setCarId("鄂F" + String.format("%05d", (traj.getRawId() % 100000)));
        });

        // 向轨迹数据上下文存储当前时间戳下的累计车辆数信息
        trajDataContext.pushNewRecordCarNum(timestamp);

        return trajList;
    } catch (Exception e) {
        MessagePrintUtil.printException(e, "collectFusionTraj");
        return List.of();
    }
//    if (CollectionEmptyUtil.forList(trajDataContext.getTrajModelLineList())) { return List.of(); }


}

    public void storeAndSendTrajListData(List<Traj> trajList, long timestamp) {
        try {
            redisTrajFusionService.storeTrajData(trajList);
            CompletableFuture.allOf(
                    sendTrajFusionFinishMessageTask(timestamp),
                    sendTrajFrameMessageTask(trajList, timestamp),
                    storeTrajFusionBatchCacheTask(trajList, timestamp)
            ).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) { MessagePrintUtil.printException(e, "storeAndSendTrajListData"); }
    }

    public CompletableFuture<Void> sendTrajFusionFinishMessageTask(long timestamp) {
        return CompletableFuture.runAsync(() -> {
            rabbitTemplate.convertAndSend(QUEUE_DEFAULT_EXCHANGE, QUEUE_NAME_EVENT, String.valueOf(timestamp));
            rabbitTemplate.convertAndSend(QUEUE_DEFAULT_EXCHANGE, QUEUE_NAME_SECTION, String.valueOf(timestamp));
            rabbitTemplate.convertAndSend(QUEUE_DEFAULT_EXCHANGE, QUEUE_NAME_POSTURE, String.valueOf(timestamp));
            rabbitTemplate.convertAndSend(QUEUE_DEFAULT_EXCHANGE, QUEUE_NAME_DEVICE, String.valueOf(timestamp));
            rabbitTemplate.convertAndSend(QUEUE_DEFAULT_EXCHANGE, QUEUE_NAME_RISK, String.valueOf(timestamp));
        }, fusionTaskTrajFusionAsyncPool);
    }

    public CompletableFuture<Void> sendTrajFrameMessageTask(List<Traj> trajList, long timestamp) {
        return CompletableFuture.runAsync(() -> {
            // 同样需要考虑历史轨迹队列取出轨迹信息的时间差
            // 轨迹数据和车辆统计信息都是(当前时间戳-600ms)下的数据
            try {
                TrajCarCountModel trajCarCountModel = trajDataContext.getCarNumRecordMap().remove(timestamp);
                int carToWH = trajCarCountModel == null
                        ? trajDataContext.getCarNumCountToWH().get()
                        : trajCarCountModel.getCarToWH();
                int carToEZ = trajCarCountModel == null
                        ? trajDataContext.getCarNumCountToEZ().get()
                        : trajCarCountModel.getCarToEZ();
                String trajFrameModelStr = objectMapper.writeValueAsString(new TrajFrameModel(
                        timestamp,
                        carToWH,
                        carToEZ,
                        trajList
                ));
                kafkaTemplate.send(TOPIC_NAME_TRAJ, trajFrameModelStr);
//                MessagePrintUtil.printProducerTransmit(TOPIC_NAME_TRAJ, trajFrameModelStr);
            } catch (Exception e) { MessagePrintUtil.printException(e, "sendTrajFrameMessageTask"); }
        }, fusionTaskTrajFusionAsyncPool);
    }

    public CompletableFuture<Void> storeTrajFusionBatchCacheTask(List<Traj> trajList, long timestamp) {
        return CompletableFuture.runAsync(() -> {
            redisBatchCacheService.storeTrajCache(trajList, timestamp);
        }, fusionTaskTrajFusionAsyncPool);
    }

    public CompletableFuture<Void> storeTrajModelDeDuplicateTask(List<VehicleModel> modelList) {
        return CompletableFuture.runAsync(() -> {
            if (CollectionEmptyUtil.forList(modelList)) { return; }
            vehicleModelDocuRepository.insert(modelList.stream().map(MongoModelTransformUtil::vehicleModelToDocu).toList());
        }, fusionTaskTrajFusionAsyncPool);
    }

    public CompletableFuture<Void> removeCarPlateDataTask(List<CarPlateModel> modelList) {
        return CompletableFuture.runAsync(() -> {
            if (CollectionEmptyUtil.forList(modelList)) { return; }
            redisModelDataService.removePlateModelData(modelList);
        }, fusionTaskTrajFusionAsyncPool);
    }

}
