package com.arsframework.util;

import java.util.*;
import java.io.File;
import java.net.URL;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

import com.arsframework.annotation.Min;
import com.arsframework.annotation.Nonnull;
import com.arsframework.annotation.Nonempty;

/**
 * 字符串处理工具类
 *
 * @author yongqiang.wu
 * @version 2019-03-22 09:38
 */
public abstract class Strings {
    /**
     * 空字符串
     */
    public static final String EMPTY_STRING = "";

    /**
     * 空字符串数组
     */
    public static final String[] EMPTY_ARRAY = new String[0];

    /**
     * GBK字符集
     */
    public static final String CHARSET_GBK = "GBK";

    /**
     * UTF8字符集
     */
    public static final String CHARSET_UTF8 = "UTF-8";

    /**
     * 16进制字符串序列
     */
    public static final String HEX_SEQUENCE = "0123456789ABCDEF";

    /**
     * 当前文件目录
     */
    public static final String CURRENT_PATH = Strings.class.getResource("/").getPath();

    /**
     * 数字/英文字符数组
     */
    public static final Character[] CHARS = new Character[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
            'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z'};

    /**
     * 字符串列表正则表达式匹配模式
     */
    private static Pattern listPattern;

    /**
     * 将字符转换字节
     *
     * @param c 字符
     * @return 字节
     */
    public static byte char2byte(char c) {
        return (byte) HEX_SEQUENCE.indexOf(c);
    }

    /**
     * 将16进制字符串转换成字节数组
     *
     * @param hex 16进制字符串
     * @return 字节数组
     */
    @Nonnull
    public static byte[] hex2byte(String hex) {
        int length = hex.length() / 2;
        byte[] bytes = new byte[length];
        for (int i = 0, pos = 0; i < length; i++, pos = i * 2) {
            bytes[i] = (byte) (char2byte(hex.charAt(pos)) << 4 | char2byte(hex.charAt(pos + 1)));
        }
        return bytes;
    }

    /**
     * 将字节数组转换16进制
     *
     * @param bytes 字节数组
     * @return 16进制字符串
     */
    @Nonnull
    public static String byte2hex(byte[] bytes) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                buffer.append('0');
            }
            buffer.append(hex);
        }
        return buffer.toString();
    }

    /**
     * 将字符按照2字符一单元转换成16进制
     *
     * @param chars 字符数组
     * @param radix 单元长度
     * @return 16进制字符串
     */
    @Nonnull
    public static String char2hex(char[] chars, @Min(1) int radix) {
        StringBuilder buffer = new StringBuilder();
        for (char c : chars) {
            String s = Integer.toHexString(c);
            int offset = radix - s.length();
            for (int i = 0; i < offset; i++) {
                buffer.append('0');
            }
            buffer.append(s);
        }
        return buffer.toString();
    }

    /**
     * 将16进制字符串按照2字符一单元转换成10进制字符
     *
     * @param hex   16进制字符串
     * @param radix 单元长度
     * @return 10进制字符数组
     */
    @Nonnull
    public static char[] hex2char(String hex, @Min(1) int radix) {
        int len = hex.length() / radix;
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) {
            chars[i] = (char) Integer.parseInt(hex.substring(i * radix, (i + 1) * radix), 16);
        }
        return chars;
    }

    /**
     * 将unicode字符串转换成字符数组
     *
     * @param unicode unicode字符串
     * @return 字符数组
     */
    @Nonnull
    public static char[] unicode2char(String unicode) {
        int index = -1, _index = -1;
        StringBuilder buffer = new StringBuilder();
        while ((index = unicode.indexOf("\\u", index + 1)) > -1) {
            int offset;
            if (_index > -1 && (offset = index - _index - 6) > 0) {
                if (offset > 0) {
                    buffer.append(unicode.substring(_index + 6, _index + 6 + offset));
                }
            } else if (index > 0 && _index < 0) {
                buffer.append(unicode.substring(0, index));
            }
            buffer.append(hex2char(unicode.substring(index + 2, index + 6), 4));
            _index = index;
        }
        if (_index < 0) {
            return unicode.toCharArray();
        }
        if (_index + 6 < unicode.length()) {
            buffer.append(unicode.substring(_index + 6));
        }
        char[] chars = new char[buffer.length()];
        buffer.getChars(0, buffer.length(), chars, 0);
        return chars;
    }

    /**
     * 判断字符串是否为空
     *
     * @param source 字符串
     * @return true/false
     */
    public static boolean isEmpty(CharSequence source) {
        return source == null || source.length() == 0;
    }

    /**
     * 判断字符串是否为空格
     *
     * @param source 字符串
     * @return true/false
     */
    public static boolean isSpace(CharSequence source) {
        return !isEmpty(source) && isBlank(source);
    }

    /**
     * 判断字符串是否为空白
     *
     * @param source 字符串
     * @return true/false
     */
    public static boolean isBlank(CharSequence source) {
        if (!isEmpty(source)) {
            for (int i = 0, len = source.length(); i < len; i++) {
                if (!Character.isWhitespace(source.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取目标字符串在指定字符串中出现的次数，非正则表达式匹配
     *
     * @param source 源字符串
     * @param sign   目标字符串
     * @return 次数
     */
    @Nonnull
    public static int count(CharSequence source, char sign) {
        int count = 0;
        if (source.length() > 0) {
            for (int i = 0, slen = source.length(); i < slen; i++) {
                if (source.charAt(i) == sign) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 获取目标字符串在指定字符串中出现的次数，非正则表达式匹配
     *
     * @param source 源字符串
     * @param sign   目标字符串
     * @return 次数
     */
    @Nonnull
    public static int count(CharSequence source, CharSequence sign) {
        int count = 0;
        if (source.length() > 0 && sign.length() > 0) {
            source:
            for (int i = 0, slen = source.length(), tlen = sign.length(); i < slen; i++) {
                for (int j = 0, k = i + 0; j < tlen; j++, k = i + j) {
                    if (k >= slen || source.charAt(k) != sign.charAt(j)) {
                        continue source;
                    }
                }
                count++;
                i += tlen - 1;
            }
        }
        return count;
    }

    /**
     * 清理字符串中所有空格及换行符
     *
     * @param source 源字符串
     * @return 清理后字符串
     */
    public static String clean(CharSequence source) {
        if (isEmpty(source)) {
            return source == null ? null : EMPTY_STRING;
        }
        StringBuilder buffer = new StringBuilder();
        for (int i = 0, len = source.length(); i < len; i++) {
            char c = source.charAt(i);
            if (!Character.isWhitespace(c)) {
                buffer.append(c);
            }
        }
        return buffer.toString();
    }

    /**
     * 将对象数组链接成字符串
     *
     * @param array 对象数组
     * @return 字符串
     */
    public static String join(Object[] array) {
        return join(array, EMPTY_STRING);
    }

    /**
     * 将对象数组链接成字符串
     *
     * @param array 对象数组
     * @param sign  链接标记
     * @return 连接后的字符串
     */
    @Nonnull
    public static String join(Object[] array, CharSequence sign) {
        return array.length == 0 ? EMPTY_STRING : join(Arrays.asList(array), sign);
    }

    /**
     * 将对象集合链接成字符串
     *
     * @param collection 对象集合
     * @return 字符串
     */
    public static String join(Collection<?> collection) {
        return join(collection, EMPTY_STRING);
    }

    /**
     * 将对象集合链接成字符串
     *
     * @param collection 对象集合
     * @param sign       链接标记
     * @return 连接后的字符串
     */
    @Nonnull
    public static String join(Collection<?> collection, CharSequence sign) {
        if (collection.isEmpty()) {
            return EMPTY_STRING;
        }
        StringBuilder buffer = new StringBuilder();
        for (Object o : collection) {
            if (buffer.length() > 0 && sign.length() > 0) {
                buffer.append(sign);
            }
            buffer.append(o);
        }
        return buffer.toString();
    }

    /**
     * 字符串匹配，不支持正则表达式匹配，多个表达式之间使用“,”号隔开（*：通配,-：排除），如果使用排除则优先生效
     *
     * @param source  源字符串
     * @param pattern 匹配模式
     * @return true/false
     */
    public static boolean matches(String source, String pattern) {
        if (source == null || pattern == null) {
            return false;
        } else if ((source.isEmpty() && pattern.isEmpty()) || (pattern.length() == 1 && pattern.charAt(0) == '*')) {
            return true;
        }
        int matches = 0;
        boolean notall = true;
        for (String section : pattern.split(",")) {
            if (section.isEmpty()) {
                continue;
            }
            int index = -1;
            boolean matched = true;
            boolean not = section.charAt(0) == '-';
            if (notall && !not) {
                notall = false;
            }
            String[] signs = (not ? section.substring(1) : section).split("\\*");
            for (String sign : signs) {
                if (sign.isEmpty()) {
                    continue;
                }
                if ((index = source.indexOf(sign, index + 1)) < 0) {
                    matched = false;
                    break;
                }
            }
            if (matched && source.length() > index + signs[signs.length - 1].length() && section.charAt(section.length() - 1) != '*') {
                matched = false;
            }
            if (matched) {
                if (not) {
                    return false;
                }
                matches++;
            }
        }
        return (notall && matches == 0) || (!notall && matches > 0);
    }

    /**
     * 特殊字符转义
     *
     * @param source 源字符串
     * @return 转义后字符串
     */
    public static String escape(CharSequence source) {
        if (isEmpty(source)) {
            return source == null ? null : EMPTY_STRING;
        }
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            switch (c) {
                case '\'':
                    buffer.append("\\\"");
                    break;
                case '\"':
                    buffer.append("\\\"");
                    break;
                case '\\':
                    buffer.append("\\\\");
                    break;
                case '\b':
                    buffer.append("\\b");
                    break;
                case '\f':
                    buffer.append("\\f");
                    break;
                case '\n':
                    buffer.append("\\n");
                    break;
                case '\r':
                    buffer.append("\\r");
                    break;
                case '\t':
                    buffer.append("\\t");
                    break;
                default:
                    if (!((c >= 0 && c <= 31) || c == 127)) {
                        buffer.append(c);
                    }
                    break;
            }
        }
        return buffer.toString();
    }

    /**
     * 获取实际路径 “./”表示当前路径、“../”表示当前路径上一级目录
     *
     * @param path 路径
     * @return 实际路径
     */
    public static String toRealPath(String path) {
        if (isEmpty(path)) {
            return CURRENT_PATH;
        } else if (path.startsWith("./")) {
            return new File(CURRENT_PATH, path.substring(1)).getPath();
        } else if (path.startsWith("../")) {
            int count = count(path, "../");
            File _path = new File(CURRENT_PATH);
            for (int i = 0; i < count; i++) {
                File parent = _path.getParentFile();
                if (parent == null) {
                    break;
                }
                _path = parent;
            }
            return new File(_path, path.substring(count * 3)).getPath();
        }
        int index = path.indexOf(':');
        if (index > 0 && path.substring(0, index).toLowerCase().equals("classpath")) {
            URL url = String.class.getClassLoader().getResource(path.substring(index + 1));
            if (url == null) {
                throw new RuntimeException("URL does not exist:" + url);
            }
            return url.getFile();
        }
        return path;
    }

    /**
     * 将对象转换成字符串形式
     *
     * @param object 对象
     * @return 字符串形式
     */
    public static String toString(Object object) {
        if (object instanceof Float || object instanceof Double) {
            return new BigDecimal(object.toString()).stripTrailingZeros().toPlainString();
        } else if (object instanceof CharSequence) {
            return ((CharSequence) object).toString();
        } else if (object instanceof Date) {
            return Dates.format((Date) object);
        } else if (object instanceof LocalDate) {
            return Dates.format((LocalDate) object);
        } else if (object instanceof LocalDateTime) {
            return Dates.format((LocalDateTime) object);
        } else if (object instanceof Class) {
            return ((Class<?>) object).getName();
        } else if (object instanceof byte[]) {
            return Arrays.toString((byte[]) object);
        } else if (object instanceof char[]) {
            return Arrays.toString((char[]) object);
        } else if (object instanceof int[]) {
            return Arrays.toString((int[]) object);
        } else if (object instanceof short[]) {
            return Arrays.toString((short[]) object);
        } else if (object instanceof long[]) {
            return Arrays.toString((long[]) object);
        } else if (object instanceof float[]) {
            return Arrays.toString((float[]) object);
        } else if (object instanceof double[]) {
            return Arrays.toString((double[]) object);
        } else if (object instanceof boolean[]) {
            return Arrays.toString((boolean[]) object);
        } else if (object instanceof Object[]) {
            return Arrays.toString((Object[]) object);
        }
        return object == null ? null : object.toString();
    }

    /**
     * 条件接口
     */
    public interface Condition {

    }

    /**
     * 条件包装器抽象实现
     */
    public static abstract class AbstractConditionWrapper implements Condition {
        private final List<Condition> conditions = new LinkedList<>(); // 条件集合

        @Nonempty
        public AbstractConditionWrapper(Condition... conditions) {
            for (Condition condition : conditions) {
                this.conditions.add(condition);
            }
        }

        @Nonempty
        public AbstractConditionWrapper(Collection<Condition> conditions) {
            this.conditions.addAll(conditions);
        }

        @Nonempty
        public AbstractConditionWrapper(Map<String, Object> conditions) {
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                this.conditions.add(new Match(entry.getKey(), entry.getValue()));
            }
        }

        /**
         * 获取条件分割标识
         *
         * @return 标识符
         */
        protected abstract String getSeparator();

        @Nonnull
        public void addCondition(Condition condition) {
            this.conditions.add(condition);
        }

        public List<Condition> getConditions() {
            return new ArrayList<>(conditions);
        }

        @Override
        public String toString() {
            StringBuilder buffer = new StringBuilder();
            for (Condition condition : this.conditions) {
                if (buffer.length() > 0) {
                    buffer.append(this.getSeparator());
                }
                if (!(condition instanceof Match)) {
                    buffer.append('(');
                }
                buffer.append(condition);
                if (!(condition instanceof Match)) {
                    buffer.append(')');
                }
            }
            return buffer.toString();
        }

    }

    /**
     * 或逻辑实现
     */
    public static class Or extends AbstractConditionWrapper {
        /**
         * 分割符号
         */
        public static final String SEPARATOR = " or ";

        public Or(Condition... conditions) {
            super(conditions);
        }

        public Or(Collection<Condition> conditions) {
            super(conditions);
        }

        public Or(Map<String, Object> conditions) {
            super(conditions);
        }

        @Override
        protected String getSeparator() {
            return SEPARATOR;
        }
    }

    /**
     * 与逻辑实现
     */
    public static class And extends AbstractConditionWrapper {
        /**
         * 分割符号
         */
        public static final String SEPARATOR = " and ";

        public And(Condition... conditions) {
            super(conditions);
        }

        public And(Collection<Condition> conditions) {
            super(conditions);
        }

        public And(Map<String, Object> conditions) {
            super(conditions);
        }

        @Override
        protected String getSeparator() {
            return SEPARATOR;
        }

    }

    /**
     * 条件匹配逻辑实现
     */
    public static class Match implements Condition {
        public final String key;
        public final Object value;

        public Match(@Nonempty String key, Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            StringBuilder buffer = new StringBuilder(this.key).append('=');
            return this.value == null ? buffer.toString() : buffer.append(this.value).toString();
        }

    }

    /**
     * 构建字符串列表正则表达式匹配模式
     *
     * @return 正则表达式匹配模式对象
     */
    private static Pattern buildListPattern() {
        if (listPattern == null) {
            synchronized (Strings.class) {
                if (listPattern == null) {
                    listPattern = Pattern.compile(" *\\[.*\\] *");
                }
            }
        }
        return listPattern;
    }

    /**
     * 判断字符串是否为列表形式
     *
     * @param source 字符串对象
     * @return true/false
     */
    public static boolean isList(CharSequence source) {
        return isBlank(source) ? false : buildListPattern().matcher(source).matches();
    }

    /**
     * 将字符串转换成列表对象
     *
     * @param source 源字符串
     * @return 对象列表
     */
    @Nonempty
    public static List<?> toList(CharSequence source) {
        int skip = 0;
        StringBuilder buffer = new StringBuilder();
        List<StringBuilder> buffers = new LinkedList<>();
        for (int i = 1; i < source.length() - 1; i++) {
            char c = source.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            } else if (c == ',' && skip == 0) {
                buffers.add(buffer);
                buffer = new StringBuilder();
            } else {
                buffer.append(c);
                if (c == '[') {
                    skip++;
                } else if (c == ']') {
                    skip--;
                }
            }
        }
        buffers.add(buffer);
        List<Object> list = new ArrayList<>(buffers.size());
        for (StringBuilder b : buffers) {
            if (b.length() == 0) {
                list.add(null);
            } else {
                list.add(isList(b) ? toList(b) : b.toString());
            }
        }
        return list;
    }

    /**
     * 条件表达式逻辑对象转换
     *
     * @param expression 条件表达式
     * @return 条件逻辑对象
     */
    public static Condition condition(String expression) {
        if (isEmpty(expression)) {
            return null;
        }
        boolean continued = false;
        int offset = 0, start = 0, end = 0;
        List<String> sections = new LinkedList<>();
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '(') {
                if (start == end) {
                    offset = i;
                }
                start++;
            } else if (c == ')') {
                if (start == ++end) {
                    sections.add(expression.substring(offset, i + 1));
                    start = 0;
                    end = 0;
                    offset = i + 1;
                    continued = true;
                }
            } else if (start == end) {
                int index;
                String handle;
                if ((i > 3 && (handle = expression.substring(index = i - 3, i + 1).toLowerCase()).equals(Or.SEPARATOR))
                        || (i > 4 && (handle = expression.substring(index = i - 4, i + 1).toLowerCase()).equals(And.SEPARATOR))) {
                    if (!continued) {
                        sections.add(expression.substring(offset, index));
                    }
                    offset = i + 1;
                    continued = false;
                    sections.add(handle);
                }
            }
        }
        if (start != end) {
            throw new IllegalStateException("Illegal expression:" + expression);
        }
        if (offset < expression.length()) {
            sections.add(expression.substring(offset));
        }
        Condition condition = null;
        for (int i = 0; i < sections.size(); i += 2) {
            Condition _condition;
            String section = sections.get(i).trim();
            if (section.isEmpty()) {
                continue;
            }
            if (section.charAt(0) == '(' && section.charAt(section.length() - 1) == ')') {
                section = section.substring(1, section.length() - 1).trim();
                if (section.isEmpty()) {
                    continue;
                }
                _condition = condition(section);
            } else {
                int split = section.indexOf("=");
                String key = split < 0 ? section.trim() : section.substring(0, split).trim();
                if (isBlank(key)) {
                    continue;
                }
                String value = split < 0 ? null : section.substring(split + 1).trim();
                _condition = new Match(key, isList(value) ? toList(value) : isBlank(value) ? null : value);
            }
            if (condition == null) {
                condition = _condition;
            } else if (sections.get(i - 1).equals(Or.SEPARATOR)) {
                if (condition instanceof Or) {
                    ((Or) condition).addCondition(_condition);
                } else {
                    condition = new Or(condition, _condition);
                }
            } else {
                if (condition instanceof And) {
                    ((And) condition).addCondition(_condition);
                } else {
                    condition = new And(condition, _condition);
                }
            }
        }
        return condition;
    }
}
