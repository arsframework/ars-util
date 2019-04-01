package com.arsframework.util;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.net.URLDecoder;
import java.net.URLEncoder;
import javax.servlet.http.Cookie;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.nio.channels.ReadableByteChannel;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.arsframework.annotation.Assert;

/**
 * @author yongqiang.wu
 * @description Web处理工具类
 * @date 2019-03-22 09:38
 */
public abstract class Webs {
    /**
     * 应用根路径
     */
    public static final String ROOT_PATH = new File(Strings.CURRENT_PATH).getParentFile().getParentFile().getPath();

    /**
     * Html标本标签正则表达匹配对象
     */
    public static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*?>[\\s\\S]*?<\\/script>", Pattern.CASE_INSENSITIVE);

    /**
     * Html解析器
     */
    private static final ParserDelegator parserDelegator = new ParserDelegator();

    /**
     * 获取Cookie
     *
     * @param request Http请求对象
     * @param name    Cookie名称
     * @return Cookie值
     */
    @Assert
    public static String getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(name)) {
                    try {
                        String value = URLDecoder.decode(cookie.getValue(), Strings.CHARSET_UTF8);
                        return value == null || value.isEmpty() ? null : value;
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 设置Cookie
     *
     * @param response Http响应对象
     * @param name     Cookie名称
     * @param value    Cookie值
     * @param timeout  过期时间（秒）
     */
    @Assert
    public static void setCookie(HttpServletResponse response, String name, String value, int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("Argument timeout must not be less than 0, got: " + timeout);
        }
        try {
            Cookie cookie = new Cookie(name, value == null ? Strings.EMPTY_STRING : URLEncoder.encode(value, Strings.CHARSET_UTF8));
            cookie.setPath("/");
            cookie.setMaxAge(timeout);
            response.addCookie(cookie);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取并删除Cookie
     *
     * @param request  Http请求对象
     * @param response Http响应对象
     * @param name     Cookie名称
     * @return Cookie值
     */
    @Assert
    public static String removeCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(name)) {
                    try {
                        return URLDecoder.decode(cookie.getValue(), Strings.CHARSET_UTF8);
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    } finally {
                        cookie.setMaxAge(0);
                        response.addCookie(cookie);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取请求资源地址（不包含应用上下文地址）
     *
     * @param request HTTP请求对象
     * @return 资源地址
     */
    @Assert
    public static String getUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        return context == null ? uri : uri.substring(context.length());
    }

    /**
     * 获取HTTP请求的URL地址（不包含资源地址）
     *
     * @param request HTTP请求对象
     * @return URL地址
     */
    @Assert
    public static String getUrl(HttpServletRequest request) {
        StringBuilder url = new StringBuilder(request.getScheme()).append("://").append(request.getServerName())
                .append(':').append(request.getServerPort());
        String context = request.getContextPath();
        return context == null ? url.toString() : url.append(context).toString();
    }

    /**
     * 拼接字符串参数
     *
     * @param param 参数串
     * @param key   拼接参数名称
     * @param value 拼接参数值
     */
    private static void concatParam(@Assert StringBuilder param, @Assert String key, Object value) {
        if (param.length() > 0) {
            param.append("&");
        }
        param.append(key).append("=");
        if (!Objects.isEmpty(value)) {
            param.append(Strings.toString(value));
        }
    }

    /**
     * 将键/值映射表转化成Http字符串形式参数
     *
     * @param map 键/值映射表
     * @return 参数字符串形式
     */
    @Assert
    public static String map2param(Map<?, ?> map) {
        if (map.isEmpty()) {
            return Strings.EMPTY_STRING;
        }
        StringBuilder param = new StringBuilder();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            String key = Strings.toString(entry.getKey());
            Object value = entry.getValue();
            if (value instanceof Object[] || value instanceof Collection) {
                for (Object o : value instanceof Object[] ? Arrays.asList((Object[]) value) : (Collection<?>) value) {
                    concatParam(param, key, o);
                }
            } else {
                concatParam(param, key, value);
            }
        }
        return param.toString();
    }

    /**
     * 将参数字符串形式转换成键/值映射
     *
     * @param param 参数字符串形式
     * @return 键/值映射
     */
    public static Map<String, Object> param2map(String param) {
        if (param.isEmpty()) {
            return new HashMap<>(0);
        }
        String[] sections = param.split("&");
        Map<String, Object> parameters = new HashMap<>(sections.length);
        for (String section : sections) {
            if ((section = section.trim()).isEmpty()) {
                continue;
            }
            String[] kv = section.split("=");
            String key = kv[0].trim();
            if (key.isEmpty()) {
                continue;
            }
            String value = kv.length > 1 ? kv[1].trim() : null;
            Object exist = parameters.get(key);
            if (exist == null) {
                parameters.put(key, value);
            } else if (value != null) {
                if (exist instanceof List) {
                    ((List<String>) exist).add(value);
                } else {
                    List<String> list = new LinkedList<>();
                    list.add((String) exist);
                    list.add(value);
                    parameters.put(key, list);
                }
            }
        }
        return parameters;
    }

    /**
     * 获取Http请求数据字节流
     *
     * @param request Http请求对象
     * @return 字节数组
     * @throws IOException IO操作异常
     */
    @Assert
    public static byte[] getBytes(HttpServletRequest request) throws IOException {
        try (InputStream is = request.getInputStream()) {
            return Streams.getBytes(is);
        }
    }

    /**
     * 获取URL参数
     *
     * @param url 资源地址
     * @return 参数键/值映射
     */
    public static Map<String, Object> getUrlParameters(String url) {
        int index = url == null ? -1 : url.indexOf('?');
        return index < 0 ? new HashMap<>(0) : param2map(url.substring(index + 1));
    }

    /**
     * 获取普通表单请求参数
     *
     * @param request HTTP请求对象
     * @return 参数键/值表
     */
    @Assert
    public static Map<String, Object> getFormParameters(HttpServletRequest request) {
        Map<String, Object> parameters = new HashMap<>();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Object value = null;
            String[] values = request.getParameterValues(name);
            if (values.length == 1) {
                String param = values[0].trim();
                if (!param.isEmpty()) {
                    value = param;
                }
            } else {
                List<String> _values = new ArrayList<>(values.length);
                for (int i = 0; i < values.length; i++) {
                    String param = values[i].trim();
                    if (!param.isEmpty()) {
                        _values.add(param);
                    }
                }
                if (_values.size() == 1) {
                    value = _values.get(0);
                } else if (!_values.isEmpty()) {
                    value = _values;
                }
            }
            parameters.put(name, value);
        }
        return parameters;
    }

    /**
     * 获取文件上传表单参数
     *
     * @param request  HTTP请求对象
     * @param uploader 文件上传处理器
     * @return 参数键/值表
     * @throws FileUploadException 文件上传异常
     */
    @Assert
    public static Map<String, Object> getUploadParameters(HttpServletRequest request, ServletFileUpload uploader)
            throws FileUploadException {
        List<?> items = uploader.parseRequest(request);
        Map<String, Object> parameters = new HashMap<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            final FileItem item = (FileItem) items.get(i);
            Object value = null;
            String name = item.getFieldName();
            if (item.isFormField()) {
                try {
                    String param = new String(item.get(), Strings.CHARSET_UTF8).trim();
                    if (!param.isEmpty()) {
                        value = param;
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                File file = ((DiskFileItem) item).getStoreLocation();
                value = new Nfile(Files.getName(item.getName())) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public long getSize() {
                        return item.getSize();
                    }

                    @Override
                    public boolean isLocal() {
                        return file.exists();
                    }

                    @Override
                    public File getLocal() {
                        return file;
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        return item.getInputStream();
                    }

                };
            }
            Object o = parameters.get(name);
            if (o == null) {
                parameters.put(name, value);
            } else if (value != null) {
                if (o instanceof List) {
                    ((List<Object>) o).add(value);
                } else {
                    List<Object> values = new LinkedList<>();
                    values.add(o);
                    values.add(value);
                    parameters.put(name, values);
                }
            }
        }
        return parameters;
    }

    /**
     * 获取JSON参数
     *
     * @param request HTTP请求对象
     * @return 参数键/值表
     * @throws IOException IO操作异常
     */
    @Assert
    public static Map<String, Object> getJsonParameters(HttpServletRequest request) throws IOException {
        String json = new String(getBytes(request));
        return json.isEmpty() ? Collections.emptyMap() : (Map<String, Object>) Jsons.parse(json);
    }

    /**
     * 视图渲染
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param template 视图模板名称
     * @throws IOException      IO操作异常
     * @throws ServletException Servlet操作异常
     */
    public static void render(HttpServletRequest request, HttpServletResponse response, String template)
            throws IOException, ServletException {
        render(request, response, template, Collections.emptyMap());
    }

    /**
     * 视图渲染
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param template 视图模板名称
     * @param context  渲染上下文数据
     * @throws IOException      IO操作异常
     * @throws ServletException Servlet操作异常
     */
    @Assert
    public static void render(HttpServletRequest request, HttpServletResponse response, String template,
                              Map<String, Object> context) throws IOException, ServletException {
        try (OutputStream os = response.getOutputStream()) {
            render(request, response, template, context, os);
        }
    }


    /**
     * 视图渲染
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param template 视图模板名称
     * @param output   数据输出流
     * @throws IOException      IO操作异常
     * @throws ServletException Servlet操作异常
     */
    public static void render(HttpServletRequest request, HttpServletResponse response, String template, OutputStream output)
            throws IOException, ServletException {
        render(request, response, template, Collections.emptyMap(), output);
    }


    /**
     * 视图渲染
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param template 视图模板名称
     * @param context  渲染上下文数据
     * @param output   数据输出流
     * @throws IOException      IO操作异常
     * @throws ServletException Servlet操作异常
     */
    @Assert
    public static void render(HttpServletRequest request, HttpServletResponse response, String template, Map<String, Object> context,
                              OutputStream output) throws IOException, ServletException {
        template = template.replace("\\", "/").replace("//", "/");
        if (template.charAt(0) != '/') {
            template = new StringBuilder("/").append(template).toString();
        }
        if (!new File(ROOT_PATH, template).exists()) {
            throw new IOException("Template does not exist:" + template);
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher(template);
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(output));
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
        dispatcher.include(request, new HttpServletResponseWrapper(response) {

            @Override
            public PrintWriter getWriter() {
                return writer;
            }

        });
        writer.flush();
    }

    /**
     * 获取视图内容
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param template 视图模板名称
     * @return 视图内容
     * @throws IOException      IO操作异常
     * @throws ServletException Servlet操作异常
     */
    public static String view(HttpServletRequest request, HttpServletResponse response, String template)
            throws IOException, ServletException {
        return view(request, response, template, Collections.emptyMap());
    }

    /**
     * 获取视图内容
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param template 视图模板名称
     * @param context  渲染上下文数据
     * @return 视图内容
     * @throws IOException      IO操作异常
     * @throws ServletException Servlet操作异常
     */
    public static String view(HttpServletRequest request, HttpServletResponse response, String template, Map<String, Object> context)
            throws IOException, ServletException {
        try (OutputStream os = new ByteArrayOutputStream()) {
            render(request, response, template, context, os);
            return os.toString();
        }
    }

    /**
     * 向Http响应对象中写入文件
     *
     * @param response HTTP响应对象
     * @param file     文件对象
     * @throws IOException IO操作异常
     */
    @Assert
    public static void write(HttpServletResponse response, File file) throws IOException {
        write(response, new Nfile(file));
    }

    /**
     * 向Http响应对象中写入文件
     *
     * @param response HTTP响应对象
     * @param file     文件对象
     * @throws IOException IO操作异常
     */
    @Assert
    public static void write(HttpServletResponse response, Nfile file) throws IOException {
        String name = new String(file.getName().getBytes(), "ISO-8859-1");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + name);
        response.setHeader("Content-Length", String.valueOf(file.getSize()));
        try (InputStream is = file.getInputStream(); OutputStream os = response.getOutputStream()) {
            Streams.write(is, os);
        }
    }

    /**
     * 向Http响应对象中写入数据
     *
     * @param response HTTP响应对象
     * @param bytes    数据字节数组
     * @throws IOException IO操作异常
     */
    @Assert
    public static void write(HttpServletResponse response, byte[] bytes) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            os.write(bytes);
        }
    }

    /**
     * 向Http响应对象中写入数据
     *
     * @param response HTTP响应对象
     * @param object   数据对象
     * @throws IOException IO操作异常
     */
    @Assert
    public static void write(HttpServletResponse response, Object object) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            os.write(Strings.toString(object).getBytes());
        }
    }

    /**
     * 向Http响应对象中写入数据
     *
     * @param response HTTP响应对象
     * @param input    数据输入流
     * @throws IOException IO操作异常
     */
    @Assert
    public static void write(HttpServletResponse response, InputStream input) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            Streams.write(input, os);
        }
    }

    /**
     * 向Http响应对象中写入数据
     *
     * @param response HTTP响应对象
     * @param input    数据输入通道
     * @throws IOException IO操作异常
     */
    @Assert
    public static void write(HttpServletResponse response, ReadableByteChannel input) throws IOException {
        try (OutputStream os = response.getOutputStream()) {
            Streams.write(input, os);
        }
    }

    /**
     * 获取安全的Html（过滤掉script标签后的html内容）
     *
     * @param html Html内容
     * @return Html内容
     */
    @Assert
    public static String getSafeHtml(String html) {
        return html.isEmpty() ? html : SCRIPT_PATTERN.matcher(html).replaceAll(Strings.EMPTY_STRING);
    }

    /**
     * 获取html中纯文本
     *
     * @param html html文本
     * @return 纯文本
     */
    @Assert
    public static String getText(String html) {
        try {
            return html.isEmpty() ? html : getText(new StringReader(html));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取html中纯文本
     *
     * @param reader html数据流
     * @return 纯文本
     * @throws IOException IO操作异常
     */
    @Assert
    public static String getText(Reader reader) throws IOException {
        StringBuilder text = new StringBuilder();
        parserDelegator.parse(reader, new HTMLEditorKit.ParserCallback() {

            @Override
            public void handleText(char[] data, int pos) {
                text.append(data);
            }

        }, true);
        return text.toString();
    }
}
