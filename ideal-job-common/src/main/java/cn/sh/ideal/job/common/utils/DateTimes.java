package cn.sh.ideal.job.common.utils;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * @author 宋志宗
 * @date 2020/8/30
 */
@SuppressWarnings("unused")
public final class DateTimes {
    private DateTimes() {
    }

    /**
     * 2020-12-12
     */
    public static final String yyyy_MM_dd = "yyyy-MM-dd";
    /**
     * 2020-12-12 19
     */
    public static final String yyyy_MM_dd_HH = "yyyy-MM-dd HH";
    /**
     * 2020-12-12 19:21
     */
    public static final String yyyy_MM_dd_HH_mm = "yyyy-MM-dd HH:mm";
    /**
     * 2020-12-12 19:21:56
     */
    public static final String yyyy_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
    /**
     * 2020-12-12 19:21:56.555
     */
    public static final String yyyy_MM_dd_HH_mm_ss_SSS = "yyyy-MM-dd HH:mm:ss.SSS";
    /**
     * 12-12 19:21:56
     */
    public static final String MM_dd_HH_mm_ss = "MM-dd HH:mm:ss";
    /**
     * 12-12 19
     */
    public static final String MM_dd_HH = "MM-dd HH";
    /**
     * 19:21:56
     */
    public static final String HH_mm_ss = "HH:mm:ss";
    /**
     * 19:21
     */
    public static final String HH_mm = "HH:mm";

    private static final ZoneOffset CHINA_ZONE_OFFSET = ZoneOffset.of("+8");
    private static final Locale CHINA_LOCAL = Locale.SIMPLIFIED_CHINESE;

    @Nonnull
    public static String format(@Nonnull LocalDateTime localDateTime, @Nonnull String pattern) {
        return format(localDateTime, pattern, CHINA_LOCAL);
    }

    @Nonnull
    public static String format(@Nonnull LocalDateTime localDateTime, @Nonnull String pattern, @Nonnull Locale locale) {
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern, locale));
    }

    @Nonnull
    public static LocalDateTime parse(@Nonnull String dateTimeString, @Nonnull String pattern) {
        return parse(dateTimeString, pattern, CHINA_LOCAL);
    }

    @Nonnull
    public static LocalDateTime parse(@Nonnull String dateTimeString, @Nonnull String pattern, @Nonnull Locale locale) {
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(pattern, locale));
    }

    @Nonnull
    public static LocalDateTime parse(long timestamp) {
        return parse(timestamp, CHINA_ZONE_OFFSET);
    }

    @Nonnull
    public static LocalDateTime parse(long timestamp, ZoneOffset zoneOffset) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        return LocalDateTime.ofInstant(instant, zoneOffset);
    }

    public static long getTimestamp(LocalDateTime localDateTime) {
        return getTimestamp(localDateTime, CHINA_ZONE_OFFSET);
    }

    public static long getTimestamp(LocalDateTime localDateTime, ZoneOffset zoneOffset) {
        return localDateTime.toInstant(zoneOffset).toEpochMilli();
    }

    @Nonnull
    public static LocalDateTime now() {
        return now(CHINA_ZONE_OFFSET);
    }

    @Nonnull
    public static LocalDateTime now(@Nonnull ZoneOffset zoneOffset) {
        return LocalDateTime.now(zoneOffset);
    }


}
