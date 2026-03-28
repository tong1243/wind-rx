package com.wut.screendbmysqlrx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlrx.Model.UcCarRealTime;

public interface UcCarRealTimeService extends IService<UcCarRealTime> {
    void storeOne(UcCarRealTime ucCarRealTime);
}
