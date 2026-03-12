package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlrx.Context.TableTimeContext;
import com.wut.screendbmysqlrx.Mapper.SectionMapper;
import com.wut.screendbmysqlrx.Model.Section;
import com.wut.screendbmysqlrx.Service.SectionService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static com.wut.screencommonrx.Static.DbModuleStatic.TABLE_SECTION_DDL_PREFIX;
import static com.wut.screencommonrx.Static.DbModuleStatic.TABLE_SUFFIX_KEY;

@Service
public class SectionServiceImpl extends ServiceImpl<SectionMapper, Section> implements SectionService{
    private final SectionMapper sectionMapper;
    private final SqlSessionFactory sqlSessionFactory;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public SectionServiceImpl(SectionMapper sectionMapper, SqlSessionFactory sqlSessionFactory, TransactionTemplate transactionTemplate) {
        this.sectionMapper = sectionMapper;
        this.sqlSessionFactory = sqlSessionFactory;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    @Transactional
    public void createTable(String tableName) {
        sectionMapper.createTable(TABLE_SECTION_DDL_PREFIX + tableName);
    }

    @Override
    @Transactional
    public void dropTable(String tableName) {
        sectionMapper.dropTable(TABLE_SECTION_DDL_PREFIX + tableName);
    }

    @Override
    @Transactional
    public void storeSectionData(String timestamp, List<Section> sectionList) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, timestamp);
        transactionTemplate.execute(status -> {
            MybatisBatch<Section> sectionBatchInsert = new MybatisBatch<>(sqlSessionFactory, sectionList);
            MybatisBatch.Method<Section> method = new MybatisBatch.Method<>(SectionMapper.class);
            return sectionBatchInsert.execute(method.insert());
        });
    }

}
