package com.wut.screendbredisrx.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wut.screencommonrx.Model.CarPlateModel;
import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.wut.screencommonrx.Static.DbModuleStatic.*;

@Component
public class RedisModelDataService {
    @Qualifier("redisTaskAsyncPool")
    private final Executor redisTaskAsyncPool;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public RedisModelDataService(StringRedisTemplate stringRedisTemplate, Executor redisTaskAsyncPool) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisTaskAsyncPool = redisTaskAsyncPool;
    }

    @PostConstruct
    public void initModelDataCache() {
        try {
            stringRedisTemplate.delete(REDIS_KEY_PLATE_MODEL_DATA);
            stringRedisTemplate.delete(REDIS_KEY_FIBER_MODEL_DATA);
            stringRedisTemplate.delete(REDIS_KEY_LASER_MODEL_DATA);
            stringRedisTemplate.delete(REDIS_KEY_WAVE_MODEL_DATA);
            stringRedisTemplate.delete(REDIS_KEY_FIBER_FLUSH_MODEL);
            stringRedisTemplate.delete(REDIS_KEY_LASER_FLUSH_MODEL);
            stringRedisTemplate.delete(REDIS_KEY_WAVE_FLUSH_MODEL);
        } catch (Exception e) {
            MessagePrintUtil.printException(e, "initModelDataCache");
        }
    }

    public Boolean isModelZSetEmpty(String key) {
        return Boolean.FALSE.equals(stringRedisTemplate.hasKey(key)) || Objects.requireNonNull(stringRedisTemplate.opsForZSet().size(key)) == 0;
    }

    public CompletableFuture<Void> storeFiberModelData(VehicleModel model) {
        return storeSingleModelGeneral(REDIS_KEY_FIBER_MODEL_DATA, model);
    }

    public CompletableFuture<Void> storeLaserModelData(VehicleModel model) {
        return storeSingleModelGeneral(REDIS_KEY_LASER_MODEL_DATA, model);
    }

    public CompletableFuture<Void> storeWaveModelData(VehicleModel model) {
        return storeSingleModelGeneral(REDIS_KEY_WAVE_MODEL_DATA, model);
    }

    public CompletableFuture<Void> storeFiberFlushModel(List<VehicleModel> fiberList) {
        return storeListModelGeneral(REDIS_KEY_FIBER_FLUSH_MODEL, fiberList);
    }

    public CompletableFuture<Void> storeLaserFlushModel(List<VehicleModel> laserList) {
        return storeListModelGeneral(REDIS_KEY_LASER_FLUSH_MODEL, laserList);
    }

    public CompletableFuture<Void> storeWaveFlushModel(List<VehicleModel> waveList) {
        return storeListModelGeneral(REDIS_KEY_WAVE_FLUSH_MODEL, waveList);
    }

    public CompletableFuture<List<VehicleModel>> collectFiberModelData(double score) {
        return collectModelGeneral(REDIS_KEY_FIBER_MODEL_DATA, score, score);
    }

    public CompletableFuture<List<VehicleModel>> collectLaserModelData(double score) {
        return collectModelGeneral(REDIS_KEY_LASER_MODEL_DATA, score, score);
    }

    public CompletableFuture<List<VehicleModel>> collectWaveModelData(double score) {
        return collectModelGeneral(REDIS_KEY_WAVE_MODEL_DATA, score, score);
    }

    public CompletableFuture<List<VehicleModel>> collectFiberFlushModel(double score) {
        return collectModelGeneral(REDIS_KEY_FIBER_FLUSH_MODEL, score, score);
    }

    public CompletableFuture<List<VehicleModel>> collectLaserFlushModel(double score) {
        return collectModelGeneral(REDIS_KEY_LASER_FLUSH_MODEL, score, score);
    }

    public CompletableFuture<List<VehicleModel>> collectWaveFlushModel(double score) {
        return collectModelGeneral(REDIS_KEY_WAVE_FLUSH_MODEL, score, score);
    }

    public CompletableFuture<List<VehicleModel>> collectFiberModel(double min, double max) {
        return collectModelGeneral(REDIS_KEY_FIBER_FLUSH_MODEL, min, max);
    }

    public CompletableFuture<List<VehicleModel>> collectLaserModel(double min, double max) {
        return collectModelGeneral(REDIS_KEY_LASER_FLUSH_MODEL, min, max);
    }

    public CompletableFuture<List<VehicleModel>> collectWaveModel(double min, double max) {
        return collectModelGeneral(REDIS_KEY_WAVE_FLUSH_MODEL, min, max);
    }

    public CompletableFuture<Void> storeSingleModelGeneral(String key, VehicleModel model) {
        return CompletableFuture.runAsync(() -> {
            try { stringRedisTemplate.opsForZSet().add(key, objectMapper.writeValueAsString(model), model.getTimestamp().doubleValue()); }
            catch (Exception e) { MessagePrintUtil.printException(e, "storeSingleModelGeneral"); }
        }, redisTaskAsyncPool);
    }

    public CompletableFuture<Void> storeListModelGeneral(String key, List<VehicleModel> models) {
        return CompletableFuture.runAsync(() -> {
            if (CollectionEmptyUtil.forList(models)) { return; }
            double timestamp = models.get(0).getTimestamp().doubleValue();
            models.stream().forEach(model -> {
                try { stringRedisTemplate.opsForZSet().add(key, objectMapper.writeValueAsString(model), timestamp); }
                catch (Exception e) { MessagePrintUtil.printException(e, "storeListModelGeneral"); }
            });
        }, redisTaskAsyncPool);
    }

    public CompletableFuture<List<VehicleModel>> collectModelGeneral(String key, double min, double max) {
        return CompletableFuture.supplyAsync(() -> {
            if (isModelZSetEmpty(key)) {
//                MessagePrintUtil.printFiberException(key);
                return List.of(); }
            Set<String> modelStrSet = stringRedisTemplate.opsForZSet().rangeByScore(key, min, max);
            if (CollectionEmptyUtil.forSet(modelStrSet)) { return List.of(); }
//            try {
////                MessagePrintUtil.printFiberModel("RedisModelDataService"+objectMapper.writeValueAsString(modelStrSet));
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
            return Objects.requireNonNull(modelStrSet).stream().map(i -> {
                try { return objectMapper.readValue(i, VehicleModel.class); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList();
        }, redisTaskAsyncPool);
    }

    public CompletableFuture<Void> storePlateModelData(CarPlateModel model) {
        return CompletableFuture.runAsync(() -> {
            try { stringRedisTemplate.opsForZSet().add(REDIS_KEY_PLATE_MODEL_DATA, objectMapper.writeValueAsString(model), model.getTimestamp().doubleValue()); }
            catch (Exception e) { MessagePrintUtil.printException(e, "storePlateModelData"); }
        }, redisTaskAsyncPool);
    }

    public CompletableFuture<List<CarPlateModel>> collectPlateModelData(double min, double max) {
        return CompletableFuture.supplyAsync(() -> {
            if (isModelZSetEmpty(REDIS_KEY_PLATE_MODEL_DATA)) { return List.of(); }
            Set<String> plateModelStrSet = stringRedisTemplate.opsForZSet().rangeByScore(REDIS_KEY_PLATE_MODEL_DATA, min, max);
            if (CollectionEmptyUtil.forSet(plateModelStrSet)) { return List.of(); }
            return Objects.requireNonNull(plateModelStrSet).stream().map(i -> {
                try { return objectMapper.readValue(i, CarPlateModel.class); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList();
        }, redisTaskAsyncPool);
    }

    public void removePlateModelData(List<CarPlateModel> list) {
        if (CollectionEmptyUtil.forList(list) || isModelZSetEmpty(REDIS_KEY_PLATE_MODEL_DATA)) { return; }
        try {
            List<String> plateStrList = list.stream().map(i -> {
                try { return objectMapper.writeValueAsString(i); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList();
            if (CollectionEmptyUtil.forList(plateStrList)) { return; }
            // 使用管道批量打包删除已匹配的牌照数据,该过程在轨迹融合中为同步操作
            stringRedisTemplate.executePipelined((RedisCallback<Object>) conn -> {
                plateStrList.forEach(plateStr -> conn.zSetCommands().zRem(REDIS_KEY_PLATE_MODEL_DATA.getBytes(), plateStr.getBytes()));
                return null;
            });
        } catch (Exception e) { MessagePrintUtil.printException(e, "removePlateModelData"); }
    }

}
