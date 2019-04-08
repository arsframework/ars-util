package com.arsframework.util;

import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.arsframework.annotation.Min;
import com.arsframework.annotation.Nonnull;
import com.arsframework.annotation.Nonempty;

/**
 * 日期处理工具类
 *
 * @author yongqiang.wu
 * @version 2019-03-22 09:38
 */
public abstract class Dates {
    /**
     * 日期格式模式
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 日期时间格式模式
     */
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期时间毫秒格式
     */
    public static final String DATETIME_NANO_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 当前线程格式模式/日期格式化对象映射表
     */
    private static final ThreadLocal<Map<String, DateFormat>> dateFormats = ThreadLocal.withInitial(() -> new HashMap<>());

    /**
     * 当前线程数字格式化对象
     */
    private static final ThreadLocal<DecimalFormat> decimalFormat = ThreadLocal.withInitial(() -> new DecimalFormat("0.##"));

    /**
     * 根据日期格式转换模式获取日期格式转换处理对象
     *
     * @param pattern 日期格式转换模式字符串
     * @return 日期格式化处理对象
     */
    @Nonempty
    public static DateFormat getDateFormat(String pattern) {
        Map<String, DateFormat> formats = dateFormats.get();
        DateFormat format = formats.get(pattern);
        if (format == null) {
            format = new SimpleDateFormat(pattern);
            formats.put(pattern, format);
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
        return parse(source, DATE_PATTERN);
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
                return getDateFormat(pattern).parse(source);
            } catch (ParseException e) {
            }
        }
        throw new RuntimeException("Unparseable date:" + source);
    }

    /**
     * 将日期时间对象转换成字符串形式
     *
     * @param date 日期时间对象
     * @return 日期时间字符串形式
     */
    public static String format(Date date) {
        return format(date, DATE_PATTERN);
    }

    /**
     * 将日期时间对象转换成字符串形式
     *
     * @param date    日期时间对象
     * @param pattern 格式模式
     * @return 日期时间字符串形式
     */
    public static String format(Date date, @Nonempty String pattern) {
        return date == null ? null : getDateFormat(pattern).format(date);
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
     * 获取制定年份第一天
     *
     * @param year 年份
     * @return 日期
     */
    public static Date getFirstDate(@Min(1970) int year) {
        return parse(new StringBuilder().append(year).append("-01-").append("01").toString());
    }

    /**
     * 将时间长度毫秒数转换成带单位的时间表示（d:天、h:时、m:分、s:秒、ms:毫秒）
     *
     * @param time 时间长度毫秒数
     * @return 带单位的时间表示
     */
    public static String toUnitTime(@Min(0) long time) {
        if (time == 0) {
            return "0ms";
        }
        StringBuilder buffer = new StringBuilder();
        if (time >= 86400000) {
            buffer.append(decimalFormat.get().format(time / 86400000d)).append('d');
        } else if (time >= 3600000) {
            buffer.append(decimalFormat.get().format(time / 3600000d)).append('h');
        } else if (time >= 60000) {
            buffer.append(decimalFormat.get().format(time / 60000d)).append('m');
        } else if (time >= 1000) {
            buffer.append(decimalFormat.get().format(time / 1000d)).append('s');
        } else {
            buffer.append(time).append("ms");
        }
        return buffer.toString();
    }
}
