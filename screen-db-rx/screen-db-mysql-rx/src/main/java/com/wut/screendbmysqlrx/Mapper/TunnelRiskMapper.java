package com.wut.screendbmysqlrx.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wut.screendbmysqlrx.Model.TunnelRisk;
import org.apache.ibatis.annotations.Param;

public interface TunnelRiskMapper extends BaseMapper<TunnelRisk> {
    public void createTable(@Param("tableName") String tableName);

    public void dropTable(@Param("tableName") String tableName);
}
