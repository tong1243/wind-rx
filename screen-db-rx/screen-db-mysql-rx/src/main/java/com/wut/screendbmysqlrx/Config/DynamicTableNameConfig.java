package com.wut.screendbmysqlrx.Config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.wut.screencommonrx.Static.DbModuleStatic;
import com.wut.screendbmysqlrx.Context.TableTimeContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.wut.screencommonrx.Static.DbModuleStatic.TABLE_SUFFIX_SEPARATOR;

@Configuration
public class DynamicTableNameConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        dynamicTableNameInnerInterceptor.setTableNameHandler(
            (sql, tableName) -> {
                if (!DbModuleStatic.DYNAMIC_TABLE_NAMES.contains(tableName)) { return tableName; }
                String timestamp = TableTimeContext.getTime(DbModuleStatic.TABLE_SUFFIX_KEY);
                return tableName + TABLE_SUFFIX_SEPARATOR + timestamp;
            }
        );
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);
        return interceptor;
    }

}
