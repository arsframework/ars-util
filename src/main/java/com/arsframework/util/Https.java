package com.arsframework.util;

import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import com.arsframework.annotation.Nonnull;

/**
 * Http调用工具类
 *
 * @author yongqiang.wu
 */
public abstract class Https {
    /**
     * 构建SSL注册对象
     *
     * @return 注册对象
     */
    public static Registry<ConnectionSocketFactory> buildSSLRegistry() {
        X509TrustManager trustManager = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[]{trustManager}, null);
            return RegistryBuilder.<ConnectionSocketFactory>create()
                    .register(Webs.Protocol.HTTP.name(), PlainConnectionSocketFactory.INSTANCE)
                    .register(Webs.Protocol.HTTPS.name(), new SSLConnectionSocketFactory(context, NoopHostnameVerifier.INSTANCE)).build();
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 构建表单实体
     *
     * @param parameters 表单参数
     * @return 实体对象
     */
    public static UrlEncodedFormEntity buildFormEntity(Map<String, ?> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return null;
        }
        List<NameValuePair> nameValues = new LinkedList<>();
        for (Map.Entry<String, ?> entry : parameters.entrySet()) {
            String key = Strings.trim(entry.getKey());
            if (key != null) {
                Objects.foreach(entry.getValue(), (object, i) ->
                        nameValues.add(new BasicNameValuePair(key, Strings.trim(Strings.toString(object)))));
            }
        }
        return new UrlEncodedFormEntity(nameValues, Charset.forName(Strings.CHARSET_UTF8));
    }

    /**
     * 获取Http请求结果字节数组
     *
     * @param client  Http客户端对象
     * @param request Http请求对象
     * @return 字节数组
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static byte[] invoke(HttpClient client, HttpUriRequest request) throws IOException {
        HttpEntity entity = client.execute(request).getEntity();
        try {
            return EntityUtils.toByteArray(entity);
        } finally {
            EntityUtils.consumeQuietly(entity);
        }
    }

    /**
     * Http get 请求
     *
     * @param client     Http客户端对象
     * @param url        资源地址
     * @param parameters 请求参数
     * @return 请求结果
     * @throws IOException IO异常
     */
    public static byte[] get(@Nonnull HttpClient client, @Nonnull String url, Map<String, ?> parameters) throws IOException {
        if (parameters != null && !parameters.isEmpty()) {
            try {
                url = new StringBuilder(url).append('?').append(EntityUtils.toString(buildFormEntity(parameters))).toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return invoke(client, new HttpGet(url));
    }

    /**
     * Http post 请求
     *
     * @param client     Http客户端对象
     * @param url        资源地址
     * @param parameters 请求参数
     * @return 请求结果
     * @throws IOException IO异常
     */
    public static byte[] post(@Nonnull HttpClient client, @Nonnull String url, Map<String, ?> parameters) throws IOException {
        HttpPost request = new HttpPost(url);
        if (parameters != null && !parameters.isEmpty()) {
            request.setEntity(buildFormEntity(parameters));
        }
        return invoke(client, request);
    }
}
