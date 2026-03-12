package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlrx.Context.TableTimeContext;
import com.wut.screendbmysqlrx.Mapper.PostureMapper;
import com.wut.screendbmysqlrx.Model.Posture;
import com.wut.screendbmysqlrx.Service.PostureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static com.wut.screencommonrx.Static.DbModuleStatic.TABLE_POSTURE_DDL_PREFIX;
import static com.wut.screencommonrx.Static.DbModuleStatic.TABLE_SUFFIX_KEY;

@Service
public class PostureServiceImpl extends ServiceImpl<PostureMapper, Posture> implements PostureService {
    private final PostureMapper postureMapper;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public PostureServiceImpl(PostureMapper postureMapper, TransactionTemplate transactionTemplate) {
        this.postureMapper = postureMapper;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    @Transactional
    public void createTable(String tableName) {
        postureMapper.createTable(TABLE_POSTURE_DDL_PREFIX + tableName);
    }

    @Override
    @Transactional
    public void dropTable(String tableName) {
        postureMapper.dropTable(TABLE_POSTURE_DDL_PREFIX + tableName);
    }

    @Override
    @Transactional
    public void storePostureData(String time, Posture posture) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, time);
        transactionTemplate.execute(status -> {
            return postureMapper.insert(posture);
        });
    }
}
