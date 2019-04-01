package com.arsframework.util;

import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

import com.arsframework.annotation.Assert;

/**
 * 文件处理工具类
 *
 * @author yongqiang.wu
 * @version 2019-03-22 09:38
 */
public abstract class Files {
    /**
     * 当前线程数字格式化对象
     */
    private static final ThreadLocal<DecimalFormat> decimalFormat = ThreadLocal.withInitial(() -> new DecimalFormat("0.##"));

    /**
     * 创建文件目录
     *
     * @param file 文件对象
     * @return 文件目录对象
     */
    public static File mkdirs(File file) {
        File path = file.getParentFile();
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
    @Assert
    public static void delete(File file) {
        if (file.exists()) {
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
    @Assert
    public static void copy(File source, File target) throws IOException {
        if (source.exists()) {
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
    @Assert
    public static void move(File source, File target) {
        if (source.exists()) {
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
     * 获取文件名称
     *
     * @param path 文件路径
     * @return 文件名称
     */
    @Assert
    public static String getName(String path) {
        for (int i = path.length() - 1; i > -1; i--) {
            if (path.charAt(i) == '\\' || path.charAt(i) == '/') {
                return path.substring(i + 1);
            }
        }
        return path;
    }

    /**
     * 获取文件后缀名
     *
     * @param path 文件路径
     * @return 后缀名
     */
    @Assert
    public static String getSuffix(String path) {
        int index = path.lastIndexOf('.');
        return index > 0 && index < path.length() - 1 ? path.substring(index + 1) : null;
    }

    /**
     * 获取文件内容
     *
     * @param file 文件对象
     * @return 文件内容
     * @throws IOException IO操作异常
     */
    @Assert
    public static String getString(File file) throws IOException {
        try (OutputStream os = new ByteArrayOutputStream()) {
            Streams.write(file, os);
            return os.toString();
        }
    }

    /**
     * 获取文件内容
     *
     * @param file 文件对象
     * @return 文件内容
     * @throws IOException IO操作异常
     */
    @Assert
    public static String getString(Nfile file) throws IOException {
        try (OutputStream os = new ByteArrayOutputStream()) {
            Streams.write(file, os);
            return os.toString();
        }
    }

    /**
     * 获取文件行列表
     *
     * @param file 文件对象
     * @return 行列表
     * @throws IOException IO操作异常
     */
    @Assert
    public static List<String> getLines(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            return getLines(is);
        }
    }

    /**
     * 获取文件行列表
     *
     * @param file 文件对象
     * @return 行列表
     * @throws IOException IO操作异常
     */
    @Assert
    public static List<String> getLines(Nfile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            return getLines(is);
        }
    }

    /**
     * 获取文件行列表
     *
     * @param reader 文件字符流
     * @return 行列表
     * @throws IOException IO操作异常
     */
    @Assert
    public static List<String> getLines(Reader reader) throws IOException {
        List<String> lines = new LinkedList<>();
        try (BufferedReader buffer = new BufferedReader(reader)) {
            String line;
            while ((line = buffer.readLine()) != null && !(line = line.trim()).isEmpty()) {
                lines.add(line);
            }
        }
        return lines;
    }

    /**
     * 获取文件行列表
     *
     * @param stream 文件输入流
     * @return 行列表
     * @throws IOException IO操作异常
     */
    @Assert
    public static List<String> getLines(InputStream stream) throws IOException {
        return getLines(new InputStreamReader(stream));
    }

    /**
     * 将文件大小转换成带单位的文件大小表示
     *
     * @param size 文件大小
     * @return 带单位的文件大小表示
     */
    public static String toUnitSize(long size) {
        if (size < 0) {
            throw new IllegalArgumentException("Argument size must not be less than 0, got: " + size);
        }
        StringBuilder buffer = new StringBuilder();
        if (size >= 1073741824) {
            buffer.append(decimalFormat.get().format(size / 1073741824d)).append("GB");
        } else if (size >= 1048576) {
            buffer.append(decimalFormat.get().format(size / 1048576d)).append("MB");
        } else if (size >= 1024) {
            buffer.append(decimalFormat.get().format(size / 1024d)).append("KB");
        } else {
            buffer.append(size).append("Byte");
        }
        return buffer.toString();
    }

    /**
     * 文件/文件目录属性枚举
     */
    public enum Property {
        /**
         * 文件/文件目录名称属性
         */
        NAME,

        /**
         * 文件/文件目录大小属性
         */
        SIZE,

        /**
         * 文件/文件目录最后修改时间属性
         */
        MODIFIED,

        /**
         * 是否为文件目录属性
         */
        DIRECTORY;

    }

    /**
     * 文件比较条件接口
     */
    public interface Condition {
    }

    /**
     * 文件描述对象
     */
    public static class Describe implements Serializable {
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
        public final Date modified;

        /**
         * 是否为文件目录
         */
        public final boolean directory;

        public Describe(File file) {
            this(file.getPath().replace("\\", "/"), file.getName(), file.length(), new Date(file.lastModified()), file.isDirectory());
        }

        public Describe(String path, String name, long size, Date modified, boolean directory) {
            this.path = path;
            this.name = name;
            this.size = size;
            this.modified = modified;
            this.directory = directory;
        }

        @Override
        public String toString() {
            return this.path == null ? super.toString() : this.path;
        }
    }

    /**
     * 等于条件
     */
    public static class Equal implements Condition {
        public final Property property; // 比较属性
        public final Object value; // 比较值

        @Assert
        public Equal(Property property, Object value) {
            this.property = property;
            this.value = value;
        }

    }

    /**
     * 不等于
     */
    public static class NotEqual implements Condition {
        public final Property property; // 比较属性
        public final Object value; // 比较值

        @Assert
        public NotEqual(Property property, Object value) {
            this.property = property;
            this.value = value;
        }

    }

    /**
     * 大于
     */
    public static class Large implements Condition {
        public final Property property; // 比较属性
        public final Object value; // 比较值

        @Assert
        public Large(Property property, Object value) {
            this.property = property;
            this.value = value;
        }

    }

    /**
     * 大于或等于
     */
    public static class LargeEqual implements Condition {
        public final Property property; // 比较属性
        public final Object value; // 比较值

        @Assert
        public LargeEqual(Property property, Object value) {
            this.property = property;
            this.value = value;
        }

    }

    /**
     * 小于
     */
    public static class Less implements Condition {
        public final Property property; // 比较属性
        public final Object value; // 比较值

        @Assert
        public Less(Property property, Object value) {
            this.property = property;
            this.value = value;
        }

    }

    /**
     * 小于或等于
     */
    public static class LessEqual implements Condition {
        public final Property property; // 比较属性
        public final Object value; // 比较值

        @Assert
        public LessEqual(Property property, Object value) {
            this.property = property;
            this.value = value;
        }

    }

    /**
     * 大于和小于
     */
    public static class Between implements Condition {
        public final Property property; // 比较属性
        public final Object low; // 低值
        public final Object high; // 高值

        @Assert
        public Between(Property property, Object low, Object high) {
            this.property = property;
            this.low = low;
            this.high = high;
        }

    }

    /**
     * 模糊匹配
     */
    public static class Like implements Condition {
        public final Property property; // 比较属性
        public final String value; // 比较值
        public final Position position; // 比较位置

        public Like(Property property, String value) {
            this(property, value, Position.ANY);
        }

        @Assert
        public Like(Property property, String value, Position position) {
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

        @Assert
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
     * @param value    属性值
     * @return 条件对象
     */
    public static Equal eq(Property property, Object value) {
        return new Equal(property, valueAdapter(property, value));
    }

    /**
     * 不等于
     *
     * @param property 属性
     * @param value    属性值
     * @return 条件对象
     */
    public static NotEqual ne(Property property, Object value) {
        return new NotEqual(property, valueAdapter(property, value));
    }

    /**
     * 大于
     *
     * @param property 属性
     * @param value    属性值
     * @return 条件对象
     */
    public static Large gt(Property property, Object value) {
        return new Large(property, valueAdapter(property, value));
    }

    /**
     * 大于或等于
     *
     * @param property 属性
     * @param value    属性值
     * @return 条件对象
     */
    public static LargeEqual ge(Property property, Object value) {
        return new LargeEqual(property, valueAdapter(property, value));
    }

    /**
     * 小于
     *
     * @param property 属性
     * @param value    属性值
     * @return 条件对象
     */
    public static Less lt(Property property, Object value) {
        return new Less(property, valueAdapter(property, value));
    }

    /**
     * 小于或等于
     *
     * @param property 属性
     * @param value    属性值
     * @return 条件对象
     */
    public static LessEqual le(Property property, Object value) {
        return new LessEqual(property, valueAdapter(property, value));
    }

    /**
     * 属性值在两个值之间
     *
     * @param property 属性
     * @param low      低值
     * @param high     高值
     * @return 条件对象
     */
    public static Between between(Property property, Object low, Object high) {
        return new Between(property, valueAdapter(property, low), valueAdapter(property, high));
    }

    /**
     * 包含指定字符串
     *
     * @param property 属性
     * @param value    属性值
     * @param position 匹配位置
     * @return 条件对象
     */
    public static Like like(Property property, String value, Like.Position position) {
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
     * 将值类型转换成属性对应类型
     *
     * @param property 属性
     * @param value    值
     * @return 转换后的值
     */
    public static Object valueAdapter(@Assert Property property, Object value) {
        boolean array = value != null && value.getClass().isArray();
        if (property == Property.NAME) {
            return array ? Objects.toArray(String.class, value) : Objects.toObject(String.class, value);
        } else if (property == Property.SIZE) {
            return array ? Objects.toArray(long.class, value) : Objects.toObject(long.class, value);
        } else if (property == Property.MODIFIED) {
            return array ? Objects.toArray(Date.class, value) : Objects.toObject(Date.class, value);
        }
        return array ? Objects.toArray(boolean.class, value) : Objects.toObject(boolean.class, value);
    }

    /**
     * 判断文件描述是否满足小于条件
     *
     * @param describe 文件描述对象
     * @param less     小于条件
     * @return true/false
     */
    @Assert
    public static boolean isSatisfy(Describe describe, Less less) {
        if (less.property == Property.NAME && describe.name.compareToIgnoreCase((String) less.value) > -1) {
            return false;
        } else if (less.property == Property.SIZE && describe.size >= (Long) less.value) {
            return false;
        } else if (less.property == Property.MODIFIED && !describe.modified.before((Date) less.value)) {
            return false;
        }
        return describe.directory == false && (Boolean) less.value == true;
    }

    /**
     * 判断文件描述是否满足模糊匹配条件
     *
     * @param describe 文件描述对象
     * @param like     模糊匹配条件
     * @return true/false
     */
    @Assert
    public static boolean isSatisfy(Describe describe, Like like) {
        String value = like.value.toUpperCase();
        String source;
        if (like.property == Property.NAME) {
            source = describe.name.toUpperCase();
        } else if (like.property == Property.SIZE) {
            source = String.valueOf(describe.size);
        } else if (like.property == Property.MODIFIED) {
            source = Dates.format(describe.modified);
        } else {
            source = String.valueOf(describe.directory);
        }
        if (like.position == Like.Position.BEGIN && source.indexOf(value) > 0) {
            return false;
        } else if (like.position == Like.Position.END && source.length() > source.lastIndexOf(value) + value.length()) {
            return false;
        } else if (like.position == Like.Position.ANY && source.indexOf(value) < 0) {
            return false;
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
    @Assert
    public static boolean isSatisfy(Describe describe, Equal equal) {
        int matched = 0;
        if (equal.property == Property.NAME) {
            if (equal.value instanceof String[]) {
                for (String name : (String[]) equal.value) {
                    if (describe.name.equalsIgnoreCase(name)) {
                        matched++;
                        break;
                    }
                }
            } else if (describe.name.equalsIgnoreCase((String) equal.value)) {
                matched++;
            }
        } else if (equal.property == Property.SIZE) {
            if (equal.value instanceof long[]) {
                for (long size : (long[]) equal.value) {
                    if (describe.size == size) {
                        matched++;
                        break;
                    }
                }
            } else if (describe.size == (Long) equal.value) {
                matched++;
            }
        } else if (equal.property == Property.MODIFIED) {
            if (equal.value instanceof Date[]) {
                for (Date date : (Date[]) equal.value) {
                    if (describe.modified.compareTo(date) == 0) {
                        matched++;
                        break;
                    }
                }
            } else if (describe.modified.compareTo((Date) equal.value) == 0) {
                matched++;
            }
        } else {
            if (equal.value instanceof boolean[]) {
                for (boolean directory : (boolean[]) equal.value) {
                    if (describe.directory == directory) {
                        matched++;
                        break;
                    }
                }
            } else if (describe.directory == (Boolean) equal.value) {
                matched++;
            }
        }
        return matched > 0;
    }

    /**
     * 判断文件描述是否满足大于条件
     *
     * @param describe 文件描述对象
     * @param large    大于条件
     * @return true/false
     */
    @Assert
    public static boolean isSatisfy(Describe describe, Large large) {
        if (large.property == Property.NAME && describe.name.compareToIgnoreCase((String) large.value) < 0) {
            return false;
        } else if (large.property == Property.SIZE && describe.size <= (Long) large.value) {
            return false;
        } else if (large.property == Property.MODIFIED && !describe.modified.after((Date) large.value)) {
            return false;
        }
        return describe.directory == true && (Boolean) large.value == false;
    }

    /**
     * 判断文件描述是否满足大于小于条件
     *
     * @param describe 文件描述对象
     * @param between  大于小于条件
     * @return true/false
     */
    @Assert
    public static boolean isSatisfy(Describe describe, Between between) {
        if (between.property == Property.NAME && (describe.name.compareToIgnoreCase((String) between.low) < 0
                || describe.name.compareToIgnoreCase((String) between.high) > 0)) {
            return false;
        } else if (between.property == Property.SIZE
                && (describe.size < (Long) between.low || describe.size > (Long) between.high)) {
            return false;
        } else if (between.property == Property.MODIFIED
                && (describe.modified.before((Date) between.low) || describe.modified.after((Date) between.high))) {
            return false;
        }
        return true;
    }

    /**
     * 判断文件描述是否满足不等于条件
     *
     * @param describe 文件描述对象
     * @param notEqual 不等于条件
     * @return true/false
     */
    @Assert
    public static boolean isSatisfy(Describe describe, NotEqual notEqual) {
        if (notEqual.property == Property.NAME) {
            if (notEqual.value instanceof String[]) {
                for (String name : (String[]) notEqual.value) {
                    if (describe.name.equalsIgnoreCase(name)) {
                        return false;
                    }
                }
            } else if (describe.name.equalsIgnoreCase((String) notEqual.value)) {
                return false;
            }
        } else if (notEqual.property == Property.SIZE) {
            if (notEqual.value instanceof long[]) {
                for (long size : (long[]) notEqual.value) {
                    if (describe.size == size) {
                        return false;
                    }
                }
            } else if (describe.size == (Long) notEqual.value) {
                return false;
            }
        } else if (notEqual.property == Property.MODIFIED) {
            if (notEqual.value instanceof Date[]) {
                for (Date date : (Date[]) notEqual.value) {
                    if (describe.modified.compareTo(date) == 0) {
                        return false;
                    }
                }
            } else if (describe.modified.compareTo((Date) notEqual.value) == 0) {
                return false;
            }
        } else {
            if (notEqual.value instanceof boolean[]) {
                for (boolean directory : (boolean[]) notEqual.value) {
                    if (describe.directory == directory) {
                        return false;
                    }
                }
            } else if (describe.directory == (Boolean) notEqual.value) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断文件描述是否满足小于或等于条件
     *
     * @param describe  文件描述对象
     * @param lessEqual 小于或等于条件
     * @return true/false
     */
    @Assert
    public static boolean isSatisfy(Describe describe, LessEqual lessEqual) {
        if (lessEqual.property == Property.NAME && describe.name.compareToIgnoreCase((String) lessEqual.value) > 0) {
            return false;
        } else if (lessEqual.property == Property.SIZE && describe.size > (Long) lessEqual.value) {
            return false;
        } else if (lessEqual.property == Property.MODIFIED && describe.modified.after((Date) lessEqual.value)) {
            return false;
        }
        return describe.directory == false;
    }

    /**
     * 判断文件描述是否满足大于或等于条件
     *
     * @param describe   文件描述对象
     * @param largeEqual 大于或等于条件
     * @return true/false
     */
    @Assert
    public static boolean isSatisfy(Describe describe, LargeEqual largeEqual) {
        if (largeEqual.property == Property.NAME && describe.name.compareToIgnoreCase((String) largeEqual.value) < 0) {
            return false;
        } else if (largeEqual.property == Property.SIZE && describe.size < (Long) largeEqual.value) {
            return false;
        } else if (largeEqual.property == Property.MODIFIED && describe.modified.before((Date) largeEqual.value)) {
            return false;
        }
        return describe.directory == true;
    }

    /**
     * 判断文件描述是否满足查询条件
     *
     * @param describe   文件描述对象
     * @param conditions 查询条件数组
     * @return true/false
     */
    @Assert
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
     * 文件比较
     *
     * @param file   比较文件
     * @param other  被比较文件
     * @param orders 排序对象
     * @return 比较结果
     */
    @Assert
    public static int compare(File file, File other, Order... orders) {
        for (Order order : orders) {
            int compare = 0;
            if (order.property == Property.NAME) {
                compare = file.getName().compareTo(other.getName());
            } else if (order.property == Property.SIZE) {
                long l1 = file.length();
                long l2 = other.length();
                compare = l1 < l2 ? -1 : l1 == l2 ? 0 : 1;
            } else if (order.property == Property.MODIFIED) {
                long m1 = file.lastModified();
                long m2 = other.lastModified();
                compare = m1 < m2 ? -1 : m1 == m2 ? 0 : 1;
            } else if (order.property == Property.DIRECTORY) {
                boolean d1 = file.isDirectory();
                boolean d2 = other.isDirectory();
                compare = d1 && !d2 ? -1 : d1 == d2 ? 0 : 1;
            }
            if (compare != 0) {
                return order instanceof Asc ? compare : -compare;
            }
        }
        return 0;
    }

    /**
     * 文件描述比较
     *
     * @param describe 比较文件描述对象
     * @param other    被比较文件描述对象
     * @param orders   排序对象
     * @return 比较结果
     */
    @Assert
    public static int compare(Describe describe, Describe other, Order... orders) {
        for (Order order : orders) {
            int compare = 0;
            if (order.property == Property.NAME) {
                compare = describe.name.compareTo(other.name);
            } else if (order.property == Property.SIZE) {
                compare = describe.size < other.size ? -1 : describe.size == other.size ? 0 : 1;
            } else if (order.property == Property.MODIFIED) {
                compare = describe.modified.compareTo(other.modified);
            } else if (order.property == Property.DIRECTORY) {
                compare = describe.directory && !other.directory ? -1 : describe.directory == other.directory ? 0 : 1;
            }
            if (compare != 0) {
                return order instanceof Asc ? compare : -compare;
            }
        }
        return 0;
    }

    /**
     * 对文件列表排序
     *
     * @param files  文件对象数组
     * @param orders 排序对象数组
     */
    @Assert
    public static void sort(File[] files, Order... orders) {
        Arrays.sort(files, (File o1, File o2) -> Files.compare(o1, o2, orders));
    }

    /**
     * 对文件描述列表排序
     *
     * @param describes 文件描述对象数组
     * @param orders    排序对象数组
     */
    @Assert
    public static void sort(Describe[] describes, Order... orders) {
        Arrays.sort(describes, (Describe o1, Describe o2) -> Files.compare(o1, o2, orders));
    }

    /**
     * 对文件描述列表排序
     *
     * @param describes 文件描述对象列表
     * @param orders    排序对象数组
     */
    @Assert
    public static void sort(List<Describe> describes, Order... orders) {
        Collections.sort(describes, (Describe o1, Describe o2) -> Files.compare(o1, o2, orders));
    }
}
