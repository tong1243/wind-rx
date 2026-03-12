package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlrx.Mapper.RotationMapper;
import com.wut.screendbmysqlrx.Model.Rotation;
import com.wut.screendbmysqlrx.Service.RotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RotationServiceImpl extends ServiceImpl<RotationMapper, Rotation> implements RotationService {
    private final RotationMapper rotationMapper;

    @Autowired
    public RotationServiceImpl(RotationMapper rotationMapper) {
        this.rotationMapper = rotationMapper;
    }

    @Override
    public List<Rotation> getAllRotation() {
        return rotationMapper.selectList(null);
    }

}
