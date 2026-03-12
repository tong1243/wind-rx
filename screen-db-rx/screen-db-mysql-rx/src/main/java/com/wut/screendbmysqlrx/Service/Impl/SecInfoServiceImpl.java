package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlrx.Mapper.SecInfoMapper;
import com.wut.screendbmysqlrx.Mapper.TunnelSecInfoMapper;
import com.wut.screendbmysqlrx.Model.SecInfo;
import com.wut.screendbmysqlrx.Model.TunnelSecInfo;
import com.wut.screendbmysqlrx.Service.SecInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecInfoServiceImpl extends ServiceImpl<SecInfoMapper, SecInfo> implements SecInfoService {
    private final SecInfoMapper secInfoMapper;
    private final TunnelSecInfoMapper tunnelSecInfoMapper;

    @Autowired
    public SecInfoServiceImpl(SecInfoMapper secInfoMapper, TunnelSecInfoMapper tunnelSecInfoMapper) {
        this.secInfoMapper = secInfoMapper;
        this.tunnelSecInfoMapper = tunnelSecInfoMapper;
    }

    @Override
    public List<SecInfo> getAllSecInfo() {
        LambdaQueryWrapper<SecInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SecInfo::getXsecValue);
        return secInfoMapper.selectList(wrapper);
    }

    @Override
    public List<TunnelSecInfo> getAllTunnelSecInfo() {
        LambdaQueryWrapper<TunnelSecInfo> wrapper = new LambdaQueryWrapper<>();
        return tunnelSecInfoMapper.selectList(wrapper);
    }

}
