package com.arsframework.util;

import java.io.*;
import java.util.Map;
import java.util.Collections;
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
    public static void setCookie(@Nonnull HttpServletResponse response, @Nonnull String name, String value, @Min(0) int timeout) {
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
    @Nonnull
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
    @Nonnull
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
    @Nonnull
    public static String getUrl(HttpServletRequest request) {
        StringBuilder url = new StringBuilder(request.getScheme()).append("://").append(request.getServerName())
                .append(':').append(request.getServerPort());
        String context = request.getContextPath();
        return context == null ? url.toString() : url.append(context).toString();
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
    @Nonnull
    public static void render(HttpServletRequest request, HttpServletResponse response, String template, Map<String, Object> context,
                              OutputStream output) throws IOException, ServletException {
        template = template.replace("\\", "/").replace("//", "/");
        if (template.charAt(0) != '/') {
            template = new StringBuilder("/").append(template).toString();
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
    @Nonnull
    public static void write(HttpServletResponse response, File file) throws IOException {
        write(response, file, file.getName());
    }

    /**
     * 向Http响应对象中写入文件
     *
     * @param response HTTP响应对象
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
     * 初始化文件响应头
     *
     * @param response HTTP响应对象
     * @param name     文件名称
     */
    @Nonnull
    public static void initializeFileResponseHeader(HttpServletResponse response, String name) {
        try {
            name = new String(name.getBytes(), Strings.CHARSET_ISO_8859_1);
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + name);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
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
