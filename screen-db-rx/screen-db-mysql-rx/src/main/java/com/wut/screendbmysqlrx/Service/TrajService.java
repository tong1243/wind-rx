package com.wut.screendbmysqlrx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlrx.Model.Traj;

import java.util.List;

public interface TrajService extends IService<Traj> {
    public void createTable(String tableName);

    public void dropTable(String tableName);

    public void storeTrajData(String timestamp, List<Traj> trajList);

}
