package com.se233.asteroid.model;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Bullet {
    private double x;
    private double y;
    private double angle;
    private double velocityX;
    private double velocityY;
    private double speed = 2;

    // Static bullet sprite
    private static BufferedImage bulletSprite;
    private static final int SPRITE_WIDTH = 32; // ปรับตามขนาดจริงของรูปที่ให้มา
    private static final int SPRITE_HEIGHT = 16; // ปรับตามขนาดจริงของรูปที่ให้มา
    private static final Logger logger = Logger.getLogger(Bullet.class.getName());

    static {
        try {
            bulletSprite = ImageIO.read(Bullet.class.getResource("/assets/bullet.png"));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load bullet sprite", e);
        }
    }

    public Bullet(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.velocityX = speed * Math.cos(Math.toRadians(angle));
        this.velocityY = speed * Math.sin(Math.toRadians(angle));
    }

    public void setVelocity(double velocityX, double velocityY) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    public void update() {
        x += velocityX;
        y += velocityY;
    }

    public boolean isOffScreen(int width, int height) {
        return x < 0 || x > width || y < 0 || y > height;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void draw(Graphics2D g) {
        if (bulletSprite == null) {
            // Fallback ถ้าโหลดรูปไม่สำเร็จ
            g.setColor(Color.BLUE);
            g.fillOval((int) x - 2, (int) y - 2, 4, 4);
            return;
        }

        // เก็บ transform เดิมไว้
        AffineTransform oldTransform = g.getTransform();

        // เคลื่อนไปที่ตำแหน่งกระสุนและหมุนตามทิศทาง
        g.translate(x, y);
        g.rotate(Math.toRadians(angle));

        // วาดกระสุนโดยให้จุดกึ่งกลางอยู่ที่ตำแหน่งปัจจุบัน
        g.drawImage(bulletSprite,
                -SPRITE_WIDTH/2,
                -SPRITE_HEIGHT/2,
                SPRITE_WIDTH,
                SPRITE_HEIGHT,
                null);

        // คืน transform กลับไปค่าเดิม
        g.setTransform(oldTransform);
    }

    public Rectangle getBounds() {
        // ใช้ hitbox ที่เล็กกว่าขนาดรูปเล็กน้อยเพื่อการชนที่แม่นยำขึ้น
        int hitboxWidth = SPRITE_WIDTH - 8;
        int hitboxHeight = SPRITE_HEIGHT - 4;
        return new Rectangle(
                (int) x - hitboxWidth/2,
                (int) y - hitboxHeight/2,
                hitboxWidth,
                hitboxHeight
        );
    }
}