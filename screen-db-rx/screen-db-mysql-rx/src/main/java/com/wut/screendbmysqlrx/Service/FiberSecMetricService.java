package com.wut.screendbmysqlrx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlrx.Model.FiberSecMetric;

import java.util.List;

public interface FiberSecMetricService extends IService<FiberSecMetric> {
    public void createTable(String tableName);

    public void dropTable(String tableName);

    public void storeFiberSecMetricData(String timestamp, List<FiberSecMetric> fiberSecMetricList);

}
