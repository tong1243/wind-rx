package com.wut.screenfusionrx.Context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wut.screencommonrx.Model.CarEventModel;
import com.wut.screencommonrx.Model.EventTypeModel;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screencommonrx.Util.ModelTransformUtil;
import com.wut.screendbmysqlrx.Model.Traj;
import com.wut.screendbmysqlrx.Util.DbModelTransformUtil;
import com.wut.screenfusionrx.Util.TrajModelParamUtil;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EventDataContext {
    // 事件轨迹记录
    // 1-记录所有正在生成轨迹的时间,位置和速度变化情况
    // 2-记录所有可能发生事件的类型,截止时间,截止位置和目标事件记录次数
    @Getter
    private final Map<String, CarEventModel> eventModelMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void initEventDataContext() {
        eventModelMap.clear();
    }

    public void putIntoEventModelMap(Traj traj, EventTypeModel entity) {
        eventModelMap.put(
                TrajModelParamUtil.getEventMapKey(traj, entity),
                DbModelTransformUtil.trajToCarEventModel(traj, entity.value)
        );
    }

    public void filterExpireEventModel(long timestamp) {
        if (CollectionEmptyUtil.forMap(eventModelMap)) { return; }
        eventModelMap.entrySet().removeIf(entry -> {
            return !entry.getValue().isChanged()
                    // 上次记录的事件时间与当前时间差,大于事件类型的阈值
                    && Math.abs(timestamp - entry.getValue().getEndTimestamp()) >= ModelTransformUtil.getEventTypeInstance(entry.getValue().getEventType()).getTimeout();
        });
    }

    // (保留)日志打印测试方法
    public void printEventModelMapToLogger(long timestamp) {
        try { MessagePrintUtil.printEventModelMap(timestamp, objectMapper.writeValueAsString(eventModelMap)); }
        catch (Exception e) { MessagePrintUtil.printException(e, "printEventModelMapToLogger"); }
    }

}
