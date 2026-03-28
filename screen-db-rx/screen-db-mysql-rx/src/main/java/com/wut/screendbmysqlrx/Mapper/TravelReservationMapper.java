package com.wut.screendbmysqlrx.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wut.screendbmysqlrx.Model.TravelReservation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TravelReservationMapper extends BaseMapper<TravelReservation> {
    TravelReservation selectLatestReservation();
}
