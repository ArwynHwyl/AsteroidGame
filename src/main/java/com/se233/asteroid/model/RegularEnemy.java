package com.se233.asteroid.model;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class RegularEnemy extends Character {
    private static final int SHOOT_COOLDOWN = 120;
    private int currentCooldown = 0;
    private List<Bullet> bullets;
    private PlayerShip target;
    private int maxHealth;
    private static final double BULLET_SPEED = 1.0;
    private BufferedImage enemyImage;

    // ค่าคงที่สำหรับขนาดของยานศัตรู
    private static final int SHIP_WIDTH = 50;    // ปรับขนาดความกว้างของรูป
    private static final int SHIP_HEIGHT = 40;   // ปรับขนาดความสูงของรูป
    private static final int HITBOX_WIDTH = 45;  // ปรับขนาด hitbox ให้เล็กกว่ารูปเล็กน้อย
    private static final int HITBOX_HEIGHT = 35; // ปรับขนาด hitbox ให้เล็กกว่ารูปเล็กน้อย

    public RegularEnemy(double x, double y, double velocityX, double velocityY, double angle, int health) {
        super(x, y, velocityX, velocityY, angle, health);
        this.bullets = new ArrayList<>();
        this.maxHealth = health;
        this.currentCooldown = (int)(Math.random() * SHOOT_COOLDOWN);

        // โหลดรูปภาพ
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/assets/Regular.png"));
            Image originalImage = icon.getImage();

            enemyImage = new BufferedImage(SHIP_WIDTH, SHIP_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = enemyImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, SHIP_WIDTH, SHIP_HEIGHT, null);
            g2d.dispose();
        } catch (Exception e) {
            System.err.println("Error loading enemy image: " + e.getMessage());
        }
    }

    @Override
    public void update() {
        x += velocityX;
        y += velocityY;

        // เปลี่ยนจาก wrap เป็น bounce
        if (x - SHIP_WIDTH/2 <= 0) {
            x = SHIP_WIDTH/2;
            velocityX = Math.abs(velocityX); // เด้งไปทางขวา
        } else if (x + SHIP_WIDTH/2 >= 800) {
            x = 800 - SHIP_WIDTH/2;
            velocityX = -Math.abs(velocityX); // เด้งไปทางซ้าย
        }

        if (y - SHIP_HEIGHT/2 <= 0) {
            y = SHIP_HEIGHT/2;
            velocityY = Math.abs(velocityY); // เด้งลงล่าง
        } else if (y + SHIP_HEIGHT/2 >= 600) {
            y = 600 - SHIP_HEIGHT/2;
            velocityY = -Math.abs(velocityY); // เด้งขึ้นบน
        }

        // ส่วนที่เหลือของ update() คงเดิม
        if (currentCooldown > 0) {
            currentCooldown--;
        }

        if (target != null && currentCooldown <= 0) {
            shoot();
            currentCooldown = SHOOT_COOLDOWN;
        }

        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.update();
            if (bullet.isOffScreen(800, 600)) {
                bullets.remove(i);
            }
        }

        if (target != null) {
            double dx = target.getX() - x;
            double dy = target.getY() - y;
            angle = Math.toDegrees(Math.atan2(dy, dx));
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (enemyImage != null) {
            AffineTransform transform = new AffineTransform();
            transform.translate(x - SHIP_WIDTH/2, y - SHIP_HEIGHT/2);
            transform.rotate(Math.toRadians(angle), SHIP_WIDTH/2, SHIP_HEIGHT/2);
            g.drawImage(enemyImage, transform, null);

            // Debug: แสดง hitbox (ถ้าต้องการดู)
            // Rectangle bounds = getBounds();
            // g.setColor(Color.RED);
            // g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }

        drawHealthBar(g);

        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }
    }

    private void drawHealthBar(Graphics2D g) {
        int healthBarWidth = 100;  // เพิ่มขนาด health bar
        int healthBarHeight = 6;   // เพิ่มความสูง health bar
        int currentHealthWidth = (int)((health / (double)maxHealth) * healthBarWidth);

        // ย้ายตำแหน่ง health bar ให้อยู่เหนือรูป
        g.setColor(new Color(255, 0, 0, 128));
        g.fillRect((int)x - healthBarWidth/2, (int)y - SHIP_HEIGHT/2 - 15,
                healthBarWidth, healthBarHeight);

        g.setColor(new Color(0, 255, 0, 192));
        g.fillRect((int)x - healthBarWidth/2, (int)y - SHIP_HEIGHT/2 - 15,
                currentHealthWidth, healthBarHeight);
    }

    private void shoot() {
        if (target != null) {
            double dx = target.getX() - x;
            double dy = target.getY() - y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            double accuracy = 0.95;
            if (Math.random() > accuracy) {
                dx += (Math.random() - 0.5) * 20;
                dy += (Math.random() - 0.5) * 20;
                distance = Math.sqrt(dx * dx + dy * dy);
            }

            dx = (dx / distance) * BULLET_SPEED;
            dy = (dy / distance) * BULLET_SPEED;

            Bullet bullet = new Bullet(x, y, angle);
            bullet.setVelocity(dx, dy);
            bullets.add(bullet);
        }
    }

    public Rectangle getBounds() {
        // ปรับ hitbox ให้เล็กกว่ารูปเล็กน้อยเพื่อความเหมาะสมในการเล่น
        return new Rectangle(
                (int)(x - HITBOX_WIDTH/2),
                (int)(y - HITBOX_HEIGHT/2),
                HITBOX_WIDTH,
                HITBOX_HEIGHT
        );
    }

    public void hit() {
        health -= 25;
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public List<Bullet> getBullets() {
        return bullets;
    }
    public void setTarget(PlayerShip target) {
        this.target = target;
    }
}