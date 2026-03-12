package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlrx.Context.TableTimeContext;
import com.wut.screendbmysqlrx.Mapper.FiberSecMetricMapper;
import com.wut.screendbmysqlrx.Model.FiberSecMetric;
import com.wut.screendbmysqlrx.Service.FiberSecMetricService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static com.wut.screencommonrx.Static.DbModuleStatic.TABLE_FIBER_SEC_METRIC_DDL_PREFIX;
import static com.wut.screencommonrx.Static.DbModuleStatic.TABLE_SUFFIX_KEY;

@Service
public class FiberSecMetricServiceImpl extends ServiceImpl<FiberSecMetricMapper, FiberSecMetric> implements FiberSecMetricService {
    private final FiberSecMetricMapper fiberSecMetricMapper;
    private final SqlSessionFactory sqlSessionFactory;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public FiberSecMetricServiceImpl(FiberSecMetricMapper fiberSecMetricMapper, SqlSessionFactory sqlSessionFactory, TransactionTemplate transactionTemplate) {
        this.fiberSecMetricMapper = fiberSecMetricMapper;
        this.sqlSessionFactory = sqlSessionFactory;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    @Transactional
    public void createTable(String tableName) {
        fiberSecMetricMapper.createTable(TABLE_FIBER_SEC_METRIC_DDL_PREFIX + tableName);
    }

    @Override
    @Transactional
    public void dropTable(String tableName) {
        fiberSecMetricMapper.dropTable(TABLE_FIBER_SEC_METRIC_DDL_PREFIX + tableName);
    }

    @Override
    @Transactional
    public void storeFiberSecMetricData(String timestamp, List<FiberSecMetric> fiberSecMetricList) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, timestamp);
        transactionTemplate.execute(status -> {
            MybatisBatch<FiberSecMetric> fiberSecMetricBatchInsert = new MybatisBatch<>(sqlSessionFactory, fiberSecMetricList);
            MybatisBatch.Method<FiberSecMetric> method = new MybatisBatch.Method<>(FiberSecMetricMapper.class);
            return fiberSecMetricBatchInsert.execute(method.insert());
        });
    }
}
