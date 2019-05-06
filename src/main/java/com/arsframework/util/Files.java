package com.arsframework.util;

import java.io.*;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

import com.arsframework.annotation.Min;
import com.arsframework.annotation.Nonnull;
import com.arsframework.annotation.Nonempty;

/**
 * 文件处理工具类
 *
 * @author yongqiang.wu
 */
public abstract class Files {
    /**
     * 创建文件目录
     *
     * @param file 文件对象
     * @return 文件目录对象
     */
    public static File mkdirs(File file) {
        File path = file == null ? null : file.getParentFile();
        if (path != null && !path.exists()) {
            path.mkdirs();
        }
        return path;
    }

    /**
     * 递归删除文件/文件目录
     *
     * @param file 源文件/文件目录
     */
    public static void delete(File file) {
        if (file != null && file.exists()) {
            if (file.isDirectory()) {
                for (File child : file.listFiles()) {
                    delete(child);
                }
            }
            file.delete();
        }
    }

    /**
     * 递归拷贝文件/文件目录
     *
     * @param source 源文件/文件目录
     * @param target 目标文件目录
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static void copy(File source, File target) throws IOException {
        if (!source.equals(target)) {
            if (source.isDirectory()) {
                File path = new File(target, source.getName());
                if (!path.exists()) {
                    path.mkdirs();
                }
                for (File child : source.listFiles()) {
                    copy(child, path);
                }
            } else {
                Streams.write(source, new File(target, source.getName()));
            }
        }
    }

    /**
     * 递归移动文件/文件目录
     *
     * @param source 源文件/文件目录
     * @param target 目标文件目录
     */
    @Nonnull
    public static void move(File source, File target) {
        if (!source.equals(target)) {
            if (source.isDirectory()) {
                File path = new File(target, source.getName());
                if (!path.exists()) {
                    path.mkdirs();
                }
                for (File child : source.listFiles()) {
                    move(child, path);
                }
                source.delete();
            } else {
                source.renameTo(new File(target, source.getName()));
            }
        }
    }

    /**
     * 文件重命名
     *
     * @param file 文件对象
     * @param name 重命名名称
     */
    @Nonnull
    public static void rename(File file, String name) {
        file.renameTo(new File(file.getParent(), name));
    }

    /**
     * 获取文件名称
     *
     * @param path 文件路径
     * @return 文件名称
     */
    @Nonnull
    public static String getName(String path) {
        int end = path.length();
        for (int i = end - 1; i > -1; i--) {
            if (path.charAt(i) == '\\' || path.charAt(i) == '/') {
                if (i < end - 1) {
                    return path.substring(i + 1, end);
                }
                end--;
            }
        }
        String name = end == path.length() ? path : path.substring(0, end);
        return name.isEmpty() ? null : name;
    }

    /**
     * 获取文件后缀名
     *
     * @param path 文件路径
     * @return 后缀名
     */
    @Nonnull
    public static String getSuffix(String path) {
        int index = path.lastIndexOf('.');
        if (index < 0) {
            return null;
        }
        int end = path.length() - 1;
        for (; end > -1 && (path.charAt(end) == '\\' || path.charAt(end) == '/'); end--) ;
        String suffix = path.substring(index + 1, end + 1);
        return suffix.isEmpty() ? null : suffix;
    }

    /**
     * 文件/文件目录属性
     *
     * @param <T> 属性类型
     */
    public static final class Property<T> {
        /**
         * 属性类型
         */
        public final Class<T> type;

        /**
         * 属性名称
         */
        public final String name;

        @Nonempty
        private Property(Class<T> type, String name) {
            this.name = name;
            this.type = type;
        }

        /**
         * 文件/文件目录名称属性
         */
        public static final Property<String> NAME = new Property<>(String.class, "name");

        /**
         * 文件/文件目录大小属性
         */
        public static final Property<Long> SIZE = new Property<>(Long.class, "size");

        /**
         * 文件/文件目录最后修改时间属性
         */
        public static final Property<Long> MODIFIED = new Property<>(Long.class, "modified");

        /**
         * 是否为文件目录属性
         */
        public static final Property<Boolean> DIRECTORY = new Property<>(Boolean.class, "directory");

        @Override
        public boolean equals(Object other) {
            return other instanceof Property && this.type == ((Property) other).type && this.name.equals(((Property) other).name);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(new Object[]{this.name, this.type});
        }
    }

    /**
     * 文件比较条件接口
     */
    public interface Condition {
    }

    /**
     * 文件描述对象
     */
    public static class Describe implements Comparable<Describe>, Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 文件/文件目录相对路径
         */
        public final String path;

        /**
         * 文件/文件目录名称
         */
        public final String name;

        /**
         * 文件/文件目录大小
         */
        public final long size;

        /**
         * 文件/文件目录最后修改时间
         */
        public final long modified;

        /**
         * 是否为文件目录
         */
        public final boolean directory;

        @Nonnull
        public Describe(String path, String name, @Min(0) long size, long modified, boolean directory) {
            this.path = path;
            this.name = name;
            this.size = size;
            this.modified = modified;
            this.directory = directory;
        }

        @Override
        public int compareTo(Describe other) {
            return this.path.compareTo(other.path);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Describe && this.path.equals(((Describe) other).path);
        }

        @Override
        public int hashCode() {
            return 31 + this.path.hashCode();
        }

        @Override
        public String toString() {
            return this.path;
        }

        /**
         * 文件/文件描述转换
         *
         * @param file 文件对象
         * @return 文件描述对象
         */
        @Nonnull
        public static Describe parse(File file) {
            return new Describe(file.getPath().replace("\\", "/"),
                    file.getName(), file.length(), file.lastModified(), file.isDirectory());
        }
    }

    /**
     * 等于条件
     *
     * @param <T> 数据类型
     */
    public static class Equal<T> implements Condition {
        public final Property<T> property; // 比较属性
        public final T[] values; // 比较值数组

        @Nonnull
        public Equal(Property<T> property, T... values) {
            this.property = property;
            this.values = values;
        }

    }

    /**
     * 不等于
     *
     * @param <T> 数据类型
     */
    public static class NotEqual<T> implements Condition {
        public final Property<T> property; // 比较属性
        public final T[] values; // 比较值数组

        @Nonnull
        public NotEqual(Property<T> property, T... values) {
            this.property = property;
            this.values = values;
        }

    }

    /**
     * 大于
     *
     * @param <T> 数据类型
     */
    public static class Large<T> implements Condition {
        public final Property<T> property; // 比较属性
        public final T value; // 比较值

        @Nonnull
        public Large(Property<T> property, T value) {
            this.property = property;
            this.value = value;
        }

    }

    /**
     * 大于或等于
     *
     * @param <T> 数据类型
     */
    public static class LargeEqual<T> implements Condition {
        public final Property<T> property; // 比较属性
        public final T value; // 比较值

        @Nonnull
        public LargeEqual(Property<T> property, T value) {
            this.property = property;
            this.value = value;
        }

    }

    /**
     * 小于
     *
     * @param <T> 数据类型
     */
    public static class Less<T> implements Condition {
        public final Property<T> property; // 比较属性
        public final T value; // 比较值

        @Nonnull
        public Less(Property<T> property, T value) {
            this.property = property;
            this.value = value;
        }

    }

    /**
     * 小于或等于
     *
     * @param <T> 数据类型
     */
    public static class LessEqual<T> implements Condition {
        public final Property<T> property; // 比较属性
        public final T value; // 比较值

        @Nonnull
        public LessEqual(Property<T> property, T value) {
            this.property = property;
            this.value = value;
        }

    }

    /**
     * 大于和小于
     *
     * @param <T> 数据类型
     */
    public static class Between<T> implements Condition {
        public final Property<T> property; // 比较属性
        public final T min; // 最小值
        public final T max; // 最大值

        @Nonnull
        public Between(Property<T> property, T min, T max) {
            this.property = property;
            this.min = min;
            this.max = max;
        }

    }

    /**
     * 模糊匹配
     *
     * @param <T> 数据类型
     */
    public static class Like<T> implements Condition {
        public final Property<T> property; // 比较属性
        public final T value; // 比较值
        public final Position position; // 比较位置

        public Like(Property<T> property, T value) {
            this(property, value, Position.ANY);
        }

        @Nonnull
        public Like(Property<T> property, T value, Position position) {
            this.property = property;
            this.value = value;
            this.position = position;
        }

        /**
         * 匹配位置
         */
        public enum Position {
            /**
             * 开始位置
             */
            BEGIN,

            /**
             * 结束位置
             */
            END,

            /**
             * 任何位置
             */
            ANY;

        }

    }

    /**
     * 排序条件
     */
    public static class Order implements Condition {
        public final Property property; // 排序属性

        @Nonnull
        public Order(Property property) {
            this.property = property;
        }

    }

    /**
     * 升序排序条件
     */
    public static class Asc extends Order {

        public Asc(Property property) {
            super(property);
        }

    }

    /**
     * 降序排序条件
     */
    public static class Desc extends Order {

        public Desc(Property property) {
            super(property);
        }

    }

    /**
     * 等于
     *
     * @param property 属性
     * @param values   属性值数组
     * @param <T>      数据类型
     * @return 条件对象
     */
    public static <T> Equal eq(Property<T> property, T... values) {
        return new Equal(property, values);
    }

    /**
     * 不等于
     *
     * @param property 属性
     * @param values   属性值数组
     * @param <T>      数据类型
     * @return 条件对象
     */
    public static <T> NotEqual ne(Property<T> property, T... values) {
        return new NotEqual(property, values);
    }

    /**
     * 大于
     *
     * @param property 属性
     * @param value    属性值
     * @param <T>      数据类型
     * @return 条件对象
     */
    public static <T> Large gt(Property<T> property, T value) {
        return new Large(property, value);
    }

    /**
     * 大于或等于
     *
     * @param property 属性
     * @param value    属性值
     * @param <T>      数据类型
     * @return 条件对象
     */
    public static <T> LargeEqual ge(Property<T> property, T value) {
        return new LargeEqual(property, value);
    }

    /**
     * 小于
     *
     * @param property 属性
     * @param value    属性值
     * @param <T>      数据类型
     * @return 条件对象
     */
    public static <T> Less lt(Property<T> property, T value) {
        return new Less(property, value);
    }

    /**
     * 小于或等于
     *
     * @param property 属性
     * @param value    属性值
     * @param <T>      数据类型
     * @return 条件对象
     */
    public static <T> LessEqual le(Property<T> property, T value) {
        return new LessEqual(property, value);
    }

    /**
     * 属性值在两个值之间
     *
     * @param property 属性
     * @param min      最小值
     * @param max      最大值
     * @param <T>      数据类型
     * @return 条件对象
     */
    public static <T> Between between(Property<T> property, T min, T max) {
        return new Between(property, min, max);
    }

    /**
     * 包含指定字符串
     *
     * @param property 属性
     * @param value    属性值
     * @param <T>      数据类型
     * @return 条件对象
     */
    public static <T> Like like(Property<T> property, T value) {
        return like(property, value, Like.Position.ANY);
    }

    /**
     * 包含指定字符串
     *
     * @param property 属性
     * @param value    属性值
     * @param position 匹配位置
     * @param <T>      数据类型
     * @return 条件对象
     */
    public static <T> Like like(Property<T> property, T value, Like.Position position) {
        return new Like(property, value, position);
    }

    /**
     * 升序条件
     *
     * @param property 属性
     * @return 条件对象
     */
    public static Asc asc(Property property) {
        return new Asc(property);
    }

    /**
     * 降序条件
     *
     * @param property 属性
     * @return 条件对象
     */
    public static Desc desc(Property property) {
        return new Desc(property);
    }

    /**
     * 判断文件描述是否满足小于条件
     *
     * @param describe 文件描述对象
     * @param less     小于条件
     * @return true/false
     */
    @Nonnull
    public static boolean isSatisfy(Describe describe, Less less) {
        return (less.property == Property.NAME && describe.name.compareToIgnoreCase((String) less.value) < 0)
                || (less.property == Property.SIZE && describe.size < (long) less.value)
                || (less.property == Property.MODIFIED && describe.modified < (long) less.value);
    }

    /**
     * 判断文件描述是否满足模糊匹配条件
     *
     * @param describe 文件描述对象
     * @param like     模糊匹配条件
     * @return true/false
     */
    @Nonnull
    public static boolean isSatisfy(Describe describe, Like like) {
        if (like.property == Property.NAME) {
            String source = describe.name.toUpperCase();
            String value = ((String) like.value).toUpperCase();
            return (like.position == Like.Position.BEGIN && source.indexOf(value) == 0)
                    || (like.position == Like.Position.END && source.lastIndexOf(value) == source.length() - value.length())
                    || (like.position == Like.Position.ANY && source.indexOf(value) > -1);
        }
        return true;
    }

    /**
     * 判断文件描述是否满足等于条件
     *
     * @param describe 文件描述对象
     * @param equal    等于条件
     * @return true/false
     */
    @Nonnull
    public static boolean isSatisfy(Describe describe, Equal equal) {
        for (Object value : equal.values) {
            if ((equal.property == Property.NAME && describe.name.equalsIgnoreCase((String) value))
                    || (equal.property == Property.SIZE && describe.size == (long) value)
                    || (equal.property == Property.MODIFIED && describe.modified == (long) value)
                    || (equal.property == Property.DIRECTORY && describe.directory == (boolean) value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断文件描述是否满足大于条件
     *
     * @param describe 文件描述对象
     * @param large    大于条件
     * @return true/false
     */
    @Nonnull
    public static boolean isSatisfy(Describe describe, Large large) {
        return (large.property == Property.NAME && describe.name.compareToIgnoreCase((String) large.value) > 0)
                || (large.property == Property.SIZE && describe.size > (long) large.value)
                || (large.property == Property.MODIFIED && describe.modified > (long) large.value);
    }

    /**
     * 判断文件描述是否满足大于小于条件
     *
     * @param describe 文件描述对象
     * @param between  大于小于条件
     * @return true/false
     */
    @Nonnull
    public static boolean isSatisfy(Describe describe, Between between) {
        return (between.property == Property.NAME && describe.name.compareToIgnoreCase((String) between.min) >= 0
                && describe.name.compareToIgnoreCase((String) between.max) <= 0)
                || (between.property == Property.SIZE
                && describe.size >= (long) between.min && describe.size <= (long) between.max)
                || (between.property == Property.MODIFIED
                && describe.modified >= (long) between.min && describe.modified <= (long) between.max);
    }

    /**
     * 判断文件描述是否满足不等于条件
     *
     * @param describe 文件描述对象
     * @param notEqual 不等于条件
     * @return true/false
     */
    @Nonnull
    public static boolean isSatisfy(Describe describe, NotEqual notEqual) {
        return !isSatisfy(describe, new Equal(notEqual.property, notEqual.values));
    }

    /**
     * 判断文件描述是否满足小于或等于条件
     *
     * @param describe  文件描述对象
     * @param lessEqual 小于或等于条件
     * @return true/false
     */
    @Nonnull
    public static boolean isSatisfy(Describe describe, LessEqual lessEqual) {
        return (lessEqual.property == Property.NAME && describe.name.compareToIgnoreCase((String) lessEqual.value) <= 0)
                || (lessEqual.property == Property.SIZE && describe.size <= (long) lessEqual.value)
                || (lessEqual.property == Property.MODIFIED && describe.modified <= (long) lessEqual.value);
    }

    /**
     * 判断文件描述是否满足大于或等于条件
     *
     * @param describe   文件描述对象
     * @param largeEqual 大于或等于条件
     * @return true/false
     */
    @Nonnull
    public static boolean isSatisfy(Describe describe, LargeEqual largeEqual) {
        return (largeEqual.property == Property.NAME && describe.name.compareToIgnoreCase((String) largeEqual.value) >= 0)
                || (largeEqual.property == Property.SIZE && describe.size >= (long) largeEqual.value)
                || (largeEqual.property == Property.MODIFIED && describe.modified >= (long) largeEqual.value);
    }

    /**
     * 判断文件描述是否满足查询条件
     *
     * @param describe   文件描述对象
     * @param conditions 查询条件数组
     * @return true/false
     */
    @Nonnull
    public static boolean isSatisfy(Describe describe, Condition... conditions) {
        for (Condition condition : conditions) {
            if (condition instanceof Less && !isSatisfy(describe, (Less) condition)) {
                return false;
            } else if (condition instanceof Like && !isSatisfy(describe, (Like) condition)) {
                return false;
            } else if (condition instanceof Equal && !isSatisfy(describe, (Equal) condition)) {
                return false;
            } else if (condition instanceof Large && !isSatisfy(describe, (Large) condition)) {
                return false;
            } else if (condition instanceof Between && !isSatisfy(describe, (Between) condition)) {
                return false;
            } else if (condition instanceof NotEqual && !isSatisfy(describe, (NotEqual) condition)) {
                return false;
            } else if (condition instanceof LessEqual && !isSatisfy(describe, (LessEqual) condition)) {
                return false;
            } else if (condition instanceof LargeEqual && !isSatisfy(describe, (LargeEqual) condition)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 文件描述比较
     *
     * @param describe 比较文件描述对象
     * @param other    被比较文件描述对象
     * @param orders   排序对象
     * @return 比较结果
     */
    public static int compare(Describe describe, Describe other, @Nonnull Order... orders) {
        if (describe != null && other != null) {
            for (Order order : orders) {
                int compare = 0;
                if (order.property == Property.NAME) {
                    compare = describe.name.compareTo(other.name);
                } else if (order.property == Property.SIZE) {
                    compare = describe.size < other.size ? -1 : describe.size == other.size ? 0 : 1;
                } else if (order.property == Property.MODIFIED) {
                    compare = describe.modified < other.modified ? -1 : describe.modified == other.modified ? 0 : 1;
                } else if (order.property == Property.DIRECTORY) {
                    compare = describe.directory && !other.directory ? -1 : describe.directory == other.directory ? 0 : 1;
                }
                if (compare != 0) {
                    return order instanceof Asc ? compare : -compare;
                }
            }
        }
        return describe == null && other == null ? 0 : describe == null ? -1 : other == null ? 1 : 0;
    }

    /**
     * 对文件描述列表排序
     *
     * @param describes 文件描述对象数组
     * @param orders    排序对象数组
     */
    @Nonnull
    public static void sort(Describe[] describes, Order... orders) {
        Arrays.sort(describes, (Describe o1, Describe o2) -> compare(o1, o2, orders));
    }

    /**
     * 对文件描述列表排序
     *
     * @param describes 文件描述对象列表
     * @param orders    排序对象数组
     */
    @Nonnull
    public static void sort(List<Describe> describes, Order... orders) {
        Collections.sort(describes, (Describe o1, Describe o2) -> compare(o1, o2, orders));
    }
}
