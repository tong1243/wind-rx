package com.wut.screendbmysqlrx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlrx.Model.RadarInfo;

import java.util.List;

public interface RadarInfoService extends IService<RadarInfo> {
    public List<RadarInfo> getAllRadarInfo();

    public void updateRadarState(List<RadarInfo> radarInfoList);

}
