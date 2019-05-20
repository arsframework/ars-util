package com.arsframework.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;
import java.util.LinkedList;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.search.SearchTerm;

import com.arsframework.annotation.Nonnull;

/**
 * 电子邮件工具类
 *
 * @author yongqiang.wu
 */
public abstract class Mails {
    /**
     * 混合子类型标识
     */
    public static final String SUB_TYPE_MIXED = "mixed";

    /**
     * 关联子类型标识
     */
    public static final String SUB_TYPE_RELATED = "related";

    /**
     * 邮件消息体对象
     */
    public static class Body {
        /**
         * 发件人
         */
        private String from;

        /**
         * 收件人
         */
        private String to;

        /**
         * 抄送人
         */
        private String cc;

        /**
         * 暗送人
         */
        private String bcc;

        /**
         * 邮件主题
         */
        private String subject;

        /**
         * 邮件正文部分
         */
        private String text;

        /**
         * 混合数据源列表
         */
        private List<MimeBodyPart> mixes = new LinkedList<>();

        /**
         * 关联数据库列表
         */
        private List<MimeBodyPart> relates = new LinkedList<>();

        /**
         * 获取发件人
         *
         * @return 发件人地址
         */
        public String from() {
            return this.from;
        }

        /**
         * 设置发件人
         *
         * @param from 发件人地址（多个地址之间使用","号隔开）
         * @return 邮件消息体对象
         */
        @Nonnull
        public Body from(String from) {
            this.from = from;
            return this;
        }

        /**
         * 获取收件人
         *
         * @return 收件人地址
         */
        public String to() {
            return this.to;
        }

        /**
         * 设置收件人
         *
         * @param to 收件人地址（多个地址之间使用","号隔开）
         * @return 邮件消息体对象
         */
        @Nonnull
        public Body to(String to) {
            this.to = to;
            return this;
        }

        /**
         * 获取抄送人
         *
         * @return 抄送人地址
         */
        public String cc() {
            return this.cc;
        }

        /**
         * 设置抄送人
         *
         * @param cc 抄送人地址（多个地址之间使用","号隔开）
         * @return 邮件消息体对象
         */
        public Body cc(String cc) {
            this.cc = cc;
            return this;
        }

        /**
         * 获取暗送人
         *
         * @return 暗送人地址
         */
        public String bcc() {
            return this.bcc;
        }

        /**
         * 设置暗送人
         *
         * @param bcc 暗送人地址（多个地址之间使用","号隔开）
         * @return 邮件消息体对象
         */
        public Body bcc(String bcc) {
            this.bcc = bcc;
            return this;
        }

        /**
         * 获取邮件主题
         *
         * @return 邮件主题
         */
        public String subject() {
            return this.subject;
        }

        /**
         * 设置邮件主题
         *
         * @param subject 邮件主题
         * @return 邮件消息体对象
         */
        public Body subject(String subject) {
            this.subject = subject;
            return this;
        }

        /**
         * 获取邮件正文
         *
         * @return 邮件正文
         */
        public String text() {
            return this.text;
        }

        /**
         * 设置邮件正文
         *
         * @param text 正文内容
         * @return 邮件消息体对象
         */
        public Body text(String text) {
            this.text = text;
            return this;
        }

        /**
         * 设置邮件混合文件数据源
         *
         * @param files 文件对象数组
         * @return 邮件消息体对象
         * @throws MessagingException 消息传递异常
         */
        @Nonnull
        public Body mixed(File... files) throws MessagingException {
            for (File file : files) {
                MimeBodyPart body = new MimeBodyPart();
                body.setDataHandler(new DataHandler(new FileDataSource(file)));
                try {
                    body.setFileName(MimeUtility.encodeText(file.getName()));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                this.mixes.add(body);
            }
            return this;
        }

        /**
         * 设置邮件混合部分
         *
         * @param parts 混合部分对象数组
         * @return 邮件消息体对象
         */
        @Nonnull
        public Body mixed(MimeBodyPart... parts) {
            for (MimeBodyPart body : parts) {
                this.mixes.add(body);
            }
            return this;
        }

        /**
         * 设置邮件关联文件数据源
         *
         * @param files 文件对象数组
         * @return 邮件消息体对象
         * @throws MessagingException 消息传递异常
         */
        @Nonnull
        public Body related(File... files) throws MessagingException {
            for (int i = 0; i < files.length; i++) {
                MimeBodyPart body = new MimeBodyPart();
                body.setDataHandler(new DataHandler(new FileDataSource(files[i])));
                body.setContentID(String.valueOf(i));
                this.relates.add(body);
            }
            return this;
        }

        /**
         * 设置邮件关联部分
         *
         * @param parts 关联部分对象数组
         * @return 邮件消息体对象
         */
        @Nonnull
        public Body related(MimeBodyPart... parts) {
            for (MimeBodyPart body : parts) {
                this.relates.add(body);
            }
            return this;
        }

        /**
         * 获取邮件内容
         *
         * @return 邮件内容对象
         * @throws MessagingException 消息传递异常
         */
        public Multipart content() throws MessagingException {
            // 构建正文部分
            MimeBodyPart text = new MimeBodyPart();
            text.setContent(this.text, "text/html;charset=UTF-8");
            if (this.relates.isEmpty() && this.mixes.isEmpty()) {
                MimeMultipart part = new MimeMultipart();
                part.addBodyPart(text);
                return part;
            }

            // 构建关联部分
            MimeMultipart part = new MimeMultipart();
            if (!this.relates.isEmpty()) {
                part.addBodyPart(text);
                for (MimeBodyPart body : this.relates) {
                    part.addBodyPart(body);
                }
                part.setSubType(SUB_TYPE_RELATED);
            }

            // 构建混合部分
            if (!this.mixes.isEmpty()) {
                if (!this.relates.isEmpty()) {
                    MimeMultipart mixed = new MimeMultipart();
                    MimeBodyPart content = new MimeBodyPart();
                    content.setContent(part);
                    mixed.addBodyPart(content);
                    part = mixed;
                }
                for (MimeBodyPart body : this.mixes) {
                    part.addBodyPart(body);
                }
                part.setSubType(SUB_TYPE_MIXED);
            }
            return part;
        }
    }

    /**
     * 构建邮件内容
     *
     * @return 邮件内容对象
     */
    public static Body body() {
        return new Body();
    }

    /**
     * 构建邮件内容
     *
     * @param text 邮件正文
     * @return 邮件内容对象
     */
    public static Body body(String text) {
        return body().text(text);
    }

    /**
     * 构建发送邮件会话
     *
     * @param host     主机地址
     * @param user     用户名称
     * @param password 用户密码
     * @return 会话对象
     */
    public static Session session(String host, String user, String password) {
        return session("smtp", host, 25, user, password, false);
    }

    /**
     * 构建邮件会话
     *
     * @param protocol 协议名称
     * @param host     主机地址
     * @param port     主机端口
     * @param user     用户名称
     * @param password 用户密码
     * @param encrypt  是否加密传输
     * @return 会话对象
     */
    @Nonnull
    public static Session session(String protocol, String host, int port, String user, String password, boolean encrypt) {
        Properties properties = new Properties();
        String prefix = "mail." + (protocol = protocol.toLowerCase());
        properties.setProperty(prefix + ".host", host);
        properties.setProperty(prefix + ".port", String.valueOf(port));
        properties.setProperty(prefix + ".auth", "true");
        properties.setProperty("mail.store.protocol", protocol);
        properties.setProperty("mail.transport.protocol", protocol);
        if (encrypt) {
            properties.setProperty(prefix + ".socketFactory.port", String.valueOf(port));
            properties.setProperty(prefix + ".socketFactory.fallback", "true");
            properties.setProperty(prefix + ".socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });
    }

    /**
     * 构建邮件消息体
     *
     * @param session 邮件会话对象
     * @param body    邮件消息体对象
     * @return 邮件消息对象
     * @throws MessagingException 消息传递异常
     */
    @Nonnull
    public static Message message(Session session, Body body) throws MessagingException {
        Asserts.expression(!Strings.isEmpty(body.from), "Mail senders must not be empty");
        Asserts.expression(!Strings.isEmpty(body.to), "Mail recipients must not be empty");
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(body.from)); // 设置发件人
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(body.to)); // 设置收件人
        if (body.cc != null) {
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(body.cc)); // 设置抄送人
        }
        if (body.bcc != null) {
            message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(body.bcc)); // 设置暗送人
        }
        message.setSubject(body.subject); // 设置邮件主题
        message.setContent(body.content()); // 设置邮件内容
        return message;
    }

    /**
     * 发送邮件
     *
     * @param session 邮件会话对象
     * @param body    邮件消息体对象
     * @throws MessagingException 消息传递异常
     */
    public static void send(Session session, Body body) throws MessagingException {
        send(session, message(session, body));
    }

    /**
     * 发送邮件
     *
     * @param session 邮件会话对象
     * @param message 邮件消息对象
     * @throws MessagingException 消息传递异常
     */
    @Nonnull
    public static void send(Session session, Message message) throws MessagingException {
        Transport transport = session.getTransport();
        try {
            transport.connect();
            transport.sendMessage(message, message.getAllRecipients());
        } finally {
            transport.close();
        }
    }

    /**
     * 接收邮件
     *
     * @param session 邮件会话对象
     * @return 邮件消息数组
     * @throws MessagingException 消息传递异常
     */
    public static Message[] receive(Session session) throws MessagingException {
        return receive(session, Folder.READ_ONLY, null);
    }

    /**
     * 接收邮件
     *
     * @param session 邮件会话对象
     * @param mode    邮件读写模式
     * @return 邮件消息数组
     * @throws MessagingException 消息传递异常
     */
    public static Message[] receive(Session session, int mode) throws MessagingException {
        return receive(session, mode, null);
    }

    /**
     * 接收邮件
     *
     * @param session 邮件会话对象
     * @param filter  邮件过滤器
     * @return 邮件消息数组
     * @throws MessagingException 消息传递异常
     */
    public static Message[] receive(Session session, SearchTerm filter) throws MessagingException {
        return receive(session, Folder.READ_ONLY, filter);
    }

    /**
     * 接收邮件
     *
     * @param session 邮件会话对象
     * @param mode    邮件读写模式
     * @param filter  邮件过滤器
     * @return 邮件消息数组
     * @throws MessagingException 消息传递异常
     */
    public static Message[] receive(@Nonnull Session session, int mode, SearchTerm filter) throws MessagingException {
        Store store = session.getStore();
        try {
            store.connect();
            Folder folder = store.getFolder("INBOX");
            folder.open(mode);
            return filter == null ? folder.getMessages() : folder.search(filter);
        } finally {
            store.close();
        }
    }
}
