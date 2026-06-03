package com.wut.screendbmysqlrx.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wut.screendbmysqlrx.Context.TableTimeContext;
import com.wut.screendbmysqlrx.Mapper.UcCarRealTimeMapper;
import com.wut.screendbmysqlrx.Model.UcCarRealTime;
import com.wut.screendbmysqlrx.Service.UcCarRealTimeService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.wut.screencommonrx.Static.DbModuleStatic.TABLE_SUFFIX_KEY;

@Service
public class UcCarRealTimeServiceImpl extends ServiceImpl<UcCarRealTimeMapper, UcCarRealTime> implements UcCarRealTimeService {
    private static final String TABLE_BASE = "uc_car_real_time";
    private static final String TABLE_PREFIX = TABLE_BASE + "_";
    private static final DateTimeFormatter TABLE_SUFFIX_FMT = DateTimeFormatter.BASIC_ISO_DATE;

    private final JdbcTemplate jdbcTemplate;
    private final Set<String> createdTableSuffixCache = ConcurrentHashMap.newKeySet();

    public UcCarRealTimeServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void storeOne(UcCarRealTime ucCarRealTime) {
        if (ucCarRealTime == null) {
            return;
        }
        if (ucCarRealTime.getReportTime() == null) {
            ucCarRealTime.setReportTime(LocalDateTime.now());
        }
        String suffix = ucCarRealTime.getReportTime().toLocalDate().format(TABLE_SUFFIX_FMT);
        ensureDailyTable(suffix);

        withTableSuffix(suffix, () -> {
            save(ucCarRealTime);
            return null;
        });

        // Keep base-table writes for existing fallback/query behavior in screen-web-sx.
        insertCompatBaseRow(ucCarRealTime);
    }

    private void insertCompatBaseRow(UcCarRealTime row) {
        jdbcTemplate.update(
                "INSERT INTO uc_car_real_time " +
                        "(user_phone, car_license, current_pile, real_speed, direction, driving_direction, lane_number, road, report_time) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                row.getUserPhone(),
                row.getCarLicense(),
                row.getCurrentPile(),
                row.getRealSpeed(),
                row.getDirection(),
                row.getDrivingDirection(),
                row.getLaneNumber(),
                row.getRoad(),
                row.getReportTime()
        );
    }

    private void ensureDailyTable(String suffix) {
        if (createdTableSuffixCache.contains(suffix)) {
            return;
        }
        String tableName = TABLE_PREFIX + suffix;
        if (!tableName.matches("^uc_car_real_time_\\d{8}$")) {
            throw new IllegalArgumentException("invalid uc realtime table name: " + tableName);
        }
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `" + tableName + "` LIKE `" + TABLE_BASE + "`");
        createdTableSuffixCache.add(suffix);
    }

    private <T> T withTableSuffix(String suffix, Supplier<T> supplier) {
        TableTimeContext.setTime(TABLE_SUFFIX_KEY, suffix);
        try {
            return supplier.get();
        } finally {
            TableTimeContext.clearTime();
        }
    }
}
