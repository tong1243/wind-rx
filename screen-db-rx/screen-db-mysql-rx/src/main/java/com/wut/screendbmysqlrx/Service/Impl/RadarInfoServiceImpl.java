package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlrx.Mapper.RadarInfoMapper;
import com.wut.screendbmysqlrx.Model.RadarInfo;
import com.wut.screendbmysqlrx.Service.RadarInfoService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Service
public class RadarInfoServiceImpl extends ServiceImpl<RadarInfoMapper, RadarInfo> implements RadarInfoService {
    private final RadarInfoMapper radarInfoMapper;
    private final SqlSessionFactory sqlSessionFactory;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public RadarInfoServiceImpl(RadarInfoMapper radarInfoMapper, SqlSessionFactory sqlSessionFactory, TransactionTemplate transactionTemplate) {
        this.radarInfoMapper = radarInfoMapper;
        this.sqlSessionFactory = sqlSessionFactory;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public List<RadarInfo> getAllRadarInfo() {
        LambdaQueryWrapper<RadarInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(RadarInfo::getRid);
        return radarInfoMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void updateRadarState(List<RadarInfo> radarInfoList) {
        transactionTemplate.execute(status -> {
            MybatisBatch<RadarInfo> radarInfoBatchUpdate = new MybatisBatch<>(sqlSessionFactory, radarInfoList);
            MybatisBatch.Method<RadarInfo> method = new MybatisBatch.Method<>(RadarInfoMapper.class);
            return radarInfoBatchUpdate.execute(method.update(radarInfo -> {
                LambdaUpdateWrapper<RadarInfo> wrapper = new LambdaUpdateWrapper<>();
                wrapper.set(RadarInfo::getState, radarInfo.getState());
                wrapper.eq(RadarInfo::getRid, radarInfo.getRid());
                wrapper.eq(RadarInfo::getType, radarInfo.getType());
                return wrapper;
            }));
        });
    }
}
