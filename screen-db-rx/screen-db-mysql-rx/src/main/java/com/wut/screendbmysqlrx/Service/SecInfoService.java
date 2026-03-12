package com.wut.screendbmysqlrx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlrx.Model.SecInfo;
import com.wut.screendbmysqlrx.Model.TunnelSecInfo;

import java.util.List;

public interface SecInfoService extends IService<SecInfo> {
    public List<SecInfo> getAllSecInfo();
    public List<TunnelSecInfo> getAllTunnelSecInfo();
}
