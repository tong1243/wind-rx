package com.wut.screendbmysqlrx.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wut.screendbmysqlrx.Model.CarEvent;
import com.wut.screendbmysqlrx.Model.RiskEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RiskEventMapper extends BaseMapper<RiskEvent> {
    public void createTable(@Param("tableName") String tableName);

    public void dropTable(@Param("tableName") String tableName);

}
