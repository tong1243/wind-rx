package com.wut.screendbmysqlrx.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wut.screendbmysqlrx.Model.RadarAllSecMetric;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RadarAllSecMetricMapper extends BaseMapper<RadarAllSecMetric> {
    public void createTable(@Param("tableName") String tableName);

    public void dropTable(@Param("tableName") String tableName);
}
