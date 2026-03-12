package com.wut.screenfusionrx.Context;

import com.google.common.collect.Range;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbmysqlrx.Model.SecInfo;
import com.wut.screendbmysqlrx.Service.SecInfoService;
import com.wut.screenfusionrx.Model.SectionIntervalModel;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

import static com.wut.screencommonrx.Static.FusionModuleStatic.SECTION_RECORD_TIME_COND;

@Component
public class SectionDataContext {
    @Qualifier("fusionTaskSectionAsyncPool")
    private final Executor fusionTaskSectionAsyncPool;
    private final SecInfoService secInfoService;

    // 全局断面任务时间戳初始化标记
    private static Boolean SECTION_TIME_INIT_FLAG = false;
    // 全局断面任务记录时间戳
    private static Long SECTION_TIME_RECORD = 0L;
    // 全局断面任务并发锁
    private static final ReentrantLock SECTION_TIME_LOCK = new ReentrantLock(true);

    // 断面信息列表(旧,与数据库存储格式相同)
    @Getter
    private final List<SecInfo> secInfoList = new ArrayList<>();

    // 断面信息列表(新,与路段格式相同,保存路段区间)
    @Getter
    private final List<SectionIntervalModel> secIntervalList = new ArrayList<>();
    @Getter
    private final Map<Integer, SectionIntervalModel> secIntervalMap = new HashMap<>();

    // 断面位置信息列表(用于生成路段区间)
    @Getter
    private final List<Double> secInfoValueList = new ArrayList<>();

    @Autowired
    public SectionDataContext(SecInfoService secInfoService, Executor fusionTaskSectionAsyncPool) {
        this.secInfoService = secInfoService;
        this.fusionTaskSectionAsyncPool = fusionTaskSectionAsyncPool;
    }

    @PostConstruct
    public void initSectionDataContext() {
        flushSecInfoList().thenRunAsync(() -> {});
        SECTION_TIME_INIT_FLAG = false;
        SECTION_TIME_RECORD = 0L;
    }

    public CompletableFuture<Void> flushSecInfoList() {
        return CompletableFuture.runAsync(() -> {
            secInfoList.clear();
            secIntervalList.clear();
            secIntervalMap.clear();
            secInfoList.addAll(secInfoService.getAllSecInfo());
            for (int i = 1; i < secInfoList.size(); i++) {
                SecInfo prev = secInfoList.get(i - 1);
                SecInfo curr = secInfoList.get(i);
                // 路段区间取桩号更大的作为基准,拼接首尾两个断面的桩号名,建立两个桩号之间的闭区间
                SectionIntervalModel model = new SectionIntervalModel(
                        curr.getSid(),
                        prev.getXsecName()+"-"+curr.getXsecName(),
                        curr.getXsecValue(),
                        Range.closed(prev.getXsecValue(), curr.getXsecValue())
                );
                secIntervalList.add(model);
                secIntervalMap.put(curr.getSid(), model);
            }
        }, fusionTaskSectionAsyncPool);
    }

    public boolean recordSectionTimestamp(long timestamp) {
        if (SECTION_TIME_INIT_FLAG) {
            return updateSectionTimestamp(timestamp);
        }
        try {
            SECTION_TIME_LOCK.lock();
            if (SECTION_TIME_INIT_FLAG) {
                return updateSectionTimestamp(timestamp);
            }
            SECTION_TIME_RECORD = timestamp;
            SECTION_TIME_INIT_FLAG = true;
            return false;
        } catch (Exception e) { MessagePrintUtil.printException(e, "recordSectionTimestamp"); }
        finally { SECTION_TIME_LOCK.unlock(); }
        return false;
    }

    public boolean updateSectionTimestamp(long timestamp) {
        if (timestamp - SECTION_TIME_RECORD == SECTION_RECORD_TIME_COND) {
            try {
                SECTION_TIME_LOCK.lock();
                if (timestamp - SECTION_TIME_RECORD == SECTION_RECORD_TIME_COND) {
                    SECTION_TIME_RECORD = timestamp;
                    return true;
                }
                return false;
            } catch (Exception e) { MessagePrintUtil.printException(e, "updateSectionTimestamp"); }
            finally { SECTION_TIME_LOCK.unlock(); }
        }
        return false;
    }

}
