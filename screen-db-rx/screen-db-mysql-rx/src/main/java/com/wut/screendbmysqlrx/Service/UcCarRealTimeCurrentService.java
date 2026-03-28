package com.wut.screendbmysqlrx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlrx.Model.UcCarRealTimeCurrent;

public interface UcCarRealTimeCurrentService extends IService<UcCarRealTimeCurrent> {
    void upsertOne(UcCarRealTimeCurrent ucCarRealTimeCurrent);
}
