package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlrx.Context.TableTimeContext;
import com.wut.screendbmysqlrx.Mapper.TunnelRiskMapper;
import com.wut.screendbmysqlrx.Model.TunnelRisk;
import com.wut.screendbmysqlrx.Service.TunnelRiskService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static com.wut.screencommonrx.Static.DbModuleStatic.TABLE_RISK_DDL_PREFIX;
import static com.wut.screencommonrx.Static.DbModuleStatic.TABLE_SUFFIX_KEY;

@Service
public class TunnelRiskServiceImpl extends ServiceImpl<TunnelRiskMapper, TunnelRisk> implements TunnelRiskService {
    private final TunnelRiskMapper riskMapper;
    private final SqlSessionFactory sqlSessionFactory;
    private final TransactionTemplate transactionTemplate;
    @Autowired
    public TunnelRiskServiceImpl(TunnelRiskMapper riskMapper, SqlSessionFactory sqlSessionFactory, TransactionTemplate transactionTemplate) {
        this.riskMapper = riskMapper;
        this.sqlSessionFactory = sqlSessionFactory;
        this.transactionTemplate = transactionTemplate;
    }
    @Override
    @Transactional
    public void createTable(String tableName) {
        riskMapper.createTable(TABLE_RISK_DDL_PREFIX+tableName);
    }

    @Override
    @Transactional
    public void dropTable(String tableName) {
        riskMapper.dropTable(TABLE_RISK_DDL_PREFIX+tableName);
    }

    @Override
    @Transactional
    public void storeRiskData(String timestamp, List<TunnelRisk> riskList) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, timestamp);
        transactionTemplate.execute(status -> {
            MybatisBatch<TunnelRisk> riskBatchInsert = new MybatisBatch<>(sqlSessionFactory, riskList);
            MybatisBatch.Method<TunnelRisk> method = new MybatisBatch.Method<>(TunnelRiskMapper.class);
            return riskBatchInsert.execute(method.insert());
        });
    }
}
