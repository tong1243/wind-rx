package com.wut.screenfusionrx.Service;

import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.DateParamParseUtil;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbmysqlrx.Model.*;
import com.wut.screendbmysqlrx.Service.BottleneckAreaStateService;
import com.wut.screendbmysqlrx.Service.ParametersService;
import com.wut.screendbmysqlrx.Service.PostureService;
import com.wut.screendbmysqlrx.Service.SecInfoService;
import com.wut.screendbmysqlrx.Util.DbModelTransformUtil;
import com.wut.screendbredisrx.Service.RedisExpireDataService;
import com.wut.screendbredisrx.Service.RedisTrajFusionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;
import static com.wut.screenfusionrx.Context.PostureDataContext.POSTURE_RECORD_MINUTE_COND;

@Component
public class PostureDataService {
    private final PostureService postureService;
    private final RedisTrajFusionService redisTrajFusionService;
    private final RedisExpireDataService redisExpireDataService;
    private final BottleneckAreaStateService bottleneckAreaStateService;
    private final SecInfoService secInfoService;
    private final ParametersService parametersService;
    private static final double FREE_SPEED = 60 / 3.6;  // 自由流速度(m/s)
    private static final double DEFAULT_SPEED = 60;      // 默认限速(km/h)
    private static final double LOW_SPEED_THRESHOLD = 15; // 排队低速阈值(km/h)
    private static final double MIN_VEHICLE_DISTANCE = 15; // 排队车辆最小间距(m)
    private final int parallelismLevel = Runtime.getRuntime().availableProcessors(); // 获取CPU核心数作为并行度
    private final ExecutorService executorService = Executors.newFixedThreadPool(parallelismLevel);
    @Autowired
    public PostureDataService(PostureService postureService, RedisTrajFusionService redisTrajFusionService,
                              RedisExpireDataService redisExpireDataService, BottleneckAreaStateService bottleneckAreaStateService,
                              SecInfoService secInfoService, ParametersService parametersService) {
        this.postureService = postureService;
        this.redisTrajFusionService = redisTrajFusionService;
        this.redisExpireDataService = redisExpireDataService;
        this.bottleneckAreaStateService = bottleneckAreaStateService;
        this.secInfoService = secInfoService;
        this.parametersService = parametersService;
    }
    public void collectAndStorePostureData(long timestamp) {
        redisExpireDataService.startRemoveExpireData(timestamp);
        MessagePrintUtil.printTaskStart(timestamp);

        // 初始化模型实例
        BottleneckAreaState bottleneckAreaState = DbModelTransformUtil.getBottleneckAreaStateInstance();
        List<TunnelSecInfo> tunnelSecInfoList = secInfoService.getAllTunnelSecInfo();
        try {
            // 采集轨迹数据
            List<Traj> trajList = redisTrajFusionService.collectTrajData(
                    timestamp - POSTURE_RECORD_TIME_COND + 1, timestamp
            ).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);

            // 使用并行流处理每个区间
            tunnelSecInfoList.parallelStream().forEach(secInfo -> {
                int secId = secInfo.getSid();
                List<Traj> secTrajList = filterTrajBySection(trajList, secInfo);

                // 初始化指标
                double upSpeed = 0.0;
                double downSpeed = 0.0;
                int rampStream = 0;
                double upDensity = 0.0;
                double downDensity = 0.0;
                double avgQueueLength = 0.0;
                int carCount = 0;

                // 特殊区间（3/4/7）计算上下游指标
                if (secId == 3 || secId == 4 || secId == 7) {
                    upSpeed = calculateSectionSpeed(trajList, secInfo.getStart() - 50, secInfo.getStart());
                    downSpeed = calculateSectionSpeed(trajList, secInfo.getEnd(), secInfo.getEnd() + 50);
                    upDensity = calculateTrafficMetrics(filterTrajByPosition(trajList, secInfo.getStart() - 50, secInfo.getStart()), secInfo.getStart() - (secInfo.getStart() - 50)).density;
                    downDensity = calculateTrafficMetrics(filterTrajByPosition(trajList, secInfo.getEnd(), secInfo.getEnd() + 50), secInfo.getEnd() + 50 - secInfo.getEnd()).density;
                    rampStream = calculateRampFlow(secTrajList) * 60;
                    carCount = calculateMainCarCount(secTrajList);
                }

                // 计算主要交通参数
                TrafficMetrics metrics = calculateTrafficMetrics(secTrajList, secInfo.getEnd() - secInfo.getStart());

                // 计算旅行时间和延误
                double travelTime = (secInfo.getEnd() - secInfo.getStart()) / (metrics.speed / 3.6);
                double delayTime = travelTime - ((secInfo.getEnd() - secInfo.getStart()) / FREE_SPEED);

                // 确定交通状态
                int trafficStatus = determineTrafficStatus(metrics.speed);

                // 瓶颈区域特殊处理
                if (secId == 4) {
                    avgQueueLength = calculateQueueLength(secTrajList);
                    bottleneckAreaState.setSpeed(metrics.speed);
                    bottleneckAreaState.setState(trafficStatus);
                    bottleneckAreaState.setStream(metrics.flow);
                    bottleneckAreaState.setQueueLength(avgQueueLength);
                    bottleneckAreaState.setQueueDelayTime(delayTime);
                    bottleneckAreaStateService.storeBottleneckAreaStateData(
                            DateParamParseUtil.getDateTimeStr(timestamp - POSTURE_RECORD_TIME_COND),
                            bottleneckAreaState
                    );
                }

                // 设置参数对象并存储
                Parameters parameters = DbModelTransformUtil.getParametersInstance(timestamp);
                setParameters(parameters, secId, metrics, trafficStatus, travelTime, delayTime, upSpeed, downSpeed, upDensity, downDensity, rampStream, carCount);

                // 长周期数据处理（大于2分钟）
                if (POSTURE_RECORD_MINUTE_COND > 2 && secId != 4) {
                    // 获取历史数据并计算旅行状态
                    int travelState = calculateHistoricalTravelState(timestamp, trajList, tunnelSecInfoList, secInfo);
                    parameters.setState(travelState);
                }

                // 存储参数数据
                parametersService.storeParametersData(
                        DateParamParseUtil.getDateTimeStr(timestamp - POSTURE_RECORD_TIME_COND),
                        parameters
                );
            });

            // 关闭线程池
            executorService.shutdown();
            executorService.awaitTermination(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);

        } catch (Exception e) {
            MessagePrintUtil.printException(e, "collectAndStorePostureData");
        }
    }

    // ================ 核心计算方法 ================ //

    /**
     * 计算交通核心指标
     * @param trajList 轨迹列表
     * @param sectionLength 区间长度(m)
     * @return 交通指标对象
     */
    private TrafficMetrics calculateTrafficMetrics(List<Traj> trajList, double sectionLength) {
        DistanceTimeResult dtResult = calculateTotalDistanceAndTime(trajList);
        double sumDistance = dtResult.totalDistance;
        double sumTime = dtResult.totalTime;

        // 计算交通流参数
        double flow = (sumDistance / (sectionLength * 60)) * 3600;  // 流量(veh/h)
        double density = (sumTime / (sectionLength * 60)) * 1000;   // 密度(veh/km)
        double speed = (density > 0) ? flow / density : 0;         // 速度(km/h)

        return new TrafficMetrics(flow, density, speed);
    }

    /**
     * 计算指定路段的速度
     * @param trajList 轨迹列表
     * @param start 开始位置
     * @param end 结束位置
     * @return 平均速度(km/h)
     */
    private double calculateSectionSpeed(List<Traj> trajList, double start, double end) {
        List<Traj> filtered = filterTrajByPosition(trajList, start, end);
        DistanceTimeResult dtResult = calculateTotalDistanceAndTime(filtered);

        if (dtResult.totalTime <= 0) return 0.0;
        double flow = (dtResult.totalDistance / (end-start)*60) * 3600;
        double density = (dtResult.totalTime / (end-start)*60) * 1000;
        return flow / density;
    }

    /**
     * 计算匝道车辆数
     * @param trajList 轨迹列表
     * @return 匝道流量(veh/h)
     */
    private int calculateRampFlow(List<Traj> trajList) {
        long count = trajList.stream()
                .filter(traj -> isRampLane(traj.getLane()))
                .map(Traj::getTrajId) // 提取车辆ID
                .distinct() // 确保唯一
                .count();
        return (int) count;
    }
    /**
     * 计算主路路段车辆数
     * @param trajList 轨迹列表
     * @return 匝道流量(veh/h)
     */
    private int calculateMainCarCount(List<Traj> trajList) {
        long count = trajList.stream()
                .filter(traj -> !isRampLane(traj.getLane()))
                .map(Traj::getTrajId) // 提取车辆ID
                .distinct() // 确保唯一
                .count();
        return (int) count;
    }
    /**
     * 计算排队长度
     * @param trajList 轨迹列表
     * @return 平均排队长度(m)
     */
    private double calculateQueueLength(List<Traj> trajList) {
        // 按时间戳分组
        Map<Long, List<Traj>> frameMap = trajList.stream()
                .collect(Collectors.groupingBy(Traj::getTimestamp));

        double totalQueueLength = 0.0;
        int validFrames = 0;

        for (List<Traj> frameTraj : frameMap.values()) {
            // 按车道分组
            Map<Integer, List<Traj>> laneMap = frameTraj.stream()
                    .collect(Collectors.groupingBy(Traj::getLane));

            double frameQueueSum = 0.0;
            int lanesWithQueue = 0;

            for (List<Traj> laneTraj : laneMap.values()) {
                // 按位置排序
                laneTraj.sort(Comparator.comparingDouble(Traj::getFrenetX));

                // 检测连续低速车辆
                QueueSegment queueSeg = detectLowSpeedQueue(laneTraj, LOW_SPEED_THRESHOLD, MIN_VEHICLE_DISTANCE);

                if (queueSeg.isValid()) {
                    frameQueueSum += queueSeg.length;
                    lanesWithQueue++;
                }
            }

            if (lanesWithQueue > 0) {
                totalQueueLength += frameQueueSum / lanesWithQueue;
                validFrames++;
            }
        }

        return (validFrames > 0) ? totalQueueLength / validFrames : 0.0;
    }

    // ================ 辅助计算方法 ================ //

    /**
     * 检测低速排队
     * @param laneTraj 车道轨迹
     * @param speedThreshold 低速阈值(km/h)
     * @param minDistance 车辆最小间距(m)
     * @return 排队段信息
     */
    private QueueSegment detectLowSpeedQueue(List<Traj> laneTraj, double speedThreshold, double minDistance) {
        // 标记低速车辆
        List<Double> lowSpeedPositions = new ArrayList<>();
        for (Traj traj : laneTraj) {
            if (traj.getSpeedX() < speedThreshold) {
                lowSpeedPositions.add(traj.getFrenetX());
            }
        }

        // 检测连续低速段
        if (lowSpeedPositions.size() < 2) return QueueSegment.INVALID;

        double maxSegmentLength = 0.0;
        int segmentStart = 0;

        for (int i = 1; i < lowSpeedPositions.size(); i++) {
            double distance = lowSpeedPositions.get(i) - lowSpeedPositions.get(i-1);

            // 重置连续段
            if (distance > minDistance) {
                double segmentLength = lowSpeedPositions.get(i-1) - lowSpeedPositions.get(segmentStart);
                maxSegmentLength = Math.max(maxSegmentLength, segmentLength);
                segmentStart = i;
            }
        }

        // 检查最后一段
        double lastSegment = lowSpeedPositions.get(lowSpeedPositions.size()-1) - lowSpeedPositions.get(segmentStart);
        maxSegmentLength = Math.max(maxSegmentLength, lastSegment);

        return (maxSegmentLength > 0) ? new QueueSegment(maxSegmentLength) : QueueSegment.INVALID;
    }

    /**
     * 计算历史旅行状态
     * @param timestamp 当前时间戳
     * @param currentTrajList 当前轨迹
     * @param tunnelSecInfoList 隧道区间列表
     * @param currentSecInfo 当前区间
     * @return 旅行状态
     */
    private int calculateHistoricalTravelState(long timestamp, List<Traj> currentTrajList,
                                               List<TunnelSecInfo> tunnelSecInfoList, TunnelSecInfo currentSecInfo) {
        try {
            int currentIndex = tunnelSecInfoList.indexOf(currentSecInfo);
            if (currentIndex <= 0) return 1; // 需要前序区间

            // 获取历史轨迹数据
            List<Traj> lastTrajList = redisTrajFusionService.collectTrajData(
                    timestamp - 2 * POSTURE_RECORD_TIME_COND + 1,
                    timestamp - POSTURE_RECORD_TIME_COND
            ).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);

            List<Traj> lastLastTrajList = redisTrajFusionService.collectTrajData(
                    timestamp - 3 * POSTURE_RECORD_TIME_COND + 1,
                    timestamp - 2 * POSTURE_RECORD_TIME_COND
            ).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);

            // 获取相关区间轨迹
            TunnelSecInfo prevSecInfo = tunnelSecInfoList.get(currentIndex - 1);
            List<Traj> downTrajList = getEdgeTrajList(currentTrajList, currentSecInfo.getEnd() - 20, currentSecInfo.getEnd());
            List<Traj> upTrajList = getEdgeTrajList(currentTrajList, prevSecInfo.getEnd() - 20, prevSecInfo.getEnd());
            List<Traj> upLastTrajList = getEdgeTrajList(lastTrajList, prevSecInfo.getEnd() - 20, prevSecInfo.getEnd());
            List<Traj> upLastLastTrajList = getEdgeTrajList(lastLastTrajList, prevSecInfo.getEnd() - 20, prevSecInfo.getEnd());

            // 计算旅行状态
            return calculateTravelState(downTrajList, upTrajList, upLastTrajList, upLastLastTrajList);
        } catch (Exception e) {
            MessagePrintUtil.printException(e, "calculateHistoricalTravelState");
            return 1;
        }
    }

    // ================ 工具方法 ================ //

    /**
     * 计算总行驶距离和时间
     * @param trajList 轨迹列表
     * @return 距离和时间结果
     */
    private DistanceTimeResult calculateTotalDistanceAndTime(List<Traj> trajList) {
        if (CollectionEmptyUtil.forList(trajList)) {
            return new DistanceTimeResult(0.0, 0.0);
        }

        double totalDistance = 0.0;
        double totalTime = 0.0;

        // 按车辆分组
        Map<Long, List<Traj>> vehicleMap = trajList.stream()
                .collect(Collectors.groupingBy(Traj::getTrajId));

        for (List<Traj> vehicleTraj : vehicleMap.values()) {
            // 按时间排序
            vehicleTraj.sort(Comparator.comparingLong(Traj::getTimestamp));

            // 计算连续点间的距离和时间
            for (int i = 0; i < vehicleTraj.size() - 1; i++) {
                Traj current = vehicleTraj.get(i);
                Traj next = vehicleTraj.get(i+1);

                double distance = Math.abs(next.getFrenetX() - current.getFrenetX());
                double time = (next.getTimestamp() - current.getTimestamp()) / 1000.0;

                totalDistance += distance;
                totalTime += time;
            }
        }

        return new DistanceTimeResult(totalDistance, totalTime);
    }

    /**
     * 确定交通状态
     * @param speed 平均速度(km/h)
     * @return 状态代码 (1-自由流, 2-稳定流, 3-不稳定流, 4-拥堵)
     */
    private int determineTrafficStatus(double speed) {
        if (speed >= 40) return 1;
        if (speed >= 30) return 2;
        if (speed >= 20) return 3;
        return 4;
    }

    /**
     * 设置参数对象
     */
    private void setParameters(Parameters params, int secId, TrafficMetrics metrics,
                               int status, double travelTime, double delayTime,
                               double upSpeed, double downSpeed,double upDensity,double downDensity, double rampStream,int carCount) {
        params.setRoadId(secId);
        params.setTime(POSTURE_RECORD_MINUTE_COND);
        params.setSpeedLimit(DEFAULT_SPEED);
        params.setTravelTime(travelTime);
        params.setDelay(delayTime);
        params.setState(status);
        params.setSpeed(metrics.speed);
        params.setStream(metrics.flow);
        params.setDensity(metrics.density);
        params.setUpSpeed(upSpeed);
        params.setDownSpeed(downSpeed);
        params.setUpDensity(upDensity);
        params.setDownDensity(downDensity);
        params.setRampStream(rampStream);
        params.setCarCount(carCount);
    }

    /**
     * 筛选区间内的轨迹
     */
    private List<Traj> filterTrajBySection(List<Traj> trajList, TunnelSecInfo secInfo) {
        return filterTrajByPosition(trajList, secInfo.getStart(), secInfo.getEnd());
    }

    /**
     * 筛选位置范围内的轨迹
     */
    private List<Traj> filterTrajByPosition(List<Traj> trajList, double start, double end) {
        return trajList.stream()
                .filter(traj -> traj.getFrenetX() >= start && traj.getFrenetX() <= end)
                .collect(Collectors.toList());
    }

    /**
     * 获取路段边缘轨迹
     */
    private List<Traj> getEdgeTrajList(List<Traj> trajList, double start, double end) {
        return trajList.stream()
                .filter(traj -> traj.getFrenetX() >= start && traj.getFrenetX() <= end)
                .sorted(Comparator.comparingLong(Traj::getTimestamp).reversed())
                .filter(distinctByKey(Traj::getTrajId))
                .collect(Collectors.toList());
    }

    private boolean isRampLane(Integer lane) {
        return lane == 4;
    }

    // ================ 保留原有方法 ================ //

    private static int calculateTravelState(List<Traj> downTrajList, List<Traj> upTrajList,
                                            List<Traj> upLastTrajList, List<Traj> upLastLastTrajList) {
        // 合并历史数据
        List<Traj> allUpTrajList = Stream.of(upTrajList, upLastTrajList, upLastLastTrajList)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        Map<Long, Traj> upTrajMap = allUpTrajList.stream()
                .collect(Collectors.toMap(Traj::getTrajId, traj -> traj, (existing, replacement) -> existing));

        double totalSpeed = 0.0;
        int matchedCount = 0;
        int totalCount = downTrajList.size();

        for (Traj downTraj : downTrajList) {
            Traj matchingUpTraj = upTrajMap.get(downTraj.getTrajId());

            if (matchingUpTraj != null) {
                double speed = calculateSpeed(downTraj, matchingUpTraj);
                totalSpeed += speed;
                matchedCount++;
            } else {
                totalSpeed += 20.0; // 默认速度
            }
        }

        if (totalCount == 0) return 4;

        double meanSpeed = totalSpeed / totalCount;
        if (meanSpeed >= 40) return 1;
        if (meanSpeed >= 30) return 2;
        if (meanSpeed >= 20) return 3;
        return 4;
    }

    private static double calculateSpeed(Traj downTraj, Traj upTraj) {
        double distance = downTraj.getFrenetX() - upTraj.getFrenetX();
        double timeHours = (downTraj.getTimestamp() - upTraj.getTimestamp()) / 3600000.0;
        return (timeHours > 0) ? distance / 1000.0 / timeHours : 0.0;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    // ================ 内部数据类 ================ //

    /** 存储距离和时间计算结果 */
    private static class DistanceTimeResult {
        public final double totalDistance;
        public final double totalTime;

        public DistanceTimeResult(double distance, double time) {
            this.totalDistance = distance;
            this.totalTime = time;
        }
    }

    /**
     * 存储交通指标
     *
     * @param flow    流量(veh/h)
     * @param density 密度(veh/km)
     * @param speed   速度(km/h)
     */
        private record TrafficMetrics(double flow, double density, double speed) {
    }

    /**
     * 存储排队段信息
     */
        private record QueueSegment(double length) {
            public static final QueueSegment INVALID = new QueueSegment(0);

        public boolean isValid() {
                return length > 0;
            }
        }

}