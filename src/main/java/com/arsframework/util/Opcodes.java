package com.arsframework.util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.arsframework.annotation.Min;
import com.arsframework.annotation.Nonnull;

/**
 * 操作证码工具类
 *
 * @author yongqiang.wu
 */
public abstract class Opcodes {
    /**
     * 默认宽度
     */
    public static final int DEFAULT_WIDTH = 120;

    /**
     * 默认高度
     */
    public static final int DEFAULT_HEIGHT = 50;

    private static Color getRandomColor(int fc, int bc) {
        if (fc > 255) {
            fc = 255;
        }
        if (bc > 255) {
            bc = 255;
        }
        Random random = ThreadLocalRandom.current();
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    private static void drawLine(Graphics graphics, int number, int width, int height) {
        Random random = ThreadLocalRandom.current();
        for (int i = 0; i < number; i++) {
            int x = random.nextInt(width - 1);
            int y = random.nextInt(height - 1);
            int xl = random.nextInt(6) + 1;
            int yl = random.nextInt(12) + 1;
            graphics.drawLine(x, y, x + xl + 40, y + yl + 20);
        }
    }

    private static void drawYawp(BufferedImage image, float rate, int width, int height) {
        Random random = ThreadLocalRandom.current();
        int area = (int) (rate * width * height);
        for (int i = 0; i < area; i++) {
            image.setRGB(random.nextInt(width), random.nextInt(height), getRandomIntColor());
        }
    }

    private static int getRandomIntColor() {
        int color = 0;
        Random random = ThreadLocalRandom.current();
        for (int i = 0; i < 3; i++) {
            color = color << 8;
            color = color | random.nextInt(255);
        }
        return color;
    }

    private static void shearX(Graphics graphics, Color color, int width, int height) {
        Random random = ThreadLocalRandom.current();
        int period = random.nextInt(2);
        boolean borderGap = true;
        int frames = 1;
        int phase = random.nextInt(2);
        for (int i = 0; i < height; i++) {
            double d = (double) (period >> 1)
                    * Math.sin((double) i / (double) period + (6.2831853071795862D * (double) phase) / (double) frames);
            graphics.copyArea(0, i, width, 1, (int) d, 0);
            if (borderGap) {
                graphics.setColor(color);
                graphics.drawLine((int) d, i, 0, i);
                graphics.drawLine((int) d + width, i, width, i);
            }
        }
    }

    private static void shearY(Graphics graphics, Color color, int width, int height) {
        int phase = 7;
        int frames = 20;
        boolean borderGap = true;
        Random random = ThreadLocalRandom.current();
        int period = random.nextInt(40) + 10; // 50;
        for (int i = 0; i < width; i++) {
            double d = (double) (period >> 1)
                    * Math.sin((double) i / (double) period + (6.2831853071795862D * (double) phase) / (double) frames);
            graphics.copyArea(i, 0, 1, height, 0, (int) d);
            if (borderGap) {
                graphics.setColor(color);
                graphics.drawLine(i, (int) d, i, 0);
                graphics.drawLine(i, (int) d + height, i, height);
            }
        }
    }

    /**
     * 验证码编码
     *
     * @param content 验证码内容
     * @return 图片对象
     */
    public static BufferedImage encode(String content) {
        return encode(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 验证码编码
     *
     * @param content 验证码内容
     * @param width   图片宽度
     * @param height  图片高度
     * @return 图片对象
     */
    @Nonnull
    public static BufferedImage encode(String content, @Min(1) int width, @Min(1) int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(Color.GRAY);// 设置边框色
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(getRandomColor(200, 250));// 设置背景色
        graphics.fillRect(0, 2, width, height - 4);
        Color color = getRandomColor(160, 200);
        graphics.setColor(color);// 设置线条的颜色
        drawLine(graphics, 20, width, height);// 绘制干扰线
        drawYawp(image, 0.05f, width, height);// 添加噪点
        shearX(graphics, color, width, height); // 扭曲横柱
        shearY(graphics, color, width, height); // 扭曲纵柱
        graphics.setColor(getRandomColor(100, 160));
        int fontSize = height - 4;
        graphics.setFont(new Font("Algerian", Font.ITALIC, fontSize));
        Random random = ThreadLocalRandom.current();
        char[] chars = content.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            AffineTransform affine = new AffineTransform();
            affine.setToRotation(Math.PI / 4 * random.nextDouble() * (random.nextBoolean() ? 1 : -1),
                    (width / chars.length) * i + fontSize / 2, height / 2);
            graphics.setTransform(affine);
            graphics.drawChars(chars, i, 1, ((width - 10) / chars.length) * i + 5, height / 2 + fontSize / 2 - 10);
        }
        graphics.dispose();
        return image;
    }

}
