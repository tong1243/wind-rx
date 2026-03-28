package com.wut.screenmsgrx.Service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import static com.wut.screencommonrx.Static.DbModuleStatic.DYNAMIC_TABLE_NAMES;

@Component
public class DynamicTableRetentionService {
    private static final Logger log = LoggerFactory.getLogger(DynamicTableRetentionService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final Set<String> DYNAMIC_PREFIX_SET = Set.copyOf(DYNAMIC_TABLE_NAMES);

    private final JdbcTemplate jdbcTemplate;

    @Value("${msg.dynamic-table-retention-days:7}")
    private int retentionDays;

    public DynamicTableRetentionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        cleanExpiredDynamicTables();
    }

    @Scheduled(cron = "${msg.dynamic-table-clean-cron:0 30 3 * * ?}")
    public void cleanExpiredDynamicTables() {
        int keepDays = Math.max(retentionDays, 1);
        LocalDate cutoffDate = LocalDate.now().minusDays(keepDays - 1L);
        List<String> allTables = jdbcTemplate.queryForList("SHOW TABLES", String.class);

        int dropCount = 0;
        for (String tableName : allTables) {
            if (!isDynamicTable(tableName)) {
                continue;
            }
            LocalDate tableDate = parseTableDate(tableName);
            if (tableDate == null || !tableDate.isBefore(cutoffDate)) {
                continue;
            }
            jdbcTemplate.execute("DROP TABLE IF EXISTS `" + tableName + "`");
            dropCount++;
            log.info("drop expired dynamic table: {}", tableName);
        }
        log.info("dynamic table retention finish, keepDays={}, cutoffDate={}, dropped={}",
                keepDays, cutoffDate, dropCount);
    }

    private boolean isDynamicTable(String tableName) {
        int index = tableName.lastIndexOf('_');
        if (index <= 0 || index >= tableName.length() - 1) {
            return false;
        }
        String prefix = tableName.substring(0, index);
        String dateSuffix = tableName.substring(index + 1);
        return DYNAMIC_PREFIX_SET.contains(prefix) && dateSuffix.matches("\\d{8}");
    }

    private LocalDate parseTableDate(String tableName) {
        int index = tableName.lastIndexOf('_');
        String dateSuffix = tableName.substring(index + 1);
        try {
            return LocalDate.parse(dateSuffix, DATE_FMT);
        } catch (Exception e) {
            return null;
        }
    }
}
