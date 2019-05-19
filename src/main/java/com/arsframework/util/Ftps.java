package com.arsframework.util;

import java.io.*;
import java.util.List;
import java.util.LinkedList;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPClient;

import com.arsframework.annotation.Nonnull;

/**
 * FTP操作工具类
 *
 * @author yongqiang.wu
 */
public abstract class Ftps {
    /**
     * FTP默认端口
     */
    public static final int DEFAULT_PORT = 21;

    /**
     * FTP文件查询实现
     */
    public static class FTPQuery extends Files.AbstractQuery {
        protected final FTPClient client;

        @Nonnull
        public FTPQuery(FTPClient client, String path) {
            super(path);
            this.client = client;
        }

        @Override
        protected List<Files.Describe> execute(String path, Files.Condition... conditions) {
            List<Files.Describe> describes = new LinkedList<>();
            try {
                this.client.listFiles(path, (file) -> {
                    Files.Describe describe = new Files.Describe(Files.path(path, file.getName()), file.getName(), file.getSize(),
                            file.getTimestamp().toInstant().toEpochMilli(), file.isDirectory());
                    if (Files.isSatisfy(describe, conditions)) {
                        describes.add(describe);
                    }
                    return false;
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return describes;
        }

    }

    /**
     * 获取FTP客户端连接对象
     *
     * @param host     主机地址
     * @param user     登陆用户名
     * @param password 登陆密码
     * @return FTP客户端连接对象
     * @throws IOException IO操作异常
     */
    public static FTPClient connect(String host, String user, String password) throws IOException {
        return connect(host, DEFAULT_PORT, user, password);
    }

    /**
     * 获取FTP客户端连接对象
     *
     * @param host     主机地址
     * @param port     主机端口
     * @param user     登陆用户名
     * @param password 登陆密码
     * @return FTP客户端连接对象
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static FTPClient connect(String host, int port, String user, String password) throws IOException {
        FTPClient client = new FTPClient();
        client.connect(host, port); // 连接服务器
        client.enterLocalPassiveMode(); // 被动模式
        if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
            client.disconnect();
            throw new IllegalStateException("Connection failed, reply code: " + client.getReplyCode());
        } else if (!client.login(user, password)) { // 登陆
            client.disconnect();
            throw new IllegalStateException("Login failed, reply code: " + client.getReplyCode());
        }
        return client;
    }

    /**
     * 断开FTP客户端连接
     *
     * @param client FTP客户端连接对象
     * @throws IOException IO操作异常
     */
    public static void disconnect(FTPClient client) throws IOException {
        if (client != null && client.isConnected()) {
            try {
                client.logout();
            } finally {
                client.disconnect();
            }
        }
    }

    /**
     * 设置FTP服务器及客户端编码格式，并返回是否设置成功
     *
     * @param client  FTP客户端连接对象
     * @param charset 编码字符集
     * @return true/false
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static boolean encoding(FTPClient client, String charset) throws IOException {
        if (FTPReply.isPositiveCompletion(client.sendCommand("OPTS " + charset, "ON"))) {
            client.setControlEncoding(charset);
            return true;
        }
        return false;
    }

    /**
     * 文件查询
     *
     * @param client FTP客户端连接对象
     * @return 查询对象
     */
    public static Files.Query query(FTPClient client) {
        return query(client, Strings.ROOT_URI);
    }

    /**
     * 文件查询
     *
     * @param client FTP客户端连接对象
     * @param path   文件目录
     * @return 查询对象
     */
    public static Files.Query query(FTPClient client, String path) {
        return new FTPQuery(client, path);
    }

    /**
     * 判断文件/文件夹是否存在
     *
     * @param client FTP客户端连接对象
     * @param path   文件/文件目录相对路径
     * @return true/false
     * @throws IOException IO操作异常
     */
    public static boolean exists(@Nonnull FTPClient client, String path) throws IOException {
        if (path == null) {
            return false;
        } else if (!client.changeWorkingDirectory(path)) { // 切换目录失败（文件）
            String name = Files.getName(path);
            String directory = Files.getDirectory(path);
            if (directory == null || client.changeWorkingDirectory(directory)) {
                for (FTPFile file : client.listFiles()) {
                    if (file.isFile() && file.getName().equals(name)) {
                        return true;
                    }
                }
            }
            return false;
        }
        return true;
    }

    /**
     * 创建目录
     *
     * @param client FTP客户端连接对象
     * @param path   文件目录路径
     * @throws IOException IO操作异常
     */
    public static void mkdirs(@Nonnull FTPClient client, String path) throws IOException {
        if (!Strings.isEmpty(path)) {
            client.makeDirectory(path);
        }
    }

    /**
     * 删除文件/文件目录
     *
     * @param client FTP客户端连接对象
     * @param path   文件/文件目录相对路径
     * @throws IOException IO操作异常
     */
    public static void delete(@Nonnull FTPClient client, String path) throws IOException {
        if (path == null) {
            return;
        }
        if (client.changeWorkingDirectory(path)) { // 目录
            for (FTPFile file : client.listFiles()) {
                String target = Files.path(path, file.getName()); // 删除目标
                if (file.isDirectory()) { // 目录
                    delete(client, target); // 递归删除
                    client.removeDirectory(target); // 删除当前目录
                } else { // 文件
                    client.deleteFile(target);
                }
            }
            client.removeDirectory(path);
        } else { // 文件
            client.deleteFile(path);
        }
    }

    /**
     * 拷贝文件/文件目录
     *
     * @param client FTP客户端连接对象
     * @param source 源文件/文件目录
     * @param target 目标文件目录
     * @throws IOException IO操作异常
     */
    public static void copy(@Nonnull FTPClient client, String source, String target) throws IOException {
        if (source == null || target == null) {
            return;
        }
        String path = Files.path(target, Files.getName(source));
        if (client.changeWorkingDirectory(source)) {
            client.makeDirectory(path);
            for (FTPFile file : client.listFiles()) {
                if (file.isDirectory()) {
                    copy(client, Files.path(source, file.getName()), path);
                } else {
                    transfer(client, Files.path(source, file.getName()), Files.path(path, file.getName()));
                }
            }
        } else {
            client.makeDirectory(target);
            transfer(client, source, path);
        }
    }

    /**
     * 文件传输
     *
     * @param client FTP客户端连接对象
     * @param source 源文件路径
     * @param target 目标文件路径
     * @throws IOException IO操作异常
     */
    public static void transfer(@Nonnull FTPClient client, String source, String target) throws IOException {
        if (source == null || target == null) {
            return;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        client.retrieveFile(source, bos);
        client.storeFile(target, new ByteArrayInputStream(bos.toByteArray()));
    }

    /**
     * 移动文件/文件目录
     *
     * @param client FTP客户端连接对象
     * @param source 源文件/文件目录
     * @param target 目标文件目录
     * @throws IOException IO操作异常
     */
    public static void move(@Nonnull FTPClient client, String source, String target) throws IOException {
        if (source == null || target == null) {
            return;
        }
        String path = Files.path(target, Files.getName(source)); // 移动目标路径
        if (client.changeWorkingDirectory(source)) { // 目录
            client.makeDirectory(path); // 创建目录
            for (FTPFile file : client.listFiles()) {
                if (file.isDirectory()) { // 目录
                    move(client, Files.path(source, file.getName()), path); // 递归删除文件
                    client.removeDirectory(Files.path(source, file.getName())); // 删除目录
                } else { // 文件
                    client.rename(Files.path(source, file.getName()), Files.path(path, file.getName()));
                }
            }
            client.removeDirectory(source); // 删除当前目录
        } else { // 文件
            client.makeDirectory(target); // 创建文件目录
            client.rename(source, path); // 移动文件
        }
    }

    /**
     * 读FTP文件
     *
     * @param client FTP客户端连接对象
     * @param path   文件路径
     * @return 文件输入流
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static InputStream read(FTPClient client, String path) throws IOException {
        return client.retrieveFileStream(path);
    }


    /**
     * 写文件
     *
     * @param client FTP客户端连接对象
     * @param path   目标文件路径
     * @param file   本地文件对象
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static void write(FTPClient client, String path, File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            write(client, path, is);
        }
    }

    /**
     * 写数据
     *
     * @param client FTP客户端连接对象
     * @param path   目标文件路径
     * @param stream 数据输入流
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static void write(FTPClient client, String path, InputStream stream) throws IOException {
        String directory = Files.getDirectory(path);
        if (directory != null) {
            client.makeDirectory(directory);
        }
        client.storeFile(path, stream);
    }
}
