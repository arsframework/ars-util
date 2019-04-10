package com.arsframework.util;

import java.util.Map;
import java.util.Date;
import java.util.Arrays;
import java.util.Collection;

import com.arsframework.annotation.Nonnull;
import com.arsframework.annotation.Nonempty;

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
     * @throws IllegalStateException 验证异常
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
     * @throws IllegalStateException 验证异常
     */
    public static void nonempty(Object object) throws IllegalStateException {
        if (Objects.isEmpty(object)) {
            throw new IllegalStateException("Object must not be empty");
        }
    }

    /**
     * 对象格式验证，适用于数字、字符串类型对象
     *
     * @param object 目标对象
     * @param regex  正则表达式
     * @throws IllegalStateException 验证异常
     */
    public static void format(Object object, @Nonnull String regex) throws IllegalStateException {
        if ((object instanceof Number || object instanceof CharSequence) && !object.toString().matches(regex)) {
            throw new IllegalStateException(String.format("Object must be matched for '%s'", regex));
        }
    }

    /**
     * 对象最小值验证，适用于数字、枚举、日期、字符序列、数组、字典、集合、列表类型对象
     *
     * @param object 目标对象
     * @param value  最大值
     * @throws IllegalStateException 验证异常
     */
    public static void min(Object object, long value) throws IllegalStateException {
        if ((object instanceof Number && ((Number) object).longValue() < value)
                || (object instanceof Enum && ((Enum) object).ordinal() < value)
                || (object instanceof Date && ((Date) object).getTime() < value)
                || (object instanceof CharSequence && ((CharSequence) object).length() < value)
                || (object instanceof Map && ((Map) object).size() < value)
                || (object instanceof Collection && ((Collection) object).size() < value)
                || (object instanceof Object[] && ((Object[]) object).length < value)
                || (object instanceof char[] && ((char[]) object).length < value)
                || (object instanceof byte[] && ((byte[]) object).length < value)
                || (object instanceof short[] && ((short[]) object).length < value)
                || (object instanceof float[] && ((float[]) object).length < value)
                || (object instanceof int[] && ((int[]) object).length < value)
                || (object instanceof long[] && ((long[]) object).length < value)
                || (object instanceof double[] && ((double[]) object).length < value)
                || (object instanceof boolean[] && ((boolean[]) object).length < value)) {
            throw new IllegalStateException(String.format("Object size must be greater than or equal to %d", value));
        }
    }

    /**
     * 对象最大值验证，适用于数字、枚举、日期、字符序列、数组、字典、集合、列表类型对象
     *
     * @param object 目标对象
     * @param value  最大值
     * @throws IllegalStateException 验证异常
     */
    public static void max(Object object, long value) throws IllegalStateException {
        if ((object instanceof Number && ((Number) object).longValue() > value)
                || (object instanceof Enum && ((Enum) object).ordinal() > value)
                || (object instanceof Date && ((Date) object).getTime() > value)
                || (object instanceof CharSequence && ((CharSequence) object).length() > value)
                || (object instanceof Map && ((Map) object).size() > value)
                || (object instanceof Collection && ((Collection) object).size() > value)
                || (object instanceof Object[] && ((Object[]) object).length > value)
                || (object instanceof char[] && ((char[]) object).length > value)
                || (object instanceof byte[] && ((byte[]) object).length > value)
                || (object instanceof short[] && ((short[]) object).length > value)
                || (object instanceof float[] && ((float[]) object).length > value)
                || (object instanceof int[] && ((int[]) object).length > value)
                || (object instanceof long[] && ((long[]) object).length > value)
                || (object instanceof double[] && ((double[]) object).length > value)
                || (object instanceof boolean[] && ((boolean[]) object).length > value)) {
            throw new IllegalStateException(String.format("Object size must be less than or equal to %d", value));
        }
    }

    /**
     * 对象值范围验证，适用于数字、枚举、日期、字符序列、数组、字典、集合、列表类型对象
     *
     * @param object 目标对象
     * @param min    最小值
     * @param max    最大值
     * @throws IllegalStateException 验证异常
     */
    public static void size(Object object, long min, long max) throws IllegalStateException {
        if ((object instanceof Number && (((Number) object).longValue() < min || ((Number) object).longValue() > max))
                || (object instanceof Enum && (((Enum) object).ordinal() < min || ((Enum) object).ordinal() > max))
                || (object instanceof Date && (((Date) object).getTime() < min || ((Date) object).getTime() > max))
                || (object instanceof CharSequence && (((CharSequence) object).length() < min || ((CharSequence) object).length() > max))
                || (object instanceof Map && (((Map) object).size() < min || ((Map) object).size() > max))
                || (object instanceof Collection && (((Collection) object).size() < min || ((Collection) object).size() > max))
                || (object instanceof Object[] && (((Object[]) object).length < min || ((Object[]) object).length > max))
                || (object instanceof char[] && (((char[]) object).length < min || ((char[]) object).length > max))
                || (object instanceof byte[] && (((byte[]) object).length < min || ((byte[]) object).length > max))
                || (object instanceof short[] && (((short[]) object).length < min || ((short[]) object).length > max))
                || (object instanceof float[] && (((float[]) object).length < min || ((float[]) object).length > max))
                || (object instanceof int[] && (((int[]) object).length < min || ((int[]) object).length > max))
                || (object instanceof long[] && (((long[]) object).length < min || ((long[]) object).length > max))
                || (object instanceof double[] && (((double[]) object).length < min || ((double[]) object).length > max))
                || (object instanceof boolean[] && (((boolean[]) object).length < min || ((boolean[]) object).length > max))) {
            throw new IllegalStateException(String.format("Object size must be in interval [%d, %d]", min, max));
        }
    }

    /**
     * 对象值选项验证，适用于数字、枚举、日期、字符序列、数组、字典、集合、列表类型对象
     *
     * @param object 目标对象
     * @param values 选项数组
     * @throws IllegalStateException 验证异常
     */
    public static void option(Object object, @Nonempty long... values) throws IllegalStateException {
        Arrays.sort(values);
        if ((object instanceof Number && Arrays.binarySearch(values, ((Number) object).longValue()) < 0)
                || (object instanceof Enum && Arrays.binarySearch(values, ((Enum) object).ordinal()) < 0)
                || (object instanceof Date && Arrays.binarySearch(values, ((Date) object).getTime()) < 0)
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
}
