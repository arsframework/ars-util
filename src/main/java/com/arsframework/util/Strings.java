package com.arsframework.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.List;
import java.util.Date;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.regex.Pattern;

import com.arsframework.annotation.Min;
import com.arsframework.annotation.Nonnull;
import com.arsframework.annotation.Nonempty;

/**
 * 字符串处理工具类
 *
 * @author yongqiang.wu
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
     * GBK编码
     */
    public static final String CHARSET_GBK = "GBK";

    /**
     * UTF8编码
     */
    public static final String CHARSET_UTF8 = "UTF-8";

    /**
     * ISO-8859-1编码
     */
    public static final String CHARSET_ISO_8859_1 = "ISO-8859-1";

    /**
     * 16进制字符串序列
     */
    public static final String HEX_SEQUENCE = "0123456789ABCDEF";

    /**
     * 当前主机
     */
    public static final String LOCALHOST = "localhost";

    /**
     * 系统名称
     */
    public static final String SYSTEM_NAME = System.getProperty("os.name");

    /**
     * 当前主机名称
     */
    public static final String LOCALHOST_NAME;

    /**
     * 当前主机地址
     */
    public static final String LOCALHOST_ADDRESS;

    /**
     * 默认当前主机地址
     */
    public static final String DEFAULT_LOCALHOST_ADDRESS = "127.0.0.1";

    /**
     * 根路径
     */
    public static final String ROOT_URI = "/";

    /**
     * 临时文件目录
     */
    public static final String TEMP_PATH = System.getProperty("java.io.tmpdir");

    /**
     * 当前文件目录
     */
    public static final String CURRENT_PATH = Strings.class.getResource("/").getPath();

    /**
     * URL正则表达式匹配模式
     */
    public static final Pattern URL_PATTERN = Pattern.compile("http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?");

    /**
     * 字符串列表正则表达式匹配模式
     */
    public static final Pattern LIST_PATTERN = Pattern.compile(" *\\[.*\\] *");

    /**
     * 邮箱正则表达式匹配模式
     */
    public static final Pattern EMAIL_PATTERN = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");

    /**
     * 因为字母正则表达式匹配模式
     */
    public static final Pattern LETTER_PATTERN = Pattern.compile("[a-zA-Z]+");

    /**
     * 字符串列表正则表达式匹配模式
     */
    public static final Pattern NUMBER_PATTERN = Pattern.compile("-?(\\.?[0-9]+|[0-9]+\\.?[0-9]+|[0-9]+\\.?)");

    /**
     * 数字/英文字符数组
     */
    public static final Character[] CHARS = new Character[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
            'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z'};

    /**
     * 当前线程数字格式化对象
     */
    public static final ThreadLocal<DecimalFormat> DEFAULT_DECIMAL_FORMAT = ThreadLocal.withInitial(() -> new DecimalFormat("0.##"));

    static {
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getLocalHost();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOCALHOST_NAME = localhost == null ? null : localhost.getHostName();

        if (SYSTEM_NAME.startsWith("Windows")) {
            LOCALHOST_ADDRESS = localhost == null ? DEFAULT_LOCALHOST_ADDRESS : localhost.getHostAddress();
        } else {
            String ip = null;
            try {
                Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
                outer:
                while (enumeration.hasMoreElements()) {
                    Enumeration<InetAddress> addresses = enumeration.nextElement().getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        if (address.isSiteLocalAddress() && !address.isLoopbackAddress()
                                && (ip = address.getHostAddress()).indexOf(':') == -1) {
                            break outer;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            LOCALHOST_ADDRESS = ip == null ? DEFAULT_LOCALHOST_ADDRESS : ip;
        }
    }

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
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0, pos = 0; i < bytes.length; i++, pos = i * 2) {
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
            for (int i = 0, offset = radix - s.length(); i < offset; i++) {
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
        char[] chars = new char[hex.length() / radix];
        for (int i = 0; i < chars.length; i++) {
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
     * 判断字符串是否为空白
     *
     * @param source 字符串
     * @return true/false
     */
    public static boolean isBlank(CharSequence source) {
        return source == null || source.length() == 0 || source.toString().trim().isEmpty();
    }

    /**
     * 判断字符串是否为URL
     *
     * @param source 字符串
     * @return true/false
     */
    public static boolean isUrl(CharSequence source) {
        return !isEmpty(source) && URL_PATTERN.matcher(source).matches();
    }

    /**
     * 判断字符串是否为Email
     *
     * @param source 字符串
     * @return true/false
     */
    public static boolean isEmail(CharSequence source) {
        return !isEmpty(source) && EMAIL_PATTERN.matcher(source).matches();
    }

    /**
     * 判断字符串是否为数字
     *
     * @param source 字符串
     * @return true/false
     */
    public static boolean isNumber(CharSequence source) {
        return !isEmpty(source) && NUMBER_PATTERN.matcher(source).matches();
    }

    /**
     * 判断字符串是否为因为字母
     *
     * @param source 字符串
     * @return true/false
     */
    public static boolean isLetter(CharSequence source) {
        return !isEmpty(source) && LETTER_PATTERN.matcher(source).matches();
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
     * 清理字符串前后空格
     *
     * @param source 源字符串
     * @return 清理后字符串
     */
    public static String trim(String source) {
        return source == null || source.isEmpty() || (source = source.trim()).isEmpty() ? null : source;
    }

    /**
     * 清理字符串中所有空格及换行符
     *
     * @param source 源字符串
     * @return 清理后字符串
     */
    public static String clean(CharSequence source) {
        if (isEmpty(source)) {
            return null;
        }
        StringBuilder buffer = new StringBuilder(source.length());
        for (int i = 0, len = source.length(); i < len; i++) {
            char c = source.charAt(i);
            if (!Character.isWhitespace(c)) {
                buffer.append(c);
            }
        }
        return buffer.length() == 0 ? null : buffer.length() == source.length() ? source.toString() : buffer.toString();
    }

    /**
     * 将对象数组链接成字符串
     *
     * @param objects 对象数组
     * @return 字符串
     */
    public static String join(Object... objects) {
        return join(objects, EMPTY_STRING);
    }

    /**
     * 将对象数组链接成字符串
     *
     * @param objects 对象数组
     * @param sign    链接标记
     * @return 连接后的字符串
     */
    @Nonnull
    public static String join(Object[] objects, CharSequence sign) {
        return objects.length == 0 ? EMPTY_STRING : join(Arrays.asList(objects), sign);
    }

    /**
     * 将对象集合链接成字符串
     *
     * @param objects 对象集合
     * @return 字符串
     */
    public static String join(Collection<?> objects) {
        return join(objects, EMPTY_STRING);
    }

    /**
     * 将对象集合链接成字符串
     *
     * @param objects 对象集合
     * @param sign    链接标记
     * @return 连接后的字符串
     */
    @Nonnull
    public static String join(Collection<?> objects, CharSequence sign) {
        if (objects.isEmpty()) {
            return EMPTY_STRING;
        }
        StringBuilder buffer = new StringBuilder();
        for (Object object : objects) {
            if (buffer.length() > 0 && sign.length() > 0) {
                buffer.append(sign);
            }
            buffer.append(object);
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
                throw new IllegalStateException("Path does not exist: " + path);
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
        if (object instanceof BigDecimal) {
            return ((BigDecimal) object).stripTrailingZeros().toPlainString();
        } else if (object instanceof Float || object instanceof Double) {
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
     * 四舍五入数字
     *
     * @param number 数字对象
     * @return 四舍五入结果数字
     */
    public static BigDecimal roundNumber(Number number) {
        return roundNumber(number, 2);
    }

    /**
     * 四舍五入数字
     *
     * @param number 数字对象
     * @param scale  保留小数位数
     * @return 四舍五入结果数字
     */
    @Nonnull
    public static BigDecimal roundNumber(Number number, @Min(0) int scale) {
        return BigDecimal.valueOf(number.doubleValue()).setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 格式化数字（四舍五入，保留2为小数）
     *
     * @param number 数字对象
     * @return 格式化数字字符串
     */
    @Nonnull
    public static String formatNumber(Number number) {
        return DEFAULT_DECIMAL_FORMAT.get().format(number);
    }

    /**
     * 将数据大小转换成带单位的大小表示
     *
     * @param size 数据大小
     * @return 数据大小单位表示
     */
    public static String formatUnitSize(@Min(0) long size) {
        if (size == 0) {
            return "0Byte";
        }
        StringBuilder buffer = new StringBuilder();
        if (size >= 1099511627776L) {
            buffer.append(formatNumber(size / 1099511627776d)).append("TB");
        } else if (size >= 1073741824) {
            buffer.append(formatNumber(size / 1073741824d)).append("GB");
        } else if (size >= 1048576) {
            buffer.append(formatNumber(size / 1048576d)).append("MB");
        } else if (size >= 1024) {
            buffer.append(formatNumber(size / 1024d)).append("KB");
        } else {
            buffer.append(size).append("Byte");
        }
        return buffer.toString();
    }

    /**
     * 将时间长度毫秒数转换成带单位的时间表示（d:天、h:时、m:分、s:秒、ms:毫秒）
     *
     * @param time 时间长度毫秒数
     * @return 带单位的时间表示
     */
    public static String formatUnitTime(@Min(0) long time) {
        if (time == 0) {
            return "0ms";
        }
        StringBuilder buffer = new StringBuilder();
        if (time >= 31536000000L) {
            buffer.append(formatNumber(time / 31536000000d)).append('y');
        } else if (time >= 86400000) {
            buffer.append(formatNumber(time / 86400000d)).append('d');
        } else if (time >= 3600000) {
            buffer.append(formatNumber(time / 3600000d)).append('h');
        } else if (time >= 60000) {
            buffer.append(formatNumber(time / 60000d)).append('m');
        } else if (time >= 1000) {
            buffer.append(formatNumber(time / 1000d)).append('s');
        } else {
            buffer.append(time).append("ms");
        }
        return buffer.toString();
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
     * 将字符串转换成列表对象
     *
     * @param source 源字符串
     * @return 对象列表
     */
    private static List<?> parseList(CharSequence source) {
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
            list.add(b.length() == 0 ? null : LIST_PATTERN.matcher(b).matches() ? parseList(b) : b.toString());
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
        if (isBlank(expression)) {
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
                String key = split < 1 ? null : section.substring(0, split).trim();
                if (isEmpty(key)) {
                    throw new IllegalArgumentException("Invalid expression: " + expression);
                }
                String value = section.substring(split + 1).trim();
                _condition = new Match(key, value.isEmpty() ? null : LIST_PATTERN.matcher(value).matches() ? parseList(value) : value);
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
