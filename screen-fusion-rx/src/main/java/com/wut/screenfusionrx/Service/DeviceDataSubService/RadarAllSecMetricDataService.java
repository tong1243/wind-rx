package com.wut.screenfusionrx.Service.DeviceDataSubService;

import com.google.common.collect.Range;
import com.wut.screencommonrx.Model.TrajRecordModel;
import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.DateParamParseUtil;
import com.wut.screendbmysqlrx.Model.FiberSecMetric;
import com.wut.screendbmysqlrx.Model.RadarAllSecMetric;
import com.wut.screendbmysqlrx.Model.RadarInfo;
import com.wut.screendbmysqlrx.Model.RadarSecMetric;
import com.wut.screendbmysqlrx.Service.RadarAllSecMetricService;
import com.wut.screenfusionrx.Context.SectionDataContext;
import com.wut.screenfusionrx.Model.SectionIntervalModel;
import com.wut.screenfusionrx.Util.DeviceModelParamUtil;
import com.wut.screenfusionrx.Util.TrafficModelParamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.wut.screencommonrx.Static.FusionModuleStatic.ROAD_DIRECT_TO_EZ;
import static com.wut.screencommonrx.Static.FusionModuleStatic.ROAD_DIRECT_TO_WH;

@Component
public class RadarAllSecMetricDataService {
    @Qualifier("fusionTaskDeviceSecAsyncPool")
    private final Executor fusionTaskDeviceSecAsyncPool;
    private final RadarAllSecMetricService radarAllSecMetricService;
    private final SectionDataContext sectionDataContext;
    public record RadarAllSecRecordModel(RadarAllSecMetric radarAllSecMetric, Range<Double> interval, Map<Long, TrajRecordModel> radarRecordMapToEZ, Map<Long, TrajRecordModel> radarRecordMapToWH, List<Long> timeoutRecordList) {}

    @Autowired
    public RadarAllSecMetricDataService(Executor fusionTaskDeviceSecAsyncPool, RadarAllSecMetricService radarAllSecMetricService, SectionDataContext sectionDataContext) {
        this.fusionTaskDeviceSecAsyncPool = fusionTaskDeviceSecAsyncPool;
        this.radarAllSecMetricService = radarAllSecMetricService;
        this.sectionDataContext = sectionDataContext;
    }

    public List<RadarAllSecMetric> collectSecMetric(long timestampStart, long timestampEnd, List<VehicleModel> vehicleModelList) {
        List<RadarAllSecRecordModel> radarAllSecRecordList = initRadarAllSecMetric(timestampStart, timestampEnd);
        if (!CollectionEmptyUtil.forList(vehicleModelList)) {
            vehicleModelList.stream().forEach(model -> recordRadarAllSecMetric(radarAllSecRecordList, model));
        }
        return flushRadarAllSecMetric(radarAllSecRecordList);
    }

    public List<RadarAllSecRecordModel> initRadarAllSecMetric(long timestampStart, long timestampEnd) {
        List<SectionIntervalModel> intervalModelList = sectionDataContext.getSecIntervalList();
        return intervalModelList.stream().map(intervalModel -> {
            return new RadarAllSecRecordModel(
                    new RadarAllSecMetric(intervalModel.getXsecName(), intervalModel.getXsecValue(), timestampStart, timestampEnd, 0.0, 0.0, 0.0, 0.0, 0L),
                    intervalModel.getInterval(),
                    new HashMap<>(),
                    new HashMap<>(),
                    new ArrayList<>()
            );
        }).toList();
    }

    public void recordRadarAllSecMetric(List<RadarAllSecRecordModel> radarAllSecRecordList, VehicleModel model) {
        radarAllSecRecordList.stream().filter(record -> record.interval.contains(model.getFrenetX()))
                .findFirst().ifPresent(record -> {
                    switch(Integer.parseInt(model.getRoadDirect())) {
                        case ROAD_DIRECT_TO_WH -> TrafficModelParamUtil.recordVehicleModelToMap(record.radarRecordMapToWH, model);
                        case ROAD_DIRECT_TO_EZ -> TrafficModelParamUtil.recordVehicleModelToMap(record.radarRecordMapToEZ, model);
                    }
                    record.timeoutRecordList.add(model.getSaveTimestamp() - model.getTimestamp());
                });
    }

    public List<RadarAllSecMetric> flushRadarAllSecMetric(List<RadarAllSecRecordModel> radarAllSecRecordList) {
        return radarAllSecRecordList.stream().map(record -> {
            RadarAllSecMetric radarAllSecMetric = record.radarAllSecMetric;
            if (!CollectionEmptyUtil.forMap(record.radarRecordMapToEZ)) {
                radarAllSecMetric.setAvgQez(TrafficModelParamUtil.getTrajRecordSectionArgQ(record.radarRecordMapToEZ));
                radarAllSecMetric.setAvgVez(TrafficModelParamUtil.getTrajRecordArgV(record.radarRecordMapToEZ));
            }
            if (!CollectionEmptyUtil.forMap(record.radarRecordMapToWH)) {
                radarAllSecMetric.setAvgQwh(TrafficModelParamUtil.getTrajRecordSectionArgQ(record.radarRecordMapToWH));
                radarAllSecMetric.setAvgVwh(TrafficModelParamUtil.getTrajRecordArgV(record.radarRecordMapToWH));
            }
            if (!CollectionEmptyUtil.forList(record.timeoutRecordList)) {
                radarAllSecMetric.setAvgTimeout(DeviceModelParamUtil.getDeviceSecAvgTimeout(record.timeoutRecordList));
            }
            return radarAllSecMetric;
        }).toList();
    }

    public CompletableFuture<Void> storeRadarAllSecMetricData(List<RadarAllSecMetric> radarAllSecMetricList, long timestamp) {
        return CompletableFuture.runAsync(() -> {
            radarAllSecMetricService.storeRadarAllSecMetricData(DateParamParseUtil.getDateTimeStr(timestamp), radarAllSecMetricList);
        }, fusionTaskDeviceSecAsyncPool);
    }


}
