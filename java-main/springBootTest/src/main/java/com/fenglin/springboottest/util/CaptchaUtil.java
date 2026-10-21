package com.fenglin.springboottest.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * 图形验证码工具：生成 4 位验证码字符 + 对应图片
 */
public class CaptchaUtil {

    /** 去除易混淆字符（0/O、1/l/I 等） */
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final int WIDTH = 104;
    private static final int HEIGHT = 42;
    private static final int LENGTH = 4;
    private static final String[] COLORS = {"#e8c66b", "#6fe9ff", "#ff9bb3", "#b8ff9b"};

    /** 生成随机验证码文本 */
    public static String nextCode() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARS.charAt(r.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /** 依据验证码文本绘制图片 */
    public static BufferedImage generate(String code) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();
        Random r = new Random();

        // 背景
        g.setColor(new Color(20, 22, 46));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 干扰线
        for (int i = 0; i < 4; i++) {
            g.setColor(new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256), 90));
            g.drawLine(r.nextInt(WIDTH), r.nextInt(HEIGHT), r.nextInt(WIDTH), r.nextInt(HEIGHT));
        }

        // 字符
        for (int i = 0; i < code.length(); i++) {
            g.setColor(Color.decode(COLORS[i % COLORS.length]));
            g.setFont(new Font("Serif", Font.BOLD, 24 + r.nextInt(8)));
            int x = 14 + i * 22;
            int y = HEIGHT / 2 + 8 + (r.nextInt(6) - 3);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
        }

        g.dispose();
        return img;
    }
}
