package com.wut.screendbmysqlrx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlrx.Model.BottleneckAreaState;


public interface BottleneckAreaStateService extends IService<BottleneckAreaState> {
    public void createTable(String tableName);

    public void dropTable(String tableName);

    public void storeBottleneckAreaStateData(String time, BottleneckAreaState bottleneckAreaState);
}
