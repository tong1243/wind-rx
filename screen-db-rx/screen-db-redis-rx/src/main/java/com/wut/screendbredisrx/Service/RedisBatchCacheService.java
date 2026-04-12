package com.wut.screendbredisrx.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.DateParamParseUtil;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbmysqlrx.Model.CarEvent;
import com.wut.screendbmysqlrx.Model.Traj;
import com.wut.screendbmysqlrx.Service.TrajService;
import com.wut.screendbredisrx.Context.BatchCacheTimeContext;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.wut.screencommonrx.Static.DbModuleStatic.*;
import static com.wut.screencommonrx.Static.FusionModuleStatic.*;

@Component
public class RedisBatchCacheService {
    private static final Logger log = LoggerFactory.getLogger(RedisBatchCacheService.class);
    @Qualifier("redisTaskAsyncPool")
    private final Executor redisTaskAsyncPool;
    private final BatchCacheTimeContext batchCacheTimeContext;
    private final StringRedisTemplate stringRedisTemplate;
    private final TrajService trajService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final ReentrantLock TRAJ_CACHE_LOCK = new ReentrantLock();
    private static final ReentrantLock EVENT_CACHE_LOCK = new ReentrantLock();

    @Autowired
    public RedisBatchCacheService(BatchCacheTimeContext batchCacheTimeContext, StringRedisTemplate stringRedisTemplate, TrajService trajService,  Executor redisTaskAsyncPool) {
        this.batchCacheTimeContext = batchCacheTimeContext;
        this.stringRedisTemplate = stringRedisTemplate;
        this.trajService = trajService;
        this.redisTaskAsyncPool = redisTaskAsyncPool;
    }

    @PostConstruct
    public void initBatchCache() {
        stringRedisTemplate.delete(REDIS_KEY_TRAJ_BATCH_CACHE);
        stringRedisTemplate.delete(REDIS_KEY_EVENT_INSERT_BATCH_CACHE);
        stringRedisTemplate.delete(REDIS_KEY_EVENT_UPDATE_BATCH_CACHE);
    }

    public Boolean isTrajBatchCacheEmpty() {
        return Boolean.FALSE.equals(stringRedisTemplate.hasKey(REDIS_KEY_TRAJ_BATCH_CACHE)) || Objects.requireNonNull(stringRedisTemplate.opsForList().size(REDIS_KEY_TRAJ_BATCH_CACHE)) == 0;
    }

    public Boolean isEventInsertBatchCacheEmpty() {
        return Boolean.FALSE.equals(stringRedisTemplate.hasKey(REDIS_KEY_EVENT_INSERT_BATCH_CACHE)) || Objects.requireNonNull(stringRedisTemplate.opsForList().size(REDIS_KEY_EVENT_INSERT_BATCH_CACHE)) == 0;
    }

    public Boolean isEventUpdateBatchCacheEmpty() {
        return Boolean.FALSE.equals(stringRedisTemplate.hasKey(REDIS_KEY_EVENT_UPDATE_BATCH_CACHE)) || Objects.requireNonNull(stringRedisTemplate.opsForList().size(REDIS_KEY_EVENT_UPDATE_BATCH_CACHE)) == 0;
    }

    public void storeTrajCache(List<Traj> trajList, long timestamp){
        if (!CollectionEmptyUtil.forList(trajList)) {
            stringRedisTemplate.opsForList().rightPushAll(REDIS_KEY_TRAJ_BATCH_CACHE, trajList.stream().map(traj -> {
                try { return objectMapper.writeValueAsString(traj); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList());
        }
        if (batchCacheTimeContext.recordTrajCacheTimestamp(timestamp)) {
            saveTrajBatchCache(DateParamParseUtil.getDateTimeStr(timestamp - BATCH_RECORD_TIME_COND), getTrajBatchCache()).thenRunAsync(() -> {});
        }
    }

//    public void storeEventCache(List<CarEvent> insertList, List<CarEvent> updateList, long timestamp) {
//        var storeEventInsertTask = storeEventInsertCache(insertList);
//        var storeEventUpdateTask = storeEventUpdateCache(updateList);
//        try {
//            CompletableFuture.allOf(storeEventInsertTask, storeEventUpdateTask).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
//            if (batchCacheTimeContext.recordEventCacheTimestamp(timestamp)) {
//                var eventInsertListTask = CompletableFuture.supplyAsync(this::getEventInsertBatchCache, redisTaskAsyncPool);
//                var eventUpdateListTask = CompletableFuture.supplyAsync(this::getEventUpdateBatchCache, redisTaskAsyncPool);
//                CompletableFuture.allOf(eventInsertListTask, eventUpdateListTask).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
//                saveEventBatchCache(
//                        DateParamParseUtil.getDateTimeStr(timestamp - EVENT_BATCH_RECORD_TIME_COND),
//                        eventInsertListTask.get(),
//                        eventUpdateListTask.get()
//                ).thenRunAsync(() -> {});
//            }
//        } catch (Exception e) { MessagePrintUtil.printException(e, "storeEventCache"); }
//    }

    public CompletableFuture<Void> storeEventInsertCache(List<CarEvent> carEventList) {
        return CompletableFuture.runAsync(() -> {
            if (CollectionEmptyUtil.forList(carEventList)) { return; }
            stringRedisTemplate.opsForList().rightPushAll(REDIS_KEY_EVENT_INSERT_BATCH_CACHE, carEventList.stream().map(event -> {
                try { return objectMapper.writeValueAsString(event); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList());
        }, redisTaskAsyncPool);
    }

    public CompletableFuture<Void> storeEventUpdateCache(List<CarEvent> carEventList) {
        return CompletableFuture.runAsync(() -> {
            if (CollectionEmptyUtil.forList(carEventList)) { return; }
            stringRedisTemplate.opsForList().rightPushAll(REDIS_KEY_EVENT_UPDATE_BATCH_CACHE, carEventList.stream().map(event -> {
                try { return objectMapper.writeValueAsString(event); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList());
        }, redisTaskAsyncPool);
    }

    public List<Traj> getTrajBatchCache(){
        if (isTrajBatchCacheEmpty()) { return List.of(); }
        return Objects.requireNonNull(stringRedisTemplate.opsForList().leftPop(REDIS_KEY_TRAJ_BATCH_CACHE, Objects.requireNonNull(stringRedisTemplate.opsForList().size(REDIS_KEY_TRAJ_BATCH_CACHE)))).stream().map(str -> {
            try { return objectMapper.readValue(str, Traj.class); }
            catch (JsonProcessingException e) { return null; }
        }).filter(Objects::nonNull).toList();
    }

    public List<CarEvent> getEventInsertBatchCache(){
        if (isEventInsertBatchCacheEmpty()) { return List.of(); }
        return Objects.requireNonNull(stringRedisTemplate.opsForList().leftPop(REDIS_KEY_EVENT_INSERT_BATCH_CACHE, Objects.requireNonNull(stringRedisTemplate.opsForList().size(REDIS_KEY_EVENT_INSERT_BATCH_CACHE)))).stream().map(str -> {
            try { return objectMapper.readValue(str, CarEvent.class); }
            catch (JsonProcessingException e) { return null; }
        }).filter(Objects::nonNull).toList();
    }

    public List<CarEvent> getEventUpdateBatchCache() {
        if (isEventUpdateBatchCacheEmpty()) { return List.of(); }
        return  Objects.requireNonNull(stringRedisTemplate.opsForList().leftPop(REDIS_KEY_EVENT_UPDATE_BATCH_CACHE, Objects.requireNonNull(stringRedisTemplate.opsForList().size(REDIS_KEY_EVENT_UPDATE_BATCH_CACHE)))).stream().map(str -> {
            try { return objectMapper.readValue(str, CarEvent.class); }
            catch (JsonProcessingException e) { return null; }
        // 指定时间段的数据中可能存在对相同事件的多次更新(采用<轨迹号#事件类型>作为唯一键标识)
        // 筛选出每组更新信息中终止时间戳最大的项即可
        }).filter(Objects::nonNull).collect(
            Collectors.groupingBy((event) -> event.getTrajId() + "#" + event.getEventType())
        ).values().stream().map(
            (eventList) -> eventList.stream().max(Comparator.comparingLong(CarEvent::getEndTimestamp)).orElse(null)
        ).filter(Objects::nonNull).toList();
    }

    public CompletableFuture<Void> saveTrajBatchCache(String time, List<Traj> readyToInsertList) {
        return CompletableFuture.runAsync(() -> {
            try {
                TRAJ_CACHE_LOCK.lock();
                if (!CollectionEmptyUtil.forList(readyToInsertList)) {
                    trajService.storeTrajData(time, readyToInsertList);
                    log.info("traj batch flush success, tableDate={}, size={}", time, readyToInsertList.size());
                }
            } catch (Exception e) { MessagePrintUtil.printException(e, "saveTrajBatchCache"); }
            finally { TRAJ_CACHE_LOCK.unlock(); }
        }, redisTaskAsyncPool);
    }

//    public CompletableFuture<Void> saveEventBatchCache(String date, List<CarEvent> readyToInsertList, List<CarEvent> readyToUpdateList) {
//        return CompletableFuture.runAsync(() -> {
//            try {
//                EVENT_CACHE_LOCK.lock();
//                if (!CollectionEmptyUtil.forList(readyToInsertList)) {
//                    carEventService.storeEventData(date, readyToInsertList);
//                }
//                if (!CollectionEmptyUtil.forList(readyToUpdateList)) {
//                    carEventService.updateEventData(date, readyToUpdateList);
//                }
//            } catch (Exception e) { MessagePrintUtil.printException(e, "saveEventBatchCache"); }
//            finally { EVENT_CACHE_LOCK.unlock(); }
//        }, redisTaskAsyncPool);
//    }

}
