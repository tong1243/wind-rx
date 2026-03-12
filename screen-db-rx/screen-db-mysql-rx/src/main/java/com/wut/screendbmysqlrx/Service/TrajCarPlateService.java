package com.wut.screendbmysqlrx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlrx.Model.TrajCarPlate;

import java.util.List;

public interface TrajCarPlateService extends IService<TrajCarPlate> {
    public void storeTrajCarPlate(List<TrajCarPlate> trajCarPlateList);

    public void clearTrajCarPlate();

}
