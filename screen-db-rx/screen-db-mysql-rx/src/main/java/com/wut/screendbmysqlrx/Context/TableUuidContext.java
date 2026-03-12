package com.wut.screendbmysqlrx.Context;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import org.springframework.stereotype.Component;

@Component
public class TableUuidContext implements IdentifierGenerator {
    @Override
    public Long nextId(Object entity) {
        return IdUtil.getSnowflakeNextId();
    }

}
