package com.wut.screendbmysqlrx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlrx.Model.CarEvent;
import com.wut.screendbmysqlrx.Model.RiskEvent;

import java.util.List;

public interface RiskEventService extends IService<RiskEvent> {
    public void createTable(String tableName);

    public void dropTable(String tableName);

   public void storeEventData(String timestamp, List<RiskEvent> riskEventList);
//
//    public void updateEventData(String timestamp, List<RiskEvent> riskEventList);

}
