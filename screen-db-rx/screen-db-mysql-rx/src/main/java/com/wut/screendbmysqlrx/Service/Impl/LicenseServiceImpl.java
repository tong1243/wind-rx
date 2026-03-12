package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlrx.Mapper.LicenseMapper;
import com.wut.screendbmysqlrx.Model.License;
import com.wut.screendbmysqlrx.Service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LicenseServiceImpl extends ServiceImpl<LicenseMapper, License> implements LicenseService {
    private final LicenseMapper licenseMapper;

    @Autowired
    public LicenseServiceImpl(LicenseMapper licenseMapper) {
        this.licenseMapper = licenseMapper;
    }

    @Override
    public List<License> getAllLicense() {
        return licenseMapper.selectList(null);
    }

}
