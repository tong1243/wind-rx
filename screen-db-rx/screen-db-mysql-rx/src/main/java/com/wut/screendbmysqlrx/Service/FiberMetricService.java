package com.wut.screendbmysqlrx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlrx.Model.FiberMetric;

public interface FiberMetricService extends IService<FiberMetric> {
    public void createTable(String tableName);

    public void dropTable(String tableName);

    public void storeFiberMetricData(String time, FiberMetric fiberMetric);
}
