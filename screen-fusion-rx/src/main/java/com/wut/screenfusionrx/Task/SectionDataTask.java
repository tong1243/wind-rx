package com.wut.screenfusionrx.Task;

import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screenfusionrx.Context.SectionDataContext;
import com.wut.screenfusionrx.Service.SectionDataService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

import static com.wut.screencommonrx.Static.FusionModuleStatic.SECTION_RECORD_TIME_COND;

@Component
public class SectionDataTask {
    @Qualifier("fusionTaskSectionAsyncPool")
    private final Executor fusionTaskSectionAsyncPool;
    private final SectionDataService sectionDataService;
    private final SectionDataContext sectionDataContext;
    private static final ReentrantLock SECTION_DATA_LOCK = new ReentrantLock(true);

    @Autowired
    public SectionDataTask(Executor fusionTaskSectionAsyncPool, SectionDataService sectionDataService, SectionDataContext sectionDataContext) {
        this.fusionTaskSectionAsyncPool = fusionTaskSectionAsyncPool;
        this.sectionDataService = sectionDataService;
        this.sectionDataContext = sectionDataContext;
    }

    @RabbitListener(queues = "section")
    public void sectionDataListener(String timestampStr) {
        startParseSectionData(Long.parseLong(timestampStr)).thenRunAsync(() -> {});
    }

    public CompletableFuture<Void> startParseSectionData(long timestamp) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (sectionDataContext.recordSectionTimestamp(timestamp)) {
                    try {
                        SECTION_DATA_LOCK.lock();
                        sectionDataService.collectAndStoreSectionData(timestamp);
                    } catch (Exception e) { MessagePrintUtil.printException(e, "startParseSectionData->inner"); }
                    finally { SECTION_DATA_LOCK.unlock(); }
                    MessagePrintUtil.printSectionData(timestamp - SECTION_RECORD_TIME_COND, timestamp);
                }
            } catch (Exception e) { MessagePrintUtil.printException(e, "startParseSectionData"); }
        }, fusionTaskSectionAsyncPool);
    }

}
