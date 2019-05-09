package com.arsframework.util;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.lang.reflect.Field;

import com.arsframework.annotation.Lt;
import com.arsframework.annotation.Min;
import com.arsframework.annotation.Nonnull;

/**
 * 随机数处理工具类
 *
 * @author yongqiang.wu
 */
public abstract class Randoms {
    /**
     * 默认随机数下钻深度
     */
    public static final int DEFAULT_RANDOM_DEPTH = 2;

    /**
     * 随机数生成接口
     */
    public interface Generator {
        /**
         * 生成随机数
         *
         * @param field 字段对象
         * @param index 字段序数（从0开始）
         * @param level 当前对象层级（从1开始）
         * @return 随机数
         */
        Object generate(Field field, int index, int level);
    }

    /**
     * 构建随机处理对象
     *
     * @return 随机处理对象
     */
    public static Random buildRandom() {
        return ThreadLocalRandom.current();
    }

    /**
     * 随机生成枚举项
     *
     * @param <T>  枚举类型
     * @param type 枚举类型
     * @return 枚举项
     */
    @Nonnull
    public static <T extends Enum<?>> T randomEnum(Class<T> type) {
        try {
            Object[] values = (Object[]) type.getMethod("values").invoke(type);
            return values.length == 0 ? null : (T) values[buildRandom().nextInt(values.length)];
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 随机生成日期（以当前年份第一天为最小日期，当前日期为最大日期）
     *
     * @return 日期
     */
    public static Date randomDate() {
        return randomDate(Dates.getFirstDate(), new Date());
    }

    /**
     * 随机生成日期
     *
     * @param min 最小日期
     * @param max 最大日期
     * @return 日期
     */
    @Nonnull
    public static Date randomDate(@Lt("max") Date min, Date max) {
        long start = min.getTime();
        long time = max.getTime() - start; // 相差毫秒数
        if (time <= 1000) { // 相差1秒内
            return new Date(start + buildRandom().nextInt((int) time));
        }
        return new Date(start + buildRandom().nextInt((int) (time / 1000)) * 1000);
    }

    /**
     * 随机生成数字
     *
     * @return 数字
     */
    public static int randomInteger() {
        return randomInteger(0, 10);
    }

    /**
     * 随机生成数字
     *
     * @param min 最小值
     * @param max 最大值
     * @return 数字
     */
    public static int randomInteger(@Lt("max") int min, int max) {
        return min + buildRandom().nextInt(max - min);
    }

    /**
     * 随机生成字符串（默认长度4）
     *
     * @return 字符串
     */
    public static String randomString() {
        return randomString(4);
    }

    /**
     * 随机生成字符串
     *
     * @param length 字符串长度
     * @return 字符串
     */
    public static String randomString(int length) {
        return randomString(Strings.CHARS, length);
    }

    /**
     * 随机生成字符串
     *
     * @param chars 随机字符数组
     * @return 字符串
     */
    public static String randomString(Character[] chars) {
        return randomString(chars, 4);
    }

    /**
     * 随机生成字符串
     *
     * @param chars  随机字符数组
     * @param length 字符串长度
     * @return 字符串
     */
    @Nonnull
    public static String randomString(Character[] chars, @Min(1) int length) {
        Random random = buildRandom();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < length; i++) {
            buffer.append(chars[random.nextInt(chars.length)]);
        }
        return buffer.toString();
    }

    /**
     * 随机生成字符
     *
     * @return 字符
     */
    public static Character randomCharacter() {
        return randomCharacter(Strings.CHARS);
    }

    /**
     * 随机生成字符
     *
     * @param chars 随机字符数组
     * @return 字符
     */
    @Nonnull
    public static Character randomCharacter(Character[] chars) {
        return chars[buildRandom().nextInt(chars.length)];
    }

    /**
     * 随机生成真假值
     *
     * @return 真假值
     */
    public static boolean randomBoolean() {
        return buildRandom().nextBoolean();
    }

    /**
     * 随机生成对象实例
     *
     * @param <T>  对象类型
     * @param type 对象类型
     * @return 对象实例
     */
    public static <T> T randomObject(Class<T> type) {
        return randomObject(type, null);
    }

    /**
     * 随机生成对象实例
     *
     * @param <T>   对象类型
     * @param type  对象类型
     * @param depth 对象下钻深度
     * @return 对象实例
     */
    public static <T> T randomObject(Class<T> type, int depth) {
        return randomObject(type, depth, null);
    }

    /**
     * 随机生成对象实例
     *
     * @param <T>       对象类型
     * @param type      对象类型
     * @param generator 随机数生成器
     * @return 对象实例
     */
    public static <T> T randomObject(Class<T> type, Generator generator) {
        return randomObject(type, DEFAULT_RANDOM_DEPTH, generator);
    }

    /**
     * 随机生成对象实例
     *
     * @param <T>       对象类型
     * @param type      对象类型
     * @param depth     对象下钻深度
     * @param generator 随机数生成器
     * @return 对象实例
     */
    public static <T> T randomObject(Class<T> type, int depth, Generator generator) {
        return randomObject(type, depth, 0, generator);
    }

    /**
     * 随机生成对象实例
     *
     * @param <T>       对象类型
     * @param type      对象类型
     * @param depth     对象下钻深度
     * @param level     当前对象层级
     * @param generator 随机数生成器
     * @return 对象实例
     */
    private static <T> T randomObject(@Nonnull Class<T> type, @Min(1) int depth, @Min(0) int level, Generator generator) {
        if (level > depth) {
            return null;
        }
        if (Enum.class.isAssignableFrom(type)) {
            return (T) randomEnum((Class<Enum<?>>) type);
        } else if (Date.class.isAssignableFrom(type)) {
            return (T) randomDate();
        } else if (LocalDate.class.isAssignableFrom(type)) {
            return (T) Dates.adapter(randomDate()).toLocalDate();
        } else if (LocalDateTime.class.isAssignableFrom(type)) {
            return (T) Dates.adapter(randomDate());
        } else if (type == byte.class || type == Byte.class) {
            return (T) Byte.valueOf((byte) randomInteger());
        } else if (type == char.class || type == Character.class) {
            return (T) randomCharacter();
        } else if (type == short.class || type == Short.class) {
            return (T) Short.valueOf((short) randomInteger());
        } else if (type == float.class || type == Float.class) {
            return (T) Float.valueOf(randomInteger());
        } else if (type == double.class || type == Double.class) {
            return (T) Double.valueOf(randomInteger());
        } else if (type == int.class || type == Integer.class) {
            return (T) Integer.valueOf(randomInteger());
        } else if (type == BigInteger.class) {
            return (T) new BigInteger(String.valueOf(randomInteger()));
        } else if (type == BigDecimal.class) {
            return (T) new BigDecimal(String.valueOf(randomInteger()));
        } else if (type == long.class || type == Long.class) {
            return (T) Long.valueOf(randomInteger());
        } else if (type == boolean.class || type == Boolean.class) {
            return (T) Boolean.valueOf(randomBoolean());
        } else if (type == String.class) {
            return (T) randomString();
        } else if (level < depth && !Objects.isMetaClass(type)) {
            T object = Objects.initialize(type);
            Objects.access(type, (field, i) -> Objects.setValue(object, field, generator == null ?
                    randomObject(field.getType(), depth, level + 1, null) : generator.generate(field, i, level + 1)));
            return object;
        }
        return null;
    }
}
