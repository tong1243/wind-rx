package com.wut.screenfusionrx.Service.TrajFusionSubService;

import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbredisrx.Service.RedisModelDataService;
import com.wut.screenfusionrx.Context.TrajDataContext;
import com.wut.screenfusionrx.Model.TrajMatchMarkModel;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;
import static com.wut.screencommonrx.Static.ModelConvertStatic.*;

@Component
public class TrajModelDeDuplicateService {
    private final TrajDataContext trajDataContext;
    private final RedisModelDataService redisModelDataService;

    @Autowired
    public TrajModelDeDuplicateService(TrajDataContext trajDataContext, RedisModelDataService redisModelDataService) {
        this.trajDataContext = trajDataContext;
        this.redisModelDataService = redisModelDataService;
    }
    public List<VehicleModel> deDuplicate(double timestamp) {
        // 内存数据库取单设备去重后的原始数据
        var fiberFlushModelTask = redisModelDataService.collectFiberFlushModel(timestamp);
        List<VehicleModel> data = new ArrayList<>();
        try {
            CompletableFuture.allOf(fiberFlushModelTask)
                    .get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
            data.addAll(fiberFlushModelTask.join());
        } catch (Exception e) {
            MessagePrintUtil.printException(e, "deDuplicate");
        }
        return data;
    }
//    public void setCarPlateData(List<VehicleModel> data) {
//
//        for (VehicleModel model : data){
//           //如果是光纤数据则为鄂F+id,如果是激光数据则为鄂L+id,如果是微波数据则为鄂M+id
//            if (model.getType() == MODEL_TYPE_WAVE) {
//                model.setCarId("鄂M" + trajDataContext.getFiberNextId().incrementAndGet());
//            } else if (model.getType() == MODEL_TYPE_LASER) {
//                model.setCarId("鄂L" + trajDataContext.getLaserNextId().incrementAndGet());
//            } else if (model.getType() == MODEL_TYPE_FIBER){
//                model.setCarId("鄂F" + trajDataContext.getWaveNextId().incrementAndGet());
//            }
//        }
//    }

//    public List<VehicleModel> deDuplicate(double timestamp) {
//        // 内存数据库取单设备去重后的原始数据
//        var fiberFlushModelTask = redisModelDataService.collectFiberFlushModel(timestamp);
//        var laserFlushModelTask = redisModelDataService.collectLaserFlushModel(timestamp);
//        var waveFlushModelTask = redisModelDataService.collectWaveFlushModel(timestamp);
//        try {
//            CompletableFuture.allOf(fiberFlushModelTask, laserFlushModelTask, waveFlushModelTask).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
//            // 1:筛选出不在黑名单范围内的光纤数据
//            // 根据是否已记录光纤ID(即光纤<->雷达有匹配关系),将光纤数据分为两部分,在多设备去重过程中采用不同的逻辑
//            Map<Integer, TrajMatchMarkModel> trajDeDuplicateMarkMap = trajDataContext.getTrajDeDuplicateMarkMap();
//            Map<Boolean, List<VehicleModel>> fiberPartitionMap = fiberFlushModelTask.get().stream()
//                    .filter(this::examineFiberModel)
//                    .collect(Collectors.partitioningBy(fiber -> trajDeDuplicateMarkMap.containsKey(fiber.getId())));
//            // 2:筛选出在白名单范围内的激光雷达数据
//            // 不在白名单中(即主干道)的激光雷达数据和微波雷达数据行为相同,合并为多设备去重过程中匹配的雷达数据
//            Map<Boolean, List<VehicleModel>> laserPartitionMap = laserFlushModelTask.get().stream().collect(Collectors.partitioningBy(this::examineLaserModel));
//            Map<Integer, VehicleModel> radarPartitionMap = Stream.concat(waveFlushModelTask.get().stream(), laserPartitionMap.get(false).stream()).collect(Collectors.toMap(VehicleModel::getId, radar -> radar));
//            // 3:已记录和未记录光纤ID的光纤数据,与合并后的雷达数据进行多设备去重匹配
//            List<VehicleModel> calibratedFiberModelList = ListUtils.union(
//                    deDuplicateOnFiberWithRecordId(fiberPartitionMap, radarPartitionMap),
//                    deDuplicateOnFiberWithoutRecordId(fiberPartitionMap, radarPartitionMap)
//            );
//            // 4:拼接白名单范围内的激光雷达数据,组成最终的多设备去重结果
//            return ListUtils.union(calibratedFiberModelList, laserPartitionMap.get(true));
//        } catch (Exception e) { MessagePrintUtil.printException(e, "deDuplicate"); }
//        return List.of();
//    }

    // 检查并填充激光雷达ID白名单,记录在白名单中的数据不再和光纤进行匹配
    // -> 当激光雷达数据的ID首次出现时,检查桩号是否处于匝道范围同时为[6,7]车道,填充白名单标志位
    // -> 当激光雷达数据的ID后续出现时,直接调取白名单获取标志位
    public boolean examineLaserModel(VehicleModel laser) {
        Map<Integer, Boolean> laserModelMarkMap = trajDataContext.getLaserModelMarkMap();
        if (laserModelMarkMap.containsKey(laser.getId())) {
            return laserModelMarkMap.get(laser.getId());
        }
        boolean flag = MODEL_EZ_ZA_RANGE1.contains(laser.getFrenetX()) && LASER_ZA_LANE_RANGE.contains(laser.getLane());
        laserModelMarkMap.put(laser.getId(), flag);
        return flag;
    }

    // 检查并填充光纤ID黑名单,记录在黑名单中的数据不再参与多设备去重(注意返回的标志位要取反,filter的false对应丢弃数据)
    // -> 当光纤数据的ID首次出现时,检查下方注释所示的条件,填充黑名单标志位
    // -> 当光纤数据的ID后续出现时,直接调取白名单获取标志位
    public boolean examineFiberModel(VehicleModel fiber) {
        Map<Integer, Boolean> fiberModelMarkMap = trajDataContext.getFiberModelMarkMap();
        if (fiberModelMarkMap.containsKey(fiber.getId())) {
            return !fiberModelMarkMap.get(fiber.getId());
        }
        // 在下列两种情况下,光纤数据对应的ID会被记录黑名单
        // -> 光纤数据2方向,桩号位于激光雷达对应匝道范围内,车道为[6,7]
        // -> 光纤数据2方向,桩号位于激光雷达对应加速车道范围内,车道为3
        boolean flag = Integer.parseInt(fiber.getRoadDirect()) == ROAD_DIRECT_TO_EZ && (
                (MODEL_EZ_ZA_RANGE1.contains(fiber.getFrenetX()) && LASER_ZA_LANE_RANGE.contains(fiber.getLane()))
                || (MODEL_EZ_ACCX_RANGE1.contains(fiber.getFrenetX()) && fiber.getLane() == 3)
        );
        fiberModelMarkMap.put(fiber.getId(), flag);
        return !flag;
    }

    // 已记录光纤ID的光纤数据多设备去重
    public List<VehicleModel> deDuplicateOnFiberWithRecordId(Map<Boolean, List<VehicleModel>> fiberPartitionMap, Map<Integer, VehicleModel> radarPartitionMap) {
        if (CollectionEmptyUtil.forList(fiberPartitionMap.get(true))) { return List.of(); }
        Map<Integer, TrajMatchMarkModel> trajDeDuplicateMarkMap = trajDataContext.getTrajDeDuplicateMarkMap();
        List<VehicleModel> modelList = new ArrayList<>();
        fiberPartitionMap.get(true).stream().forEach(fiber -> {
            TrajMatchMarkModel mark = trajDeDuplicateMarkMap.get(fiber.getId());
            // 如果指定时间戳有对应ID的雷达数据,取该雷达数据校准光纤数据后删除,表中记录更新为true
            if (radarPartitionMap.containsKey(mark.getRadarId())) {
                modelList.add(trajModelCalibration(fiber, radarPartitionMap.get(mark.getRadarId())));
                radarPartitionMap.remove(mark.getRadarId());
                mark.setStatus(true);
            } else {
                // 如果指定时间戳没有对应ID的雷达数据,分为下列两种情况
                // -> 当表中记录为true时,说明上个时间戳有对应的雷达数据,当前为断点,直接沿用光纤数据,表中记录更新为false
                // -> 当表中记录为false时,说明上个时间戳也为断点,此时删除表中记录,该光纤数据后续和无ID数据一同进行匹配
                if (mark.isStatus()) {
                    fiber.setType(MODEL_TYPE_FIBER_DISRUPT);
                    modelList.add(fiber);
                    mark.setStatus(false);
                } else {
                    fiberPartitionMap.get(false).add(fiber);
                    trajDeDuplicateMarkMap.remove(fiber.getId());
                }
            }
        });
        return modelList;
    }

    // 未记录光纤ID的光纤数据多设备去重
    public List<VehicleModel> deDuplicateOnFiberWithoutRecordId(Map<Boolean, List<VehicleModel>> fiberPartitionMap, Map<Integer, VehicleModel> radarPartitionMap) {
        if (CollectionEmptyUtil.forList(fiberPartitionMap.get(false))) { return List.of(); }
        Map<Integer, TrajMatchMarkModel> trajDeDuplicateMarkMap = trajDataContext.getTrajDeDuplicateMarkMap();
        List<VehicleModel> modelList = new ArrayList<>();
        fiberPartitionMap.get(false).stream().forEach(fiber -> {
            // 光纤数据和雷达数据匹配规则:方向相同,FrenetX差 < 10,FrenetY差 < 3.75
            // 如果存在满足匹配规则的雷达数据,取FrenetX差最小的雷达数据作去重操作
            radarPartitionMap.values().stream().filter(radar ->
                    Objects.equals(fiber.getRoadDirect(), radar.getRoadDirect())
                    && Math.abs(fiber.getFrenetX() - radar.getFrenetX()) < FRENETX_TOLERANCE
                    && Math.abs(fiber.getFrenetY() - radar.getFrenetY()) < FRENETY_TOLERANCE
            ).min(Comparator.comparingDouble(radar -> Math.abs(fiber.getFrenetX() - radar.getFrenetX()))).ifPresentOrElse(
                    // 使用雷达数据参数校准光纤数据,校准结束后更新光纤雷达ID映射表
                    (radar) -> {
                        modelList.add(trajModelCalibration(fiber, radar));
                        trajDeDuplicateMarkMap.put(fiber.getId(), new TrajMatchMarkModel(radar.getId(), true));
                        radarPartitionMap.remove(radar.getId());
                    },
                    () -> modelList.add(fiber)
            );
        });
        return modelList;
    }

    public VehicleModel trajModelCalibration(VehicleModel source, VehicleModel target) {
        source.setLongitude(target.getLongitude());
        source.setLatitude(target.getLatitude());
        source.setMercatorX(target.getMercatorX());
        source.setMercatorY(target.getMercatorY());
        source.setFrenetX(target.getFrenetX());
        source.setFrenetY(target.getFrenetY());
        source.setHeadingAngle(target.getHeadingAngle());
        source.setFrenetAngle(target.getFrenetAngle());
        // 光纤数据原始Type = 10
        // 激光雷达数据原始Type = 20
        // 微波雷达数据原始Type = 30
        // -> 使用激光雷达校准的光纤数据Type = 12
        // -> 使用微波雷达校准的光纤数据Type = 13
        // -> 光纤数据匹配断点Type = 15
        int type = source.getType() + (target.getType() / 10);
        source.setType(type);
        // 微波雷达校准光纤数据的同时校准轨迹的车长
        if (target.getType() == MODEL_TYPE_WAVE) {
            source.setLength(target.getLength());
        }
        source.setIdDup(target.getId());
        return source;
    }

}
