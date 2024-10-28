package com.se233.asteroid.model;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Asteroid extends Character {
    private int maxHealth;
    private boolean isLarge;
    private double rotationAngle;
    private double rotationSpeed;
    private BufferedImage asteroidImage;

    // กำหนดขนาดความกว้างและความสูงแยกกัน
    private static final int LARGE_WIDTH = 170;  // เพิ่มความกว้าง
    private static final int LARGE_HEIGHT = 120; // คงความสูงไว้
    private static final int SMALL_WIDTH = 140;  // เพิ่มความกว้าง
    private static final int SMALL_HEIGHT = 100; // คงความสูงไว้
    private static final int LARGE_HITBOX = 90;
    private static final int SMALL_HITBOX = 70;

    public Asteroid(double x, double y, boolean isLarge) {
        super(x, y,
                Math.random() * (isLarge ? 1.0 : 2.0) - (isLarge ? 0.5 : 1.0),
                Math.random() * (isLarge ? 1.0 : 2.0) - (isLarge ? 0.5 : 1.0),
                0,
                isLarge ? 100 : 50);

        this.isLarge = isLarge;
        this.maxHealth = health;
        this.rotationAngle = Math.random() * 360;
        this.rotationSpeed = Math.random() * 2 - 1;

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/assets/asteroid.png"));
            Image originalImage = icon.getImage();

            // กำหนดขนาดตามความกว้างและความสูงที่ต้องการ
            int width = isLarge ? LARGE_WIDTH : SMALL_WIDTH;
            int height = isLarge ? LARGE_HEIGHT : SMALL_HEIGHT;

            asteroidImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = asteroidImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // คำนวณขนาดและตำแหน่งเพื่อให้รูปไม่ผิดสัดส่วน
            int drawWidth = width;
            int drawHeight = height;

            // วาดรูปให้เต็มพื้นที่ที่กำหนด
            g2d.drawImage(originalImage, 0, 0, drawWidth, drawHeight, null);
            g2d.dispose();
        } catch (Exception e) {
            System.err.println("Error loading asteroid image: " + e.getMessage());
        }
    }

    @Override
    public void update() {
        x += velocityX;
        y += velocityY;
        rotationAngle += rotationSpeed;

        // ปรับขอบเขตการ wrap around ตามขนาดความกว้าง
        int boundWidth = isLarge ? LARGE_WIDTH : SMALL_WIDTH;
        int boundHeight = isLarge ? LARGE_HEIGHT : SMALL_HEIGHT;

        if (x < -boundWidth) x = 840 + boundWidth/2;
        if (x > 840 + boundWidth/2) x = -boundWidth;
        if (y < -boundHeight) y = 640 + boundHeight/2;
        if (y > 640 + boundHeight/2) y = -boundHeight;
    }

    @Override
    public void draw(Graphics2D g) {
        if (asteroidImage != null) {
            AffineTransform transform = new AffineTransform();
            // ปรับตำแหน่งการวาดรูปให้อยู่ตรงกลาง
            transform.translate(x - asteroidImage.getWidth()/2, y - asteroidImage.getHeight()/2);
            transform.rotate(Math.toRadians(rotationAngle), asteroidImage.getWidth()/2, asteroidImage.getHeight()/2);
            g.drawImage(asteroidImage, transform, null);

            // Debug: แสดง hitbox (ถ้าต้องการดู)
            // Rectangle bounds = getBounds();
            // g.setColor(Color.RED);
            // g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }

        drawHealthBar(g);
    }

    private void drawHealthBar(Graphics2D g) {
        int healthBarWidth = isLarge ? 120 : 80; // ปรับความกว้างของ health bar
        int healthBarHeight = 6;
        int currentHealthWidth = (int)((health / (double)maxHealth) * healthBarWidth);
        int yOffset = isLarge ? LARGE_HEIGHT/2 + 15 : SMALL_HEIGHT/2 + 15;

        // Background of health bar
        g.setColor(new Color(255, 0, 0, 128));
        g.fillRect((int)x - healthBarWidth/2, (int)y - yOffset,
                healthBarWidth, healthBarHeight);

        // Current health
        g.setColor(new Color(0, 255, 0, 192));
        g.fillRect((int)x - healthBarWidth/2, (int)y - yOffset,
                currentHealthWidth, healthBarHeight);
    }

    public Rectangle getBounds() {
        int hitboxSize = isLarge ? LARGE_HITBOX : SMALL_HITBOX;
        return new Rectangle(
                (int)(x - hitboxSize/2),
                (int)(y - hitboxSize/2),
                hitboxSize,
                hitboxSize
        );
    }

    public void hit() {
        health -= 10;
        if (health <= 0) {
            System.out.println("Asteroid destroyed!");
        }
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public boolean isLarge() {
        return isLarge;
    }
}