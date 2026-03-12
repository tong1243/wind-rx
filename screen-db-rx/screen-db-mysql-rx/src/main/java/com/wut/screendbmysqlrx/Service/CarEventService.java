package com.wut.screendbmysqlrx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlrx.Model.CarEvent;

import java.util.List;

public interface CarEventService extends IService<CarEvent> {
    public void createTable(String tableName);

    public void dropTable(String tableName);

    public void storeEventData(String timestamp, List<CarEvent> carEventList);

    public void updateEventData(String timestamp, List<CarEvent> carEventList);

}
