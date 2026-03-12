package com.wut.screendbmysqlrx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlrx.Model.Parameters;

import java.util.List;

public interface ParametersService extends IService<Parameters> {
    public void createTable(String tableName);

    public void dropTable(String tableName);

    public void storeParametersData(String time, Parameters parameters);

    void storeParametersBatch(String time, List<Parameters> parametersList);
}
