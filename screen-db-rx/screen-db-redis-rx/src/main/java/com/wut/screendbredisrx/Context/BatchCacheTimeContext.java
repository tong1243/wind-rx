package com.wut.screendbredisrx.Context;

import com.wut.screencommonrx.Util.MessagePrintUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

import static com.wut.screencommonrx.Static.FusionModuleStatic.BATCH_RECORD_TIME_COND;
import static com.wut.screencommonrx.Static.FusionModuleStatic.EVENT_BATCH_RECORD_TIME_COND;

@Component
public class BatchCacheTimeContext {
    private static Long TRAJ_BATCH_TIME = 0L;
    private static Long EVENT_BATCH_TIME = 0L;
    private static Boolean TRAJ_BATCH_INIT_FLAG = false;
    private static Boolean EVENT_BATCH_INIT_FLAG = false;
    private static final ReentrantLock TRAJ_BATCH_LOCK = new ReentrantLock(true);
    private static final ReentrantLock EVENT_BATCH_LOCK = new ReentrantLock(true);

    @PostConstruct
    public void initBatchCacheTime() {
        TRAJ_BATCH_TIME = 0L;
        EVENT_BATCH_TIME = 0L;
        TRAJ_BATCH_INIT_FLAG = false;
        EVENT_BATCH_INIT_FLAG = false;
    }

    public boolean recordTrajCacheTimestamp(long timestamp) {
        if (TRAJ_BATCH_INIT_FLAG) {
            return updateTrajCacheTimestamp(timestamp);
        }
        try {
            TRAJ_BATCH_LOCK.lock();
            if (TRAJ_BATCH_INIT_FLAG) {
                return updateTrajCacheTimestamp(timestamp);
            }
            TRAJ_BATCH_TIME = timestamp;
            TRAJ_BATCH_INIT_FLAG = true;
            return false;
        } catch (Exception e) { MessagePrintUtil.printException(e, "recordTrajCacheTimestamp"); }
        finally { TRAJ_BATCH_LOCK.unlock(); }
        return false;
    }

    public boolean recordEventCacheTimestamp(long timestamp) {
        if (EVENT_BATCH_INIT_FLAG) {
            return updateEventCacheTimestamp(timestamp);
        }
        try {
            EVENT_BATCH_LOCK.lock();
            if (EVENT_BATCH_INIT_FLAG) {
                return updateEventCacheTimestamp(timestamp);
            }
            EVENT_BATCH_TIME = timestamp;
            EVENT_BATCH_INIT_FLAG = true;
            return false;
        } catch (Exception e) { MessagePrintUtil.printException(e, "recordEventCacheTimestamp"); }
        finally { EVENT_BATCH_LOCK.unlock(); }
        return false;
    }

    public boolean updateTrajCacheTimestamp(long timestamp) {
        if (timestamp - TRAJ_BATCH_TIME == BATCH_RECORD_TIME_COND) {
            try {
                TRAJ_BATCH_LOCK.lock();
                if (timestamp - TRAJ_BATCH_TIME == BATCH_RECORD_TIME_COND) {
                    TRAJ_BATCH_TIME = timestamp;
                    return true;
                }
                return false;
            } catch (Exception e) { MessagePrintUtil.printException(e, "updateTrajCacheTimestamp"); }
            finally { TRAJ_BATCH_LOCK.unlock(); }
        }
        return false;
    }

    public boolean updateEventCacheTimestamp(long timestamp) {
        if (timestamp - EVENT_BATCH_TIME == EVENT_BATCH_RECORD_TIME_COND) {
            try {
                EVENT_BATCH_LOCK.lock();
                if (timestamp - EVENT_BATCH_TIME == EVENT_BATCH_RECORD_TIME_COND) {
                    EVENT_BATCH_TIME = timestamp;
                    return true;
                }
            } catch (Exception e) { MessagePrintUtil.printException(e, "updateEventCacheTimestamp"); }
            finally { EVENT_BATCH_LOCK.unlock(); }
        }
        return false;
    }

}
