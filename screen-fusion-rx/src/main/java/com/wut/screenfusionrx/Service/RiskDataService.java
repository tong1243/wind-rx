package com.wut.screenfusionrx.Service;

import com.google.common.collect.Range;
import com.wut.screencommonrx.Util.DataParamParseUtil;
import com.wut.screencommonrx.Util.DateParamParseUtil;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbmysqlrx.Model.RiskEvent;
import com.wut.screendbmysqlrx.Model.Traj;
import com.wut.screendbmysqlrx.Model.TunnelRisk;
import com.wut.screendbmysqlrx.Model.TunnelSecInfo;
import com.wut.screendbmysqlrx.Service.RiskEventService;
import com.wut.screendbmysqlrx.Service.SecInfoService;
import com.wut.screendbmysqlrx.Service.TunnelRiskService;
import com.wut.screendbredisrx.Service.RedisBatchCacheService;
import com.wut.screendbredisrx.Service.RedisTrajFusionService;
import com.wut.screenfusionrx.Context.RiskDataContext;
import com.wut.screenfusionrx.Context.SectionDataContext;
import com.wut.screenfusionrx.Model.SectionIntervalModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;
import static com.wut.screencommonrx.Util.DataParamParseUtil.getPositionStr;

@Component
public class RiskDataService {
    private final Executor fusionTaskRiskAsyncPool;
    private final RiskDataContext riskDataContext;
    private final SectionDataContext sectionDataContext;
    private final RedisTrajFusionService redisTrajFusionService;
    private final RedisBatchCacheService redisBatchCacheService;
    private final TunnelRiskService tunnelRiskService;
    private final SecInfoService secInfoService;
    private final RiskEventService riskEventService;

    // 使用 ConcurrentHashMap 替代静态 Map，以提高线程安全性
    private static final ConcurrentHashMap<Long, AtomicInteger> consecutiveCountMap = new ConcurrentHashMap<>();
    private static boolean isEvent = false;
    private static final List<RiskEvent> riskEventList = new CopyOnWriteArrayList<>(); // 线程安全的列表
    private static final ConcurrentHashMap<Integer, ConcurrentHashMap<Long, Double>> intervalMinTTCMap = new ConcurrentHashMap<>();

    // 使用 ConcurrentHashMap 存储每个区间的计数器
    private static final ConcurrentHashMap<Integer, AtomicInteger> sectionRiskCounts = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, AtomicInteger> sectionRiskEventCounts = new ConcurrentHashMap<>();
    // 在类中添加
    private static final ConcurrentHashMap<Integer, AtomicInteger> sectionRiskMaxLevel = new ConcurrentHashMap<>();
    private static int ttcCount=0;
    @Autowired
    public RiskDataService(Executor fusionTaskRiskAsyncPool, RiskDataContext riskDataContext, SectionDataContext sectionDataContext, RedisTrajFusionService redisTrajFusionService, RedisBatchCacheService redisBatchCacheService, TunnelRiskService tunnelRiskService, SecInfoService secInfoService, RiskEventService riskEventService) {
        this.fusionTaskRiskAsyncPool = fusionTaskRiskAsyncPool;
        this.riskDataContext = riskDataContext;
        this.sectionDataContext = sectionDataContext;
        this.redisTrajFusionService = redisTrajFusionService;
        this.redisBatchCacheService = redisBatchCacheService;
        this.tunnelRiskService = tunnelRiskService;
        this.secInfoService = secInfoService;
        this.riskEventService = riskEventService;
    }

    public void collectAndStoreRiskData(long timestamp) {
        try {
            // 初始化每个区间的计数器
            for (int section : riskDataContext.getSectionTSCMap().keySet()) {
                sectionRiskCounts.put(section, new AtomicInteger(0));
                sectionRiskEventCounts.put(section, new AtomicInteger(0));
            }
            List<Traj> trajList = redisTrajFusionService.collectTrajData(timestamp - RISK_RECORD_TIME_COND + 1, timestamp).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
            List<TunnelSecInfo> tunnelSecInfo = secInfoService.getAllTunnelSecInfo();
            for (TunnelSecInfo info : tunnelSecInfo) {
                ttcCount=0;
                if (isRiskArea(info.getSid())) {
                    List<Traj> sectionTrajList = trajList.stream()
                            .filter(traj -> traj.getFrenetX() >= info.getStart() && traj.getFrenetX() <= info.getEnd() && isMainLine(traj.getLane()))
                            .sorted(Comparator.comparing(Traj::getTimestamp))
                            .toList();
                    Map<Integer, List<Traj>> laneTrajMap = sectionTrajList.stream()
                            .collect(Collectors.groupingBy(Traj::getLane));
                    for (Map.Entry<Integer, List<Traj>> laneEntry : laneTrajMap.entrySet()) {
                        int lane = laneEntry.getKey();
                        List<Traj> laneTrajList = laneEntry.getValue();
                        Map<Long, List<Traj>> listMap = laneTrajList.stream()
                                .collect(Collectors.groupingBy(Traj::getTimestamp));
                        List<Long> sortedTimestamps = new ArrayList<>(listMap.keySet());
                        Collections.sort(sortedTimestamps);
                        for (Long sortedTimestamp : sortedTimestamps) {
                            List<Traj> vehicleTrajList = listMap.get(sortedTimestamp);
                            vehicleTrajList.sort(Comparator.comparingDouble(Traj::getFrenetX));
                            MessagePrintUtil.printTrajList(sortedTimestamp, vehicleTrajList.toString());
                            if (vehicleTrajList.size() > 1) {
                                Traj firstCar = vehicleTrajList.get(0);
                                setRiskTSC(firstCar, vehicleTrajList, info.getSid());
                            }
                        }
                    }
                }
                MessagePrintUtil.printRiskCount(ttcCount);

            }
        } catch (Exception e) {
            MessagePrintUtil.printException(e, "collectRiskData");
        } finally {
            List<TunnelRisk> riskList = getRiskList(timestamp);
            tunnelRiskService.storeRiskData(DateParamParseUtil.getDateTimeStr(timestamp - RISK_RECORD_TIME_COND), riskList);
            riskEventService.storeEventData(DateParamParseUtil.getDateTimeStr(timestamp - RISK_RECORD_TIME_COND), riskEventList);
            riskEventList.clear();
            sectionRiskCounts.forEach((section, count) -> count.set(0));
            sectionRiskEventCounts.forEach((section, count) -> count.set(0));
            consecutiveCountMap.forEach((trajId, count) -> count.set(0));
            isEvent = false;
        }
    }

    private List<TunnelRisk> getRiskList(long timestamp) {
        List<TunnelRisk> riskList = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : riskDataContext.getSectionTSCMap().entrySet()) {
            int section = entry.getKey();
            int riskCount = getSectionRiskCount(section);
            int sectionRiskEventCount = getSectionRiskEventCount(section);
            double TSC = riskCount * 2;
            int riskLevel = calculateRiskLevel(TSC, sectionRiskEventCount, section);
            int maxRiskLevel = getSectionMaxRiskLevel(section);
            if (isRiskArea(section)) {
                riskList.add(new TunnelRisk(section, riskLevel, maxRiskLevel, timestamp, 0.0, 0.0, 0.0, TSC,  sectionRiskEventCount));
            }
        }
        riskDataContext.getSectionTSCMap().forEach((key, value) -> riskDataContext.getSectionTSCMap().put(key, 0.0));
        return riskList;
    }

    private int getSectionRiskCount(int section) {
        return sectionRiskCounts.getOrDefault(section, new AtomicInteger(0)).get();
    }

    private int getSectionRiskEventCount(int section) {
        return sectionRiskEventCounts.getOrDefault(section, new AtomicInteger(0)).get();
    }

    private static void incrementSectionRiskCount(int section) {
        sectionRiskCounts.computeIfAbsent(section, k -> new AtomicInteger(0)).incrementAndGet();
    }
    private static void plusFiveSectionRiskCount(int section) {
        sectionRiskCounts.computeIfAbsent(section, k -> new AtomicInteger(0)).addAndGet(5);
    }
    private static void incrementSectionRiskEventCount(int section) {
        sectionRiskEventCounts.computeIfAbsent(section, k -> new AtomicInteger(0)).incrementAndGet();
    }
    private int getSectionMaxRiskLevel(int section) {
        // 如果没有记录过该区间的最大风险等级，则默认为0
        return sectionRiskMaxLevel.getOrDefault(section, new AtomicInteger(0)).get();
    }
    private int calculateRiskLevel(double ttcStar, int riskCount, int section) {
        MessagePrintUtil.printTSCandRiskCount(ttcStar, riskCount);
        int riskLevel; // 默认值
        if (ttcStar >= 0 && ttcStar <= 200 && riskCount >= 0 && riskCount <= 10) {
            riskLevel = 1;
        } else if (ttcStar >= 600 || riskCount > 30) {
            riskLevel = 3;
        } else {
            riskLevel = 2;
        }
        // 更新最大等级风险
        sectionRiskMaxLevel.computeIfAbsent(section, k -> new AtomicInteger(0))
                .updateAndGet(v -> Math.max(v, riskLevel));
        return riskLevel;
    }
    private int getRiskType(double TTCStar) {
        if (TTCStar < 1.5) return 1;
        else if (TTCStar < 3) return 2;
        else if (TTCStar < 5) return 3;
        else return 0;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private void setRiskTSC(Traj traj, List<Traj> allTrajs, int sid) {
        Traj currentCar = traj;
        Set<Long> visitedCars = ConcurrentHashMap.newKeySet();
        while (currentCar != null) {
            visitedCars.add(currentCar.getTrajId());
            Traj frontCar = getFrontCar(currentCar, allTrajs, visitedCars);
            if (frontCar == null) break;
            double TTCStar = getTTCStar(currentCar, frontCar, sid);
            if (isEvent && TTCStar < 5 && TTCStar != 0) {
                synchronized (RiskDataService.class) {
                    RiskEvent riskEvent = new RiskEvent(
                            currentCar.getTimestamp(),
                            currentCar.getCarId(),
                            getPositionStr(currentCar.getFrenetX()),
                            currentCar.getSpeedX(),
                            DataParamParseUtil.getRoundValue(frontCar.getSpeedX() - currentCar.getSpeedX()),
                            frontCar.getFrenetX() - currentCar.getFrenetX() - 3.5,
                            getRiskType(TTCStar)
                    );
                    riskEventList.add(riskEvent);
                    isEvent = false;
                }
            }
            currentCar = frontCar;
        }
    }

    private Traj getFrontCar(Traj currentCar, List<Traj> allTrajs, Set<Long> visitedCars) {
        Traj frontCar = null;
        double minDistance = Double.MAX_VALUE;
        for (Traj otherCar : allTrajs) {
            if (visitedCars.contains(otherCar.getTrajId())) continue;
            double distance = Math.abs(otherCar.getFrenetX() - currentCar.getFrenetX());
            if (distance < minDistance) {
                minDistance = distance;
                frontCar = otherCar;
            }
        }
        return frontCar;
    }

    private boolean isMainLine(Integer lane) {
        return lane >= 1 && lane <= 3;
    }

    private boolean isRiskArea(int sid) {
        return sid == 3 || sid == 4 || sid == 7;
    }

    private static double getTTCStar(Traj currentCar, Traj frontCar, int sid) {
        double v1 = currentCar.getSpeedX();
        double v2 = frontCar.getSpeedX();
        double s = Math.abs(frontCar.getFrenetX() - currentCar.getFrenetX() - 3.5);
        double TTC = 0;
        if (v1 != v2 && v1 > v2) {
            TTC = s / ((v1 - v2) / 3.6);
        }
        double TTCStar = TTC < 5 ? TTC : 0;
        MessagePrintUtil.printTTCStarandCar(currentCar.toString(), TTCStar);
        if (TTCStar < 5 && TTCStar != 0) {
            synchronized (RiskDataService.class) {
                ttcCount++;
            }
            consecutiveCountMap.computeIfAbsent(currentCar.getTrajId(), k -> new AtomicInteger(0)).incrementAndGet();
        } else {
            consecutiveCountMap.computeIfAbsent(currentCar.getTrajId(), k -> new AtomicInteger(0)).set(0);
        }
        if (consecutiveCountMap.get(currentCar.getTrajId()).get() == 5) {
            synchronized (RiskDataService.class) {
                plusFiveSectionRiskCount(sid);
                incrementSectionRiskEventCount(sid);
                isEvent = true;
            }
        }
        if (consecutiveCountMap.get(currentCar.getTrajId()).get() > 5) {
            synchronized (RiskDataService.class) {
                incrementSectionRiskCount(sid);
            }
        }
        return TTCStar;
    }
}