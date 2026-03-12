package com.wut.screenfusionrx.Service;

import com.google.common.util.concurrent.AtomicDouble;
import com.wut.screencommonrx.Model.CarEventModel;
import com.wut.screencommonrx.Model.EventTypeModel;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screencommonrx.Util.ModelTransformUtil;
import com.wut.screendbmysqlrx.Model.CarEvent;
import com.wut.screendbmysqlrx.Model.Traj;
import com.wut.screendbmysqlrx.Model.TunnelSecInfo;
import com.wut.screendbmysqlrx.Service.SecInfoService;
import com.wut.screendbmysqlrx.Util.DbModelTransformUtil;
import com.wut.screendbredisrx.Service.RedisBatchCacheService;
import com.wut.screendbredisrx.Service.RedisTrajFusionService;
import com.wut.screenfusionrx.Context.EventDataContext;
import com.wut.screenfusionrx.Util.EventModelEstimateUtil;
import com.wut.screenfusionrx.Util.TrajModelParamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;

@Component
public class EventDataService {
    @Qualifier("fusionTaskEventAsyncPool")
    private final Executor fusionTaskEventAsyncPool;
    private final EventDataContext eventDataContext;
    private final RedisTrajFusionService redisTrajFusionService;
    private final RedisBatchCacheService redisBatchCacheService;
    private final SecInfoService secInfoService;

    @Autowired
    public EventDataService(Executor fusionTaskEventAsyncPool, EventDataContext eventDataContext, RedisTrajFusionService redisTrajFusionService, RedisBatchCacheService redisBatchCacheService, SecInfoService secInfoService) {
        this.fusionTaskEventAsyncPool = fusionTaskEventAsyncPool;
        this.eventDataContext = eventDataContext;
        this.redisTrajFusionService = redisTrajFusionService;
        this.redisBatchCacheService = redisBatchCacheService;
        this.secInfoService = secInfoService;
    }

    public void collectAndStoreEventModelData(long timestamp) {
        try {
            List<Traj> trajList = redisTrajFusionService.collectTrajData(timestamp).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
            List<TunnelSecInfo> allTunnelSecInfo = secInfoService.getAllTunnelSecInfo();
            for (TunnelSecInfo tunnelSecInfo : allTunnelSecInfo) {

                    if (tunnelSecInfo.getSid()==3|| tunnelSecInfo.getSid()==4|| tunnelSecInfo.getSid()==7) {
                        List<Traj> accidentAreaTrajList = trajList.stream().filter(traj -> traj.getFrenetX() >= tunnelSecInfo.getStart() && traj.getFrenetX() <= tunnelSecInfo.getEnd()
                                &&(traj.getLane()==1||traj.getLane()==2||traj.getLane()==3)).toList();
                        double maxQueueLength = 0.0;
                        double queueLength = calculateQueueLength(accidentAreaTrajList, maxQueueLength);
                        // 计算平均速度
                        double avgSpeed = accidentAreaTrajList.stream().mapToDouble(Traj::getSpeedX).average().orElse(0.0);
                        if (!CollectionEmptyUtil.forList(accidentAreaTrajList)) {
                            List<CompletableFuture<Void>> matchEventModelTask = accidentAreaTrajList.stream().map(traj -> {
                                return CompletableFuture.runAsync(() -> matchEventModel(traj, avgSpeed, queueLength), fusionTaskEventAsyncPool);
                            }).toList();
                            CompletableFuture.allOf(matchEventModelTask.toArray(CompletableFuture[]::new)).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
                        }
                        // 清理未更新的事件数据中超时的部分
                        eventDataContext.filterExpireEventModel(timestamp);
                        saveAndStoreEventModel(timestamp);
                        // 测试日志输出,非必要情况下应当注释
                        // eventDataContext.printEventModelMapToLogger(timestamp);
                    }
                }
            }catch (Exception e) { MessagePrintUtil.printException(e, "collectEventModelData"); }
    }

    public void matchEventModel(Traj traj, double avgSpeed, double queueLength) {
        AtomicDouble firstSpeed = new AtomicDouble(0.0);    // 事件轨迹记录的最早速度
        AtomicDouble lastSpeed = new AtomicDouble(0.0);     // 事件轨迹记录的最新速度
        AtomicDouble lastFrenetX = new AtomicDouble(0.0);   // 事件轨迹记录的最新桩号
        // 更新轨迹对应的速度记录事件(该事件不持久化到数据库中)
        CarEventModel eventModel = eventDataContext.getEventModelMap().get(TrajModelParamUtil.getEventMapKey(traj, EVENT_TYPE_NORMAL));
        if (eventModel == null) {
            eventDataContext.putIntoEventModelMap(traj, EVENT_TYPE_NORMAL);
        } else {
            List<Double> speedList = eventModel.getSpeedList();
            firstSpeed.set(speedList.get(0));
            lastSpeed.set(speedList.get(speedList.size() - 1));
            lastFrenetX.set(eventModel.getEndFrenetX());
            // 事件轨迹只记录最新的5个速度值,拼接最新的速度值
            int skipIndex = speedList.size() >= 5 ? (speedList.size() - 4) : 0;
            eventModel.setSpeedList(Stream.concat(speedList.stream().skip(skipIndex), List.of(traj.getSpeedX()).stream()).toList());
            eventModel.setEndTimestamp(traj.getTimestamp());
            eventModel.setEndFrenetX(traj.getFrenetX());
            eventModel.setQueueLength(queueLength); // 更新排队长度
            eventModel.setChanged(true);
        }
        try {
            // 逆行类型事件判断
            if (EventModelEstimateUtil.hasAgainstEvent(traj, lastFrenetX.get())) {
                updateEventModelMap(traj, EVENT_TYPE_AGAINST,queueLength);
            }
            // 违停类型事件判断
            if (EventModelEstimateUtil.hasParkingEvent(traj, firstSpeed.get(), lastSpeed.get(), avgSpeed)) {
                updateEventModelMap(traj, EVENT_TYPE_PARKING,queueLength);
            }
            // 慢速类型事件判断
            if (EventModelEstimateUtil.hasSlowEvent(traj, avgSpeed)) {
                updateEventModelMap(traj, EVENT_TYPE_SLOW,queueLength);
            }
            // 超速类型事件判断
            if (EventModelEstimateUtil.hasFastEvent(traj, lastSpeed.get())) {
                updateEventModelMap(traj, EVENT_TYPE_FAST,queueLength);
            }
//            // 占用应急车道类型事件判断
//            if (EventModelEstimateUtil.hasOccupyEvent(traj)) {
//                updateEventModelMap(traj, EVENT_TYPE_OCCUPY);
//            }
        } catch (Exception e) { MessagePrintUtil.printException(e, "matchEventModel"); }
    }

    public void updateEventModelMap(Traj traj, EventTypeModel eventTypeModel,  double queueLength) {
        CarEventModel eventModel = eventDataContext.getEventModelMap().get(TrajModelParamUtil.getEventMapKey(traj, eventTypeModel));
        if (eventModel == null) {
            eventDataContext.putIntoEventModelMap(traj, eventTypeModel);
        }
        // 记录最严重的事件
        else if (eventModel.getEventType()>=eventTypeModel.value) {
            eventModel.setQueueLength(queueLength); // 更新排队长度
            eventModel.setEventTime(eventModel.getEventTime() + 1);
            eventModel.setPicLicense(traj.getCarId());
            eventModel.setEndTimestamp(traj.getTimestamp());
            eventModel.setEndFrenetX(traj.getFrenetX());
            eventModel.setLane(traj.getLane());
            eventModel.setChanged(true);
        }
    }

    public void saveAndStoreEventModel(long timestamp) {
        List<CarEvent> readyToInsertList = new ArrayList<>();
        List<CarEvent> readyToUpdateList = new ArrayList<>();
        if (!CollectionEmptyUtil.forMap(eventDataContext.getEventModelMap())) {
            try {
                eventDataContext.getEventModelMap().entrySet().stream()
                // 筛选该时间戳下发生更新的事件,重置已更新的事件数据标志位
                .filter(i -> i.getValue().isChanged()).peek(i -> i.getValue().setChanged(false))
                // 筛选实际保存事件类型的事件数据
                .filter(i -> i.getValue().getEventType() != EVENT_TYPE_NORMAL.value)
                .forEach(entry -> {
                    CarEventModel carEventModel = entry.getValue();
                    // 如果事件已经存入数据库中,由于本时间戳下发生了状态变化,加入更新列表
                    if (carEventModel.isStored()) {
                        readyToUpdateList.add(DbModelTransformUtil.eventModelToEvent(entry.getValue()));
                        // 如果事件未存入数据库中,当其检测次数超过了事件阈值时,加入插入列表
                    } else if (carEventModel.getEventTime() >= ModelTransformUtil.getEventTypeInstance(carEventModel.getEventType()).time) {
                        carEventModel.setStored(true);
                        readyToInsertList.add(DbModelTransformUtil.eventModelToEvent(carEventModel));
                    }
                });
            } catch (Exception e) { MessagePrintUtil.printException(e, "saveAndStoreEventModel"); }
        }
        // 存储待新增事件/待更新事件数据,按时间段批量更新
        redisBatchCacheService.storeEventCache(readyToInsertList, readyToUpdateList, timestamp);
    }
    private double calculateQueueLength(List<Traj> trajListCurr,double maxQueueLength) {
        double lowSpeed = 15; // 低速阈值，单位千米每时
        double minVehDistance = 15; // 车辆最小间距，单位米
        // 按时间戳分组
        Map<Long, List<Traj>> trajMap = trajListCurr.stream().collect(Collectors.groupingBy(Traj::getTimestamp));
        double totalQueueLength = 0;
        for (Map.Entry<Long, List<Traj>> entry : trajMap.entrySet()) {
            List<Traj> frameTrajList = entry.getValue();
            // 按车道分组
            Map<Integer, List<Traj>> laneMap = frameTrajList.stream()
                    .collect(Collectors.groupingBy(Traj::getLane));
            double[] queueLengths = new double[3]; // 假设最多3条车道
            int numLanesWithQueue = 0;
            for (Map.Entry<Integer, List<Traj>> laneEntry : laneMap.entrySet()) {
                int lane = laneEntry.getKey();
                List<Traj> laneTraj = laneEntry.getValue();
                if (laneTraj.isEmpty()) {
                    continue;
                }
                // 按FrenetX排序
                laneTraj.sort(Comparator.comparingDouble(Traj::getFrenetX));
                // 计算连续低速车辆
                List<Double> speeds = laneTraj.stream().map(Traj::getSpeedX).collect(Collectors.toList());
                List<Boolean> lowSpeedMask = speeds.stream().map(speed -> speed < lowSpeed).collect(Collectors.toList());
                if (Collections.frequency(lowSpeedMask, true) <= 1) {
                    continue;
                }
                // 获取低速车辆的位置
                List<Double> lowSpeedPositions = new ArrayList<>();
                for (int idx = 0; idx < laneTraj.size(); idx++) {
                    if (lowSpeedMask.get(idx)) {
                        lowSpeedPositions.add(laneTraj.get(idx).getFrenetX());
                    }
                }
                // 判断连续性：计算位置差值
                List<Double> positionDiff = new ArrayList<>();
                for (int idx = 0; idx < lowSpeedPositions.size() - 1; idx++) {
                    positionDiff.add(lowSpeedPositions.get(idx + 1) - lowSpeedPositions.get(idx));
                }
                List<Boolean> continuousMask = positionDiff.stream().map(diff -> diff < minVehDistance).collect(Collectors.toList());
                // 找到连续队列
                int queueStartIdx = -1;
                int queueEndIdx = -1;
                for (int idx = 0; idx < continuousMask.size(); idx++) {
                    if (continuousMask.get(idx)) {
                        if (queueStartIdx == -1) {
                            queueStartIdx = idx;
                        }
                        queueEndIdx = idx;
                    }
                }
                if (queueStartIdx != -1 && queueEndIdx != -1) {
                    double queueStartPos = lowSpeedPositions.get(queueStartIdx);
                    double queueEndPos = lowSpeedPositions.get(queueEndIdx + 1);
                    double queueLength = queueEndPos - queueStartPos;
                    queueLengths[lane - 1] = queueLength;
                    // 动态确定车道索引，避免数组越界
                    if (lane <= queueLengths.length) {
                        queueLengths[lane - 1] = queueLength;
                    } else {
                        // 如果车道编号超出数组范围，可以扩展数组或记录错误
                        System.err.println("Lane number exceeds the assumed maximum. Lane: " + lane);
                    }
                }
            }

            // 计算三条车道排队长度的均值作为当前时间帧的排队长度
            for (int k = 0; k < 3; k++) {
                if (queueLengths[k] > 0) {
                    numLanesWithQueue++;
                    maxQueueLength = Math.max(maxQueueLength, queueLengths[k]);
                }
            }
            if (numLanesWithQueue > 0) {
                double avgQueueLengthTemp = Arrays.stream(queueLengths)
                        .filter(len -> len > 0)
                        .average()
                        .orElse(0.0);
                totalQueueLength += avgQueueLengthTemp;
            }
        }
        return totalQueueLength ;
    }


}
