package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlrx.Context.TableTimeContext;
import com.wut.screendbmysqlrx.Mapper.RadarMetricMapper;
import com.wut.screendbmysqlrx.Model.RadarMetric;
import com.wut.screendbmysqlrx.Service.RadarMetricService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static com.wut.screencommonrx.Static.DbModuleStatic.TABLE_RADAR_METRIC_DDL_PREFIX;
import static com.wut.screencommonrx.Static.DbModuleStatic.TABLE_SUFFIX_KEY;

@Service
public class RadarMetricServiceImpl extends ServiceImpl<RadarMetricMapper, RadarMetric> implements RadarMetricService {
    private final RadarMetricMapper radarMetricMapper;
    private final SqlSessionFactory sqlSessionFactory;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public RadarMetricServiceImpl(RadarMetricMapper radarMetricMapper, SqlSessionFactory sqlSessionFactory, TransactionTemplate transactionTemplate) {
        this.radarMetricMapper = radarMetricMapper;
        this.sqlSessionFactory = sqlSessionFactory;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    @Transactional
    public void createTable(String tableName) {
        radarMetricMapper.createTable(TABLE_RADAR_METRIC_DDL_PREFIX + tableName);
    }

    @Override
    @Transactional
    public void dropTable(String tableName) {
        radarMetricMapper.dropTable(TABLE_RADAR_METRIC_DDL_PREFIX + tableName);
    }

    @Override
    @Transactional
    public void storeRadarMetricData(String time, List<RadarMetric> radarMetricList) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, time);
        transactionTemplate.execute(status -> {
            MybatisBatch<RadarMetric> radarMetricBatchInsert = new MybatisBatch<>(sqlSessionFactory, radarMetricList);
            MybatisBatch.Method<RadarMetric> method = new MybatisBatch.Method<>(RadarMetricMapper.class);
            return radarMetricBatchInsert.execute(method.insert());
        });
    }
}
