package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlrx.Context.TableTimeContext;
import com.wut.screendbmysqlrx.Mapper.RadarAllSecMetricMapper;
import com.wut.screendbmysqlrx.Mapper.RadarSecMetricMapper;
import com.wut.screendbmysqlrx.Model.RadarAllSecMetric;
import com.wut.screendbmysqlrx.Model.RadarSecMetric;
import com.wut.screendbmysqlrx.Service.RadarAllSecMetricService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static com.wut.screencommonrx.Static.DbModuleStatic.TABLE_RADAR_ALL_SEC_METRIC_DDL_PREFIX;
import static com.wut.screencommonrx.Static.DbModuleStatic.TABLE_SUFFIX_KEY;

@Service
public class RadarAllSecMetricServiceImpl extends ServiceImpl<RadarAllSecMetricMapper, RadarAllSecMetric> implements RadarAllSecMetricService {
    private final RadarAllSecMetricMapper radarAllSecMetricMapper;
    private final SqlSessionFactory sqlSessionFactory;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public RadarAllSecMetricServiceImpl(RadarAllSecMetricMapper radarAllSecMetricMapper, SqlSessionFactory sqlSessionFactory, TransactionTemplate transactionTemplate) {
        this.radarAllSecMetricMapper = radarAllSecMetricMapper;
        this.sqlSessionFactory = sqlSessionFactory;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    @Transactional
    public void createTable(String tableName) {
        radarAllSecMetricMapper.createTable(TABLE_RADAR_ALL_SEC_METRIC_DDL_PREFIX + tableName);
    }

    @Override
    @Transactional
    public void dropTable(String tableName) {
        radarAllSecMetricMapper.dropTable(TABLE_RADAR_ALL_SEC_METRIC_DDL_PREFIX + tableName);
    }

    @Override
    public void storeRadarAllSecMetricData(String timestamp, List<RadarAllSecMetric> radarAllSecMetricList) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, timestamp);
        transactionTemplate.execute(status -> {
            MybatisBatch<RadarAllSecMetric> radarAllSecMetricBatchInsert = new MybatisBatch<>(sqlSessionFactory, radarAllSecMetricList);
            MybatisBatch.Method<RadarAllSecMetric> method = new MybatisBatch.Method<>(RadarAllSecMetricMapper.class);
            return radarAllSecMetricBatchInsert.execute(method.insert());
        });
    }
}
