package com.wut.screendbmysqlrx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlrx.Model.RadarAllSecMetric;
import com.wut.screendbmysqlrx.Model.RadarSecMetric;

import java.util.List;

public interface RadarAllSecMetricService extends IService<RadarAllSecMetric> {
    public void createTable(String tableName);

    public void dropTable(String tableName);
    public void storeRadarAllSecMetricData(String timestamp, List<RadarAllSecMetric> radarAllSecMetricList);
}
