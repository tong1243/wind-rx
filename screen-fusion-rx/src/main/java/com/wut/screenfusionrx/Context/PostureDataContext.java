package com.wut.screenfusionrx.Context;

import com.wut.screencommonrx.Util.MessagePrintUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

import static com.wut.screencommonrx.Static.FusionModuleStatic.POSTURE_RECORD_TIME_COND;

@Component
public class PostureDataContext { // 全局实时任务记录时间戳
    private static Long POSTURE_TIME_RECORD = 0L;
    // 全局实时任务时间戳初始化标记
    private static Boolean POSTURE_TIME_INIT_FLAG = false;
    // 全局实时任务并发锁
    private static final ReentrantLock POSTURE_TIME_LOCK = new ReentrantLock(true);
    // 全局实时任务记录分钟数
    public static int POSTURE_RECORD_MINUTE_COND = 0;

    @PostConstruct
    public void initPostureDataContext() {
        POSTURE_TIME_RECORD = 0L;
        POSTURE_TIME_INIT_FLAG = false;
    }

    public boolean recordPostureTimestamp(long timestamp) {
        if (POSTURE_TIME_INIT_FLAG) {
            return updatePostureTimestamp(timestamp);
        }
        try {
            POSTURE_TIME_LOCK.lock();
            if (POSTURE_TIME_INIT_FLAG) {
                return updatePostureTimestamp(timestamp);
            }
            POSTURE_TIME_RECORD = timestamp;
            POSTURE_TIME_INIT_FLAG = true;
            return false;
        } catch (Exception e) { MessagePrintUtil.printException(e, "recordPostureTimestamp"); }
        finally { POSTURE_TIME_LOCK.unlock(); }
        return false;
    }

    public boolean updatePostureTimestamp(long timestamp) {
        if (timestamp - POSTURE_TIME_RECORD == POSTURE_RECORD_TIME_COND) {
            try {
                POSTURE_TIME_LOCK.lock();
                if (timestamp - POSTURE_TIME_RECORD == POSTURE_RECORD_TIME_COND) {
                    POSTURE_TIME_RECORD = timestamp;
                    POSTURE_RECORD_MINUTE_COND++;
                    return true;
                }
                return false;
            } catch (Exception e) { MessagePrintUtil.printException(e, "updatePostureTimestamp"); }
            finally { POSTURE_TIME_LOCK.unlock(); }
        }
        return false;
    }

}
