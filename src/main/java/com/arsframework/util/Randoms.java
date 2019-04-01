package com.arsframework.util;

import java.util.*;
import java.lang.reflect.Field;

import com.arsframework.annotation.Assert;

/**
 * 随机数处理工具类
 *
 * @author yongqiang.wu
 * @version 2019-03-22 09:38
 */
public abstract class Randoms {
    /**
     * 当前线程随机处理对象
     */
    private static final ThreadLocal<Random> random = ThreadLocal.withInitial(() -> new Random());

    /**
     * 随机数生成接口
     *
     * @param <T> 数据类型
     * @author yongqiangwu
     */
    public interface RandomGenerator<T> {
        /**
         * 生成随机数
         *
         * @return 随机数
         */
        T generate();

    }

    /**
     * 随机生成对象属性排除策略接口
     *
     * @author yongqiangwu
     */
    public interface ExcludeStrategy {
        /**
         * 判断是否排除
         *
         * @param type  对象类型
         * @param field 字段对象
         * @return true/false
         */
        boolean exclude(Class<?> type, Field field);

    }

    /**
     * 对象属性随机数生成接口工厂
     *
     * @author yongqiangwu
     */
    public interface RandomGeneratorFactory {
        /**
         * 获取随机数生成接口
         *
         * @param <T>   数据类型
         * @param type  对象类型
         * @param field 字段对象
         * @return 随机数生成接口
         */
        <T> RandomGenerator<T> getRandomGenerator(Class<T> type, Field field);

    }

    /**
     * 随机对象实例生成工厂
     *
     * @param <T> 对象类型
     * @author yongqiangwu
     */
    public static class RandomBeanFactory<T> {
        protected final Class<T> type; // 对象类型
        private ExcludeStrategy excludeStrategy;
        private RandomGeneratorFactory randomGeneratorFactory;
        private final LinkedList<Class<?>> executed = new LinkedList<>(); // 已执行对象类型

        @Assert
        public RandomBeanFactory(Class<T> type) {
            this.type = type;
        }

        /**
         * 执行对象实例构建
         *
         * @param <M>  对象类型
         * @param type 对象类型
         * @return 对象实例
         */
        @Assert
        protected <M> M execute(Class<M> type) {
            if (this.excludeStrategy != null && this.excludeStrategy.exclude(type, null)) {
                return null;
            }
            RandomGenerator<?> generator = this.randomGeneratorFactory == null ? null
                    : this.randomGeneratorFactory.getRandomGenerator(type, null);
            if (generator != null) {
                return (M) generator.generate();
            }
            if (Enum.class.isAssignableFrom(type)) {
                return (M) randomEnum((Class<Enum<?>>) type);
            } else if (Date.class.isAssignableFrom(type)) {
                return (M) randomDate();
            } else if (type == byte.class || type == Byte.class) {
                return (M) Byte.valueOf((byte) randomInteger());
            } else if (type == char.class || type == Character.class) {
                return (M) randomCharacter();
            } else if (type == short.class || type == Short.class) {
                return (M) Short.valueOf((short) randomInteger());
            } else if (type == float.class || type == Float.class) {
                return (M) Float.valueOf(randomInteger());
            } else if (type == double.class || type == Double.class) {
                return (M) Double.valueOf(randomInteger());
            } else if (type == int.class || type == Integer.class) {
                return (M) Integer.valueOf(randomInteger());
            } else if (type == long.class || type == Long.class) {
                return (M) Long.valueOf(randomInteger());
            } else if (type == boolean.class || type == Boolean.class) {
                return (M) Boolean.valueOf(randomBoolean());
            } else if (type == String.class) {
                return (M) randomString();
            } else if (type.isArray()) {
                Class<?> component = type.getComponentType();
                Object[] array = Objects.buildArray(component, 1);
                array[0] = this.execute(component);
                return (M) array;
            }
            if (this.executed.contains(type)) {
                return null;
            }
            this.executed.add(type);
            M instance = Objects.initialize(type);
            for (Field field : Objects.getFields(type)) {
                if (this.excludeStrategy != null && this.excludeStrategy.exclude(type, field)) {
                    continue;
                }
                Object value;
                generator = this.randomGeneratorFactory == null ? null
                        : this.randomGeneratorFactory.getRandomGenerator(type, field);
                if (generator != null) {
                    value = generator.generate();
                } else if (Map.class.isAssignableFrom(field.getType())) {
                    Class<?>[] genericTypes = Objects.getGenericTypes(field);
                    Map<Object, Object> map = new HashMap<>(genericTypes.length == 2 ? 1 : 0);
                    if (genericTypes.length == 2) {
                        map.put(this.execute(genericTypes[0]), this.execute(genericTypes[1]));
                    }
                    value = map;
                } else if (Collection.class.isAssignableFrom(field.getType())) {
                    Class<?>[] genericTypes = Objects.getGenericTypes(field);
                    Collection<Object> collection = Set.class.isAssignableFrom(field.getType())
                            ? new HashSet<>(genericTypes.length == 1 ? 1 : 0)
                            : new ArrayList<>(genericTypes.length == 1 ? 1 : 0);
                    if (genericTypes.length == 1) {
                        collection.add(this.execute(genericTypes[0]));
                    }
                    value = collection;
                } else {
                    value = this.execute(field.getType());
                }
                field.setAccessible(true);
                try {
                    field.set(instance, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            this.executed.removeLast();
            return instance;
        }

        /**
         * 注册随机生成属性排除策略
         *
         * @param excludeStrategy 随机生成属性排除策略
         * @return 随机对象实例生成工厂
         */
        public RandomBeanFactory<T> register(ExcludeStrategy excludeStrategy) {
            this.excludeStrategy = excludeStrategy;
            return this;
        }

        /**
         * 注册随机数生成接口工厂
         *
         * @param randomGeneratorFactory 随机数生成接口工厂
         * @return 随机对象实例生成工厂
         */
        public RandomBeanFactory<T> register(RandomGeneratorFactory randomGeneratorFactory) {
            this.randomGeneratorFactory = randomGeneratorFactory;
            return this;
        }

        /**
         * 构建对象实例
         *
         * @return 对象实例
         */
        public T build() {
            return this.execute(this.type);
        }

    }

    /**
     * 随机生成对象实例
     *
     * @param <T>  对象类型
     * @param type 对象类型
     * @return 随机对象实例生成工厂
     */
    public static <T> RandomBeanFactory<T> random(Class<T> type) {
        return new RandomBeanFactory<T>(type);
    }

    /**
     * 随机生成枚举项
     *
     * @param <T>  枚举类型
     * @param type 枚举类型
     * @return 枚举项
     */
    @Assert
    public static <T extends Enum<?>> T randomEnum(Class<T> type) {
        try {
            Object[] values = (Object[]) type.getMethod("values").invoke(type);
            return values.length == 0 ? null : (T) values[random.get().nextInt(values.length)];
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
     * @param min 最小日
     * @param max 最大日期
     * @return 日期
     */
    @Assert
    public static Date randomDate(Date min, Date max) {
        if (max.before(min)) {
            throw new IllegalArgumentException("Argument max must not be less than min, min: " + min + ", max: " + max);
        }
        long start = min.getTime();
        long time = max.getTime() - start; // 相差毫秒数
        if (time <= 1000) { // 相差1秒内
            return new Date(start + random.get().nextInt((int) time));
        }
        return new Date(start + random.get().nextInt((int) (time / 1000)) * 1000);
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
    public static int randomInteger(int min, int max) {
        if (max < min) {
            throw new IllegalArgumentException("Argument max must not be less than min, min: " + min + ", max: " + max);
        }
        return min + random.get().nextInt(max - min);
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
    @Assert(nonempty = true)
    public static String randomString(Character[] chars, int length) {
        if (length < 1) {
            throw new IllegalArgumentException("Argument length must not be less than 1, got: " + length);
        }
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < length; i++) {
            buffer.append(chars[random.get().nextInt(chars.length)]);
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
    @Assert(nonempty = true)
    public static Character randomCharacter(Character[] chars) {
        return chars[random.get().nextInt(chars.length)];
    }

    /**
     * 随机生成真假值
     *
     * @return 真假值
     */
    public static boolean randomBoolean() {
        return random.get().nextBoolean();
    }

}
