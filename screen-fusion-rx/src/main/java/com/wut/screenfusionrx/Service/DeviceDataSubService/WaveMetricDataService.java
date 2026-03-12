package com.wut.screenfusionrx.Service.DeviceDataSubService;

import com.google.common.collect.Range;
import com.wut.screencommonrx.Model.TrajRecordModel;
import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screendbmysqlrx.Model.RadarInfo;
import com.wut.screendbmysqlrx.Model.RadarMetric;
import com.wut.screendbmysqlrx.Model.RadarSecMetric;
import com.wut.screendbmysqlrx.Model.SecInfo;
import com.wut.screendbmysqlrx.Util.DbModelTransformUtil;
import com.wut.screenfusionrx.Context.ModelDataContext;
import com.wut.screenfusionrx.Context.SectionDataContext;
import com.wut.screenfusionrx.Model.RotationMatrixModel;
import com.wut.screenfusionrx.Model.SectionIntervalModel;
import com.wut.screenfusionrx.Util.DeviceModelParamUtil;
import com.wut.screenfusionrx.Util.TrafficModelParamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;

@Component
public class WaveMetricDataService {
    private final SectionDataContext sectionDataContext;
    private final ModelDataContext modelDataContext;
    public record RadarSecRecordModel(RadarSecMetric radarSecMetric, Range<Double> interval, Map<Long, TrajRecordModel> radarRecordMapToEZ, Map<Long, TrajRecordModel> radarRecordMapToWH, List<Long> timeoutRecordList) {}

    @Autowired
    public WaveMetricDataService(SectionDataContext sectionDataContext, ModelDataContext modelDataContext) {
        this.sectionDataContext = sectionDataContext;
        this.modelDataContext = modelDataContext;
    }

    public List<RadarMetric> collectMetric(long timestampStart, long timestampEnd, List<RadarInfo> waveRadarInfoList, List<VehicleModel> waveModelDataList) {
        List<RadarMetric> waveMetricList = waveRadarInfoList.stream().map(wave -> DbModelTransformUtil.getRadarMetricInstance(timestampStart, timestampEnd, wave)).toList();
        if (CollectionEmptyUtil.forList(waveModelDataList)) {
            waveRadarInfoList.stream().forEach(waveInfo -> waveInfo.setState(DEVICE_STATE_OFFLINE));
            return waveMetricList;
        }
        AtomicInteger index = new AtomicInteger(0);
        waveMetricList.stream().forEach(waveMetric -> {
            RadarInfo waveRadarInfo = waveRadarInfoList.get(index.getAndIncrement());
            List<VehicleModel> targetWaveModelList = waveModelDataList.stream().filter(wave -> Objects.equals(wave.getIp(), waveMetric.getIp())).toList();
            long avgTimeout = 0L;
            if (!CollectionEmptyUtil.forList(targetWaveModelList)) {
                recordAndFlushWaveMetric(waveMetric, targetWaveModelList);
                avgTimeout = DeviceModelParamUtil.getDeviceAvgTimeout(targetWaveModelList);
                waveMetric.setAvgTimeout(avgTimeout);
            }
            waveRadarInfo.setState(DeviceModelParamUtil.getRadarDeviceNewState(avgTimeout));
        });
        return waveMetricList;
    }

    public List<RadarSecMetric> collectSecMetric(long timestampStart, long timestampEnd, List<RadarInfo> waveRadarInfoList, List<VehicleModel> waveModelDataList) {
        List<RadarSecRecordModel> waveSecRecordList = initWaveSecMetric(timestampStart, timestampEnd, waveRadarInfoList);
        if (!CollectionEmptyUtil.forList(waveModelDataList)) {
            waveModelDataList.stream().forEach(wave -> recordWaveSecMetric(waveSecRecordList, wave));
        }
        return flushWaveSecMetric(waveSecRecordList);
    }

    public void recordAndFlushWaveMetric(RadarMetric waveMetric, List<VehicleModel> targetWaveModelList) {
        Map<Long, TrajRecordModel> waveRecordDataMapToWH = new HashMap<>();
        Map<Long, TrajRecordModel> waveRecordDataMapToEZ = new HashMap<>();
        targetWaveModelList.stream().forEach(wave -> {
            switch (Integer.parseInt(wave.getRoadDirect())) {
                case ROAD_DIRECT_TO_WH -> TrafficModelParamUtil.recordVehicleModelToMap(waveRecordDataMapToWH, wave);
                case ROAD_DIRECT_TO_EZ -> TrafficModelParamUtil.recordVehicleModelToMap(waveRecordDataMapToEZ, wave);
            }
        });
        if (!CollectionEmptyUtil.forMap(waveRecordDataMapToWH)) {
            waveMetric.setAvgQwh(TrafficModelParamUtil.getTrajRecordPostureArgQ(waveRecordDataMapToWH));
            waveMetric.setAvgVwh(TrafficModelParamUtil.getTrajRecordArgV(waveRecordDataMapToWH));
        }
        if (!CollectionEmptyUtil.forMap(waveRecordDataMapToEZ)) {
            waveMetric.setAvgQez(TrafficModelParamUtil.getTrajRecordPostureArgQ(waveRecordDataMapToEZ));
            waveMetric.setAvgVez(TrafficModelParamUtil.getTrajRecordArgV(waveRecordDataMapToEZ));
        }
    }

    public List<RadarSecRecordModel> initWaveSecMetric(long timestampStart, long timestampEnd, List<RadarInfo> waveRadarInfoList) {
        List<RadarSecRecordModel> waveSecRecordList = new ArrayList<>();
        waveRadarInfoList.stream().forEach(radarInfo -> {
            RotationMatrixModel rotationMatrixModel = modelDataContext.getWaveRotationMap().get(radarInfo.getIp());
            SectionIntervalModel sectionIntervalModel = sectionDataContext.getSecIntervalMap().get(rotationMatrixModel.getSid());
            waveSecRecordList.add(new RadarSecRecordModel(
                    new RadarSecMetric(
                            sectionIntervalModel.getXsecName(),
                            sectionIntervalModel.getXsecValue(),
                            timestampStart,
                            timestampEnd,
                            radarInfo.getRid(),
                            radarInfo.getIp(),
                            radarInfo.getType(),
                            radarInfo.getRoadDirect(),
                            0.0,
                            0.0,
                            0.0,
                            0.0,
                            0L
                    ),
                    sectionIntervalModel.getInterval(),
                    new HashMap<>(),
                    new HashMap<>(),
                    new ArrayList<>()
            ));
        });
        return waveSecRecordList;
    }

    public void recordWaveSecMetric(List<RadarSecRecordModel> waveSecRecordList, VehicleModel wave) {
        waveSecRecordList.stream().filter(record -> {
            String ip = record.radarSecMetric.getIp();
            // 微波雷达同时匹配设备IP,方向和断面范围
            return Objects.equals(wave.getIp(), ip) && record.interval.contains(wave.getFrenetX());
        }).findFirst().ifPresent(record -> {
            switch(Integer.parseInt(wave.getRoadDirect())) {
                case ROAD_DIRECT_TO_WH -> TrafficModelParamUtil.recordVehicleModelToMap(record.radarRecordMapToWH, wave);
                case ROAD_DIRECT_TO_EZ -> TrafficModelParamUtil.recordVehicleModelToMap(record.radarRecordMapToEZ, wave);
            }
            record.timeoutRecordList.add(wave.getSaveTimestamp() - wave.getTimestamp());
        });
    }

    public List<RadarSecMetric> flushWaveSecMetric(List<RadarSecRecordModel> waveSecRecordList) {
        return waveSecRecordList.stream().map(record -> {
            RadarSecMetric waveSecMetric = record.radarSecMetric;
            if (!CollectionEmptyUtil.forMap(record.radarRecordMapToWH)) {
                waveSecMetric.setAvgQwh(TrafficModelParamUtil.getTrajRecordSectionArgQ(record.radarRecordMapToWH));
                waveSecMetric.setAvgVwh(TrafficModelParamUtil.getTrajRecordArgV(record.radarRecordMapToWH));
            }
            if (!CollectionEmptyUtil.forMap(record.radarRecordMapToEZ)) {
                waveSecMetric.setAvgQez(TrafficModelParamUtil.getTrajRecordSectionArgQ(record.radarRecordMapToEZ));
                waveSecMetric.setAvgVez(TrafficModelParamUtil.getTrajRecordArgV(record.radarRecordMapToEZ));
            }
            if (!CollectionEmptyUtil.forList(record.timeoutRecordList)) {
                waveSecMetric.setAvgTimeout(DeviceModelParamUtil.getDeviceSecAvgTimeout(record.timeoutRecordList));
            }
            return waveSecMetric;
        }).toList();
    }

}
