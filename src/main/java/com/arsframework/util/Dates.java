package com.arsframework.util;

import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.Calendar;
import java.time.ZoneId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.arsframework.annotation.Min;
import com.arsframework.annotation.Nonnull;
import com.arsframework.annotation.Nonempty;

/**
 * 日期处理工具类
 *
 * @author yongqiang.wu
 */
public abstract class Dates {
    /**
     * 默认时区
     */
    public static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    /**
     * 默认日期格式
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 默认时间格式
     */
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    /**
     * 默认日期时间格式
     */
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期格式数组
     */
    public static final String[] DATE_FORMATS = {DEFAULT_DATE_FORMAT, "yyyy/MM/dd", "yyyyMMdd", "yyyy-MM", "yyyyMM"};

    /**
     * 时间格式数组
     */
    public static final String[] TIME_FORMATS = {DEFAULT_TIME_FORMAT, "HHmmss", "HH:mm", "HHmm"};

    /**
     * 日期时间格式数组
     */
    public static final String[] DATETIME_FORMATS = {DEFAULT_DATETIME_FORMAT,
            "yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy/MM/dd HH:mm", "yyyyMMddHHmmss", "yyyyMMddHHmm"};

    /**
     * 所有日期格式数组
     */
    public static final String[] ALL_DATE_FORMATS = new String[DATE_FORMATS.length + DATETIME_FORMATS.length + TIME_FORMATS.length];

    static {
        // 拷贝日期格式
        System.arraycopy(DATETIME_FORMATS, 0, ALL_DATE_FORMATS, 0, DATETIME_FORMATS.length);
        System.arraycopy(DATE_FORMATS, 0, ALL_DATE_FORMATS, DATETIME_FORMATS.length, DATE_FORMATS.length);
        System.arraycopy(TIME_FORMATS, 0, ALL_DATE_FORMATS, DATETIME_FORMATS.length + DATE_FORMATS.length,
                TIME_FORMATS.length);
    }

    /**
     * 当前线程格式模式/日期格式化对象映射表
     */
    private static final ThreadLocal<Map<String, DateFormat>> dateFormats = ThreadLocal.withInitial(() -> new HashMap<>());

    /**
     * 格式模式/日期时间格式处理对象映射表
     */
    private static final Map<String, DateTimeFormatter> dateTimeFormats = new HashMap<>();

    /**
     * 构建日期格式处理器
     *
     * @param pattern 日期格式转换模式字符串
     * @return 日期格式处理对象
     */
    @Nonempty
    public static DateFormat buildDateFormat(String pattern) {
        Map<String, DateFormat> formats = dateFormats.get();
        DateFormat format = formats.get(pattern);
        if (format == null) {
            format = new SimpleDateFormat(pattern);
            formats.put(pattern, format);
        }
        return format;
    }

    /**
     * 构建日期时间格式处理器
     *
     * @param pattern 日期时间格式转换模式字符串
     * @return 日期时间格式处理器
     */
    @Nonempty
    public static DateTimeFormatter buildDateTimeFormat(String pattern) {
        DateTimeFormatter format = dateTimeFormats.get(pattern);
        if (format == null) {
            synchronized (Dates.class) {
                if ((format = dateTimeFormats.get(pattern)) == null) {
                    format = DateTimeFormatter.ofPattern(pattern);
                    dateTimeFormats.put(pattern, format);
                }
            }
        }
        return format;
    }

    /**
     * 将字符串形式日期时间转换成日期时间对象
     *
     * @param source 日期时间字符串形式
     * @return 日期时间对象
     */
    public static Date parse(String source) {
        return parse(source, ALL_DATE_FORMATS);
    }

    /**
     * 将字符串形式日期时间转换成日期时间对象
     *
     * @param source   日期时间字符串形式
     * @param patterns 格式模式数组
     * @return 日期时间对象
     */
    public static Date parse(String source, @Nonempty String... patterns) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        for (String pattern : patterns) {
            try {
                return buildDateFormat(pattern).parse(source);
            } catch (ParseException e) {
            }
        }
        throw new RuntimeException("Unparseable date: " + source);
    }

    /**
     * 日期对象适配
     *
     * @param date 本地日期对象
     * @return 日期对象
     */
    public static Date adapter(LocalDate date) {
        return date == null ? null : Date.from(date.atStartOfDay().atZone(DEFAULT_ZONE).toInstant());
    }

    /**
     * 日期对象适配
     *
     * @param date 本地日期时间对象
     * @return 日期对象
     */
    public static Date adapter(LocalDateTime date) {
        return date == null ? null : Date.from(date.atZone(DEFAULT_ZONE).toInstant());
    }

    /**
     * 日期对象适配
     *
     * @param date 日期对象
     * @return 本地日期时间对象
     */
    public static LocalDateTime adapter(Date date) {
        return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), DEFAULT_ZONE);
    }


    /**
     * 将日期时间对象转换成字符串形式
     *
     * @param date 日期时间对象
     * @return 日期时间字符串形式
     */
    public static String format(Date date) {
        return format(adapter(date));
    }

    /**
     * 将日期时间对象转换成字符串形式
     *
     * @param date    日期时间对象
     * @param pattern 格式模式
     * @return 日期时间字符串形式
     */
    public static String format(Date date, @Nonempty String pattern) {
        return date == null ? null : buildDateFormat(pattern).format(date);
    }

    /**
     * 将日期时间对象转换成字符串形式
     *
     * @param date 日期时间对象
     * @return 日期时间字符串形式
     */
    public static String format(LocalDate date) {
        return format(date, DEFAULT_DATE_FORMAT);
    }

    /**
     * 将日期时间对象转换成字符串形式
     *
     * @param date    日期时间对象
     * @param pattern 格式模式
     * @return 日期时间字符串形式
     */
    public static String format(LocalDate date, @Nonempty String pattern) {
        return date == null ? null : date.format(buildDateTimeFormat(pattern));
    }

    /**
     * 将日期时间对象转换成字符串形式
     *
     * @param date 日期时间对象
     * @return 日期时间字符串形式
     */
    public static String format(LocalDateTime date) {
        return date == null ? null : date.getHour() == 0 && date.getMinute() == 0 && date.getSecond() == 0 ?
                format(date, DEFAULT_DATE_FORMAT) : format(date, DEFAULT_DATETIME_FORMAT);
    }

    /**
     * 将日期时间对象转换成字符串形式
     *
     * @param date    日期时间对象
     * @param pattern 格式模式
     * @return 日期时间字符串形式
     */
    public static String format(LocalDateTime date, @Nonempty String pattern) {
        return date == null ? null : date.format(buildDateTimeFormat(pattern));
    }

    /**
     * 计算与指定日期相差指定时间量的日期
     *
     * @param date   目标日期时间
     * @param type   时间量类型
     * @param amount 相差时间量
     * @return 结果日期时间
     */
    @Nonnull
    public static Date differ(Date date, int type, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(type, amount);
        return calendar.getTime();
    }

    /**
     * 根据生日获取年龄
     *
     * @param birthday 生日
     * @return 年龄
     */
    @Nonnull
    public static int getAge(Date birthday) {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.setTime(birthday);
        int birthdayYear = calendar.get(Calendar.YEAR);
        int birthdayMonth = calendar.get(Calendar.MONTH) + 1;
        int birthdayDay = calendar.get(Calendar.DAY_OF_MONTH);
        int age = currentYear - birthdayYear;
        if (currentMonth < birthdayMonth || (currentMonth == birthdayMonth && currentDay < birthdayDay)) {
            age--;
        }
        return age;
    }

    /**
     * 获取当前年份
     *
     * @return 年份
     */
    public static int getYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    /**
     * 获取制定日期年份
     *
     * @param date 日期
     * @return 年份
     */
    @Nonnull
    public static int getYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    /**
     * 获取当前月份
     *
     * @return 月份
     */
    public static int getMonth() {
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }

    /**
     * 获取制定日期月份
     *
     * @param date 日期
     * @return 月份
     */
    @Nonnull
    public static int getMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * 获取当前日
     *
     * @return 日
     */
    public static int getDay() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取制定日期日
     *
     * @param date 日期
     * @return 日
     */
    @Nonnull
    public static int getDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取当前小时
     *
     * @return 小时
     */
    public static int getHour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取制定日期小时
     *
     * @param date 日期
     * @return 小时
     */
    @Nonnull
    public static int getHour(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取当前分钟
     *
     * @return 分钟
     */
    public static int getMinute() {
        return Calendar.getInstance().get(Calendar.MINUTE);
    }

    /**
     * 获取制定日期分钟
     *
     * @param date 日期
     * @return 分钟
     */
    @Nonnull
    public static int getMinute(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MINUTE);
    }

    /**
     * 获取当前秒
     *
     * @return 秒
     */
    public static int getSecond() {
        return Calendar.getInstance().get(Calendar.SECOND);
    }

    /**
     * 获取制定日期秒
     *
     * @param date 日期
     * @return 秒
     */
    @Nonnull
    public static int getSecond(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.SECOND);
    }

    /**
     * 获取当前毫秒
     *
     * @return 毫秒
     */
    public static int getMillisecond() {
        return Calendar.getInstance().get(Calendar.MILLISECOND);
    }

    /**
     * 获取制定日期毫秒
     *
     * @param date 日期
     * @return 毫秒
     */
    @Nonnull
    public static int getMillisecond(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MILLISECOND);
    }

    /**
     * 获取当前年份第一天
     *
     * @return 日期
     */
    public static Date getFirstDate() {
        return getFirstDate(getYear());
    }

    /**
     * 获取指定年份第一天
     *
     * @param year 年份
     * @return 日期
     */
    public static Date getFirstDate(@Min(1970) int year) {
        return getFirstDate(year, 1);
    }

    /**
     * 获取指定年份及月份第一天
     *
     * @param year  年份
     * @param month 月份(从1开始)
     * @return 日期
     */
    public static Date getFirstDate(@Min(1970) int year, @Min(1) int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    /**
     * 获取当前年份最后一天
     *
     * @return 日期
     */
    public static Date getLastDate() {
        return getLastDate(getYear());
    }

    /**
     * 获取指定年份最后一天
     *
     * @param year 年份
     * @return 日期
     */
    public static Date getLastDate(@Min(1970) int year) {
        return getLastDate(year, 1);
    }

    /**
     * 获取指定年份及月份最后一天
     *
     * @param year  年份
     * @param month 月份(从1开始)
     * @return 日期
     */
    public static Date getLastDate(@Min(1970) int year, @Min(1) int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    /**
     * 获取本地日期毫秒数
     *
     * @param date 本地日期对象
     * @return 毫秒数
     */
    @Nonnull
    public static long getTime(LocalDate date) {
        return date.atStartOfDay(DEFAULT_ZONE).toInstant().toEpochMilli();
    }

    /**
     * 获取本地日期时间毫秒数
     *
     * @param date 本地日期时间对象
     * @return 毫秒数
     */
    @Nonnull
    public static long getTime(LocalDateTime date) {
        return date.atZone(DEFAULT_ZONE).toInstant().toEpochMilli();
    }
}
