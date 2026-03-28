package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlrx.Mapper.TravelReservationMapper;
import com.wut.screendbmysqlrx.Model.TravelReservation;
import com.wut.screendbmysqlrx.Service.TravelReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TravelReservationServiceImpl extends ServiceImpl<TravelReservationMapper, TravelReservation> implements TravelReservationService {
    private final TravelReservationMapper travelReservationMapper;

    @Autowired
    public TravelReservationServiceImpl(TravelReservationMapper travelReservationMapper) {
        this.travelReservationMapper = travelReservationMapper;
    }

    @Override
    public TravelReservation getLatestReservation() {
        return travelReservationMapper.selectLatestReservation();
    }
}
