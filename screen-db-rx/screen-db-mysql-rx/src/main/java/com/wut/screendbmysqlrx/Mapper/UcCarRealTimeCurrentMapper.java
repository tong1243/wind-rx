package com.wut.screendbmysqlrx.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wut.screendbmysqlrx.Model.UcCarRealTimeCurrent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UcCarRealTimeCurrentMapper extends BaseMapper<UcCarRealTimeCurrent> {
    void upsertOne(@Param("item") UcCarRealTimeCurrent item);
}
