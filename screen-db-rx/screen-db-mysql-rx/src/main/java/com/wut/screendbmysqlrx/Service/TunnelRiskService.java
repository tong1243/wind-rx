    package com.wut.screendbmysqlrx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlrx.Model.TunnelRisk;

import java.util.List;

public interface TunnelRiskService extends IService<TunnelRisk> {
    public void createTable(String tableName);

    public void dropTable(String tableName);

    public void storeRiskData(String timestamp, List<TunnelRisk> riskList);
}
