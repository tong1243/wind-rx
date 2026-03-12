package com.wut.screendbmysqlrx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlrx.Model.RadarMetric;

import java.util.List;

public interface RadarMetricService extends IService<RadarMetric> {
    public void createTable(String tableName);

    public void dropTable(String tableName);

    public void storeRadarMetricData(String time, List<RadarMetric> radarMetricList);
}
