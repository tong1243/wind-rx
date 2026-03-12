package com.wut.screenfusionrx.Service;

import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbmysqlrx.Model.FiberMetric;
import com.wut.screendbmysqlrx.Model.RadarInfo;
import com.wut.screendbmysqlrx.Model.RadarMetric;
import com.wut.screendbredisrx.Service.RedisModelDataService;
import com.wut.screenfusionrx.Context.DeviceDataContext;
import com.wut.screenfusionrx.Service.DeviceDataSubService.FiberMetricDataService;
import com.wut.screenfusionrx.Service.DeviceDataSubService.LaserMetricDataService;
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
public class DeviceDataService {
    @Qualifier("fusionTaskDeviceAsyncPool")
    private final Executor fusionTaskDeviceAsyncPool;
    private final RedisModelDataService redisModelDataService;
    private final DeviceDataContext deviceDataContext;
    private final FiberMetricDataService fiberMetricDataService;
    private final LaserMetricDataService laserMetricDataService;
    private final WaveMetricDataService waveMetricDataService;
    public record RadarRecordModel(List<RadarInfo> radarInfoList, List<RadarMetric> radarMetricList) {}

    @Autowired
    public DeviceDataService(Executor fusionTaskDeviceAsyncPool, RedisModelDataService redisModelDataService, DeviceDataContext deviceDataContext, FiberMetricDataService fiberMetricDataService, LaserMetricDataService laserMetricDataService, WaveMetricDataService waveMetricDataService) {
        this.fusionTaskDeviceAsyncPool = fusionTaskDeviceAsyncPool;
        this.redisModelDataService = redisModelDataService;
        this.deviceDataContext = deviceDataContext;
        this.fiberMetricDataService = fiberMetricDataService;
        this.laserMetricDataService = laserMetricDataService;
        this.waveMetricDataService = waveMetricDataService;
    }

    public void collectAndStoreDeviceData(long timestamp) {
        long timestampStart = timestamp - DEVICE_RECORD_TIME_COND + 1;
        try {
            var fiberDataTask = collectFiberDataTask(timestampStart, timestamp);
            var laserDataTask = collectLaserDataTask(timestampStart, timestamp);
            var waveDataTask = collectWaveDataTask(timestampStart, timestamp);
            CompletableFuture.allOf(fiberDataTask, laserDataTask, waveDataTask).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
            RadarRecordModel laserRadarRecordModel = laserDataTask.get();
            RadarRecordModel waveRadarRecordModel = waveDataTask.get();
            CompletableFuture.allOf(
                    fiberMetricDataService.storeFiberMetricData(fiberDataTask.get(), timestampStart),
                    laserMetricDataService.storeRadarMetricData(ListUtils.union(waveRadarRecordModel.radarMetricList, laserRadarRecordModel.radarMetricList), timestampStart),
                    deviceDataContext.updateRadarInfoList(ListUtils.union(waveRadarRecordModel.radarInfoList, laserRadarRecordModel.radarInfoList))
            ).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) { MessagePrintUtil.printException(e, "collectAndStoreDeviceData"); }
    }

    public CompletableFuture<FiberMetric> collectFiberDataTask(long timestampStart, long timestampEnd) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<VehicleModel> fiberModelDataList = redisModelDataService.collectFiberModel(timestampStart, timestampEnd).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
                return fiberMetricDataService.collectMetric(timestampStart - 1, timestampEnd, fiberModelDataList);
            } catch (Exception e) { MessagePrintUtil.printException(e, "collectFiberDataTask"); }
            return null;
        }, fusionTaskDeviceAsyncPool);
    }

    public CompletableFuture<RadarRecordModel> collectLaserDataTask(long timestampStart, long timestampEnd) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<VehicleModel> laserModelDataList = redisModelDataService.collectLaserModel(timestampStart, timestampEnd).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
                List<RadarInfo> laserRadarInfoList = deviceDataContext.getRadarInfoList().stream().filter(radarInfo -> radarInfo.getType() == DEVICE_TYPE_LASER).sorted(Comparator.comparingInt(RadarInfo::getRid)).toList();
                List<RadarMetric> laserRadarMetricList = laserMetricDataService.collectMetric(timestampStart - 1, timestampEnd, laserRadarInfoList, laserModelDataList);
                return new RadarRecordModel(laserRadarInfoList, laserRadarMetricList);
            } catch (Exception e) { MessagePrintUtil.printException(e, "collectLaserDataTask"); }
            return null;
        }, fusionTaskDeviceAsyncPool);
    }

    public CompletableFuture<RadarRecordModel> collectWaveDataTask(long timestampStart, long timestampEnd) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<VehicleModel> waveModelDataList = redisModelDataService.collectWaveModel(timestampStart, timestampEnd).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
                List<RadarInfo> waveRadarInfoList = deviceDataContext.getRadarInfoList().stream().filter(radarInfo -> radarInfo.getType() == DEVICE_TYPE_WAVE).sorted(Comparator.comparingInt(RadarInfo::getRid)).toList();
                List<RadarMetric> waveRadarMetricList = waveMetricDataService.collectMetric(timestampStart - 1, timestampEnd, waveRadarInfoList, waveModelDataList);
                return new RadarRecordModel(waveRadarInfoList, waveRadarMetricList);
            } catch (Exception e) { MessagePrintUtil.printException(e, "collectWaveDataTask"); }
            return null;
        }, fusionTaskDeviceAsyncPool);
    }

}
