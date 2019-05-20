package com.arsframework.util;

import java.util.Map;
import java.util.HashMap;
import java.awt.image.BufferedImage;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.arsframework.annotation.Min;
import com.arsframework.annotation.Nonnull;

/**
 * 条码工具类
 *
 * @author yongqiang.wu
 */
public abstract class Barcodes {
    /**
     * 默认宽度
     */
    public static final int DEFAULT_WIDTH = 200;

    /**
     * 默认高度
     */
    public static final int DEFAULT_HEIGHT = 200;

    /**
     * 加密提示映射表
     */
    private static final Map<EncodeHintType, Object> ENCODE_HINTS = new HashMap<>(1);

    /**
     * 解密提示映射表
     */
    private static final Map<DecodeHintType, Object> DECODE_HINTS = new HashMap<>(1);

    static {
        ENCODE_HINTS.put(EncodeHintType.CHARACTER_SET, Strings.CHARSET_UTF8);
        DECODE_HINTS.put(DecodeHintType.CHARACTER_SET, Strings.CHARSET_UTF8);
    }

    /**
     * 将内容编码
     *
     * @param content 图片内容
     * @return 图片对象
     */
    public static BufferedImage encode(String content) {
        return encode(content, BarcodeFormat.QR_CODE);
    }

    /**
     * 将内容编码
     *
     * @param content 图片内容
     * @param format  图片格式
     * @return 图片对象
     */
    public static BufferedImage encode(String content, BarcodeFormat format) {
        return encode(content, format, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 将内容编码
     *
     * @param content 图片内容
     * @param format  图片格式
     * @param width   图片宽度
     * @param height  图片高度
     * @return 图片对象
     */
    @Nonnull
    public static BufferedImage encode(String content, BarcodeFormat format, @Min(1) int width, @Min(1) int height) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(content, format, width, height, ENCODE_HINTS);
            return MatrixToImageWriter.toBufferedImage(matrix);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 内容解码
     *
     * @param image 图片对象
     * @return 图形内容
     */
    @Nonnull
    public static String decode(BufferedImage image) {
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        try {
            return new MultiFormatReader().decode(bitmap, DECODE_HINTS).getText();
        } catch (ReaderException e) {
            throw new RuntimeException(e);
        }
    }

}
