package com.wut.screendbmysqlrx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlrx.Model.Posture;

public interface PostureService extends IService<Posture> {
    public void createTable(String tableName);

    public void dropTable(String tableName);

    public void storePostureData(String time, Posture posture);

}
