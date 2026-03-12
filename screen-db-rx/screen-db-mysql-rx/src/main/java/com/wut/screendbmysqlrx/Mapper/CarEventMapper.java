package com.wut.screendbmysqlrx.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wut.screendbmysqlrx.Model.CarEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CarEventMapper extends BaseMapper<CarEvent> {
    public void createTable(@Param("tableName") String tableName);

    public void dropTable(@Param("tableName") String tableName);

}
