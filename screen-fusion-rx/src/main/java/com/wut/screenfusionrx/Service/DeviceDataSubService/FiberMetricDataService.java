package com.wut.screenfusionrx.Service.DeviceDataSubService;

import com.google.common.collect.Range;
import com.wut.screencommonrx.Model.TrajRecordModel;
import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.DateParamParseUtil;
import com.wut.screendbmysqlrx.Model.FiberMetric;
import com.wut.screendbmysqlrx.Model.FiberSecMetric;
import com.wut.screendbmysqlrx.Model.SecInfo;
import com.wut.screendbmysqlrx.Service.FiberMetricService;
import com.wut.screendbmysqlrx.Service.FiberSecMetricService;
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

import static com.wut.screencommonrx.Static.FusionModuleStatic.ROAD_DIRECT_TO_EZ;
import static com.wut.screencommonrx.Static.FusionModuleStatic.ROAD_DIRECT_TO_WH;

@Component
public class FiberMetricDataService {
    @Qualifier("fusionTaskDeviceAsyncPool")
    private final Executor fusionTaskDeviceAsyncPool;
    @Qualifier("fusionTaskDeviceSecAsyncPool")
    private final Executor fusionTaskDeviceSecAsyncPool;
    private final FiberMetricService fiberMetricService;
    private final FiberSecMetricService fiberSecMetricService;
    private final SectionDataContext sectionDataContext;
    public record FiberSecRecordModel(FiberSecMetric fiberSecMetric, Range<Double> interval, Map<Long, TrajRecordModel> fiberRecordMapToEZ, Map<Long, TrajRecordModel> fiberRecordMapToWH, List<Long> timeoutRecordList) {}

    @Autowired
    public FiberMetricDataService(Executor fusionTaskDeviceAsyncPool, Executor fusionTaskDeviceSecAsyncPool, FiberMetricService fiberMetricService, FiberSecMetricService fiberSecMetricService, SectionDataContext sectionDataContext) {
        this.fusionTaskDeviceAsyncPool = fusionTaskDeviceAsyncPool;
        this.fusionTaskDeviceSecAsyncPool = fusionTaskDeviceSecAsyncPool;
        this.fiberMetricService = fiberMetricService;
        this.fiberSecMetricService = fiberSecMetricService;
        this.sectionDataContext = sectionDataContext;
    }

    public FiberMetric collectMetric(long timestampStart, long timestampEnd, List<VehicleModel> fiberModelDataList) {
        FiberMetric fiberMetric = DbModelTransformUtil.getFiberMetricInstance(timestampStart, timestampEnd);
        if (!CollectionEmptyUtil.forList(fiberModelDataList)) {
            recordAndFlushFiberMetric(fiberMetric, fiberModelDataList);
            fiberMetric.setAvgTimeout(DeviceModelParamUtil.getDeviceAvgTimeout(fiberModelDataList));
        }
        return fiberMetric;
    }

    public List<FiberSecMetric> collectSecMetric(long timestampStart, long timestampEnd, List<VehicleModel> fiberModelDataList) {
        List<FiberSecRecordModel> fiberSecRecordList = initFiberSecMetric(timestampStart, timestampEnd);
        if (!CollectionEmptyUtil.forList(fiberModelDataList)) {
            fiberModelDataList.stream().forEach(fiber -> recordFiberSecMetric(fiberSecRecordList, fiber));
        }
        return flushFiberSecMetric(fiberSecRecordList);
    }

    public void recordAndFlushFiberMetric(FiberMetric fiberMetric, List<VehicleModel> fiberModelDataList) {
        Map<Long, TrajRecordModel> fiberRecordDataMapToWH = new HashMap<>();
        Map<Long, TrajRecordModel> fiberRecordDataMapToEZ = new HashMap<>();
        fiberModelDataList.stream().forEach(fiber -> {
            int direction = Integer.parseInt(fiber.getRoadDirect());
            switch (direction) {
                case ROAD_DIRECT_TO_WH -> TrafficModelParamUtil.recordVehicleModelToMap(fiberRecordDataMapToWH, fiber);
                case ROAD_DIRECT_TO_EZ -> TrafficModelParamUtil.recordVehicleModelToMap(fiberRecordDataMapToEZ, fiber);
            }
        });
        if (!CollectionEmptyUtil.forMap(fiberRecordDataMapToWH)) {
            fiberMetric.setAvgQwh(TrafficModelParamUtil.getTrajRecordPostureArgQ(fiberRecordDataMapToWH));
            fiberMetric.setAvgVwh(TrafficModelParamUtil.getTrajRecordArgV(fiberRecordDataMapToWH));
        }
        if (!CollectionEmptyUtil.forMap(fiberRecordDataMapToEZ)) {
            fiberMetric.setAvgQez(TrafficModelParamUtil.getTrajRecordPostureArgQ(fiberRecordDataMapToEZ));
            fiberMetric.setAvgVez(TrafficModelParamUtil.getTrajRecordArgV(fiberRecordDataMapToEZ));
        }
    }

    public List<FiberSecRecordModel> initFiberSecMetric(long timestampStart, long timestampEnd) {
        List<SectionIntervalModel> intervalModelList = sectionDataContext.getSecIntervalList();
        return intervalModelList.stream().map(intervalModel -> {
            return new FiberSecRecordModel(
                    new FiberSecMetric(intervalModel.getXsecName(), intervalModel.getXsecValue(), timestampStart, timestampEnd, 0.0, 0.0, 0.0, 0.0, 0L),
                    intervalModel.getInterval(),
                    new HashMap<>(),
                    new HashMap<>(),
                    new ArrayList<>()
            );
        }).toList();
    }

    public void recordFiberSecMetric(List<FiberSecRecordModel> fiberSecRecordList, VehicleModel fiber) {
        fiberSecRecordList.stream().filter(record -> record.interval.contains(fiber.getFrenetX()))
        .findFirst().ifPresent(record -> {
            switch (Integer.parseInt(fiber.getRoadDirect())) {
                case ROAD_DIRECT_TO_WH -> TrafficModelParamUtil.recordVehicleModelToMap(record.fiberRecordMapToWH, fiber);
                case ROAD_DIRECT_TO_EZ -> TrafficModelParamUtil.recordVehicleModelToMap(record.fiberRecordMapToEZ, fiber);
            }
            record.timeoutRecordList.add(fiber.getSaveTimestamp() - fiber.getTimestamp());
        });
    }

    public List<FiberSecMetric> flushFiberSecMetric(List<FiberSecRecordModel> fiberSecRecordList) {
        return fiberSecRecordList.stream().map(record -> {
            FiberSecMetric fiberSecMetric = record.fiberSecMetric;
            if (!CollectionEmptyUtil.forMap(record.fiberRecordMapToWH)) {
                fiberSecMetric.setAvgQwh(TrafficModelParamUtil.getTrajRecordSectionArgQ(record.fiberRecordMapToWH));
                fiberSecMetric.setAvgVwh(TrafficModelParamUtil.getTrajRecordArgV(record.fiberRecordMapToWH));
            }
            if (!CollectionEmptyUtil.forMap(record.fiberRecordMapToEZ)) {
                fiberSecMetric.setAvgQez(TrafficModelParamUtil.getTrajRecordSectionArgQ(record.fiberRecordMapToEZ));
                fiberSecMetric.setAvgVez(TrafficModelParamUtil.getTrajRecordArgV(record.fiberRecordMapToEZ));
            }
            if (!CollectionEmptyUtil.forList(record.timeoutRecordList)) {
                fiberSecMetric.setAvgTimeout(DeviceModelParamUtil.getDeviceSecAvgTimeout(record.timeoutRecordList));
            }
            return fiberSecMetric;
        }).toList();
    }

    public CompletableFuture<Void> storeFiberMetricData(FiberMetric fiberMetric, long timestamp) {
        return CompletableFuture.runAsync(() -> {
            fiberMetricService.storeFiberMetricData(DateParamParseUtil.getDateTimeStr(timestamp), fiberMetric);
        }, fusionTaskDeviceAsyncPool);
    }

    public CompletableFuture<Void> storeFiberSecMetricData(List<FiberSecMetric> fiberSecMetricList, long timestamp) {
        return CompletableFuture.runAsync(() -> {
            fiberSecMetricService.storeFiberSecMetricData(DateParamParseUtil.getDateTimeStr(timestamp), fiberSecMetricList);
        }, fusionTaskDeviceSecAsyncPool);
    }

}
