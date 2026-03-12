package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlrx.Context.TableTimeContext;
import com.wut.screendbmysqlrx.Mapper.CarEventMapper;
import com.wut.screendbmysqlrx.Model.CarEvent;
import com.wut.screendbmysqlrx.Service.CarEventService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static com.wut.screencommonrx.Static.DbModuleStatic.TABLE_EVENT_DDL_PREFIX;
import static com.wut.screencommonrx.Static.DbModuleStatic.TABLE_SUFFIX_KEY;

@Service
public class CarEventServiceImpl extends ServiceImpl<CarEventMapper, CarEvent> implements CarEventService {
    private final CarEventMapper carEventMapper;
    private final SqlSessionFactory sqlSessionFactory;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public CarEventServiceImpl(CarEventMapper carEventMapper, SqlSessionFactory sqlSessionFactory, TransactionTemplate transactionTemplate) {
        this.carEventMapper = carEventMapper;
        this.sqlSessionFactory = sqlSessionFactory;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    @Transactional
    public void createTable(String tableName) {
        carEventMapper.createTable(TABLE_EVENT_DDL_PREFIX + tableName);
    }

    @Override
    @Transactional
    public void dropTable(String tableName) {
        carEventMapper.dropTable(TABLE_EVENT_DDL_PREFIX + tableName);
    }

    @Override
    @Transactional
    public void storeEventData(String timestamp, List<CarEvent> carEventList) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, timestamp);
        transactionTemplate.execute(status -> {
            MybatisBatch<CarEvent> eventDataBatchInsert = new MybatisBatch<>(sqlSessionFactory, carEventList);
            MybatisBatch.Method<CarEvent> method = new MybatisBatch.Method<>(CarEventMapper.class);
            return eventDataBatchInsert.execute(method.insert());
        });
    }

    @Override
    @Transactional
    public void updateEventData(String timestamp, List<CarEvent> carEventList) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, timestamp);
        transactionTemplate.execute(status -> {
            MybatisBatch<CarEvent> eventBatchUpdate = new MybatisBatch<>(sqlSessionFactory, carEventList);
            MybatisBatch.Method<CarEvent> method = new MybatisBatch.Method<>(CarEventMapper.class);
            return eventBatchUpdate.execute(method.update(carEvent -> {
                LambdaUpdateWrapper<CarEvent> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(CarEvent::getTrajId, carEvent.getTrajId());
                wrapper.eq(CarEvent::getEventType, carEvent.getEventType());
                wrapper.eq(CarEvent::getStartTimestamp, carEvent.getStartTimestamp());
                wrapper.set(CarEvent::getEndTimestamp, carEvent.getEndTimestamp());
                wrapper.set(CarEvent::getEndMileage, carEvent.getEndMileage());
                wrapper.set(CarEvent::getLane, carEvent.getLane());
                wrapper.set(CarEvent::getId, carEvent.getId());
                return wrapper;
            }));
        });
    }
}
