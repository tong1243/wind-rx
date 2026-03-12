package com.wut.screendbmysqlrx.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wut.screendbmysqlrx.Model.FiberMetric;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FiberMetricMapper extends BaseMapper<FiberMetric> {
    public void createTable(@Param("tableName") String tableName);

    public void dropTable(@Param("tableName") String tableName);

}
