package com.wut.screenfusionrx.Service;

import com.google.common.collect.Range;
import com.wut.screencommonrx.Model.TrajRecordModel;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.DateParamParseUtil;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbmysqlrx.Model.SecInfo;
import com.wut.screendbmysqlrx.Model.Section;
import com.wut.screendbmysqlrx.Model.Traj;
import com.wut.screendbmysqlrx.Service.SectionService;
import com.wut.screendbmysqlrx.Util.DbModelTransformUtil;
import com.wut.screendbredisrx.Service.RedisExpireDataService;
import com.wut.screendbredisrx.Service.RedisTrajFusionService;
import com.wut.screenfusionrx.Context.SectionDataContext;
import com.wut.screenfusionrx.Model.SectionIntervalModel;
import com.wut.screenfusionrx.Util.TrafficModelParamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;

@Component
public class SectionDataService {
    private final SectionService sectionService;
    private final SectionDataContext sectionDataContext;
    private final RedisTrajFusionService redisTrajFusionService;
    private final RedisExpireDataService redisExpireDataService;
    public record SecRecordModel(Section section, Range<Double> interval, Map<Long, TrajRecordModel> trajRecordMapToEZ, Map<Long, TrajRecordModel> trajRecordMapToWH) {}

    @Autowired
    public SectionDataService(RedisTrajFusionService redisTrajFusionService, SectionService sectionService, SectionDataContext sectionDataContext, RedisExpireDataService redisExpireDataService) {
        this.redisTrajFusionService = redisTrajFusionService;
        this.sectionService = sectionService;
        this.sectionDataContext = sectionDataContext;
        this.redisExpireDataService = redisExpireDataService;
    }

    public void collectAndStoreSectionData(long timestamp) {
        List<SecRecordModel> secRecordModelList = initSectionCollection(timestamp - SECTION_RECORD_TIME_COND, timestamp);
        try {
            redisExpireDataService.startRemoveExpireData(timestamp);
            List<Traj> trajList = redisTrajFusionService.collectTrajData(timestamp - SECTION_RECORD_TIME_COND + 1, timestamp).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
            if (!CollectionEmptyUtil.forList(trajList)) {
                trajList.stream().forEach(traj -> recordTrajToSection(secRecordModelList, traj));
            }
        } catch (Exception e) { MessagePrintUtil.printException(e, "collectSectionData"); }
        List<Section> sectionList = flushSectionData(secRecordModelList);
        sectionService.storeSectionData(DateParamParseUtil.getDateTimeStr(timestamp - SECTION_RECORD_TIME_COND), sectionList);
    }

    public List<SecRecordModel> initSectionCollection(long timeStart, long timeEnd) {
        List<SectionIntervalModel> intervalModelList = sectionDataContext.getSecIntervalList();
        return intervalModelList.stream().map(intervalModel -> new SecRecordModel(
                new Section(intervalModel.getXsecName(), intervalModel.getXsecValue(), timeStart, timeEnd, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                intervalModel.getInterval(),
                new HashMap<>(),
                new HashMap<>()
        )).toList();
    }

    public void recordTrajToSection(List<SecRecordModel> secRecordModelList, Traj traj) {
        // 获取指定断面匹配的路段区间
        secRecordModelList.stream()
                .filter(record -> record.interval.contains(traj.getFrenetX()))
                .findFirst().ifPresent(record -> {
                    switch (traj.getRoadDirect()) {
                        case ROAD_DIRECT_TO_WH -> TrafficModelParamUtil.recordTrajToMap(record.trajRecordMapToWH, traj);
                        case ROAD_DIRECT_TO_EZ -> TrafficModelParamUtil.recordTrajToMap(record.trajRecordMapToEZ, traj);
                    }
        });
    }

    public List<Section> flushSectionData(List<SecRecordModel> secRecordModelList) {
        return secRecordModelList.stream().map(secRecordModel -> {
            Section section = secRecordModel.section;
            Map<Long, TrajRecordModel> trajRecordMapToEZ = secRecordModel.trajRecordMapToEZ;
            Map<Long, TrajRecordModel> trajRecordMapToWH = secRecordModel.trajRecordMapToWH;
            double interval = secRecordModel.interval.upperEndpoint()-secRecordModel.interval.lowerEndpoint();
            if (!CollectionEmptyUtil.forMap(trajRecordMapToEZ)) {
                section.setAvgQez(TrafficModelParamUtil.getTrajRecordSectionArgQ(trajRecordMapToEZ));
                section.setAvgVez(TrafficModelParamUtil.getTrajRecordArgV(trajRecordMapToEZ));
                section.setAvgKez(TrafficModelParamUtil.getTrajRecordArgK(trajRecordMapToEZ, section.getAvgVez(), interval));
            }
            if (!CollectionEmptyUtil.forMap(trajRecordMapToWH)) {
                section.setAvgQwh(TrafficModelParamUtil.getTrajRecordSectionArgQ(trajRecordMapToWH));
                section.setAvgVwh(TrafficModelParamUtil.getTrajRecordArgV(trajRecordMapToWH));
                section.setAvgKwh(TrafficModelParamUtil.getTrajRecordArgK(trajRecordMapToWH, section.getAvgVwh(), interval));
            }
            // 拥堵指数最小值为1.0
            if (section.getAvgKez() < 1.0) {
                section.setAvgKez(1.0);
            }
            if (section.getAvgKwh() < 1.0) {
                section.setAvgKwh(1.0);
            }
            return section;
        }).toList();
    }

}
