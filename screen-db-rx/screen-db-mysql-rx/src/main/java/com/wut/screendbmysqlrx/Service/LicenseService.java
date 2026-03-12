package com.wut.screendbmysqlrx.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wut.screendbmysqlrx.Model.License;

import java.util.List;

public interface LicenseService extends IService<License> {
    public List<License> getAllLicense();

}
