package com.arsframework.util;

import java.util.Map;
import java.util.Date;
import java.util.Arrays;
import java.util.Collection;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.arsframework.annotation.Nonnull;

/**
 * 断言工具类
 *
 * @author yongqiang.wu
 */
public abstract class Asserts {

    /**
     * 对象非Null验证
     *
     * @param object 目标对象
     * @throws IllegalStateException 数据状态异常
     */
    public static void nonnull(Object object) throws IllegalStateException {
        if (object == null) {
            throw new IllegalStateException("Object must not be null");
        }
    }

    /**
     * 对象非空验证
     *
     * @param object 目标对象
     * @throws IllegalStateException 数据状态异常
     */
    public static void nonempty(Object object) throws IllegalStateException {
        nonempty(object, true);
    }

    /**
     * 对象非空验证
     *
     * @param object 目标对象
     * @param blank  字符串是否允许空白
     * @throws IllegalStateException 数据状态异常
     */
    public static void nonempty(Object object, boolean blank) throws IllegalStateException {
        if (Objects.isEmpty(object) || (!blank && object instanceof CharSequence && Strings.isBlank((CharSequence) object))) {
            throw new IllegalStateException("Object must not be empty");
        }
    }

    /**
     * 对象格式验证，适用于数字、字符串类型对象
     *
     * @param object 目标对象
     * @param regex  正则表达式
     * @throws IllegalStateException 数据状态异常
     */
    public static void format(Object object, @Nonnull String regex) throws IllegalStateException {
        if (object == null || ((object instanceof Number || object instanceof CharSequence) && !object.toString().matches(regex))) {
            throw new IllegalStateException(String.format("Object must be matched for '%s'", regex));
        }
    }

    /**
     * 对象固定值验证，适用于数字、枚举、日期、字符序列、数组、字典、集合、列表类型对象
     *
     * @param object 目标对象
     * @param value  值
     * @throws IllegalStateException 数据状态异常
     */
    public static void eq(Object object, @Nonnull Number value) throws IllegalStateException {
        if (object == null
                || (object instanceof Number && ((Number) object).doubleValue() != value.doubleValue())
                || (object instanceof Enum && ((Enum) object).ordinal() != value.longValue())
                || (object instanceof Date && ((Date) object).getTime() != value.longValue())
                || (object instanceof LocalDate && Dates.getTime((LocalDate) object) != value.longValue())
                || (object instanceof LocalDateTime && Dates.getTime((LocalDateTime) object) != value.longValue())
                || (object instanceof CharSequence && ((CharSequence) object).length() != value.longValue())
                || (object instanceof Map && ((Map) object).size() != value.longValue())
                || (object instanceof Collection && ((Collection) object).size() != value.longValue())
                || (object instanceof Object[] && ((Object[]) object).length != value.longValue())
                || (object instanceof char[] && ((char[]) object).length != value.longValue())
                || (object instanceof byte[] && ((byte[]) object).length != value.longValue())
                || (object instanceof short[] && ((short[]) object).length != value.longValue())
                || (object instanceof float[] && ((float[]) object).length != value.longValue())
                || (object instanceof int[] && ((int[]) object).length != value.longValue())
                || (object instanceof long[] && ((long[]) object).length != value.longValue())
                || (object instanceof double[] && ((double[]) object).length != value.longValue())
                || (object instanceof boolean[] && ((boolean[]) object).length != value.longValue())) {
            throw new IllegalStateException(String.format("Object value must be %s", value.toString()));
        }
    }

    /**
     * 对象真/假值验证，适用于Boolean类型对象
     *
     * @param object 目标对象
     * @param value  值
     * @throws IllegalStateException 数据状态异常
     */
    public static void eq(Boolean object, boolean value) throws IllegalStateException {
        if (object == null || object != value) {
            throw new IllegalStateException(String.format("Object value must be %b", value));
        }
    }

    /**
     * 对象非固定值验证，适用于数字、枚举、日期、字符序列、数组、字典、集合、列表类型对象
     *
     * @param object 目标对象
     * @param value  值
     * @throws IllegalStateException 数据状态异常
     */
    public static void ne(Object object, @Nonnull Number value) throws IllegalStateException {
        if (object == null
                || (object instanceof Number && ((Number) object).doubleValue() == value.doubleValue())
                || (object instanceof Enum && ((Enum) object).ordinal() == value.longValue())
                || (object instanceof Date && ((Date) object).getTime() == value.longValue())
                || (object instanceof LocalDate && Dates.getTime((LocalDate) object) == value.longValue())
                || (object instanceof LocalDateTime && Dates.getTime((LocalDateTime) object) == value.longValue())
                || (object instanceof CharSequence && ((CharSequence) object).length() == value.longValue())
                || (object instanceof Map && ((Map) object).size() == value.longValue())
                || (object instanceof Collection && ((Collection) object).size() == value.longValue())
                || (object instanceof Object[] && ((Object[]) object).length == value.longValue())
                || (object instanceof char[] && ((char[]) object).length == value.longValue())
                || (object instanceof byte[] && ((byte[]) object).length == value.longValue())
                || (object instanceof short[] && ((short[]) object).length == value.longValue())
                || (object instanceof float[] && ((float[]) object).length == value.longValue())
                || (object instanceof int[] && ((int[]) object).length == value.longValue())
                || (object instanceof long[] && ((long[]) object).length == value.longValue())
                || (object instanceof double[] && ((double[]) object).length == value.longValue())
                || (object instanceof boolean[] && ((boolean[]) object).length == value.longValue())) {
            throw new IllegalStateException(String.format("Object value must not be %s", value.toString()));
        }
    }

    /**
     * 对象非真/假值验证，适用于Boolean类型对象
     *
     * @param object 目标对象
     * @param value  值
     * @throws IllegalStateException 数据状态异常
     */
    public static void ne(Boolean object, boolean value) throws IllegalStateException {
        if (object == null || object == value) {
            throw new IllegalStateException(String.format("Object value must not be %b", value));
        }
    }

    /**
     * 对象最小值验证，适用于数字、枚举、日期、字符序列、数组、字典、集合、列表类型对象
     *
     * @param object 目标对象
     * @param value  最大值
     * @throws IllegalStateException 数据状态异常
     */
    public static void min(Object object, @Nonnull Number value) throws IllegalStateException {
        if (object == null
                || (object instanceof Number && ((Number) object).doubleValue() < value.doubleValue())
                || (object instanceof Enum && ((Enum) object).ordinal() < value.longValue())
                || (object instanceof Date && ((Date) object).getTime() < value.longValue())
                || (object instanceof LocalDate && Dates.getTime((LocalDate) object) < value.longValue())
                || (object instanceof LocalDateTime && Dates.getTime((LocalDateTime) object) < value.longValue())
                || (object instanceof CharSequence && ((CharSequence) object).length() < value.longValue())
                || (object instanceof Map && ((Map) object).size() < value.longValue())
                || (object instanceof Collection && ((Collection) object).size() < value.longValue())
                || (object instanceof Object[] && ((Object[]) object).length < value.longValue())
                || (object instanceof char[] && ((char[]) object).length < value.longValue())
                || (object instanceof byte[] && ((byte[]) object).length < value.longValue())
                || (object instanceof short[] && ((short[]) object).length < value.longValue())
                || (object instanceof float[] && ((float[]) object).length < value.longValue())
                || (object instanceof int[] && ((int[]) object).length < value.longValue())
                || (object instanceof long[] && ((long[]) object).length < value.longValue())
                || (object instanceof double[] && ((double[]) object).length < value.longValue())
                || (object instanceof boolean[] && ((boolean[]) object).length < value.longValue())) {
            throw new IllegalStateException(String.format("Object value must be greater than or equal to %s", value.toString()));
        }
    }

    /**
     * 对象最大值验证，适用于数字、枚举、日期、字符序列、数组、字典、集合、列表类型对象
     *
     * @param object 目标对象
     * @param value  最大值
     * @throws IllegalStateException 数据状态异常
     */
    public static void max(Object object, @Nonnull Number value) throws IllegalStateException {
        if (object == null
                || (object instanceof Number && ((Number) object).doubleValue() > value.doubleValue())
                || (object instanceof Enum && ((Enum) object).ordinal() > value.longValue())
                || (object instanceof Date && ((Date) object).getTime() > value.longValue())
                || (object instanceof LocalDate && Dates.getTime((LocalDate) object) > value.longValue())
                || (object instanceof LocalDateTime && Dates.getTime((LocalDateTime) object) > value.longValue())
                || (object instanceof CharSequence && ((CharSequence) object).length() > value.longValue())
                || (object instanceof Map && ((Map) object).size() > value.longValue())
                || (object instanceof Collection && ((Collection) object).size() > value.longValue())
                || (object instanceof Object[] && ((Object[]) object).length > value.longValue())
                || (object instanceof char[] && ((char[]) object).length > value.longValue())
                || (object instanceof byte[] && ((byte[]) object).length > value.longValue())
                || (object instanceof short[] && ((short[]) object).length > value.longValue())
                || (object instanceof float[] && ((float[]) object).length > value.longValue())
                || (object instanceof int[] && ((int[]) object).length > value.longValue())
                || (object instanceof long[] && ((long[]) object).length > value.longValue())
                || (object instanceof double[] && ((double[]) object).length > value.longValue())
                || (object instanceof boolean[] && ((boolean[]) object).length > value.longValue())) {
            throw new IllegalStateException(String.format("Object value must be less than or equal to %s", value.toString()));
        }
    }

    /**
     * 对象值范围验证，适用于数字、枚举、日期、字符序列、数组、字典、集合、列表类型对象
     *
     * @param object 目标对象
     * @param min    最小值
     * @param max    最大值
     * @throws IllegalStateException 数据状态异常
     */
    public static void size(Object object, @Nonnull Number min, @Nonnull Number max) throws IllegalStateException {
        max(min, max);
        if (object == null
                || (object instanceof Number
                && (((Number) object).doubleValue() < min.doubleValue() || ((Number) object).doubleValue() > max.doubleValue()))
                || (object instanceof Enum && (((Enum) object).ordinal() < min.longValue() || ((Enum) object).ordinal() > max.longValue()))
                || (object instanceof Date && (((Date) object).getTime() < min.longValue() || ((Date) object).getTime() > max.longValue()))
                || (object instanceof LocalDate
                && (Dates.getTime((LocalDate) object) < min.longValue() || Dates.getTime((LocalDate) object) > max.longValue()))
                || (object instanceof LocalDateTime
                && (Dates.getTime((LocalDateTime) object) < min.longValue() || Dates.getTime((LocalDateTime) object) > max.longValue()))
                || (object instanceof CharSequence
                && (((CharSequence) object).length() < min.longValue() || ((CharSequence) object).length() > max.longValue()))
                || (object instanceof Map && (((Map) object).size() < min.longValue() || ((Map) object).size() > max.longValue()))
                || (object instanceof Collection
                && (((Collection) object).size() < min.longValue() || ((Collection) object).size() > max.longValue()))
                || (object instanceof Object[]
                && (((Object[]) object).length < min.longValue() || ((Object[]) object).length > max.longValue()))
                || (object instanceof char[] && (((char[]) object).length < min.longValue() || ((char[]) object).length > max.longValue()))
                || (object instanceof byte[] && (((byte[]) object).length < min.longValue() || ((byte[]) object).length > max.longValue()))
                || (object instanceof short[]
                && (((short[]) object).length < min.longValue() || ((short[]) object).length > max.longValue()))
                || (object instanceof float[]
                && (((float[]) object).length < min.longValue() || ((float[]) object).length > max.longValue()))
                || (object instanceof int[] && (((int[]) object).length < min.longValue() || ((int[]) object).length > max.longValue()))
                || (object instanceof long[] && (((long[]) object).length < min.longValue() || ((long[]) object).length > max.longValue()))
                || (object instanceof double[]
                && (((double[]) object).length < min.longValue() || ((double[]) object).length > max.longValue()))
                || (object instanceof boolean[]
                && (((boolean[]) object).length < min.longValue() || ((boolean[]) object).length > max.longValue()))) {
            throw new IllegalStateException(String.format("Object value must be in interval [%s, %s]", min.toString(), max.toString()));
        }
    }

    /**
     * 对象值选项验证，适用于数字、枚举、日期、字符序列、数组、字典、集合、列表类型对象
     *
     * @param object 目标对象
     * @param values 选项数组
     * @throws IllegalStateException 数据状态异常
     */
    public static void option(Object object, @Nonnull Number... values) throws IllegalStateException {
        Arrays.sort(values);
        if (object == null
                || (object instanceof Number && Arrays.binarySearch(values, ((Number) object).doubleValue()) < 0)
                || (object instanceof Enum && Arrays.binarySearch(values, ((Enum) object).ordinal()) < 0)
                || (object instanceof Date && Arrays.binarySearch(values, ((Date) object).getTime()) < 0)
                || (object instanceof LocalDate && Arrays.binarySearch(values, Dates.getTime((LocalDate) object)) < 0)
                || (object instanceof LocalDateTime && Arrays.binarySearch(values, Dates.getTime((LocalDateTime) object)) < 0)
                || (object instanceof CharSequence && Arrays.binarySearch(values, ((CharSequence) object).length()) < 0)
                || (object instanceof Map && Arrays.binarySearch(values, ((Map) object).size()) < 0)
                || (object instanceof Collection && Arrays.binarySearch(values, ((Collection) object).size()) < 0)
                || (object instanceof Object[] && Arrays.binarySearch(values, ((Object[]) object).length) < 0)
                || (object instanceof char[] && Arrays.binarySearch(values, ((char[]) object).length) < 0)
                || (object instanceof byte[] && Arrays.binarySearch(values, ((byte[]) object).length) < 0)
                || (object instanceof short[] && Arrays.binarySearch(values, ((short[]) object).length) < 0)
                || (object instanceof float[] && Arrays.binarySearch(values, ((float[]) object).length) < 0)
                || (object instanceof int[] && Arrays.binarySearch(values, ((int[]) object).length) < 0)
                || (object instanceof long[] && Arrays.binarySearch(values, ((long[]) object).length) < 0)
                || (object instanceof double[] && Arrays.binarySearch(values, ((double[]) object).length) < 0)
                || (object instanceof boolean[] && Arrays.binarySearch(values, ((boolean[]) object).length) < 0)) {
            throw new IllegalStateException(String.format("Object must be in option %s", Arrays.toString(values)));
        }
    }

    /**
     * 表达式断言
     *
     * @param expression 表达式逻辑值
     * @param message    异常消息
     * @throws IllegalStateException 数据状态异常
     */
    public static void expression(boolean expression, @Nonnull String message) throws IllegalStateException {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * URL格式验证
     *
     * @param object 目标对象
     * @throws IllegalStateException 数据状态异常
     */
    public static void url(Object object) throws IllegalStateException {
        if (object == null || !(object instanceof CharSequence && Strings.isUrl((CharSequence) object))) {
            throw new IllegalStateException("Object mismatch url format");
        }
    }

    /**
     * Email格式验证
     *
     * @param object 目标对象
     * @throws IllegalStateException 数据状态异常
     */
    public static void email(Object object) throws IllegalStateException {
        if (object == null || !(object instanceof CharSequence && Strings.isEmail((CharSequence) object))) {
            throw new IllegalStateException("Object mismatch email format");
        }
    }

    /**
     * 对象数字验证
     *
     * @param object 目标对象
     * @throws IllegalStateException 数据状态异常
     */
    public static void number(Object object) throws IllegalStateException {
        if (object == null || !(object instanceof Number || (object instanceof CharSequence && Strings.isNumber((CharSequence) object)))) {
            throw new IllegalStateException("Object must be number");
        }
    }

    /**
     * 对象字母验证
     *
     * @param object 目标对象
     * @throws IllegalStateException 数据状态异常
     */
    public static void letter(Object object) throws IllegalStateException {
        if (object == null || !(object instanceof CharSequence && Strings.isLetter((CharSequence) object))) {
            throw new IllegalStateException("Object must be letter");
        }
    }
}
