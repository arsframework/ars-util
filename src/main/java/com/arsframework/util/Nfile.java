package com.arsframework.util;

import java.io.*;
import java.util.Arrays;

import com.arsframework.annotation.Nonnull;
import com.arsframework.annotation.Nonempty;

/**
 * 非完全本地文件对象
 *
 * @author yongqiang.wu
 * @version 2019-03-22 09:38
 */
public class Nfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private File file; // 本地文件对象
    private long size; // 文件大小
    private String name; // 文件名称
    private byte[] bytes; // 文件字节数组
    private long modified; // 文件修改时间戳
    private transient InputStream input; // 文件输入流

    public Nfile(String name) {
        this(name, Objects.EMPTY_BYTE_ARRAY);
    }

    public Nfile(File file) {
        this(file == null ? null : file.getName(), file);
    }

    @Nonempty
    public Nfile(String name, File file) {
        this.file = file;
        this.name = name;
        this.size = file.length();
        this.modified = file.lastModified();
    }

    @Nonempty
    public Nfile(String name, byte[] bytes) {
        this.name = name;
        this.bytes = bytes;
        this.size = bytes.length;
        this.modified = System.currentTimeMillis();
    }

    @Nonempty
    public Nfile(String name, InputStream input) throws IOException {
        this.name = name;
        this.input = input;
        this.size = input.available();
        this.modified = System.currentTimeMillis();
    }

    /**
     * 获取文件大小
     *
     * @return 文件大小
     */
    public long getSize() {
        return this.size;
    }

    /**
     * 获取文件名称
     *
     * @return 文件名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 获取文件修改时间戳
     *
     * @return 时间戳
     */
    public long getModified() {
        return this.modified;
    }

    /**
     * 是否为本地文件
     *
     * @return true/false
     */
    public boolean isLocal() {
        return this.file != null;
    }

    /**
     * 获取本地文件对象
     *
     * @return 文件对象
     */
    public File getLocal() {
        return this.file;
    }

    /**
     * 将文件写入到输出流
     *
     * @param output 输出流对象
     * @throws IOException IO操作异常
     */
    @Nonnull
    public void write(OutputStream output) throws IOException {
        try (InputStream is = this.getInputStream()) {
            Streams.write(is, output);
        }
    }

    /**
     * 获取文件字节内容
     *
     * @return 字节数组
     * @throws IOException IO操作异常
     */
    public byte[] getBytes() throws IOException {
        if (this.bytes == null) {
            synchronized (this) {
                if (this.bytes == null) {
                    try (InputStream is = this.getInputStream()) {
                        this.bytes = Streams.getBytes(is);
                    }
                }
            }
        }
        return Arrays.copyOf(this.bytes, this.bytes.length);
    }

    /**
     * 获取文件数据输入流
     *
     * @return 输入流
     * @throws IOException IO操作异常
     */
    public InputStream getInputStream() throws IOException {
        return this.input != null ? this.input : this.bytes != null ? new ByteArrayInputStream(this.bytes) : new FileInputStream(this.file);
    }

    /**
     * 获取文件数据输出流
     *
     * @return 输出流
     * @throws IOException IO操作异常
     */
    public OutputStream getOutputStream() throws IOException {
        return this.getOutputStream(false);
    }

    /**
     * 获取文件数据输出流
     *
     * @param append 是否追加到文件末尾
     * @return 输出流
     * @throws IOException IO操作异常
     */
    public OutputStream getOutputStream(boolean append) throws IOException {
        if (this.file != null) {
            Files.mkdirs(this.file);
            return new FileOutputStream(this.file, append);
        }
        return new ByteArrayOutputStream() {

            @Override
            public void close() throws IOException {
                try {
                    byte[] array = this.toByteArray();
                    if (!append || (bytes.length == 0 && array.length == 0)) {
                        bytes = array;
                    } else {
                        byte[] copy = new byte[bytes.length + this.size()];
                        System.arraycopy(bytes, 0, copy, 0, bytes.length);
                        System.arraycopy(array, 0, copy, bytes.length, array.length);
                        bytes = copy;
                    }
                    size = bytes.length;
                    modified = System.currentTimeMillis();
                } finally {
                    super.close();
                }
            }

        };
    }

    @Override
    public String toString() {
        return this.file == null ? this.name : this.file.toString();
    }

}
