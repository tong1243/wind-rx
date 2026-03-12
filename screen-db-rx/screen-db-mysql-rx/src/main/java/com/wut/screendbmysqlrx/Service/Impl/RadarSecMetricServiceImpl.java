package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlrx.Context.TableTimeContext;
import com.wut.screendbmysqlrx.Mapper.RadarSecMetricMapper;
import com.wut.screendbmysqlrx.Model.RadarSecMetric;
import com.wut.screendbmysqlrx.Service.RadarSecMetricService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static com.wut.screencommonrx.Static.DbModuleStatic.TABLE_RADAR_SEC_METRIC_DDL_PREFIX;
import static com.wut.screencommonrx.Static.DbModuleStatic.TABLE_SUFFIX_KEY;

@Service
public class RadarSecMetricServiceImpl extends ServiceImpl<RadarSecMetricMapper, RadarSecMetric> implements RadarSecMetricService {
    private final RadarSecMetricMapper radarSecMetricMapper;
    private final SqlSessionFactory sqlSessionFactory;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public RadarSecMetricServiceImpl(RadarSecMetricMapper radarSecMetricMapper, SqlSessionFactory sqlSessionFactory, TransactionTemplate transactionTemplate) {
        this.radarSecMetricMapper = radarSecMetricMapper;
        this.sqlSessionFactory = sqlSessionFactory;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    @Transactional
    public void createTable(String tableName) {
        radarSecMetricMapper.createTable(TABLE_RADAR_SEC_METRIC_DDL_PREFIX + tableName);
    }

    @Override
    @Transactional
    public void dropTable(String tableName) {
        radarSecMetricMapper.dropTable(TABLE_RADAR_SEC_METRIC_DDL_PREFIX + tableName);
    }

    @Override
    @Transactional
    public void storeRadarSecMetricData(String timestamp, List<RadarSecMetric> radarSecMetricList) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, timestamp);
        transactionTemplate.execute(status -> {
            MybatisBatch<RadarSecMetric> radarSecMetricBatchInsert = new MybatisBatch<>(sqlSessionFactory, radarSecMetricList);
            MybatisBatch.Method<RadarSecMetric> method = new MybatisBatch.Method<>(RadarSecMetricMapper.class);
            return radarSecMetricBatchInsert.execute(method.insert());
        });
    }
}
