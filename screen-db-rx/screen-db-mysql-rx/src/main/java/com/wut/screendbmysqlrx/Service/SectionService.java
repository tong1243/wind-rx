package com.wut.screendbmysqlrx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlrx.Model.Section;

import java.util.List;

public interface SectionService extends IService<Section> {
    public void createTable(String tableName);

    public void dropTable(String tableName);

    public void storeSectionData(String timestamp, List<Section> sectionList);

}
