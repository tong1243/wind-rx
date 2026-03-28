package com.wut.screendbmysqlrx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlrx.Model.TravelReservation;

public interface TravelReservationService extends IService<TravelReservation> {
    TravelReservation getLatestReservation();
}
