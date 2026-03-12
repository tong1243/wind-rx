package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbmysqlrx.Context.TableTimeContext;
import com.wut.screendbmysqlrx.Mapper.BottleneckAreaStateMapper;
import com.wut.screendbmysqlrx.Model.BottleneckAreaState;
import com.wut.screendbmysqlrx.Service.BottleneckAreaStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static com.wut.screencommonrx.Static.DbModuleStatic.*;

@Service
public class BottleneckAreaStateServiceImpl extends ServiceImpl<BottleneckAreaStateMapper, BottleneckAreaState> implements BottleneckAreaStateService {

        private final BottleneckAreaStateMapper bottleneckAreaStateMapper;
        private final TransactionTemplate transactionTemplate;

        @Autowired
     public BottleneckAreaStateServiceImpl(BottleneckAreaStateMapper bottleneckAreaStateMapper, TransactionTemplate transactionTemplate) {
        this.bottleneckAreaStateMapper = bottleneckAreaStateMapper;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
        @Transactional
        public void createTable(String tableName) {
        bottleneckAreaStateMapper.createTable(TABLE_BOTTLE_DDL_PREFIX + tableName);
        }

        @Override
        @Transactional
        public void dropTable(String tableName) {
            bottleneckAreaStateMapper.dropTable(TABLE_BOTTLE_DDL_PREFIX + tableName);
        }

        @Override
        @Transactional
        public void storeBottleneckAreaStateData(String time, BottleneckAreaState bottleneckAreaState) {
            MessagePrintUtil.printstoreBottleneckAreaStateData();
            TableTimeContext.setTime(TABLE_SUFFIX_KEY, time);
            transactionTemplate.execute(status -> {
                return bottleneckAreaStateMapper.insert(bottleneckAreaState);
            });
        }
    }

