package com.se233.asteroid.model;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class SecondTier extends Character {
    private static final int SHOOT_COOLDOWN = 120;
    private int currentCooldown = 0;
    private List<Bullet> bullets;
    private PlayerShip target;
    private int maxHealth;
    private static final double BULLET_SPEED = 1.0;
    private BufferedImage shipImage;

    public SecondTier(double x, double y, double velocityX, double velocityY, double angle, int health) {
        super(x, y, velocityX, velocityY, angle, health);
        this.bullets = new ArrayList<>();
        this.maxHealth = health;
        this.currentCooldown = (int)(Math.random() * SHOOT_COOLDOWN);

        // โหลดรูปภาพยาน
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/assets/secondtier.png"));
            Image originalImage = icon.getImage();
            shipImage = new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = shipImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, 60, 60, null);
            g2d.dispose();
        } catch (Exception e) {
            System.err.println("Error loading ship image: " + e.getMessage());
        }
    }

    private void shoot() {
        if (target != null) {
            // ยิง 2 นัดพร้อมกัน
            double[] offsets = {-10, 10}; // ระยะห่างระหว่างจุดยิง

            for (double offset : offsets) {
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

                // ปรับตำแหน่งจุดเริ่มต้นของกระสุน
                double bulletStartX = x + offset * Math.cos(Math.toRadians(angle + 90));
                double bulletStartY = y + offset * Math.sin(Math.toRadians(angle + 90));

                Bullet bullet = new Bullet(bulletStartX, bulletStartY, angle);
                bullet.setVelocity(dx, dy);
                bullets.add(bullet);
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (shipImage != null) {
            AffineTransform transform = new AffineTransform();
            transform.translate(x - shipImage.getWidth()/2, y - shipImage.getHeight()/2);
            transform.rotate(Math.toRadians(angle), shipImage.getWidth()/2, shipImage.getHeight()/2);
            g.drawImage(shipImage, transform, null);
        }

        // วาด health bar
        drawHealthBar(g);

        // วาดกระสุน
        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }
    }

    private void drawHealthBar(Graphics2D g) {
        int healthBarWidth = 40;
        int healthBarHeight = 4;
        int currentHealthWidth = (int)((health / (double)maxHealth) * healthBarWidth);

        g.setColor(new Color(255, 0, 0, 128));
        g.fillRect((int)x - healthBarWidth/2, (int)y - 35,
                healthBarWidth, healthBarHeight);

        g.setColor(new Color(0, 255, 0, 192));
        g.fillRect((int)x - healthBarWidth/2, (int)y - 35,
                currentHealthWidth, healthBarHeight);
    }

    @Override
    public void update() {
        x += velocityX;
        y += velocityY;

        // เปลี่ยนจาก wrap เป็น bounce
        if (x - 30 <= 0) {
            x = 30;
            velocityX = Math.abs(velocityX);
        } else if (x + 30 >= 800) {
            x = 800 - 30;
            velocityX = -Math.abs(velocityX);
        }

        if (y - 30 <= 0) {
            y = 30;
            velocityY = Math.abs(velocityY);
        } else if (y + 30 >= 600) {
            y = 600 - 30;
            velocityY = -Math.abs(velocityY);
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

    public void setTarget(PlayerShip target) {
        this.target = target;
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x - 25, (int)y - 25, 50, 50);
    }

    public List<Bullet> getBullets() {
        return bullets;
    }

    public void hit() {
        health -= 25;
    }

    public boolean isDestroyed() {
        return health <= 0;
    }
}