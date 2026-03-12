package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbmysqlrx.Context.TableTimeContext;
import com.wut.screendbmysqlrx.Mapper.ParametersMapper;
import com.wut.screendbmysqlrx.Mapper.TrajMapper;
import com.wut.screendbmysqlrx.Model.Parameters;
import com.wut.screendbmysqlrx.Model.Traj;
import com.wut.screendbmysqlrx.Service.ParametersService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static com.wut.screencommonrx.Static.DbModuleStatic.*;

@Service
public class ParametersServiceImpl extends ServiceImpl<ParametersMapper, Parameters> implements ParametersService {
    private final ParametersMapper parametersMapper;
    private final TransactionTemplate transactionTemplate;
    private final SqlSessionFactory sqlSessionFactory;


    @Autowired
    public ParametersServiceImpl(ParametersMapper parametersMapper, TransactionTemplate transactionTemplate, SqlSessionFactory sqlSessionFactory) {
        this.parametersMapper = parametersMapper;
        this.transactionTemplate = transactionTemplate;
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    @Transactional
    public void createTable(String tableName) {
        parametersMapper.createTable(TABLE_PARAMETERS_DDL_PREFIX + tableName);
    }

    @Override
    @Transactional
    public void dropTable(String tableName) {
        parametersMapper.dropTable(TABLE_PARAMETERS_DDL_PREFIX + tableName);
    }

    @Override
    @Transactional
    public void storeParametersData(String time, Parameters parameters) {
        MessagePrintUtil.printStoreParametersData();
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, time);
        transactionTemplate.execute(status -> {
            return parametersMapper.insert(parameters);
        });
    }


    @Override
    @Transactional
    public void storeParametersBatch(String timestamp, List<Parameters> parametersList) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, timestamp);
        transactionTemplate.execute(status -> {
            MybatisBatch<Parameters> trajDataBatchInsert = new MybatisBatch<>(sqlSessionFactory, parametersList);
            MybatisBatch.Method<Parameters> method = new MybatisBatch.Method<>(TrajMapper.class);
            return trajDataBatchInsert.execute(method.insert());
        });
    }


}
