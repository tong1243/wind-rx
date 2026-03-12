package com.wut.screendbmysqlrx.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wut.screendbmysqlrx.Model.RadarMetric;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RadarMetricMapper extends BaseMapper<RadarMetric> {
    public void createTable(@Param("tableName") String tableName);

    public void dropTable(@Param("tableName") String tableName);

}
