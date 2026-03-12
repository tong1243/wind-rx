package com.wut.screencommonrx.Util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateParamParseUtil {
    public static String getDateTimeStr(long timestamp) {
        LocalDateTime datetime = new Date(timestamp).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        int month = datetime.getMonthValue();
        int day = datetime.getDayOfMonth();
        return datetime.getYear() +
                (month < 10 ? "0" + month : Integer.toString(month)) +
                (day < 10 ? "0" + day : Integer.toString(day));
    }

    // 日期格式化字符串(yyyyMMdd)转当天结束毫秒级时间戳(对应24:00:00)
    public static long getEndTimestamp(String date) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd")).plusDays(1);
        LocalDateTime startOfNextDay = localDate.atStartOfDay();
        ZonedDateTime zonedDateTime = startOfNextDay.atZone(ZoneId.systemDefault());
        return zonedDateTime.toInstant().toEpochMilli();
    }

}
