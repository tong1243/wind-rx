package com.wut.screenfusionrx.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wut.screencommonrx.Model.TrajModel;
import com.wut.screencommonrx.Model.TrajModelLine;
import com.wut.screencommonrx.Util.DataParamParseUtil;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbmysqlrx.Model.License;
import com.wut.screendbmysqlrx.Model.Traj;
import com.wut.screendbmysqlrx.Service.LicenseService;
import com.wut.screenfusionrx.Model.TrajCarCountModel;
import com.wut.screenfusionrx.Model.TrajMatchMarkModel;
import com.wut.screenfusionrx.Util.TrajModelParamUtil;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;

@Component
public class TrajDataContext {
    @Qualifier("fusionTaskTrajFusionAsyncPool")
    private final Executor fusionTaskTrajFusionAsyncPool;
    private final LicenseService licenseService;
    // 轨迹时间戳偏移量达标标志位
    private static Boolean TIME_FLAG = false;
    // 轨迹起始记录时间戳(用于计算轨迹时间戳偏移量)
    private static Long TIME_RECORD = 0L;
    // 轨迹起始记录时间戳初始化并发锁
    private static final ReentrantLock TRAJ_TIME_LOCK = new ReentrantLock(true);
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 鄂州至武汉方向总计车辆数
    @Getter
    private final AtomicInteger carNumCountToWH = new AtomicInteger(0);
    // 武汉至鄂州方向总计车辆数
    @Getter
    private final AtomicInteger carNumCountToEZ = new AtomicInteger(0);

    // 轨迹队列(初始容量128,避免频繁扩容)
    @Getter
    private final List<TrajModelLine> trajModelLineList = new ArrayList<>(128);
    // 轨迹队列生成轨迹号自增位(每次创建新轨迹队列时自增1)
    @Getter
    private final AtomicInteger trajNextId = new AtomicInteger(0);

    @Getter
    private final  AtomicInteger fiberNextId = new AtomicInteger(10000);
    @Getter
    private final  AtomicInteger laserNextId = new AtomicInteger(10000);
    @Getter
    private final  AtomicInteger waveNextId = new AtomicInteger(10000);
    // 时间戳与两方向当前车辆数记录(相同时间戳数据存取操作之间的偏移量为轨迹帧队列容量对应的600ms)
    @Getter
    private final Map<Long, TrajCarCountModel> carNumRecordMap = new java.util.concurrent.ConcurrentHashMap<>(128);
    // 多设备去重阶段光纤数据黑名单(屏蔽从激光雷达匝道处出发的光纤数据)
    @Getter
    private final Map<Integer, Boolean> fiberModelMarkMap = new HashMap<>(128);
    // 多设备去重阶段激光雷达数据白名单(激光雷达匝道处出发的激光雷达数据,直至行驶到主干道后始终不会和光纤数据匹配)
    @Getter
    private final Map<Integer, Boolean> laserModelMarkMap = new HashMap<>(128);
    // 多设备去重阶段光纤和雷达ID匹配映射表(提供status字段,允许单帧丢失,丢失后删除对应的键值对)
    @Getter
    private final Map<Integer, TrajMatchMarkModel> trajDeDuplicateMarkMap = new HashMap<>(128);

    // 车辆牌照绑定设备静态表(门架号查找设备相关参数)
    @Getter
    private final Map<String, License> trajCarPlateLicenseMap = new HashMap<>();
    // 车辆牌照绑定历史记录表(避免相同牌照绑定了不同车辆)
    @Getter
    private final Map<String, Boolean> trajCarPlateRecordHistoryMap = new HashMap<>(128);
    // 雷达ID绑定牌照记录表
    @Getter
    private final Map<Integer, String> trajLicenseRecordMap = new HashMap<>(128);
    // 雷达ID绑定轨迹队列记录表(防止激光雷达中途有0~2s的断帧现象)
    @Getter
    private final Map<Integer, Long> trajRawIdRecordMap = new HashMap<>(128);

    @Autowired
    public TrajDataContext(Executor fusionTaskTrajFusionAsyncPool, LicenseService licenseService) {
        this.fusionTaskTrajFusionAsyncPool = fusionTaskTrajFusionAsyncPool;
        this.licenseService = licenseService;
    }

    @PostConstruct
    public void initTrajDataContext() {
        TIME_FLAG = false;
        TIME_RECORD = 0L;
        trajNextId.set(0);
        carNumCountToWH.set(0);
        carNumCountToEZ.set(0);
        carNumRecordMap.clear();
        trajModelLineList.clear();
        fiberModelMarkMap.clear();
        laserModelMarkMap.clear();
        trajDeDuplicateMarkMap.clear();
        trajLicenseRecordMap.clear();
        trajCarPlateRecordHistoryMap.clear();
        trajRawIdRecordMap.clear();
        initTrajCarPlateLicenseMap().thenRunAsync(() -> {});
    }

    // 初始化填充车辆牌照绑定设备静态表
    public CompletableFuture<Void> initTrajCarPlateLicenseMap() {
        return CompletableFuture.runAsync(() -> {
            trajCarPlateLicenseMap.clear();
            trajCarPlateLicenseMap.putAll(licenseService.getAllLicense().stream().collect(Collectors.toMap(License::getDeviceCode, license -> license)));
        }, fusionTaskTrajFusionAsyncPool);
    }

    // 添加新的车辆数量统计数据
    public void pushNewRecordCarNum(long timestamp) {
        carNumRecordMap.put(timestamp, new TrajCarCountModel(carNumCountToWH.get(), carNumCountToEZ.get()));
    }

    // 添加新的轨迹队列
    public void pushNewTrajModelLine(TrajModel trajModel) {
        long trajId = trajNextId.incrementAndGet() % 1000;
        if (Objects.equals(trajModel.getCarId(), DEFAULT_CAR_ID)) {
            // 创建新的轨迹队列时,判断该轨迹数据的ID是否已有记录的车牌号(解决轨迹因丢失帧导致牌照丢失问题)
            // 如果有记录的车牌号,则直接赋给新的轨迹队列(后续加入队列的轨迹帧也可以沿用该牌照)
            if (trajLicenseRecordMap.containsKey(trajModel.getRawId())) {
                trajModel.setCarId(trajLicenseRecordMap.get(trajModel.getRawId()));
            } else {
                String defaultPicLicense = TrajModelParamUtil.getTrajIdToPicLicense(trajId);
                trajModel.setCarId(defaultPicLicense);
                trajLicenseRecordMap.put(trajModel.getRawId(), defaultPicLicense);
            }
            trajModel.setLicenseColor(9);
        }
        trajModel.setTrajId((trajModel.getTimestamp() / 1000) * 1000 + trajId);
        trajModelLineList.add(DataParamParseUtil.getTrajModelLineInstance(trajModel));
        // 记录当前雷达ID/光纤ID绑定的轨迹号
        trajRawIdRecordMap.put(trajModel.getRawId(), trajModel.getTrajId());
    }


    public void pushNewTrajId(Traj traj) {
        // 生成新的轨迹ID
        long trajId = trajNextId.incrementAndGet() % 1000;
        long trajIdWithTimestamp = (traj.getTimestamp() / 1000) * 1000 + trajId;
        traj.setTrajId(trajIdWithTimestamp);
        // 记录融合数据ID相匹配的轨迹ID
        trajRawIdRecordMap.put(traj.getRawId(), trajIdWithTimestamp);
    }

    // 获取模拟车牌号
    public int getPicLicense() {
        return trajNextId.incrementAndGet() % 100000;
    }

    // 已有记录的历史轨迹号时,重新添加轨迹队列
    public void pushNewTrajModelLineWithExistTrajId(TrajModel trajModel) {
        // 此时必定有已记录的车牌号(无论是默认车牌号还是真实的车牌号)
        trajModel.setCarId(trajLicenseRecordMap.get(trajModel.getRawId()));
        trajModel.setLicenseColor(9);
        trajModelLineList.add(DataParamParseUtil.getTrajModelLineInstance(trajModel));
    }

    // 重置当前时间戳的所有轨迹队列状态(准备下个时间戳接收数据)
    public void resetTrajModelLineStatus() {
        trajModelLineList.forEach(trajModelLine -> trajModelLine.setState(TRAJ_MODEL_LINE_STATUS_EMPTY));
    }

    // (保留)日志打印测试方法
    public void printTrajModelLineListToLogger(long timestamp) {
        try { MessagePrintUtil.printTrajModelList(timestamp, objectMapper.writeValueAsString(trajModelLineList)); }
        catch (Exception e) { MessagePrintUtil.printException(e, "printTrajModelLineListToLogger"); }
    }

    public boolean recordTrajOffsetTimestamp(long timestamp) {
        if (TIME_FLAG) { return true; }
        try {
            TRAJ_TIME_LOCK.lock();
            if (TIME_FLAG) { return true; }
            if (TIME_RECORD == 0L) {
                TIME_RECORD = timestamp;
                return false;
            }
            if (timestamp - TIME_RECORD >= TRAJ_TIMESTAMP_OFFSET) {
                TIME_FLAG = true;
                return true;
            }
            return false;
        } catch (Exception e) { MessagePrintUtil.printException(e, "recordTrajOffsetTimestamp"); }
        finally { TRAJ_TIME_LOCK.unlock(); }
        return false;
    }

}
