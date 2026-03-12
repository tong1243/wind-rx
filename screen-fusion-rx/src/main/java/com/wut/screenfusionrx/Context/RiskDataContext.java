package com.wut.screenfusionrx.Context;

import com.google.common.collect.Range;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbmysqlrx.Model.TunnelRisk;
import com.wut.screendbmysqlrx.Model.SecInfo;
import com.wut.screendbmysqlrx.Model.TunnelSecInfo;
import com.wut.screendbmysqlrx.Service.SecInfoService;
import com.wut.screenfusionrx.Model.SectionIntervalModel;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.wut.screencommonrx.Static.FusionModuleStatic.RISK_RECORD_TIME_COND;

@Component
public class RiskDataContext {
    @Qualifier("fusionTaskRiskAsyncPool")
    private final Executor fusionTaskRiskAsyncPool;
    private final SecInfoService secInfoService;
    // 全局风险任务时间戳初始化标记
    private static Boolean RISK_TIME_INIT_FLAG = false;
    // 全局风险任务记录时间戳
    private static Long RISK_TIME_RECORD = 0L;
    // 全局风险任务并发锁
    private static final ReentrantLock RISK_TIME_LOCK = new ReentrantLock(true);
    // 每个路段的 iTSC
    private final Map<Integer, Double> sectionITSCMap = new LinkedHashMap<Integer, Double>();
    // 记录事件数
//    public static int RISK_COUNT = 0;
    private final Map<String, Double> sectionITSCToEZMap = new LinkedHashMap<String, Double>();
    private double iTSCwh = 0.0;
    private double iTSCez = 0.0;
    private final List<TunnelRisk> riskList = new ArrayList<>();
    private final SectionDataContext sectionDataContext;
    // 断面信息列表(旧,与数据库存储格式相同)
    @Getter
    private List<TunnelSecInfo> secInfoList = new ArrayList<>();


    @Autowired
    public RiskDataContext(Executor fusionTaskRiskAsyncPool, SecInfoService secInfoService, SectionDataContext sectionDataContext) {
        this.fusionTaskRiskAsyncPool = fusionTaskRiskAsyncPool;
        this.secInfoService = secInfoService;
        this.sectionDataContext = sectionDataContext;
    }

    @PostConstruct
    public void initRiskDataContext() {
        flushRiskInfoList().thenRunAsync(() -> {});
        RISK_TIME_INIT_FLAG = false;
        RISK_TIME_RECORD = 0L;
    }
    public CompletableFuture<Void> flushRiskInfoList() {
        return CompletableFuture.runAsync(() -> {
            secInfoList.addAll(secInfoService.getAllTunnelSecInfo());
            for (int i = 0; i < secInfoList.size(); i++) {
                sectionITSCMap.put(secInfoList.get(i).getSid(), 0.0);
            }
        },fusionTaskRiskAsyncPool);
    }


    public boolean recordRiskTimestamp(long timestamp) {
        if (RISK_TIME_INIT_FLAG) {
            return updateRiskTimestamp(timestamp);
        }
        try {
            RISK_TIME_LOCK.lock();
            if (RISK_TIME_INIT_FLAG) {
                return updateRiskTimestamp(timestamp);
            }
            RISK_TIME_RECORD = timestamp;
            RISK_TIME_INIT_FLAG = true;
            return false;
        } catch (Exception e) { MessagePrintUtil.printException(e, "recordRiskTimestamp"); }
        finally { RISK_TIME_LOCK.unlock(); }
        return false;
    }
    public boolean updateRiskTimestamp(long timestamp) {
        if (timestamp - RISK_TIME_RECORD == RISK_RECORD_TIME_COND) {
            try {
                RISK_TIME_LOCK.lock();
                if (timestamp - RISK_TIME_RECORD == RISK_RECORD_TIME_COND) {
                    RISK_TIME_RECORD = timestamp;
                    return true;
                }
                return false;
            } catch (Exception e) { MessagePrintUtil.printException(e, "updateRiskTimestamp"); }
            finally { RISK_TIME_LOCK.unlock(); }
        }
        return false;
    }


    public void addToSectionTSC(int sid, double TTCStar) {
        sectionITSCMap.put(sid, sectionITSCMap.getOrDefault(sid, 0.0) + TTCStar);
    }


    public Map<Integer, Double> getSectionTSCMap() {
        return sectionITSCMap;
    }
}
