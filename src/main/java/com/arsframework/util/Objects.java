package com.arsframework.util;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.net.URLDecoder;
import java.net.JarURLConnection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.lang.reflect.*;

import com.arsframework.annotation.Min;
import com.arsframework.annotation.Nonnull;
import com.arsframework.annotation.Nonempty;

/**
 * 对象处理工具类
 *
 * @author yongqiang.wu
 * @version 2019-03-22 09:38
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
     * @param cls 类对象对象
     * @return 泛型类型数组
     */
    @Nonnull
    public static Class<?>[] getGenericTypes(Class<?> cls) {
        Type type = cls.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            return getGenericTypes((ParameterizedType) type);
        }
        Class<?> parent = cls.getSuperclass();
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
     * @param cls 基本数据类型
     * @return 基本数据包装类型
     */
    @Nonnull
    public static Class<?> getBasicWrapClass(Class<?> cls) {
        if (cls == byte.class) {
            return Byte.class;
        } else if (cls == char.class) {
            return Character.class;
        } else if (cls == int.class) {
            return Integer.class;
        } else if (cls == short.class) {
            return Short.class;
        } else if (cls == long.class) {
            return Long.class;
        } else if (cls == float.class) {
            return Float.class;
        } else if (cls == double.class) {
            return Double.class;
        } else if (cls == boolean.class) {
            return Boolean.class;
        }
        return cls;
    }

    /**
     * 判断类型是否是基本数据类型
     *
     * @param cls 数据类型
     * @return true/false
     */
    public static boolean isBasicClass(Class<?> cls) {
        try {
            return cls == null ? false : Number.class.isAssignableFrom(cls) ? true :
                    ((Class<?>) cls.getField("TYPE").get(null)).isPrimitive();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return cls.isPrimitive();
        }
    }

    /**
     * 判断对象类型是否是元类型
     * <p>
     * 元类型包括基本数据类型、字符串类型、枚举类型、日期类型、类对象类型
     *
     * @param cls 对象类型
     * @return true/false
     */
    public static boolean isMetaClass(Class<?> cls) {
        return cls != null && (isBasicClass(cls) || cls == String.class || cls == Class.class || cls == Object.class
                || Enum.class.isAssignableFrom(cls) || Date.class.isAssignableFrom(cls));
    }

    /**
     * 判断类型是否是基本数据包装类型
     *
     * @param cls 数据类型
     * @return true/false
     */
    public static boolean isBasicWrapClass(Class<?> cls) {
        return cls != null && (cls == Byte.class || cls == Character.class || cls == Integer.class || cls == Short.class
                || cls == Long.class || cls == Float.class || cls == Double.class || cls == Boolean.class);
    }

    /**
     * 判断类型是否是基本数据数字类型
     *
     * @param cls 数据类型
     * @return true/false
     */
    public static boolean isBasicNumberClass(Class<?> cls) {
        return cls != null && (cls == byte.class || cls == char.class || cls == short.class || cls == int.class
                || cls == double.class || cls == long.class);
    }

    /**
     * 判断类型是否是基本数据数字包装类型
     *
     * @param cls 数据类型
     * @return true/false
     */
    public static boolean isBasicNumberWrapClass(Class<?> cls) {
        return cls != null && (cls == Byte.class || cls == Character.class || cls == Short.class || cls == Integer.class
                || cls == Double.class || cls == Long.class);
    }

    /**
     * 判断数据类型是否是数字类型
     *
     * @param cls 数据类型
     * @return true/false
     */
    public static boolean isNumberClass(Class<?> cls) {
        return cls != null && (isBasicNumberClass(cls) || isBasicNumberWrapClass(cls));
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
                || (object instanceof Collection && ((Collection<?>) object).isEmpty())
                || (object instanceof Iterable && !((Iterable<?>) object).iterator().hasNext())) {
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
     * @param cls  类对象
     * @param name 字段名称
     * @return 字段对象
     */
    @Nonempty
    public static Field getField(Class<?> cls, String name) {
        try {
            return cls.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            Class<?> parent = cls.getSuperclass();
            if (parent == null) {
                throw new RuntimeException(e);
            }
            return getField(parent, name);
        }
    }

    /**
     * 根据字段名称获取字段对象
     *
     * @param cls   类对象
     * @param names 字段名称数组
     * @return 字段对象数组
     */
    @Nonnull
    public static Field[] getFields(Class<?> cls, String... names) {
        if (names == null || names.length == 0) {
            List<Field> fields = new LinkedList<>();
            while (cls != Object.class) {
                for (Field field : cls.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers()) && !field.getName().startsWith("this$")) {
                        fields.add(field);
                    }
                }
                cls = cls.getSuperclass();
            }
            return fields.toArray(EMPTY_FIELD_ARRAY);
        }
        Field[] fields = new Field[names.length];
        Field[] _fields = cls.getDeclaredFields();
        outer:
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            for (Field field : _fields) {
                if (field.getName().equals(name)) {
                    fields[i] = field;
                    continue outer;
                }
            }
            Class<?> parent = cls.getSuperclass();
            while (parent != Object.class) {
                try {
                    fields[i] = parent.getDeclaredField(name);
                    continue outer;
                } catch (NoSuchFieldException e) {
                    parent = parent.getSuperclass();
                }
            }
            throw new RuntimeException("No such field: " + name);
        }
        return fields;
    }

    /**
     * 获取对象属性名称
     *
     * @param cls 对象类型
     * @return 字段名称数组
     */
    @Nonnull
    public static String[] getProperties(Class<?> cls) {
        if (Enum.class.isAssignableFrom(cls)) {
            try {
                Method method = cls.getMethod("values");
                method.setAccessible(true);
                Object[] values = (Object[]) method.invoke(cls);
                String[] properties = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    properties[i] = values[i].toString();
                }
                return properties;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        List<String> properties = new LinkedList<>();
        while (cls != Object.class) {
            Field[] fields = cls.getDeclaredFields();
            for (Field field : fields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    properties.add(field.getName());
                }
            }
            cls = cls.getSuperclass();
        }
        return properties.toArray(Strings.EMPTY_ARRAY);
    }

    /**
     * 获取对象指定字段的值
     *
     * @param object   对象实例
     * @param property 属性名称
     * @return 字段值
     */
    @Nonempty
    public static Object getValue(Object object, String property) {
        String suffix = null;
        int index = property.indexOf('.');
        if (index > 0) {
            suffix = property.substring(index + 1);
            property = property.substring(0, index);
        }
        Field field = getField(object.getClass(), property);
        field.setAccessible(true);
        try {
            Object value = field.get(object);
            return value == null || suffix == null ? value : getValue(value, suffix);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取对象实例指定属性
     *
     * @param object     对象实例
     * @param properties 属性名称数组（如果为空则获取所有属性值）
     * @return 键/值对象
     */
    @Nonnull
    public static Map<String, Object> getValues(Object object, String... properties) {
        if (properties.length == 0) {
            Map<String, Object> values = new HashMap<>();
            Class<?> meta = object.getClass();
            while (meta != Object.class) {
                for (Field field : meta.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        field.setAccessible(true);
                        try {
                            values.put(field.getName(), field.get(object));
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                meta = meta.getSuperclass();
            }
            return values;
        }
        Map<String, Object> values = new HashMap<>(properties.length);
        for (String property : properties) {
            values.put(property, getValue(object, property));
        }
        return values;
    }

    /**
     * 设置对象指定属性的值，对象属性必须支持set方法
     *
     * @param object   对象实例
     * @param property 属性名称
     * @param value    字段值
     */
    public static void setValue(@Nonnull Object object, @Nonempty String property, Object value) {
        Field field = getField(object.getClass(), property);
        field.setAccessible(true);
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
            Class<?> meta = object.getClass();
            while (meta != Object.class) {
                for (Field field : meta.getDeclaredFields()) {
                    if (values.containsKey(field.getName()) && !Modifier.isStatic(field.getModifiers())) {
                        field.setAccessible(true);
                        try {
                            field.set(object, values.get(field.getName()));
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                meta = meta.getSuperclass();
            }
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
        Class<?> type = source.getClass();
        while (type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    field.set(target, field.get(source));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            type = type.getSuperclass();
        }
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
     * @param values 初始化参数
     * @return 对象实例
     */
    @Nonnull
    public static <T> T initialize(Class<T> type, Map<String, ?> values) {
        Class<?> cls = type;
        T instance = initialize(type);
        if (!values.isEmpty()) {
            while (cls != Object.class) {
                for (Field field : cls.getDeclaredFields()) {
                    Object value;
                    if (values.containsKey(field.getName()) && !Modifier.isStatic(field.getModifiers())
                            && !isEmpty(value = values.get(field.getName()))) {
                        field.setAccessible(true);
                        try {
                            field.set(instance, value);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                cls = cls.getSuperclass();
            }
        }
        return instance;
    }

    /**
     * 获取数组对象
     *
     * @param <T>    数据类型
     * @param type   数组类型
     * @param length 数组长度
     * @return 数组对象
     */
    @Nonnull
    public static <T> T[] buildArray(Class<T> type, @Min(0) int length) {
        Class<?> _type = isBasicClass(type) ? getBasicWrapClass(type) : type;
        return (T[]) Array.newInstance(_type, length);
    }

    /**
     * 对象比较
     *
     * @param o1 比较对象
     * @param o2 比较对象
     * @return 比较结果数字
     */
    public static int compare(Object o1, Object o2) {
        if (o1 == o2) {
            return 0;
        } else if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        } else if (o1.getClass() == o2.getClass() && o1 instanceof Comparable) {
            return ((Comparable<Object>) o1).compareTo(o2);
        }
        int h1 = o1.hashCode();
        int h2 = o2.hashCode();
        return h1 < h2 ? -1 : h1 == h2 ? 0 : 1;
    }

    /**
     * 将对象集合按照属性值排序（属性名以“+”号开头或不以“-”号开头表示升序，以“-”号开头表示降序）
     *
     * @param <M>        数据类型
     * @param collection 对象集合
     * @param properties 属性名称数组
     * @return 排序后对象集合
     */
    @Nonnull
    public static <M> List<M> sort(Collection<M> collection, String... properties) {
        List<M> list = collection instanceof List ? (List<M>) collection : new ArrayList<M>(collection);
        Collections.sort(list, (o1, o2) -> {
            if (properties.length == 0) {
                return Objects.compare(o1, o2);
            }
            for (String property : properties) {
                Boolean asc = property.charAt(0) == '+' ? Boolean.TRUE
                        : property.charAt(0) == '-' ? Boolean.FALSE : null;
                if (asc != null) {
                    property = property.substring(1);
                }
                int offset = Objects.compare(getValue(o1, property), getValue(o2, property));
                if (offset != 0) {
                    return asc == null || asc == Boolean.TRUE ? offset : -offset;
                }
            }
            return 0;
        });
        return list;
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
     * 从包package中获取所有的Class
     *
     * @param pack 包路径名
     * @return Java类集合
     */
    @Nonempty
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
    @Nonempty
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
     * 获取树型键/值对象比较器
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @param map 树型键/值对象
     * @return 比较器对象
     */
    @Nonnull
    private static <K, V> Comparator<K> getTreeMapComparator(TreeMap<K, V> map) {
        try {
            Field field = map.getClass().getDeclaredField("comparator");
            field.setAccessible(true);
            return (Comparator<K>) field.get(map);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 对象类型转换
     *
     * @param type   转换目标类型
     * @param object 被转换对象
     * @return 转换后对象
     */
    public static Object toObject(@Nonnull Class<?> type, Object object) {
        if (type == Object.class) {
            return object;
        } else if (type.isArray()) {
            return toArray(type.getComponentType(), object);
        } else if (type == byte.class || type == Byte.class) {
            return toByte((Class<Byte>) type, object);
        } else if (type == char.class || type == Character.class) {
            return toCharacter((Class<Character>) type, object);
        } else if (type == boolean.class || type == Boolean.class) {
            return toBoolean((Class<Boolean>) type, object);
        } else if (type == int.class || type == Integer.class) {
            return toInteger((Class<Integer>) type, object);
        } else if (type == short.class || type == Short.class) {
            return toShort((Class<Short>) type, object);
        } else if (type == float.class || type == Float.class) {
            return toFloat((Class<Float>) type, object);
        } else if (type == double.class || type == Double.class) {
            return toDouble((Class<Double>) type, object);
        } else if (type == long.class || type == Long.class) {
            return toLong((Class<Long>) type, object);
        } else if (Enum.class.isAssignableFrom(type)) {
            return toEnum((Class<Enum>) type, object);
        } else if (Date.class.isAssignableFrom(type)) {
            return toDate(object);
        } else if (LocalDate.class.isAssignableFrom(type)) {
            return Dates.adapter(toDate(object)).toLocalDate();
        } else if (LocalDateTime.class.isAssignableFrom(type)) {
            return Dates.adapter(toDate(object));
        } else if (type == String.class) {
            return Strings.toString(object);
        } else if (type == Class.class) {
            return toClass(object);
        } else if (object instanceof Set) {
            return toSet(type, object);
        } else if (object instanceof List) {
            return toList(type, object);
        } else if (object instanceof Iterable) {
            return toList(type, object);
        } else if (object instanceof Map) {
            return toMap(type, (Map<?, ?>) object);
        } else if (object instanceof byte[]) {
            return toArray(type.isArray() ? (Class<Object>) type.getComponentType() : (Class<Object>) type, object);
        } else if (object instanceof char[]) {
            return toArray(type.isArray() ? (Class<Object>) type.getComponentType() : (Class<Object>) type, object);
        } else if (object instanceof int[]) {
            return toArray(type.isArray() ? (Class<Object>) type.getComponentType() : (Class<Object>) type, object);
        } else if (object instanceof short[]) {
            return toArray(type.isArray() ? (Class<Object>) type.getComponentType() : (Class<Object>) type, object);
        } else if (object instanceof long[]) {
            return toArray(type.isArray() ? (Class<Object>) type.getComponentType() : (Class<Object>) type, object);
        } else if (object instanceof float[]) {
            return toArray(type.isArray() ? (Class<Object>) type.getComponentType() : (Class<Object>) type, object);
        } else if (object instanceof double[]) {
            return toArray(type.isArray() ? (Class<Object>) type.getComponentType() : (Class<Object>) type, object);
        } else if (object instanceof boolean[]) {
            return toArray(type.isArray() ? (Class<Object>) type.getComponentType() : (Class<Object>) type, object);
        } else if (object instanceof Object[]) {
            return toArray(type.isArray() ? (Class<Object>) type.getComponentType() : (Class<Object>) type, object);
        } else if (object != null && !type.isAssignableFrom(object.getClass())) {
            throw new IllegalArgumentException("Cannot convert " + object + " to " + type);
        }
        return object;
    }

    /**
     * 键/值对类型转换
     *
     * @param <K>    键类型
     * @param <V>    值类型
     * @param <T>    目标数据类型
     * @param type   转换类型
     * @param object 被转换对象
     * @return 键/值对象
     */
    public static <K, V, T> Map<K, T> toMap(@Nonnull Class<T> type, Map<K, V> object) {
        if (object == null) {
            return new HashMap<K, T>(0);
        }
        Map<K, T> map = object instanceof TreeMap ? new TreeMap<K, T>(getTreeMapComparator((TreeMap<K, V>) object))
                : object instanceof LinkedHashMap ? new LinkedHashMap<K, T>(object.size())
                : new HashMap<K, T>(object.size());
        for (Entry<K, V> entry : object.entrySet()) {
            map.put(entry.getKey(), (T) toObject(type, entry.getValue()));
        }
        return map;
    }

    /**
     * 集合类型转换
     *
     * @param <T>    数据类型
     * @param type   转换类型
     * @param object 被转换对象
     * @return Set
     */
    public static <T> Set<T> toSet(@Nonnull Class<T> type, Object object) {
        if (object == null) {
            return new HashSet<T>(0);
        }
        T[] array = toArray(type, object);
        Set<T> set = new HashSet<T>(array.length);
        for (T o : array) {
            set.add(o);
        }
        return set;
    }

    /**
     * 列表类型转换
     *
     * @param <T>    数据类型
     * @param type   转换类型
     * @param object 被转换对象
     * @return List
     */
    public static <T> List<T> toList(@Nonnull Class<T> type, Object object) {
        if (object == null) {
            return new ArrayList<T>(0);
        }
        T[] array = toArray(type, object);
        List<T> list = new ArrayList<T>(array.length);
        for (T o : array) {
            list.add(o);
        }
        return list;
    }

    /**
     * 将对象转换成数组
     *
     * @param <T>    数据类型
     * @param type   数组类型
     * @param object 被转换对象
     * @return 数组对象
     */
    public static <T> T[] toArray(@Nonnull Class<T> type, Object object) {
        if (object == null) {
            return buildArray(type, 0);
        } else if (object instanceof List) {
            int i = 0;
            List<?> list = (List<?>) object;
            T[] array = buildArray(type, list.size());
            for (Object o : list) {
                array[i++] = (T) toObject(type, o);
            }
            return array;
        } else if (object instanceof Collection) {
            Collection<?> collection = (Collection<?>) object;
            T[] array = buildArray(type, collection.size());
            int i = 0;
            for (Object o : collection) {
                array[i++] = (T) toObject(type, o);
            }
            return array;
        } else if (object instanceof Iterable) {
            List<T> list = new LinkedList<T>();
            Iterator<?> iterator = ((Iterable<?>) object).iterator();
            while (iterator.hasNext()) {
                list.add((T) toObject(type, iterator.next()));
            }
            return list.toArray(buildArray(type, 0));
        } else if (object.getClass().isArray()) {
            Class<?> component = object.getClass().getComponentType();
            if (type == component || type.isAssignableFrom(component)) {
                return (T[]) object;
            }
            Collection<?> collection;
            if (component == byte.class) {
                collection = Arrays.asList((byte[]) object);
            } else if (component == char.class) {
                collection = Arrays.asList((char[]) object);
            } else if (component == int.class) {
                collection = Arrays.asList((int[]) object);
            } else if (component == short.class) {
                collection = Arrays.asList((short[]) object);
            } else if (component == long.class) {
                collection = Arrays.asList((long[]) object);
            } else if (component == float.class) {
                collection = Arrays.asList((float[]) object);
            } else if (component == double.class) {
                collection = Arrays.asList((double[]) object);
            } else if (component == boolean.class) {
                collection = Arrays.asList((boolean[]) object);
            } else {
                collection = Arrays.asList((Object[]) object);
            }
            T[] array = buildArray(type, collection.size());
            for (int i = 0, len = collection.size(); i < len; i++) {
                array[i] = (T) toObject(type, ((List<?>) collection).get(i));
            }
            return array;
        }
        T[] array = buildArray(type, 1);
        array[0] = (T) toObject(type, object);
        return array;
    }

    /**
     * 字节类型转换
     *
     * @param object 被转换对象
     * @return 字节对象
     */
    public static Byte toByte(Object object) {
        return toByte(Byte.class, object);
    }

    /**
     * 字节类型转换
     *
     * @param type   字节类型
     * @param object 被转换对象
     * @return 字节对象
     */
    private static Byte toByte(@Nonnull Class<Byte> type, Object object) {
        if (object == null || (object instanceof CharSequence && ((CharSequence) object).length() == 0)) {
            return type == byte.class ? (byte) 0 : null;
        }
        return (Byte) (object instanceof Byte ? object
                : object instanceof Number ? ((Number) object).byteValue() : Byte.parseByte(object.toString()));
    }

    /**
     * 字符类型转换
     *
     * @param object 被转换对象
     * @return 字符对象
     */
    public static Character toCharacter(Object object) {
        return toCharacter(Character.class, object);
    }

    /**
     * 字符类型转换
     *
     * @param type   字符类型
     * @param object 被转换对象
     * @return 字符对象
     */
    private static Character toCharacter(@Nonnull Class<Character> type, Object object) {
        if (object == null || (object instanceof CharSequence && ((CharSequence) object).length() == 0)) {
            return type == char.class ? (char) 0 : null;
        }
        return (Character) (object instanceof Character ? object
                : object instanceof Number ? ((Number) object).intValue() : Integer.parseInt(object.toString()));
    }

    /**
     * 真假类型转换
     *
     * @param object 被转换对象
     * @return 真假对象
     */
    public static Boolean toBoolean(Object object) {
        return toBoolean(Boolean.class, object);
    }

    /**
     * 真假类型转换
     *
     * @param type   真假类型
     * @param object 被转换对象
     * @return 真假对象
     */
    private static Boolean toBoolean(@Nonnull Class<Boolean> type, Object object) {
        if (object == null || (object instanceof CharSequence && ((CharSequence) object).length() == 0)) {
            return type == boolean.class ? false : null;
        }
        return (Boolean) (object instanceof Boolean ? object : Boolean.parseBoolean(object.toString()));
    }

    /**
     * 整形类型转换
     *
     * @param object 被转换对象
     * @return 整形对象
     */
    public static Integer toInteger(Object object) {
        return toInteger(Integer.class, object);
    }

    /**
     * 整形类型转换
     *
     * @param type   整形类型
     * @param object 被转换对象
     * @return 整形对象
     */
    private static Integer toInteger(@Nonnull Class<Integer> type, Object object) {
        if (object == null || (object instanceof CharSequence && ((CharSequence) object).length() == 0)) {
            return type == int.class ? 0 : null;
        }
        return (Integer) (object instanceof Character ? object
                : object instanceof Number ? ((Number) object).intValue() : Integer.parseInt(object.toString()));
    }

    /**
     * 短整形类型转换
     *
     * @param object 被转换对象
     * @return 短整形对象
     */
    public static Short toShort(Object object) {
        return toShort(Short.class, object);
    }

    /**
     * 短整形类型转换
     *
     * @param type   短整形类型
     * @param object 被转换对象
     * @return 短整形对象
     */
    private static Short toShort(@Nonnull Class<Short> type, Object object) {
        if (object == null || (object instanceof CharSequence && ((CharSequence) object).length() == 0)) {
            return type == short.class ? (short) 0 : null;
        }
        return (Short) (object instanceof Short ? object
                : object instanceof Number ? ((Number) object).shortValue() : Short.parseShort(object.toString()));
    }

    /**
     * 单精度浮点类型转换
     *
     * @param object 被转换对象
     * @return 单精度浮点对象
     */
    public static Float toFloat(Object object) {
        return toFloat(Float.class, object);
    }

    /**
     * 单精度浮点类型转换
     *
     * @param type   单精度浮点类型
     * @param object 被转换对象
     * @return 单精度浮点对象
     */
    private static Float toFloat(@Nonnull Class<Float> type, Object object) {
        if (object == null || (object instanceof CharSequence && ((CharSequence) object).length() == 0)) {
            return type == float.class ? (float) 0 : null;
        }
        return (Float) (object instanceof Short ? object
                : object instanceof Number ? ((Number) object).floatValue() : Float.parseFloat(object.toString()));
    }

    /**
     * 双精度浮点类型转换
     *
     * @param object 被转换对象
     * @return 双精度浮点对象
     */
    public static Double toDouble(Object object) {
        return toDouble(Double.class, object);
    }

    /**
     * 双精度浮点类型转换
     *
     * @param type   双精度浮点类型
     * @param object 被转换对象
     * @return 双精度浮点对象
     */
    private static Double toDouble(@Nonnull Class<Double> type, Object object) {
        if (object == null || (object instanceof CharSequence && ((CharSequence) object).length() == 0)) {
            return type == double.class ? (double) 0 : null;
        }
        return (Double) (object instanceof Double ? object
                : object instanceof Number ? ((Number) object).doubleValue() : Double.parseDouble(object.toString()));
    }

    /**
     * 长整形类型转换
     *
     * @param object 被转换对象
     * @return 长整形对象
     */
    public static Long toLong(Object object) {
        return toLong(Long.class, object);
    }

    /**
     * 长整形类型转换
     *
     * @param type   长整形类型
     * @param object 被转换对象
     * @return 长整形对象
     */
    private static Long toLong(@Nonnull Class<Long> type, Object object) {
        if (object == null || (object instanceof CharSequence && ((CharSequence) object).length() == 0)) {
            return type == long.class ? (long) 0 : null;
        }
        return (Long) (object instanceof Long ? object
                : object instanceof Number ? ((Number) object).longValue() : Long.parseLong(object.toString()));
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
        return object == null || (object instanceof CharSequence && ((CharSequence) object).length() == 0) ? null
                : object instanceof Enum ? (T) object : Enum.valueOf(type, object.toString());
    }

    /**
     * 日期类型转换
     *
     * @param object 被转换对象
     * @return 日期
     */
    public static Date toDate(Object object) {
        return object == null || (object instanceof CharSequence && ((CharSequence) object).length() == 0) ? null
                : object instanceof Date ? (Date) object
                : object instanceof Number ? new Date(((Number) object).longValue())
                : Dates.parse(object.toString());
    }

    /**
     * 类对象转换
     *
     * @param object 被转换对象
     * @return 类对象
     */
    public static Class<?> toClass(Object object) {
        try {
            return object == null || (object instanceof CharSequence && ((CharSequence) object).length() == 0) ? null
                    : object instanceof Class ? (Class<?>) object : Class.forName(object.toString());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
