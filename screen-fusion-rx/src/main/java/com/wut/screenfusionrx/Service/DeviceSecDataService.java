package com.wut.screenfusionrx.Service;

import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbmysqlrx.Model.FiberSecMetric;
import com.wut.screendbmysqlrx.Model.RadarAllSecMetric;
import com.wut.screendbmysqlrx.Model.RadarInfo;
import com.wut.screendbmysqlrx.Model.RadarSecMetric;
import com.wut.screendbredisrx.Service.RedisModelDataService;
import com.wut.screenfusionrx.Context.DeviceDataContext;
import com.wut.screenfusionrx.Service.DeviceDataSubService.FiberMetricDataService;
import com.wut.screenfusionrx.Service.DeviceDataSubService.LaserMetricDataService;
import com.wut.screenfusionrx.Service.DeviceDataSubService.RadarAllSecMetricDataService;
import com.wut.screenfusionrx.Service.DeviceDataSubService.WaveMetricDataService;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;

@Component
public class DeviceSecDataService {
    @Qualifier("fusionTaskDeviceSecAsyncPool")
    private final Executor fusionTaskDeviceSecAsyncPool;
    private final RedisModelDataService redisModelDataService;
    private final DeviceDataContext deviceDataContext;
    private final FiberMetricDataService fiberMetricDataService;
    private final LaserMetricDataService laserMetricDataService;
    private final WaveMetricDataService waveMetricDataService;
    private final RadarAllSecMetricDataService radarAllSecMetricDataService;

    @Autowired
    public DeviceSecDataService(Executor fusionTaskDeviceSecAsyncPool, RedisModelDataService redisModelDataService, DeviceDataContext deviceDataContext, FiberMetricDataService fiberMetricDataService, LaserMetricDataService laserMetricDataService, WaveMetricDataService waveMetricDataService, RadarAllSecMetricDataService radarAllSecMetricDataService) {
        this.fusionTaskDeviceSecAsyncPool = fusionTaskDeviceSecAsyncPool;
        this.redisModelDataService = redisModelDataService;
        this.deviceDataContext = deviceDataContext;
        this.fiberMetricDataService = fiberMetricDataService;
        this.laserMetricDataService = laserMetricDataService;
        this.waveMetricDataService = waveMetricDataService;
        this.radarAllSecMetricDataService = radarAllSecMetricDataService;
    }

    public void collectAndStoreDeviceSecData(long timestamp) {
        long timestampStart = timestamp - DEVICE_SEC_RECORD_TIME_COND + 1;
        try {
            var fiberSecDataTask = collectFiberSecDataTask(timestampStart, timestamp);
            var laserSecDataTask = collectLaserSecDataTask(timestampStart, timestamp);
            var waveSecDataTask = collectWaveSecDataTask(timestampStart, timestamp);
            var radarAllSecDataTask = collectRadarAllSecDataTask(timestampStart, timestamp);
            CompletableFuture.allOf(fiberSecDataTask, laserSecDataTask, waveSecDataTask, radarAllSecDataTask).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
            CompletableFuture.allOf(
                    fiberMetricDataService.storeFiberSecMetricData(fiberSecDataTask.get(), timestampStart),
                    laserMetricDataService.storeRadarSecMetricData(ListUtils.union(laserSecDataTask.get(), waveSecDataTask.get()), timestampStart),
                    radarAllSecMetricDataService.storeRadarAllSecMetricData(radarAllSecDataTask.get(), timestampStart)
            ).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) { MessagePrintUtil.printException(e, "collectAndStoreDeviceSecData"); }
    }

    public CompletableFuture<List<FiberSecMetric>> collectFiberSecDataTask(long timestampStart, long timestampEnd) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<VehicleModel> fiberModelDataList = redisModelDataService.collectFiberModel(timestampStart, timestampEnd).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
                return fiberMetricDataService.collectSecMetric(timestampStart - 1, timestampEnd, fiberModelDataList);
            } catch (Exception e) { MessagePrintUtil.printException(e, "collectFiberSecDataTask"); }
            return null;
        }, fusionTaskDeviceSecAsyncPool);
    }

    public CompletableFuture<List<RadarSecMetric>> collectLaserSecDataTask(long timestampStart, long timestampEnd) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<VehicleModel> laserModelDataList = redisModelDataService.collectLaserModel(timestampStart, timestampEnd).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
                List<RadarInfo> laserRadarInfoList = deviceDataContext.getRadarInfoListForSec().stream().filter(radarInfo -> radarInfo.getType() == DEVICE_TYPE_LASER).sorted(Comparator.comparingInt(RadarInfo::getRid)).toList();
                return laserMetricDataService.collectSecMetric(timestampStart - 1, timestampEnd, laserRadarInfoList, laserModelDataList);
            } catch (Exception e) { MessagePrintUtil.printException(e, "collectLaserSecDataTask"); }
            return null;
        }, fusionTaskDeviceSecAsyncPool);
    }

    public CompletableFuture<List<RadarSecMetric>> collectWaveSecDataTask(long timestampStart, long timestampEnd) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<VehicleModel> waveModelDataList = redisModelDataService.collectWaveModel(timestampStart, timestampEnd).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
                List<RadarInfo> waveRadarInfoList = deviceDataContext.getRadarInfoListForSec().stream().filter(radarInfo -> radarInfo.getType() == DEVICE_TYPE_WAVE).sorted(Comparator.comparingInt(RadarInfo::getRid)).toList();
                return waveMetricDataService.collectSecMetric(timestampStart - 1, timestampEnd, waveRadarInfoList, waveModelDataList);
            } catch (Exception e) { MessagePrintUtil.printException(e, "collectWaveSecDataTask"); }
            return null;
        }, fusionTaskDeviceSecAsyncPool);
    }

    public CompletableFuture<List<RadarAllSecMetric>> collectRadarAllSecDataTask(long timestampStart, long timestampEnd) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<VehicleModel> laserModelDataList = redisModelDataService.collectLaserModel(timestampStart, timestampEnd).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
                List<VehicleModel> waveModelDataList = redisModelDataService.collectWaveModel(timestampStart, timestampEnd).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
                return radarAllSecMetricDataService.collectSecMetric(timestampStart-1, timestampEnd, ListUtils.union(laserModelDataList, waveModelDataList));
            } catch (Exception e) { MessagePrintUtil.printException(e, "collectRadarAllSecDataTask"); }
            return null;
        }, fusionTaskDeviceSecAsyncPool);
    }

}
