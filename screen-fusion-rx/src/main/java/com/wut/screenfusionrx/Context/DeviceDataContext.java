package com.wut.screenfusionrx.Context;

import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbmysqlrx.Model.RadarInfo;
import com.wut.screendbmysqlrx.Service.RadarInfoService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;

@Component
public class DeviceDataContext {
    @Qualifier("fusionTaskDeviceAsyncPool")
    private final Executor fusionTaskDeviceAsyncPool;
    private final RadarInfoService radarInfoService;

    // 雷达实时任务记录时间戳
    private static Long DEVICE_TIME_RECORD = 0L;
    // 雷达断面任务记录时间戳
    private static Long DEVICE_SEC_TIME_RECORD = 0L;
    // 雷达实时任务并发锁
    private static final ReentrantLock DEVICE_TIME_LOCK = new ReentrantLock(true);
    // 雷达断面任务并发锁
    private static final ReentrantLock DEVICE_SEC_TIME_LOCK = new ReentrantLock(true);
    // 雷达实时任务时间戳初始化标记
    private static Boolean DEVICE_TIME_INIT_FLAG = false;
    // 雷达断面任务时间戳初始化标记
    private static Boolean DEVICE_SEC_TIME_INIT_FLAG = false;

    // 雷达设备信息(随雷达实时任务更新,保存状态)
    @Getter
    private final List<RadarInfo> radarInfoList = new ArrayList<>();
    // 雷达设备信息(供雷达断面任务使用,不保存状态也不更新)
    @Getter
    private final List<RadarInfo> radarInfoListForSec = new ArrayList<>();


    @Autowired
    public DeviceDataContext(RadarInfoService radarInfoService, Executor fusionTaskDeviceAsyncPool) {
        this.radarInfoService = radarInfoService;
        this.fusionTaskDeviceAsyncPool = fusionTaskDeviceAsyncPool;
    }

    @PostConstruct
    public void initDeviceDataContext() {
        flushRadarInfoList().thenRunAsync(() -> {
            radarInfoListForSec.addAll(radarInfoList);
        });
        DEVICE_TIME_RECORD = 0L;
        DEVICE_SEC_TIME_RECORD = 0L;
        DEVICE_TIME_INIT_FLAG = false;
        DEVICE_SEC_TIME_INIT_FLAG = false;
    }

    public CompletableFuture<Void> flushRadarInfoList() {
        return CompletableFuture.runAsync(() -> {
            radarInfoList.clear();
            // 不统计屏蔽的设备数据,也不更新其状态
            radarInfoList.addAll(radarInfoService.getAllRadarInfo().stream().filter(info -> info.getState() != DEVICE_STATE_DISABLE).toList());
        }, fusionTaskDeviceAsyncPool);
    }

    public CompletableFuture<Void> updateRadarInfoList(List<RadarInfo> radarInfoList) {
        return CompletableFuture.runAsync(() -> {
            radarInfoService.updateRadarState(radarInfoList);
            flushRadarInfoList().thenRunAsync(() -> {});
        }, fusionTaskDeviceAsyncPool);
    }

    public boolean recordDeviceTimestamp(long timestamp) {
        if (DEVICE_TIME_INIT_FLAG) {
            return updateDeviceTime(timestamp);
        }
        try {
            DEVICE_TIME_LOCK.lock();
            if (DEVICE_TIME_INIT_FLAG) {
                return updateDeviceTime(timestamp);
            }
            DEVICE_TIME_RECORD = timestamp;
            DEVICE_TIME_INIT_FLAG = true;
            return false;
        } catch (Exception e) { MessagePrintUtil.printException(e, "recordDeviceTimestamp"); }
        finally { DEVICE_TIME_LOCK.unlock(); }
        return false;
    }

    public boolean recordDeviceSecTimestamp(long timestamp) {
        if (DEVICE_SEC_TIME_INIT_FLAG) {
            return updateDeviceSecTime(timestamp);
        }
        try {
            DEVICE_SEC_TIME_LOCK.lock();
            if (DEVICE_SEC_TIME_INIT_FLAG) {
                return updateDeviceSecTime(timestamp);
            }
            DEVICE_SEC_TIME_RECORD = timestamp;
            DEVICE_SEC_TIME_INIT_FLAG = true;
            return false;
        } catch (Exception e) { MessagePrintUtil.printException(e, "recordDeviceSecTimestamp"); }
        finally { DEVICE_SEC_TIME_LOCK.unlock(); }
        return false;
    }

    public boolean updateDeviceTime(long timestamp) {
        if (timestamp - DEVICE_TIME_RECORD == DEVICE_RECORD_TIME_COND) {
            try {
                DEVICE_TIME_LOCK.lock();
                if (timestamp - DEVICE_TIME_RECORD == DEVICE_RECORD_TIME_COND) {
                    DEVICE_TIME_RECORD = timestamp;
                    return true;
                }
                return false;
            } catch (Exception e) { MessagePrintUtil.printException(e, "updateDeviceTime"); }
            finally { DEVICE_TIME_LOCK.unlock(); }
        }
        return false;
    }

    public boolean updateDeviceSecTime(long timestamp) {
        if (timestamp - DEVICE_SEC_TIME_RECORD == DEVICE_SEC_RECORD_TIME_COND) {
            try {
                DEVICE_SEC_TIME_LOCK.lock();
                if (timestamp - DEVICE_SEC_TIME_RECORD == DEVICE_SEC_RECORD_TIME_COND) {
                    DEVICE_SEC_TIME_RECORD = timestamp;
                    return true;
                }
                return false;
            } catch (Exception e) { MessagePrintUtil.printException(e, "updateDeviceSecTime"); }
            finally { DEVICE_SEC_TIME_LOCK.unlock(); }
        }
        return false;
    }

}
