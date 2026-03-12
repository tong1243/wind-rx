package com.wut.screendbredisrx.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screendbmysqlrx.Model.Traj;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.function.SupplierUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.wut.screencommonrx.Static.DbModuleStatic.REDIS_KEY_TRAJ_DATA;

@Component
public class RedisTrajFusionService {
    @Qualifier("redisTaskAsyncPool")
    private final Executor redisTaskAsyncPool;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public RedisTrajFusionService(StringRedisTemplate stringRedisTemplate, Executor redisTaskAsyncPool) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisTaskAsyncPool = redisTaskAsyncPool;
    }

    @PostConstruct
    public void initTrajFusionCache() {
        stringRedisTemplate.delete(REDIS_KEY_TRAJ_DATA);
    }

    public Boolean isTrajDataListEmpty() {
        return (Boolean.FALSE.equals(stringRedisTemplate.hasKey(REDIS_KEY_TRAJ_DATA))) || Objects.requireNonNull(stringRedisTemplate.opsForZSet().size(REDIS_KEY_TRAJ_DATA)).intValue() == 0;
    }

    public CompletableFuture<List<Traj>> collectTrajData(long timestamp) {
        return CompletableFuture.supplyAsync(() -> {
            if (isTrajDataListEmpty()) { return List.of(); }
            Set<String> trajStrSet = stringRedisTemplate.opsForZSet().rangeByScore(REDIS_KEY_TRAJ_DATA, (double)timestamp, (double)timestamp);
            if (CollectionEmptyUtil.forSet(trajStrSet)) { return List.of(); }
            return Objects.requireNonNull(trajStrSet).stream().map(str -> {
                try { return objectMapper.readValue(str, Traj.class); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList();
        }, redisTaskAsyncPool);
    }

    public CompletableFuture<List<Traj>> collectTrajData(long timestampStart, long timestampEnd) {
        return CompletableFuture.supplyAsync(() -> {
            if (isTrajDataListEmpty()) { return List.of(); }
            Set<String> trajStrSet = stringRedisTemplate.opsForZSet().rangeByScore(REDIS_KEY_TRAJ_DATA, (double)timestampStart, (double)timestampEnd);
            if (CollectionEmptyUtil.forSet(trajStrSet)) { return List.of(); }
            return Objects.requireNonNull(trajStrSet).stream().map(str -> {
                try { return objectMapper.readValue(str, Traj.class); }
                catch (JsonProcessingException e) { return null; }
            }).filter(Objects::nonNull).toList();
        }, redisTaskAsyncPool);
    }

    public void storeTrajData(List<Traj> trajList) {
        if (CollectionEmptyUtil.forList(trajList)) { return; }
        trajList.stream().forEach(traj -> {
            stringRedisTemplate.opsForZSet().add(REDIS_KEY_TRAJ_DATA, Objects.requireNonNull(SupplierUtils.resolve(() -> {
                try { return objectMapper.writeValueAsString(traj); }
                catch (JsonProcessingException e) { return null; }
            })), traj.getTimestamp().doubleValue());
        });
    }

}
