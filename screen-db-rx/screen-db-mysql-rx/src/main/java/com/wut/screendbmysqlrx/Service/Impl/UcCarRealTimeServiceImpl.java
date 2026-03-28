package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlrx.Mapper.UcCarRealTimeMapper;
import com.wut.screendbmysqlrx.Model.UcCarRealTime;
import com.wut.screendbmysqlrx.Service.UcCarRealTimeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UcCarRealTimeServiceImpl extends ServiceImpl<UcCarRealTimeMapper, UcCarRealTime> implements UcCarRealTimeService {
    @Override
    @Transactional
    public void storeOne(UcCarRealTime ucCarRealTime) {
        save(ucCarRealTime);
    }
}
