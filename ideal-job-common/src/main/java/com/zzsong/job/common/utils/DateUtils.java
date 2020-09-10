package com.zzsong.job.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 时间格式化工具
 *
 * @author 宋志宗 on 2018/7/16
 */
@SuppressWarnings("unused")
public class DateUtils {
  @SuppressWarnings("SpellCheckingInspection")
  public static final String DEFAULT_ISO_PATTERN = "yyyyMMdd'T'HHmmss'Z'";

  private static final Logger LOGGER = LoggerFactory.getLogger(DateUtils.class);

  private DateUtils() {
  }

  /**
   * 存放不同的日期模板格式的sdf的Map ThreadLocal为每个线程创建一个SimpleDateFormat局部变量,因此不存在线程安全问题
   */
  private static final ConcurrentMap<String, ThreadLocal<SimpleDateFormat>> SDF_MAP
      = new ConcurrentHashMap<>();

  /**
   * 根据模式串从sdfMap中获取一个SimpleDateFormat实例
   *
   * @param pattern 模式串
   * @return SimpleDateFormat实例
   */
  private static SimpleDateFormat getSimpleDateFormat(final String pattern) {
    ThreadLocal<SimpleDateFormat> threadLocal = SDF_MAP
        .computeIfAbsent(pattern, k -> ThreadLocal.withInitial(() -> new SimpleDateFormat(pattern)));
    return threadLocal.get();
  }

  /**
   * Date转换为时间字符串
   *
   * @param date    Date
   * @param pattern 模式串 如<code>yyyy-MM-dd</code>
   * @return String 时间字符串 如<code>2017-01-01</code>
   */
  public static String format(Date date, String pattern) {
    if (date == null || StringUtils.isBlank(pattern)) {
      LOGGER.info("参数校验失败,date或pattern 为空");
      return null;
    }
    return getSimpleDateFormat(pattern).format(date);
  }

  /**
   * 时间字符串转换为Date
   *
   * @param dateString 时间字符串 如<code>2018-06-07</code>
   * @param pattern    模式串 如<code>yyyy-MM-dd</code>
   * @return Date
   */
  public static Date parse(String dateString, String pattern) {
    try {
      if (StringUtils.isBlank(dateString) || StringUtils.isBlank(pattern)) {
        LOGGER.info("参数校验失败,dateString或pattern 为空");
        return null;
      }
      return getSimpleDateFormat(pattern).parse(dateString);
    } catch (ParseException e) {
      LOGGER.debug("e : ", e);
      return null;
    }
  }

  /**
   * 将ISO格式的时间字符串转换成本地时间<br/>
   * ISO格式的时间字符串中包含 T 和 Z 字符,在传入时间模式串时需要将这两个字符上加上单引号<br/>
   * 如 20180607T025920Z 对应的模式串为 yyyyMMdd'T'HHmmss'Z'
   *
   * @param dateString 时间戳 如<code>20180607T025920Z</code>
   * @param pattern    模式串 如<code>yyyyMMdd'T'HHmmss'Z'</code>
   * @return Date
   */
  @SuppressWarnings("SpellCheckingInspection")
  public static Date parseISODate(String dateString, String pattern) {
    if (StringUtils.isBlank(dateString) || StringUtils.isBlank(pattern)) {
      LOGGER.info("参数校验失败,dateString或pattern 为空");
      return null;
    }
    Date date = parse(dateString, pattern);
    if (date == null) {
      return null;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
    int dstOffset = cal.get(Calendar.DST_OFFSET);
    cal.add(Calendar.MILLISECOND, (zoneOffset + dstOffset));
    return cal.getTime();
  }

  /**
   * 将时间戳转换为时间字符串
   *
   * @param timestamp 时间戳 如<code>1483200000000</code>
   * @param pattern   模式串 如<code>yyyy-MM-dd</code>
   * @return String 时间字符串 如<code>2017-01-01</code>
   */
  public static String format(long timestamp, String pattern) {
    return format(new Date(timestamp), pattern);
  }

  /**
   * 时间戳转换为时间字符串
   *
   * @param timestamp 时间戳 如<code>1483200000000</code>
   * @param pattern   模式串 如<code>yyyy-MM-dd</code>
   * @return String 时间字符串 如<code>2017-01-01</code>
   * @throws NumberFormatException NumberFormatException
   */
  public static String format(String timestamp, String pattern) throws NumberFormatException {
    if (StringUtils.isBlank(timestamp) || StringUtils.isBlank(pattern)) {
      LOGGER.info("参数校验失败,timestamp或pattern 为空");
      return null;
    }
    long time = Long.parseLong(timestamp);
    return format(time, pattern);
  }

  /**
   * 将时间字符串转换为时间戳
   *
   * @param dateString 时间字符串 如<code>2017-01-01</code>
   * @param pattern    模式串 如<code>yyyy-MM-dd</code>
   * @return 时间戳 如<code>1483200000000</code>
   */
  public static Long getTimestamp(String dateString, String pattern) {
    Date parse = parse(dateString, pattern);
    return parse == null ? null : parse.getTime();
  }

  /**
   * 获取两个时间戳之间的时间差
   *
   * @param start 起始时间戳
   * @param end   终止时间戳
   * @return 时分表示, 如<code>1小时10分钟</code>
   */
  public static String getTimeDifference(Long start, Long end) {
    if (start == null || end == null) {
      return null;
    }
    long time = end - start;
    long h = time / (1000 * 60 * 60);
    long m = time % (1000 * 60 * 60) / (1000 * 60);
    if (h == 0) {
      return m + "分钟";
    }
    return h + "小时" + m + "分钟";
  }

  /**
   * 获取两个Date之间的时间差
   *
   * @param startDate 起始时间
   * @param endDate   终止时间
   * @return 时分表示, 如<code>1小时10分钟</code>
   */
  public static String getTimeDifference(Date startDate, Date endDate) {
    if (startDate == null || endDate == null) {
      return null;
    }
    long end = endDate.getTime();
    long start = startDate.getTime();
    return getTimeDifference(start, end);
  }

  /**
   * 计算两日期之间的间隔
   *
   * @param beginDate 起始时间
   * @param endDate   结束时间
   * @return 间隔天数
   */
  public static int getTimeDistance(Date beginDate, Date endDate) {
    Calendar beginCalendar = Calendar.getInstance();
    beginCalendar.setTime(beginDate);
    Calendar endCalendar = Calendar.getInstance();
    endCalendar.setTime(endDate);
    long beginTime = beginCalendar.getTime().getTime();
    long endTime = endCalendar.getTime().getTime();
    //先算出两时间的毫秒数之差大于一天的天数
    int betweenDays = (int) ((endTime - beginTime) / (1000 * 60 * 60 * 24));
    //使endCalendar减去这些天数，将问题转换为两时间的毫秒数之差不足一天的情况
    endCalendar.add(Calendar.DAY_OF_MONTH, -betweenDays);
    //再使endCalendar减去1天
    endCalendar.add(Calendar.DAY_OF_MONTH, -1);
    //比较两日期的DAY_OF_MONTH是否相等
    if (beginCalendar.get(Calendar.DAY_OF_MONTH) == endCalendar.get(Calendar.DAY_OF_MONTH)) {
      //相等说明确实跨天了
      return betweenDays + 1;
    } else {
      //不相等说明确实未跨天
      return betweenDays;
    }
  }

  /**
   * 获取两个日期间隔几个月
   */
  public static int compute(Date minDate, Date maxDate) {
    Calendar minCalendar = Calendar.getInstance();
    minCalendar.setTime(minDate);

    Calendar maxCalender = Calendar.getInstance();
    maxCalender.setTime(maxDate);

    LocalDate minLocalDate = LocalDate.of(minCalendar.get(Calendar.YEAR), minCalendar.get(Calendar.MONTH) + 1, minCalendar.get(Calendar.DAY_OF_MONTH));
    LocalDate maxLocalDate = LocalDate.of(maxCalender.get(Calendar.YEAR), maxCalender.get(Calendar.MONTH) + 1, maxCalender.get(Calendar.DAY_OF_MONTH));

    Period period = Period.between(minLocalDate, maxLocalDate);

    int months = period.getMonths() + period.getYears() * 12;

    if (maxCalender.get(Calendar.DAY_OF_MONTH) - minCalendar.get(Calendar.DAY_OF_MONTH) > 0) {
      months++;
    }

    return months;
  }


  /**
   * 小时的开始时间
   */
  public static Date hourBegin() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }


  /**
   * 小时的结束时间
   */
  public static Date hourEnd() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.SECOND, 59);
    calendar.set(Calendar.MILLISECOND, 999);
    return calendar.getTime();
  }


  /**
   * 获取今天开始时间
   */
  public static Date todayBegin() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }

  /**
   * 获取今天结束时间
   */
  public static Date todayEnd() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 23);
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.SECOND, 59);
    calendar.set(Calendar.MILLISECOND, 999);
    return calendar.getTime();
  }


  /**
   * 每个月的开始时间
   */
  public static Date monthBegin() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }

  /*获取今天周几*/
  public static int getWeek() {
    Date today = new Date();
    Calendar c = Calendar.getInstance();
    c.setTime(today);
    int weekday = c.get(Calendar.DAY_OF_WEEK);
    return weekday - 1;
  }
}
