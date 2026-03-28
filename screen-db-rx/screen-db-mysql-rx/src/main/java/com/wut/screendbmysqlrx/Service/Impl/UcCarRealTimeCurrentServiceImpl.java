package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlrx.Mapper.UcCarRealTimeCurrentMapper;
import com.wut.screendbmysqlrx.Model.UcCarRealTimeCurrent;
import com.wut.screendbmysqlrx.Service.UcCarRealTimeCurrentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UcCarRealTimeCurrentServiceImpl extends ServiceImpl<UcCarRealTimeCurrentMapper, UcCarRealTimeCurrent> implements UcCarRealTimeCurrentService {
    @Override
    @Transactional
    public void upsertOne(UcCarRealTimeCurrent ucCarRealTimeCurrent) {
        baseMapper.upsertOne(ucCarRealTimeCurrent);
    }
}
