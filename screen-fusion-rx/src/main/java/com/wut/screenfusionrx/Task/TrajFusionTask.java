package com.wut.screenfusionrx.Task;

import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbmysqlrx.Model.Traj;
import com.wut.screenfusionrx.Context.TrajDataContext;
import com.wut.screenfusionrx.Service.TrajFusionService;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import static com.wut.screencommonrx.Static.FusionModuleStatic.FUSION_TIME_INTER;
import static com.wut.screencommonrx.Static.FusionModuleStatic.TRAJ_TIMESTAMP_OFFSET;
import static com.wut.screencommonrx.Static.MsgModuleStatic.TRAJ_FUSION_WAIT_TIME;

@Component
public class TrajFusionTask {
    @Qualifier("trajFusionTaskScheduler")
    private final ThreadPoolTaskScheduler trajFusionTaskScheduler;
    @Qualifier("fusionTaskTrajFusionTimerAsyncPool")
    private final Executor fusionTaskTrajFusionTimerAsyncPool;
    private final TrajFusionService trajFusionService;
    private final TrajDataContext trajDataContext;
    // 可进行轨迹融合任务的最新时间戳
    private static final AtomicLong TRAJ_LATEST_TIME = new AtomicLong(0L);
    // 轨迹融合任务时间戳初始化标志位
    private static Boolean TRAJ_FUSION_TASK_FLAG = false;
    // 已处理完轨迹融合任务的最新时间戳
    private static Long TRAJ_FUSION_TIME = 0L;
    // 轨迹融合任务并发锁
    private static final ReentrantLock TRAJ_FUSION_LOCK = new ReentrantLock(true);

    @Autowired
    public TrajFusionTask(ThreadPoolTaskScheduler trajFusionTaskScheduler, Executor fusionTaskTrajFusionTimerAsyncPool, TrajFusionService trajFusionService, TrajDataContext trajDataContext) {
        this.trajFusionTaskScheduler = trajFusionTaskScheduler;
        this.fusionTaskTrajFusionTimerAsyncPool = fusionTaskTrajFusionTimerAsyncPool;
        this.trajFusionService = trajFusionService;
        this.trajDataContext = trajDataContext;
    }

    @PostConstruct
    public void initTrajFusionParam() {
        TRAJ_FUSION_TASK_FLAG = false;
        TRAJ_LATEST_TIME.set(0L);
        TRAJ_FUSION_TIME = 0L;
    }

    @RabbitListener(queues = "fusion")
    public void trajFusionListener(String timestampStr) {
        storeTrajTimestamp(Long.parseLong(timestampStr)).thenRunAsync(() -> {});
    }

    public CompletableFuture<Void> storeTrajTimestamp(long timestamp) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 首个时间戳(必定是最小时间戳)到达时,初始化轨迹融合时间戳并启动定时任务
                // 其余时间戳到达时,CAS更改当前可进行轨迹融合任务的最新时间戳
                if (!TRAJ_FUSION_TASK_FLAG) {
                    TRAJ_FUSION_TASK_FLAG = true;
                    while (!TRAJ_LATEST_TIME.compareAndSet(0L, timestamp)) {}
                    TRAJ_FUSION_TIME = timestamp - FUSION_TIME_INTER;
                    Thread.sleep(TRAJ_FUSION_WAIT_TIME);
                    trajFusionTaskScheduler.scheduleAtFixedRate(this::startTrajFusion, Duration.ofMillis(FUSION_TIME_INTER));
                }
                while (!TRAJ_LATEST_TIME.compareAndSet(timestamp - FUSION_TIME_INTER, timestamp)) {}
            } catch (Exception e) { MessagePrintUtil.printException(e, "storeTrajTimestamp"); }
        }, fusionTaskTrajFusionTimerAsyncPool);
    }

    // 轨迹融合主任务
    // 将清洗后的原始数据转换为连续的轨迹数据
    public void startTrajFusion() {
        // 轨迹融合定时任务最快每200ms执行一次
        // 只要当前轨迹融合的时间戳小于可操作的最新时间戳(该时间戳由数据清洗模块更新),就进入执行轨迹融合,否则等待下一轮
        if (TRAJ_FUSION_TIME < TRAJ_LATEST_TIME.get()) {
            TRAJ_FUSION_TIME += FUSION_TIME_INTER;
            try {
                TRAJ_FUSION_LOCK.lock();
                // 获取本轮轨迹融合任务弹出的轨迹帧数据
                // 当前轨迹队列容量为4,前600ms的任务均不会弹出时间戳,600ms偏移量之后才会进入发送逻辑
                List<Traj> trajList = trajFusionService.collectFusionTraj(TRAJ_FUSION_TIME);
                // 测试日志输出,非必要情况下应当注释
                // trajDataContext.printTrajModelLineListToLogger(TRAJ_FUSION_TIME);
                if (trajDataContext.recordTrajOffsetTimestamp(TRAJ_FUSION_TIME)) {
                    // 向内存数据库存储后处理用轨迹数据和批量插入用轨迹数据
                    // 向服务端发送当前时间戳的轨迹数据和统计车辆数量
                    trajFusionService.storeAndSendTrajListData(trajList, TRAJ_FUSION_TIME - TRAJ_TIMESTAMP_OFFSET);
                }
                MessagePrintUtil.printTrajFusionMain(TRAJ_FUSION_TIME);
            } catch (Exception e) { MessagePrintUtil.printException(e, "startTrajFusion"); }
            finally { TRAJ_FUSION_LOCK.unlock(); }
        }
    }

}
