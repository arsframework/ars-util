package com.arsframework.util;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLDecoder;
import java.net.JarURLConnection;
import java.time.ZoneId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.lang.reflect.*;

import com.arsframework.annotation.Nonnull;

/**
 * 对象处理工具类
 *
 * @author yongqiang.wu
 */
public abstract class Objects {
    /**
     * 空字节数组
     */
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * 空字段数组
     */
    public static final Field[] EMPTY_FIELD_ARRAY = new Field[0];

    /**
     * 空类对象数组
     */
    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

    /**
     * 类字段访问接口
     */
    public interface FieldAccessor {
        /**
         * 字段访问
         *
         * @param field 字段对象
         * @param index 字段序数
         */
        void access(Field field, int index);
    }

    /**
     * 类字段访问中断接口
     */
    public interface FieldAccessBreaker {
        /**
         * 是否已中断字段访问
         *
         * @param field 字段对象
         * @param index 字段序数
         * @return true/false
         */
        boolean isBroken(Field field, int index);
    }

    /**
     * 类及父类实例字段遍历
     *
     * @param clazz    对象类型
     * @param accessor 字段访问接口
     */
    public static void foreach(Class<?> clazz, FieldAccessor accessor) {
        foreach(clazz, accessor, null);
    }

    /**
     * 类及父类实例字段遍历
     *
     * @param clazz    对象类型
     * @param accessor 字段访问接口
     * @param breaker  字段访问中断接口
     */
    public static void foreach(@Nonnull Class<?> clazz, @Nonnull FieldAccessor accessor, FieldAccessBreaker breaker) {
        int index = 0;
        boolean broken = false;
        do {
            for (Field field : clazz.getDeclaredFields()) {
                if (breaker != null && (broken = breaker.isBroken(field, index))) {
                    break;
                }
                if (!Modifier.isStatic(field.getModifiers()) && !field.getName().startsWith("this$")) {
                    accessor.access(field, index++);
                }
            }
        } while (!broken && (clazz = clazz.getSuperclass()) != Object.class);
    }

    /**
     * 判断类型是否是基本数据类型
     *
     * @param clazz 数据类型
     * @return true/false
     */
    public static boolean isBasicClass(Class<?> clazz) {
        try {
            return clazz == null ? false : Number.class.isAssignableFrom(clazz) ? true :
                    ((Class<?>) clazz.getField("TYPE").get(null)).isPrimitive();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return clazz.isPrimitive() || clazz == Byte.class || clazz == Character.class || clazz == Integer.class || clazz == Short.class
                    || clazz == Long.class || clazz == Float.class || clazz == Double.class || clazz == Boolean.class;
        }
    }

    /**
     * 判断数据类型是否是数字类型
     *
     * @param clazz 数据类型
     * @return true/false
     */
    public static boolean isNumberClass(Class<?> clazz) {
        return clazz != null && (Number.class.isAssignableFrom(clazz) || clazz == byte.class || clazz == char.class
                || clazz == short.class || clazz == int.class || clazz == double.class || clazz == long.class);
    }

    /**
     * 判断对象是否为空
     *
     * @param object 对象
     * @return true/false
     */
    public static boolean isEmpty(Object object) {
        if (object == null || (object instanceof CharSequence && ((CharSequence) object).length() == 0)
                || (object instanceof Map && ((Map<?, ?>) object).isEmpty())
                || (object instanceof Collection && ((Collection<?>) object).isEmpty())) {
            return true;
        }
        Class<?> type = object.getClass();
        if (type.isArray()) {
            Class<?> component = type.getComponentType();
            if (component == byte.class) {
                return ((byte[]) object).length == 0;
            } else if (component == char.class) {
                return ((char[]) object).length == 0;
            } else if (component == int.class) {
                return ((int[]) object).length == 0;
            } else if (component == short.class) {
                return ((short[]) object).length == 0;
            } else if (component == long.class) {
                return ((long[]) object).length == 0;
            } else if (component == float.class) {
                return ((float[]) object).length == 0;
            } else if (component == double.class) {
                return ((double[]) object).length == 0;
            } else if (component == boolean.class) {
                return ((boolean[]) object).length == 0;
            }
            return ((Object[]) object).length == 0;
        }
        return false;
    }

    /**
     * 根据字段名称获取字段对象
     *
     * @param clazz 类对象
     * @param name  字段名称
     * @return 字段对象
     */
    @Nonnull
    public static Field getField(Class<?> clazz, String name) {
        do {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
            }
        } while ((clazz = clazz.getSuperclass()) != Object.class);
        throw new RuntimeException(String.format("No such field '%s'", name));
    }

    /**
     * 获取对象所有实例字段
     *
     * @param clazz 对象类型
     * @return 字段对象数组
     */
    @Nonnull
    public static Field[] getFields(Class<?> clazz) {
        List<Field> fields = new LinkedList<>();
        foreach(clazz, (field, i) -> fields.add(field));
        return fields.toArray(EMPTY_FIELD_ARRAY);
    }

    /**
     * 获取对象所有实例属性名称
     *
     * @param clazz 对象类型
     * @return 字段名称数组
     */
    @Nonnull
    public static String[] getProperties(Class<?> clazz) {
        List<String> properties = new LinkedList<>();
        foreach(clazz, (field, i) -> properties.add(field.getName()));
        return properties.toArray(Strings.EMPTY_ARRAY);
    }

    /**
     * 获取对象指定字段的值
     *
     * @param object 对象实例
     * @param field  字段对象
     * @return 字段值
     */
    @Nonnull
    public static Object getValue(Object object, Field field) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取对象指定字段的值
     *
     * @param object   对象实例
     * @param property 属性名称
     * @return 字段值
     */
    @Nonnull
    public static Object getValue(Object object, String property) {
        int division = property.indexOf('.'); // 属性分割符下标
        if (division <= 0) {
            return getValue(object, getField(object.getClass(), property));
        }
        Object value = getValue(object, getField(object.getClass(), property.substring(0, division)));
        return value == null ? null : getValue(value, property.substring(division + 1));
    }

    /**
     * 获取对象实例属性值
     *
     * @param object 对象实例
     * @return 键/值对象
     */
    @Nonnull
    public static Map<String, Object> getValues(Object object) {
        Map<String, Object> values = new HashMap<>();
        foreach(object.getClass(), (field, i) -> values.put(field.getName(), getValue(object, field)));
        return values;
    }

    /**
     * 设置对象指定属性的值
     *
     * @param object 对象实例
     * @param field  字段对象
     * @param value  字段值
     */
    public static void setValue(@Nonnull Object object, @Nonnull Field field, Object value) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        try {
            field.set(object, toObject(field.getType(), value));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置对象指定属性的值，对象属性必须支持set方法
     *
     * @param object   对象实例
     * @param property 属性名称
     * @param value    字段值
     */
    public static void setValue(@Nonnull Object object, @Nonnull String property, Object value) {
        setValue(object, getField(object.getClass(), property), value);
    }

    /**
     * 将Map对象所包含的键/值填充到Bean对象实例对应的非静态属性中
     *
     * @param object 对象实例
     * @param values 需要填充的属性/值Map对象
     */
    @Nonnull
    public static void setValues(Object object, Map<String, ?> values) {
        if (!values.isEmpty()) {
            foreach(object.getClass(), (field, i) -> {
                if (values.containsKey(field.getName())) {
                    setValue(object, field, values.get(field.getName()));
                }
            });
        }
    }

    /**
     * 将属性值填充到Bean对象实例对应的非静态属性中
     *
     * @param object 对象实例
     * @param values 需要填充的属性值数组
     */
    public static void setValues(Object object, Object... values) {
        if (values.length > 0) {
            foreach(object.getClass(), (field, i) -> setValue(object, field, values[i]), (field, i) -> i >= values.length);
        }
    }

    /**
     * 对象拷贝（属性复制）
     *
     * @param <T>    数据类型
     * @param source 源对象
     * @return 目标对象
     */
    @Nonnull
    public static <T> T copy(T source) {
        T target = (T) initialize(source.getClass());
        copy(source, target);
        return target;
    }

    /**
     * 拷贝对象实例，深度克隆
     *
     * @param <T>    数据类型
     * @param source 源对象
     * @return 对象实例副本
     */
    @Nonnull
    public static <T extends Serializable> T clone(T source) {
        try {
            return (T) Streams.deserialize(Streams.serialize(source));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 对象拷贝（属性复制）
     *
     * @param <T>    数据类型
     * @param source 源对象
     * @param target 目标对象
     */
    @Nonnull
    public static <T> void copy(T source, T target) {
        foreach(source.getClass(), (field, i) -> setValue(target, field, getValue(source, field)));
    }

    /**
     * 初始化对象实例
     *
     * @param <T>  数据类型
     * @param type 对象类型
     * @return 对象实例
     */
    @Nonnull
    public static <T> T initialize(Class<T> type) {
        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 初始化对象实例
     *
     * @param <T>    数据类型
     * @param type   对象类型
     * @param values 属性值数组
     * @return 对象实例
     */
    public static <T> T initialize(Class<T> type, Object... values) {
        T instance = initialize(type);
        setValues(instance, values);
        return instance;
    }

    /**
     * 初始化对象实例
     *
     * @param <T>    数据类型
     * @param type   对象类型
     * @param values 初始化参数
     * @return 对象实例
     */
    public static <T> T initialize(Class<T> type, Map<String, ?> values) {
        T instance = initialize(type);
        setValues(instance, values);
        return instance;
    }

    /**
     * 根据泛型参数类型获取泛型类型
     *
     * @param parameterizedType 泛型参数类型
     * @return 泛型类型数组
     */
    @Nonnull
    private static Class<?>[] getGenericTypes(ParameterizedType parameterizedType) {
        Type[] types = parameterizedType.getActualTypeArguments();
        List<Class<?>> classes = new ArrayList<>(types.length);
        for (Type type : types) {
            if (type instanceof Class) {
                classes.add((Class<?>) type);
            }
        }
        return classes.toArray(EMPTY_CLASS_ARRAY);
    }

    /**
     * 获取类对象的泛型
     *
     * @param clazz 类对象对象
     * @return 泛型类型数组
     */
    @Nonnull
    public static Class<?>[] getGenericTypes(Class<?> clazz) {
        Type type = clazz.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            return getGenericTypes((ParameterizedType) type);
        }
        Class<?> parent = clazz.getSuperclass();
        return parent == null ? EMPTY_CLASS_ARRAY : getGenericTypes(parent);
    }

    /**
     * 获取字段的泛型
     *
     * @param field 字段对象
     * @return 泛型类型数组
     */
    @Nonnull
    public static Class<?>[] getGenericTypes(Field field) {
        Type type = field.getGenericType();
        return type instanceof ParameterizedType ? getGenericTypes((ParameterizedType) type) : EMPTY_CLASS_ARRAY;
    }

    /**
     * 获取基本数据包装类型
     *
     * @param clazz 基本数据类型
     * @return 基本数据包装类型
     */
    @Nonnull
    public static Class<?> getBasicWrapClass(Class<?> clazz) {
        if (clazz == byte.class) {
            return Byte.class;
        } else if (clazz == char.class) {
            return Character.class;
        } else if (clazz == int.class) {
            return Integer.class;
        } else if (clazz == short.class) {
            return Short.class;
        } else if (clazz == long.class) {
            return Long.class;
        } else if (clazz == float.class) {
            return Float.class;
        } else if (clazz == double.class) {
            return Double.class;
        } else if (clazz == boolean.class) {
            return Boolean.class;
        }
        throw new IllegalArgumentException("Class must be basic type");
    }

    /**
     * 从包package中获取所有的Class
     *
     * @param pack 包路径名
     * @return Java类集合
     */
    @Nonnull
    public static List<Class<?>> getClasses(String pack) {
        List<Class<?>> classes = new ArrayList<>();
        // 获取包的名字 并进行替换
        String path = pack.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        try {
            Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(path);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    classes.addAll(getClasses(pack, URLDecoder.decode(url.getFile(), Strings.CHARSET_UTF8)));
                } else if ("jar".equals(protocol)) {
                    // 获取jar
                    JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
                    // 从此jar包 得到一个枚举类
                    Enumeration<JarEntry> entries = jar.entries();
                    // 同样的进行循环迭代
                    while (entries.hasMoreElements()) {
                        // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        // 如果是以/开头的
                        if (name.charAt(0) == '/') {
                            // 获取后面的字符串
                            name = name.substring(1);
                        }
                        // 如果前半部分和定义的包名相同
                        if (name.startsWith(path)) {
                            int idx = name.lastIndexOf('/');
                            // 如果以"/"结尾 是一个包
                            if (idx != -1) {
                                // 获取包名 把"/"替换成"."
                                pack = name.substring(0, idx).replace('/', '.');
                            }
                            // 如果可以迭代下去 并且是一个包，且是一个.class文件 而且不是目录
                            if (idx != -1 && name.endsWith(".class") && !entry.isDirectory()) {
                                // 去掉后面的".class" 获取真正的类名
                                String className = name.substring(pack.length() + 1, name.length() - 6);
                                classes.add(Class.forName(pack + '.' + className));
                            }
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return classes;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @param pack 包名
     * @param path 包路径
     * @return 对象列表
     */
    @Nonnull
    private static List<Class<?>> getClasses(String pack, String path) {
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            return new ArrayList<>(0);
        }
        List<Class<?>> classes = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (File file : dir.listFiles((file) -> file.isDirectory() || file.getName().endsWith(".class"))) {
            if (file.isDirectory()) {
                classes.addAll(getClasses(pack + '.' + file.getName(), file.getAbsolutePath()));
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    classes.add(classLoader.loadClass(pack + '.' + className));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return classes;
    }

    /**
     * 获取目标异常信息
     *
     * @param throwable 异常对象
     * @return 信息内容
     */
    @Nonnull
    public static String getThrowableMessage(Throwable throwable) {
        Throwable cause;
        while ((cause = throwable.getCause()) != null) {
            throwable = cause;
        }
        return throwable.getMessage();
    }

    /**
     * 对象类型转换
     *
     * @param <T>    对象类型
     * @param type   转换目标类型
     * @param object 被转换对象
     * @return 转换后对象
     */
    public static <T> T toObject(@Nonnull Class<T> type, Object object) {
        if (object == null || type == Object.class || type.isAssignableFrom(object.getClass())) {
            return (T) object;
        } else if (type == byte.class || type == Byte.class) {
            return (T) toByte(object);
        } else if (type == char.class || type == Character.class) {
            return (T) toCharacter(object);
        } else if (type == boolean.class || type == Boolean.class) {
            return (T) toBoolean(object);
        } else if (type == int.class || type == Integer.class) {
            return (T) toInteger(object);
        } else if (type == BigInteger.class) {
            return (T) (object instanceof BigInteger ? object : new BigInteger(object.toString()));
        } else if (type == BigDecimal.class) {
            return (T) (object instanceof BigDecimal ? object : new BigDecimal(object.toString()));
        } else if (type == short.class || type == Short.class) {
            return (T) toShort(object);
        } else if (type == float.class || type == Float.class) {
            return (T) toFloat(object);
        } else if (type == double.class || type == Double.class) {
            return (T) toDouble(object);
        } else if (type == long.class || type == Long.class) {
            return (T) toLong(object);
        } else if (Enum.class.isAssignableFrom(type)) {
            return (T) toEnum((Class<Enum>) type, object);
        } else if (Date.class.isAssignableFrom(type)) {
            return (T) toDate(object);
        } else if (LocalDate.class.isAssignableFrom(type)) {
            return (T) (object instanceof LocalDate ? object : object instanceof LocalDateTime ? ((LocalDateTime) object).toLocalDate() :
                    Dates.adapter(toDate(object)).toLocalDate());
        } else if (LocalDateTime.class.isAssignableFrom(type)) {
            return (T) (object instanceof LocalDateTime ? object : object instanceof LocalDate ?
                    ((LocalDate) object).atStartOfDay(ZoneId.systemDefault()).toLocalDateTime() : Dates.adapter(toDate(object)));
        } else if (type == String.class) {
            return (T) Strings.toString(object);
        } else if (type == Class.class) {
            return (T) toClass(object);
        }
        throw new IllegalArgumentException("Cannot convert " + object + " to " + type);
    }

    /**
     * 字节类型转换
     *
     * @param object 被转换对象
     * @return 字节对象
     */
    public static Byte toByte(Object object) {
        return object == null ? null : object instanceof Byte ? (Byte) object :
                object instanceof Number ? ((Number) object).byteValue() : Byte.parseByte(object.toString());
    }

    /**
     * 字符类型转换
     *
     * @param object 被转换对象
     * @return 字符对象
     */
    public static Character toCharacter(Object object) {
        return object == null ? null : object instanceof Character ? (Character) object :
                Character.valueOf((char) (object instanceof Number ? ((Number) object).intValue() : Integer.parseInt(object.toString())));
    }

    /**
     * 真假类型转换
     *
     * @param object 被转换对象
     * @return 真假对象
     */
    public static Boolean toBoolean(Object object) {
        return object == null ? null : object instanceof Boolean ? (Boolean) object : object instanceof Number ?
                ((Number) object).intValue() > 0 ? Boolean.TRUE : Boolean.FALSE : Boolean.parseBoolean(object.toString());
    }

    /**
     * 整形类型转换
     *
     * @param object 被转换对象
     * @return 整形对象
     */
    public static Integer toInteger(Object object) {
        return object == null ? null : object instanceof Integer ? (Integer) object :
                object instanceof Number ? ((Number) object).intValue() : Integer.parseInt(object.toString());
    }

    /**
     * 短整形类型转换
     *
     * @param object 被转换对象
     * @return 短整形对象
     */
    public static Short toShort(Object object) {
        return object == null ? null : object instanceof Short ? (Short) object :
                object instanceof Number ? ((Number) object).shortValue() : Short.parseShort(object.toString());
    }

    /**
     * 单精度浮点类型转换
     *
     * @param object 被转换对象
     * @return 单精度浮点对象
     */
    public static Float toFloat(Object object) {
        return object == null ? null : object instanceof Float ? (Float) object :
                object instanceof Number ? ((Number) object).floatValue() : Float.parseFloat(object.toString());
    }


    /**
     * 双精度浮点类型转换
     *
     * @param object 被转换对象
     * @return 双精度浮点对象
     */
    public static Double toDouble(Object object) {
        return object == null ? null : object instanceof Double ? (Double) object :
                object instanceof Number ? ((Number) object).doubleValue() : Double.parseDouble(object.toString());
    }

    /**
     * 长整形类型转换
     *
     * @param object 被转换对象
     * @return 长整形对象
     */
    public static Long toLong(Object object) {
        return object == null ? null : object instanceof Long ? (Long) object :
                object instanceof Number ? ((Number) object).longValue() : Long.parseLong(object.toString());
    }

    /**
     * 枚举类型转换
     *
     * @param <T>    数据类型
     * @param type   Enum类型
     * @param object 被转换对象
     * @return 枚举实例
     */
    public static <T extends Enum<T>> T toEnum(@Nonnull Class<T> type, Object object) {
        if (object == null || object.getClass() == type) {
            return (T) object;
        } else if (object instanceof Number) {
            int ordinal = ((Number) object).intValue();
            try {
                Method method = type.getDeclaredMethod("values");
                method.setAccessible(true);
                for (T value : (T[]) method.invoke(type)) {
                    if (value.ordinal() == ordinal) {
                        return value;
                    }
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            throw new IllegalArgumentException("No enum of " + type.getCanonicalName() + " with ordinal " + ordinal);
        }
        return Enum.valueOf(type, object.toString());
    }

    /**
     * 日期类型转换
     *
     * @param object 被转换对象
     * @return 日期
     */
    public static Date toDate(Object object) {
        return toDate(object, Dates.ALL_DATE_FORMATS);
    }

    /**
     * 日期类型转换
     *
     * @param object   被转换对象
     * @param patterns 格式模式数组
     * @return 日期
     */
    public static Date toDate(Object object, String... patterns) {
        return object == null ? null : object instanceof Date ? (Date) object : object instanceof LocalDate ?
                Dates.adapter((LocalDate) object) : object instanceof LocalDateTime ? Dates.adapter((LocalDateTime) object) :
                object instanceof Number ? new Date(((Number) object).longValue()) : Dates.parse(object.toString(), patterns);
    }

    /**
     * 类对象转换
     *
     * @param object 被转换对象
     * @return 类对象
     */
    public static Class<?> toClass(Object object) {
        try {
            return object == null ? null : object instanceof Class ? (Class<?>) object : Class.forName(object.toString());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 计算对象数组的hash值
     *
     * @param objects 对象数组
     * @return hash值
     */
    public static int hash(Object... objects) {
        return java.util.Objects.hash(objects);
    }

    /**
     * 判断两个对象是否相同
     *
     * @param object 对象
     * @param other  对象
     * @return true/false
     */
    public static boolean equal(Object object, Object other) {
        return object != null && other != null && object.getClass() == other.getClass() && object.equals(other);
    }
}
