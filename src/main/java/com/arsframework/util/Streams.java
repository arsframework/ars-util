package com.arsframework.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import com.arsframework.annotation.Nonnull;

/**
 * 数据流处理工具类
 *
 * @author yongqiang.wu
 */
public abstract class Streams {
    /**
     * 默认数据缓冲区大小
     */
    public static final int DEFAULT_BUFFER_SIZE = 2048;

    /**
     * 判断对象是否为流数据
     *
     * @param object 对象实例
     * @return true/false
     */
    public static boolean isStream(Object object) {
        return object instanceof byte[] || object instanceof File || object instanceof InputStream || object instanceof ReadableByteChannel;
    }

    /**
     * 对象序列化，将对象转换成字节数组
     *
     * @param object 需要转换的对象
     * @return 字节数组
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static byte[] serialize(Serializable object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream os = new ObjectOutputStream(bos)) {
            os.writeObject(object);
        }
        return bos.toByteArray();
    }

    /**
     * 对象反序列化，将对象的字节数组转换成对象
     *
     * @param bytes 字节数组
     * @return 对象
     * @throws IOException            IO操作异常
     * @throws ClassNotFoundException 类不存在异常
     */
    @Nonnull
    public static Serializable deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        return bytes.length == 0 ? null : (Serializable) new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
    }

    /**
     * 对象反序列化，从数据输入流中获取对象的字节数据，并将字节数据转换成对象
     *
     * @param input 输入流
     * @return 对象
     * @throws IOException            IO操作异常
     * @throws ClassNotFoundException 类不存在异常
     */
    @Nonnull
    public static Serializable deserialize(InputStream input) throws IOException, ClassNotFoundException {
        return (Serializable) new ObjectInputStream(input).readObject();
    }

    /**
     * 对象反序列化，从套节字通道中获取对象的字节数据，并将字节数据转换成对象
     *
     * @param channel 套节字连接通道
     * @return 对象
     * @throws IOException            IO操作异常
     * @throws ClassNotFoundException 类不存在异常
     */
    @Nonnull
    public static Serializable deserialize(ReadableByteChannel channel) throws IOException, ClassNotFoundException {
        int n;
        ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
        try (PipedOutputStream pos = new PipedOutputStream(); PipedInputStream pis = new PipedInputStream(pos)) {
            while ((n = channel.read(buffer)) > 0) {
                pos.write(buffer.array(), 0, n);
            }
            return (Serializable) new ObjectInputStream(pis).readObject();
        }
    }

    /**
     * 从输入流中获取字节
     *
     * @param input 输入流
     * @return 字节数组
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static byte[] getBytes(InputStream input) throws IOException {
        int n;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            while ((n = input.read(buffer)) > 0) {
                bos.write(buffer, 0, n);
            }
            return bos.toByteArray();
        }
    }

    /**
     * 从文件中获取字节
     *
     * @param file 文件对象
     * @return 字节数组
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static byte[] getBytes(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            return getBytes(is);
        }
    }

    /**
     * 从套节字通道中获取字节
     *
     * @param channel 套接字读取通道
     * @return 字节数组
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static byte[] getBytes(ReadableByteChannel channel) throws IOException {
        int n;
        ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            while ((n = channel.read(buffer)) > 0) {
                bos.write(buffer.array(), 0, n);
            }
            return bos.toByteArray();
        }
    }

    /**
     * 将字节数据写入到文件
     *
     * @param source 源字节数据
     * @param target 目标文件对象
     * @throws IOException IO操作异常
     */
    public static void write(byte[] source, File target) throws IOException {
        write(source, target, false);
    }

    /**
     * 将字节数据写入到文件
     *
     * @param source 源字节数据
     * @param target 目标文件对象
     * @param append 是否追加
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static void write(byte[] source, File target, boolean append) throws IOException {
        Files.mkdirs(target);
        try (FileOutputStream os = new FileOutputStream(target, append)) {
            os.write(source);
        }
    }

    /**
     * 将文件数据写入到文件
     *
     * @param source 源文件
     * @param target 目标文件
     * @throws IOException IO操作异常
     */
    public static void write(File source, File target) throws IOException {
        write(source, target, false);
    }

    /**
     * 将文件数据写入到文件
     *
     * @param source 源文件
     * @param target 目标文件
     * @param append 是否追加
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static void write(File source, File target, boolean append) throws IOException {
        if (source.exists()) {
            Files.mkdirs(target);
            try (FileInputStream fis = new FileInputStream(source); FileOutputStream fos = new FileOutputStream(target, append)) {
                FileChannel in = fis.getChannel();
                in.transferTo(0, in.size(), fos.getChannel());
            }
        }
    }

    /**
     * 将文件数据写入到输出流
     *
     * @param source 源文件对象
     * @param target 目标输出流
     * @throws IOException IO操作异常
     */
    public static void write(File source, OutputStream target) throws IOException {
        write(source, target, false);
    }

    /**
     * 将文件数据写入到输出流
     *
     * @param source 源文件对象
     * @param target 目标输出流
     * @param append 是否追加
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static void write(File source, OutputStream target, boolean append) throws IOException {
        try (InputStream is = new FileInputStream(source);) {
            write(is, target);
        }
    }

    /**
     * 将文件数据写入到套接字写通道中
     *
     * @param source 源文件对象
     * @param target 目标输出流
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static void write(File source, WritableByteChannel target) throws IOException {
        try (InputStream is = new FileInputStream(source)) {
            write(is, target);
        }
    }

    /**
     * 将输入流中的数据写入文件
     *
     * @param source 源输入流
     * @param target 目标文件对象
     * @throws IOException IO操作异常
     */
    public static void write(InputStream source, File target) throws IOException {
        write(source, target, false);
    }

    /**
     * 将输入流中的数据写入文件
     *
     * @param source 源输入流
     * @param target 目标文件对象
     * @param append 是否追加
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static void write(InputStream source, File target, boolean append) throws IOException {
        Files.mkdirs(target);
        try (OutputStream os = new FileOutputStream(target, append)) {
            write(source, os);
        }
    }

    /**
     * 将输入流中的数据写入输出流
     *
     * @param source 源输入流
     * @param target 目标输出流
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static void write(InputStream source, OutputStream target) throws IOException {
        int n;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        while ((n = source.read(buffer)) > 0) {
            target.write(buffer, 0, n);
        }
    }

    /**
     * 将输入流中的数据写入套接字写通道
     *
     * @param source 源输入流
     * @param target 目标输出流
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static void write(InputStream source, WritableByteChannel target) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        while (source.read(buffer) > 0) {
            target.write(ByteBuffer.wrap(buffer));
        }
    }

    /**
     * 将套接字读取通道中的数据写入文件
     *
     * @param source 源套节字输入流通道
     * @param target 目标文件对象
     * @throws IOException IO操作异常
     */
    public static void write(ReadableByteChannel source, File target) throws IOException {
        write(source, target, false);
    }

    /**
     * 将套接字读取通道中的数据写入文件
     *
     * @param source 源套节字输入流通道
     * @param target 目标文件对象
     * @param append 是否追加
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static void write(ReadableByteChannel source, File target, boolean append) throws IOException {
        Files.mkdirs(target);
        try (OutputStream os = new FileOutputStream(target, append)) {
            write(source, os);
        }
    }

    /**
     * 将套接字读取通道中的数据写入输出流
     *
     * @param source 源套节字输入流通道
     * @param target 目标输出流
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static void write(ReadableByteChannel source, OutputStream target) throws IOException {
        int n;
        ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
        while ((n = source.read(buffer)) > 0) {
            target.write(buffer.array(), 0, n);
        }
    }

    /**
     * 将套接字读通道中的数据写入套接字写通道中
     *
     * @param source 源套接字读通道
     * @param target 目标套接字写通道
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static void write(ReadableByteChannel source, WritableByteChannel target) throws IOException {
        int n;
        ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
        while ((n = source.read(buffer)) > 0) {
            target.write(ByteBuffer.wrap(buffer.array(), 0, n));
        }
    }

}
