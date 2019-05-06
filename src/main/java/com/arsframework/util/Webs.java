package com.arsframework.util;

import java.io.*;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Enumeration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;
import java.net.URLDecoder;
import java.net.URLEncoder;
import javax.servlet.http.Cookie;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import com.arsframework.annotation.Min;
import com.arsframework.annotation.Nonnull;

/**
 * Web处理工具类
 *
 * @author yongqiang.wu
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
    @Nonnull
    public static String getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    String value = cookie.getValue();
                    try {
                        return Strings.isEmpty(value) ? null : URLDecoder.decode(value, Strings.CHARSET_UTF8);
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
    public static void setCookie(@Nonnull HttpServletResponse response, @Nonnull String name, String value, @Min(0) int timeout) {
        try {
            Cookie cookie = new Cookie(name, value == null ? Strings.EMPTY_STRING : URLEncoder.encode(value, Strings.CHARSET_UTF8));
            cookie.setPath(Strings.ROOT_URI);
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
    @Nonnull
    public static String removeCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    String value = cookie.getValue();
                    try {
                        return Strings.isEmpty(value) ? null : URLDecoder.decode(value, Strings.CHARSET_UTF8);
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
     * @param request Http请求对象
     * @return 资源地址
     */
    @Nonnull
    public static String getUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        return context == null ? uri : uri.substring(context.length());
    }

    /**
     * 获取HTTP请求的URL地址（不包含资源地址）
     *
     * @param request Http请求对象
     * @return URL地址
     */
    @Nonnull
    public static String getUrl(HttpServletRequest request) {
        StringBuilder url = new StringBuilder(request.getScheme()).append("://").append(request.getServerName())
                .append(':').append(request.getServerPort());
        String context = request.getContextPath();
        return context == null ? url.toString() : url.append(context).toString();
    }

    /**
     * 将参数字符串形式转换成键/值映射
     *
     * @param str 参数字符串形式
     * @return 键/值映射
     */
    public static Map<String, Object> string2param(String str) {
        if (Strings.isBlank(str)) {
            return new LinkedHashMap<>(0);
        }
        String[] sections = str.split("&");
        Map<String, Object> parameters = new LinkedHashMap<>(sections.length);
        for (String section : sections) {
            int division = section.indexOf('=');
            if (division < 1) {
                continue;
            }
            String key = section.substring(0, division).trim();
            if (key.isEmpty()) {
                continue;
            }
            String value = Strings.trim(section.substring(division + 1));
            Object exist = parameters.get(key);
            if (exist == null) {
                parameters.put(key, value);
            } else if (!Strings.isEmpty(value)) {
                if (exist instanceof List) {
                    ((List<String>) exist).add(value);
                } else {
                    List<String> values = new LinkedList<>();
                    values.add((String) exist);
                    values.add(value);
                    parameters.put(key, values);
                }
            }
        }
        return parameters;
    }

    /**
     * 获取数据流请求体
     *
     * @param request Http请求对象
     * @return 请求体字符串
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static String getBody(HttpServletRequest request) throws IOException {
        try (InputStream is = request.getInputStream()) {
            return new String(Streams.getBytes(is));
        }
    }

    /**
     * 获取URL参数
     *
     * @param url 资源地址
     * @return 参数键/值映射
     */
    @Nonnull
    public static Map<String, Object> getParameters(String url) {
        int division = url.indexOf('?');
        return division < 0 ? new LinkedHashMap<>(0) : string2param(url.substring(division + 1));
    }

    /**
     * 获取普通表单请求参数
     *
     * @param request Http请求对象
     * @return 参数键/值表
     */
    @Nonnull
    public static Map<String, Object> getParameters(HttpServletRequest request) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            LinkedList<String> values = new LinkedList<>();
            for (String value : request.getParameterValues(name)) {
                if (!(value = value.trim()).isEmpty()) {
                    values.add(value);
                }
            }
            parameters.put(name, values.isEmpty() ? null : values.size() == 1 ? values.getFirst() : values);
        }
        return parameters;
    }

    /**
     * 视图渲染
     *
     * @param request  Http请求对象
     * @param response Http响应对象
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
     * @param request  Http请求对象
     * @param response Http响应对象
     * @param template 视图模板名称
     * @param context  渲染上下文数据
     * @throws IOException      IO操作异常
     * @throws ServletException Servlet操作异常
     */
    @Nonnull
    public static void render(HttpServletRequest request, HttpServletResponse response, String template,
                              Map<String, Object> context) throws IOException, ServletException {
        try (OutputStream os = response.getOutputStream()) {
            render(request, response, template, context, os);
        }
    }


    /**
     * 视图渲染
     *
     * @param request  Http请求对象
     * @param response Http响应对象
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
     * @param request  Http请求对象
     * @param response Http响应对象
     * @param template 视图模板名称
     * @param context  渲染上下文数据
     * @param output   数据输出流
     * @throws IOException      IO操作异常
     * @throws ServletException Servlet操作异常
     */
    @Nonnull
    public static void render(HttpServletRequest request, HttpServletResponse response, String template, Map<String, Object> context,
                              OutputStream output) throws IOException, ServletException {
        template = template.replace("\\", "/").replace("//", "/");
        if (template.charAt(0) != '/') {
            template = new StringBuilder(Strings.ROOT_URI).append(template).toString();
        }
        if (!new File(ROOT_PATH, template).exists()) {
            throw new IOException("Template does not exist: " + template);
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
     * @param request  Http请求对象
     * @param response Http响应对象
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
     * @param request  Http请求对象
     * @param response Http响应对象
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
     * 初始化文件响应头
     *
     * @param response Http响应对象
     * @param name     文件名称
     */
    @Nonnull
    public static void initializeFileResponseHeader(HttpServletResponse response, String name) {
        try {
            name = URLEncoder.encode(name, Strings.CHARSET_UTF8);
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8'zh_cn'" + name);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 响应字节数组
     *
     * @param response Http响应对象
     * @param bytes    字节数组
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static void write(HttpServletResponse response, byte[] bytes) throws IOException {
        response.setHeader("Content-type", "text/plain;charset=UTF-8");
        try (OutputStream os = response.getOutputStream()) {
            os.write(bytes);
        }
    }

    /**
     * 响应文件
     *
     * @param response Http响应对象
     * @param file     文件对象
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static void write(HttpServletResponse response, File file) throws IOException {
        write(response, file, file.getName());
    }

    /**
     * 响应文件
     *
     * @param response Http响应对象
     * @param file     文件对象
     * @param name     文件名称
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static void write(HttpServletResponse response, File file, String name) throws IOException {
        initializeFileResponseHeader(response, name);
        try (OutputStream os = response.getOutputStream()) {
            Streams.write(file, os);
        }
    }

    /**
     * 响应字符串
     *
     * @param response Http响应对象
     * @param value    字符串
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static void write(HttpServletResponse response, String value) throws IOException {
        response.setHeader("Content-type", "text/plain;charset=UTF-8");
        try (OutputStream os = response.getOutputStream()) {
            os.write(value.getBytes(Strings.CHARSET_UTF8));
        }
    }

    /**
     * 响应输入流
     *
     * @param response Http响应对象
     * @param input    输入流
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static void write(HttpServletResponse response, InputStream input) throws IOException {
        response.setHeader("Content-type", "text/plain;charset=UTF-8");
        try (OutputStream os = response.getOutputStream()) {
            Streams.write(input, os);
        }
    }

    /**
     * 请求转发
     *
     * @param request  Http请求对象
     * @param response Http响应对象
     * @param path     转发路径
     * @throws IOException      IO操作异常
     * @throws ServletException Servlet操作异常
     */
    @Nonnull
    public static void forward(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException, ServletException {
        String context = request.getContextPath();
        if (context == null) {
            request.getRequestDispatcher(path).forward(request, response);
        } else {
            request.getRequestDispatcher(context + path).forward(request, response);
        }
    }

    /**
     * 请求重定向
     *
     * @param request  Http请求对象
     * @param response Http响应对象
     * @param path     重定向路径
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static void redirect(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        String context = request.getContextPath();
        if (context == null) {
            response.sendRedirect(path);
        } else {
            response.sendRedirect(context + path);
        }
    }

    /**
     * 获取安全的Html（过滤掉script标签后的html内容）
     *
     * @param html Html内容
     * @return Html内容
     */
    @Nonnull
    public static String getSafeHtml(String html) {
        return html.isEmpty() ? html : SCRIPT_PATTERN.matcher(html).replaceAll(Strings.EMPTY_STRING);
    }

    /**
     * 获取html中纯文本
     *
     * @param html html文本
     * @return 纯文本
     */
    @Nonnull
    public static String getHtmlText(String html) {
        try {
            return html.isEmpty() ? html : getHtmlText(new StringReader(html));
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
    @Nonnull
    public static String getHtmlText(Reader reader) throws IOException {
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
