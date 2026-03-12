package com.wut.screendbmysqlrx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlrx.Model.RadarSecMetric;

import java.util.List;

public interface RadarSecMetricService extends IService<RadarSecMetric> {
    public void createTable(String tableName);

    public void dropTable(String tableName);

    public void storeRadarSecMetricData(String timestamp, List<RadarSecMetric> radarSecMetricList);

}
