package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlrx.Context.TableTimeContext;
import com.wut.screendbmysqlrx.Mapper.FiberMetricMapper;
import com.wut.screendbmysqlrx.Model.FiberMetric;
import com.wut.screendbmysqlrx.Service.FiberMetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static com.wut.screencommonrx.Static.DbModuleStatic.TABLE_FIBER_METRIC_DDL_PREFIX;
import static com.wut.screencommonrx.Static.DbModuleStatic.TABLE_SUFFIX_KEY;

@Service
public class FiberMetricServiceImpl extends ServiceImpl<FiberMetricMapper, FiberMetric> implements FiberMetricService {
    private final FiberMetricMapper fiberMetricMapper;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public FiberMetricServiceImpl(FiberMetricMapper fiberMetricMapper, TransactionTemplate transactionTemplate) {
        this.fiberMetricMapper = fiberMetricMapper;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    @Transactional
    public void createTable(String tableName) {
        fiberMetricMapper.createTable(TABLE_FIBER_METRIC_DDL_PREFIX + tableName);
    }

    @Override
    @Transactional
    public void dropTable(String tableName) {
        fiberMetricMapper.dropTable(TABLE_FIBER_METRIC_DDL_PREFIX + tableName);
    }

    @Override
    @Transactional
    public void storeFiberMetricData(String time, FiberMetric fiberMetric) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, time);
        transactionTemplate.execute(status -> {
           return fiberMetricMapper.insert(fiberMetric);
        });
    }

}
