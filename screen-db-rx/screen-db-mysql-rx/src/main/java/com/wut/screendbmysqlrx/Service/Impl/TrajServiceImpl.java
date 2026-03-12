package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlrx.Context.TableTimeContext;
import com.wut.screendbmysqlrx.Mapper.TrajMapper;
import com.wut.screendbmysqlrx.Model.Traj;
import com.wut.screendbmysqlrx.Service.TrajService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static com.wut.screencommonrx.Static.DbModuleStatic.TABLE_SUFFIX_KEY;
import static com.wut.screencommonrx.Static.DbModuleStatic.TABLE_TRAJ_DDL_PREFIX;

@Service
public class TrajServiceImpl extends ServiceImpl<TrajMapper, Traj> implements TrajService {
    private final TrajMapper trajMapper;
    private final SqlSessionFactory sqlSessionFactory;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public TrajServiceImpl(TrajMapper trajMapper, SqlSessionFactory sqlSessionFactory, TransactionTemplate transactionTemplate) {
        this.trajMapper = trajMapper;
        this.sqlSessionFactory = sqlSessionFactory;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    @Transactional
    public void createTable(String tableName) {
        trajMapper.createTable(TABLE_TRAJ_DDL_PREFIX + tableName);
    }

    @Override
    @Transactional
    public void dropTable(String tableName) {
        trajMapper.dropTable(TABLE_TRAJ_DDL_PREFIX + tableName);
    }

    @Override
    @Transactional
    public void storeTrajData(String timestamp, List<Traj> trajList) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, timestamp);
        transactionTemplate.execute(status -> {
            MybatisBatch<Traj> trajDataBatchInsert = new MybatisBatch<>(sqlSessionFactory, trajList);
            MybatisBatch.Method<Traj> method = new MybatisBatch.Method<>(TrajMapper.class);
            return trajDataBatchInsert.execute(method.insert());
        });
    }

}
