package com.wut.screenfusionrx.Service.DeviceDataSubService;

import com.google.common.collect.Range;
import com.wut.screencommonrx.Model.TrajRecordModel;
import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.DateParamParseUtil;
import com.wut.screendbmysqlrx.Model.*;
import com.wut.screendbmysqlrx.Service.RadarAllSecMetricService;
import com.wut.screendbmysqlrx.Service.RadarMetricService;
import com.wut.screendbmysqlrx.Service.RadarSecMetricService;
import com.wut.screendbmysqlrx.Util.DbModelTransformUtil;
import com.wut.screenfusionrx.Context.SectionDataContext;
import com.wut.screenfusionrx.Model.SectionIntervalModel;
import com.wut.screenfusionrx.Util.DeviceModelParamUtil;
import com.wut.screenfusionrx.Util.TrafficModelParamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;

@Component
public class LaserMetricDataService {
    @Qualifier("fusionTaskDeviceAsyncPool")
    private final Executor fusionTaskDeviceAsyncPool;
    @Qualifier("fusionTaskDeviceSecAsyncPool")
    private final Executor fusionTaskDeviceSecAsyncPool;
    private final RadarMetricService radarMetricService;
    private final RadarSecMetricService radarSecMetricService;
    private final SectionDataContext sectionDataContext;
    public record RadarSecRecordModel(RadarSecMetric radarSecMetric, Range<Double> interval, Map<Long, TrajRecordModel> radarRecordMapToEZ, Map<Long, TrajRecordModel> radarRecordMapToWH, List<Long> timeoutRecordList) {}

    @Autowired
    public LaserMetricDataService(Executor fusionTaskDeviceAsyncPool, Executor fusionTaskDeviceSecAsyncPool, RadarMetricService radarMetricService, RadarSecMetricService radarSecMetricService, SectionDataContext sectionDataContext) {
        this.fusionTaskDeviceAsyncPool = fusionTaskDeviceAsyncPool;
        this.fusionTaskDeviceSecAsyncPool = fusionTaskDeviceSecAsyncPool;
        this.radarMetricService = radarMetricService;
        this.radarSecMetricService = radarSecMetricService;
        this.sectionDataContext = sectionDataContext;
    }

    public List<RadarMetric> collectMetric(long timestampStart, long timestampEnd, List<RadarInfo> laserRadarInfoList, List<VehicleModel> laserModelDataList) {
        List<RadarMetric> laserMetricList = laserRadarInfoList.stream().map(info -> DbModelTransformUtil.getRadarMetricInstance(timestampStart, timestampEnd, info)).toList();
        // 激光雷达只接受武汉至鄂州方向(2方向)数据,简化统计逻辑
        if (CollectionEmptyUtil.forList(laserModelDataList)) {
            laserRadarInfoList.get(0).setState(DEVICE_STATE_OFFLINE);
            return laserMetricList;
        }
        recordAndFlushLaserMetric(laserMetricList.get(0), laserModelDataList);
        long avgTimeout = DeviceModelParamUtil.getDeviceAvgTimeout(laserModelDataList);
        laserMetricList.get(0).setAvgTimeout(avgTimeout);
        laserRadarInfoList.get(0).setState(DeviceModelParamUtil.getRadarDeviceNewState(avgTimeout));
        return laserMetricList;
    }

    public List<RadarSecMetric> collectSecMetric(long timestampStart, long timestampEnd, List<RadarInfo> laserRadarInfoList, List<VehicleModel> laserModelDataList) {
        List<RadarSecRecordModel> laserSecRecordList = initLaserSecMetric(timestampStart, timestampEnd, laserRadarInfoList);
        if (!CollectionEmptyUtil.forList(laserModelDataList)) {
            laserModelDataList.stream().forEach(laser -> recordLaserSecMetric(laserSecRecordList, laser));
        }
        return flushLaserSecMetric(laserSecRecordList);
    }

    public void recordAndFlushLaserMetric(RadarMetric laserMetric, List<VehicleModel> targetLaserModelList) {
        Map<Long, TrajRecordModel> laserRecordDataMap = new HashMap<>();
        targetLaserModelList.stream().forEach(laser -> TrafficModelParamUtil.recordVehicleModelToMap(laserRecordDataMap, laser));
        switch (laserMetric.getDirection()) {
            case ROAD_DIRECT_TO_WH -> {
                laserMetric.setAvgQwh(TrafficModelParamUtil.getTrajRecordPostureArgQ(laserRecordDataMap));
                laserMetric.setAvgVwh(TrafficModelParamUtil.getTrajRecordArgV(laserRecordDataMap));
            }
            case ROAD_DIRECT_TO_EZ -> {
                laserMetric.setAvgQez(TrafficModelParamUtil.getTrajRecordPostureArgQ(laserRecordDataMap));
                laserMetric.setAvgVez(TrafficModelParamUtil.getTrajRecordArgV(laserRecordDataMap));
            }
        }
    }

    public List<RadarSecRecordModel> initLaserSecMetric(long timestampStart, long timestampEnd, List<RadarInfo> laserRadarInfoList) {
        List<RadarSecRecordModel> laserSecRecordList = new ArrayList<>();
        // 激光雷达只接受武汉至鄂州方向(2方向)数据,简化统计逻辑
        RadarInfo radarInfo = laserRadarInfoList.get(0);
        SectionIntervalModel sectionIntervalModel = sectionDataContext.getSecIntervalMap().get(DEVICE_LASER_SECTION_SID);
        laserSecRecordList.add(new RadarSecRecordModel(
                new RadarSecMetric(sectionIntervalModel.getXsecName(), sectionIntervalModel.getXsecValue(), timestampStart, timestampEnd, radarInfo.getRid(), radarInfo.getIp(), radarInfo.getType(), radarInfo.getRoadDirect(), 0.0, 0.0, 0.0, 0.0, 0L),
                sectionIntervalModel.getInterval(),
                new HashMap<>(),
                new HashMap<>(),
                new ArrayList<>()
        ));
        return laserSecRecordList;
    }

    public void recordLaserSecMetric(List<RadarSecRecordModel> laserSecRecordList, VehicleModel laser) {
        laserSecRecordList.stream().filter(record -> {
            // 激光雷达同时匹配设备(方向相同)和断面(断面区间位置范围内)
            int direction = record.radarSecMetric.getDirection();
            return direction == Integer.parseInt(laser.getRoadDirect()) && record.interval.contains(laser.getFrenetX());
        }).findFirst().ifPresent(record -> {
            switch (Integer.parseInt(laser.getRoadDirect())) {
                case ROAD_DIRECT_TO_WH -> TrafficModelParamUtil.recordVehicleModelToMap(record.radarRecordMapToWH, laser);
                case ROAD_DIRECT_TO_EZ -> TrafficModelParamUtil.recordVehicleModelToMap(record.radarRecordMapToEZ, laser);
            }
            record.timeoutRecordList.add(laser.getSaveTimestamp() - laser.getTimestamp());
        });
    }

    public List<RadarSecMetric> flushLaserSecMetric(List<RadarSecRecordModel> laserSecRecordList) {
        return laserSecRecordList.stream().map(record -> {
           RadarSecMetric laserSecMetric = record.radarSecMetric;
           if (!CollectionEmptyUtil.forMap(record.radarRecordMapToWH)) {
               laserSecMetric.setAvgQwh(TrafficModelParamUtil.getTrajRecordSectionArgQ(record.radarRecordMapToWH));
               laserSecMetric.setAvgVwh(TrafficModelParamUtil.getTrajRecordArgV(record.radarRecordMapToWH));
           }
           if (!CollectionEmptyUtil.forMap(record.radarRecordMapToEZ)) {
               laserSecMetric.setAvgQez(TrafficModelParamUtil.getTrajRecordSectionArgQ(record.radarRecordMapToEZ));
               laserSecMetric.setAvgVez(TrafficModelParamUtil.getTrajRecordArgV(record.radarRecordMapToEZ));
           }
           if (!CollectionEmptyUtil.forList(record.timeoutRecordList)) {
               laserSecMetric.setAvgTimeout(DeviceModelParamUtil.getDeviceSecAvgTimeout(record.timeoutRecordList));
           }
           return laserSecMetric;
        }).toList();
    }

    public CompletableFuture<Void> storeRadarMetricData(List<RadarMetric> radarMetricList, long timestamp) {
        return CompletableFuture.runAsync(() -> {
            radarMetricService.storeRadarMetricData(DateParamParseUtil.getDateTimeStr(timestamp), radarMetricList);
        }, fusionTaskDeviceAsyncPool);
    }

    public CompletableFuture<Void> storeRadarSecMetricData(List<RadarSecMetric> radarSecMetricList, long timestamp) {
        return CompletableFuture.runAsync(() -> {
            radarSecMetricService.storeRadarSecMetricData(DateParamParseUtil.getDateTimeStr(timestamp), radarSecMetricList);
        }, fusionTaskDeviceSecAsyncPool);
    }
}
