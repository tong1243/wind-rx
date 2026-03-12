package com.wut.screendbredisrx.Service;

import com.wut.screencommonrx.Util.MessagePrintUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.wut.screencommonrx.Static.DbModuleStatic.*;

@Component
public class RedisExpireDataService {
    @Qualifier("redisExpireTaskAsyncPool")
    private final Executor redisExpireTaskAsyncPool;
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public RedisExpireDataService(Executor redisExpireTaskAsyncPool, StringRedisTemplate stringRedisTemplate) {
        this.redisExpireTaskAsyncPool = redisExpireTaskAsyncPool;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void startRemoveExpireData(long timestamp) {
        removeExpireModelData(timestamp).thenRunAsync(() -> {});
        removeExpireTrajData(timestamp).thenRunAsync(() -> {});
    }

    public CompletableFuture<Void> removeExpireModelData(long timestamp) {
        return CompletableFuture.runAsync(() -> {
            try {
                stringRedisTemplate.opsForZSet().removeRangeByScore(REDIS_KEY_FIBER_MODEL_DATA, (double)(timestamp - 240000), (double)(timestamp - 180000));
                stringRedisTemplate.opsForZSet().removeRangeByScore(REDIS_KEY_FIBER_FLUSH_MODEL, (double)(timestamp - 240000), (double)(timestamp - 180000));
            } catch (Exception e) { MessagePrintUtil.printException(e, "removeExpireModelData"); }
        }, redisExpireTaskAsyncPool);
    }

    public CompletableFuture<Void> removeExpireTrajData(long timestamp) {
        return CompletableFuture.runAsync(() -> {
            try {
                stringRedisTemplate.opsForZSet().removeRangeByScore(REDIS_KEY_TRAJ_DATA, (double)(timestamp - 240000), (double)(timestamp - 180000));
            } catch (Exception e) { MessagePrintUtil.printException(e, "removeExpireTrajData"); }
        }, redisExpireTaskAsyncPool);
    }

}
