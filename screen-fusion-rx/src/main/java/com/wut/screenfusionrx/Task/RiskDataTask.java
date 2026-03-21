package com.wut.screenfusionrx.Task;

import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screenfusionrx.Context.RiskDataContext;
import com.wut.screenfusionrx.Service.RiskDataService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class RiskDataTask {
    @Qualifier("fusionTaskRiskAsyncPool")
    private final Executor fusionTaskRiskAsyncPool;
    private final RiskDataService riskDataService;
    private final RiskDataContext riskDataContext;
    private static final ReentrantLock RISK_DATA_LOCK = new ReentrantLock(true);

    @Autowired
    public RiskDataTask(Executor fusionTaskRiskAsyncPool, RiskDataService riskDataService, RiskDataContext riskDataContext) {

        this.fusionTaskRiskAsyncPool = fusionTaskRiskAsyncPool;
        this.riskDataService = riskDataService;
        this.riskDataContext = riskDataContext;
    }

    @RabbitListener(queues = "risk")
    public void riskDataListener(String timestampStr) {
//        startParseRiskData(Long.parseLong(timestampStr)).thenRunAsync(() -> {});
    }

    public CompletableFuture<Void> startParseRiskData(long timestamp) {
        return CompletableFuture.runAsync(() -> {
            try {
//                MessagePrintUtil.printStartRiskData();
                if (riskDataContext.recordRiskTimestamp(timestamp)) {
                    try {
                        RISK_DATA_LOCK.lock();
                        riskDataService.collectAndStoreRiskData(timestamp);
                    } catch (Exception e) { MessagePrintUtil.printException(e, "startParseRiskData->inner"); }
                    finally { RISK_DATA_LOCK.unlock(); }
                }
            } catch (Exception e) { MessagePrintUtil.printException(e, "startParseRiskData"); }
        }, fusionTaskRiskAsyncPool);
    }
}
