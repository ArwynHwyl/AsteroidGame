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

    private static final int SHIP_WIDTH = 50;
    private static final int SHIP_HEIGHT = 40;
    private static final int HITBOX_WIDTH = 45;
    private static final int HITBOX_HEIGHT = 35;

    // ค่าควบคุมการเคลื่อนที่แบบฟิสิกส์
    private static final double MAX_SPEED = 3.0;
    private static final double THRUST = 0.05;
    private static final double DRAG = 0.99; // ค่าความเฉื่อย (inertia)
    private static final int PADDING = 50;

    // ตัวแปรควบคุมพฤติกรรม
    private double targetVelocityX = 0;
    private double targetVelocityY = 0;
    private double lastDirectionChangeTime = 0;
    private static final double DIRECTION_CHANGE_INTERVAL = 2000; // 2 วินาที
    private double currentThrust = 0;

    public RegularEnemy(double x, double y, double velocityX, double velocityY, double angle, int health) {
        super(x, y, velocityX, velocityY, angle, health);
        this.bullets = new ArrayList<>();
        this.maxHealth = health;
        this.currentCooldown = (int)(Math.random() * SHOOT_COOLDOWN);

        // กำหนดความเร็วเริ่มต้นแบบสุ่ม
        this.velocityX = (Math.random() - 0.5) * 2;
        this.velocityY = (Math.random() - 0.5) * 2;

        loadImage();
    }

    private void loadImage() {
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
        updateMovement();
        updateRotation();
        updateShooting();
        updateBullets();
    }

    private void updateMovement() {
        double currentTime = System.currentTimeMillis();

        // สุ่มเปลี่ยนทิศทางเป็นระยะ
        if (currentTime - lastDirectionChangeTime > DIRECTION_CHANGE_INTERVAL) {
            // เปลี่ยนเป้าหมายความเร็วแบบสุ่ม
            targetVelocityX = (Math.random() - 0.5) * MAX_SPEED;
            targetVelocityY = (Math.random() - 0.5) * MAX_SPEED;
            lastDirectionChangeTime = currentTime;
            currentThrust = THRUST * (1 + Math.random()); // สุ่มแรงขับเคลื่อน
        }

        // ค่อยๆ ปรับความเร็วเข้าหาเป้าหมาย
        velocityX += (targetVelocityX - velocityX) * currentThrust;
        velocityY += (targetVelocityY - velocityY) * currentThrust;

        // จำกัดความเร็วสูงสุด
        double speed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        if (speed > MAX_SPEED) {
            velocityX = (velocityX / speed) * MAX_SPEED;
            velocityY = (velocityY / speed) * MAX_SPEED;
        }

        // อัพเดทตำแหน่ง
        x += velocityX;
        y += velocityY;

        // เช็คและปรับการชนขอบจอ
        handleBoundaryCollision();

        // ใส่ความเฉื่อย
        velocityX *= DRAG;
        velocityY *= DRAG;
    }

    private void handleBoundaryCollision() {
        // เช็คขอบซ้าย-ขวา
        if (x < PADDING) {
            x = PADDING;
            velocityX = Math.abs(velocityX) * 0.8; // ลดความเร็วเมื่อชนขอบ
            targetVelocityX = Math.abs(targetVelocityX);
        } else if (x > 800 - PADDING) {
            x = 800 - PADDING;
            velocityX = -Math.abs(velocityX) * 0.8;
            targetVelocityX = -Math.abs(targetVelocityX);
        }

        // เช็คขอบบน-ล่าง
        if (y < PADDING) {
            y = PADDING;
            velocityY = Math.abs(velocityY) * 0.8;
            targetVelocityY = Math.abs(targetVelocityY);
        } else if (y > 600 - PADDING) {
            y = 600 - PADDING;
            velocityY = -Math.abs(velocityY) * 0.8;
            targetVelocityY = -Math.abs(targetVelocityY);
        }
    }

    private void updateRotation() {
        if (target != null) {
            // หันเข้าหาผู้เล่นอย่างนุ่มนวล
            double dx = target.getX() - x;
            double dy = target.getY() - y;
            double targetAngle = Math.toDegrees(Math.atan2(dy, dx));
            double angleDiff = targetAngle - angle;

            // ปรับมุมให้อยู่ในช่วง -180 ถึง 180
            while (angleDiff > 180) angleDiff -= 360;
            while (angleDiff < -180) angleDiff += 360;

            // หมุนด้วยความเร็วที่แปรผันตามระยะห่างของมุม
            double rotationSpeed = Math.min(0.15, Math.abs(angleDiff) / 360);
            angle += angleDiff * rotationSpeed;
        }
    }

    private void updateShooting() {
        if (currentCooldown > 0) {
            currentCooldown--;
        }

        if (target != null && currentCooldown <= 0) {
            shoot();
            currentCooldown = SHOOT_COOLDOWN;
        }
    }

    private void shoot() {
        if (target != null) {
            double dx = target.getX() - x;
            double dy = target.getY() - y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            // เพิ่มความแม่นยำเมื่ออยู่นิ่งมากขึ้น
            double speed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
            double accuracy = 0.95 - (speed / MAX_SPEED) * 0.2;

            if (Math.random() > accuracy) {
                dx += (Math.random() - 0.5) * 20;
                dy += (Math.random() - 0.5) * 20;
                distance = Math.sqrt(dx * dx + dy * dy);
            }

            dx = (dx / distance) * BULLET_SPEED;
            dy = (dy / distance) * BULLET_SPEED;

            Bullet bullet = new Bullet(x, y, angle);
            // เพิ่มความเร็วของยานเข้าไปในความเร็วกระสุนเพื่อให้สมจริง
            bullet.setVelocity(dx + velocityX * 0.3, dy + velocityY * 0.3);
            bullets.add(bullet);
        }
    }

    private void updateBullets() {
        bullets.removeIf(bullet -> bullet.isOffScreen(800, 600));
        bullets.forEach(Bullet::update);
    }

    @Override
    public void draw(Graphics2D g) {
        if (enemyImage != null) {
            AffineTransform transform = new AffineTransform();
            transform.translate(x - SHIP_WIDTH/2, y - SHIP_HEIGHT/2);
            transform.rotate(Math.toRadians(angle), SHIP_WIDTH/2, SHIP_HEIGHT/2);
            g.drawImage(enemyImage, transform, null);
        }

        drawHealthBar(g);

        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }
    }

    private void drawHealthBar(Graphics2D g) {
        int healthBarWidth = 40;
        int healthBarHeight = 4;
        int currentHealthWidth = (int)((health / (double)maxHealth) * healthBarWidth);

        g.setColor(new Color(255, 0, 0, 128));
        g.fillRect((int)x - healthBarWidth/2, (int)y - SHIP_HEIGHT/2 - 15,
                healthBarWidth, healthBarHeight);

        g.setColor(new Color(0, 255, 0, 192));
        g.fillRect((int)x - healthBarWidth/2, (int)y - SHIP_HEIGHT/2 - 15,
                currentHealthWidth, healthBarHeight);
    }

    public Rectangle getBounds() {
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