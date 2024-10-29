package se233.asteroid.model;

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

    // Movement variables
    private double moveAngle = 0;
    private double moveSpeed = 2.0;
    private int stateTimer = 0;
    private int currentState = 0;

    // Screen boundaries with padding
    private static final int PADDING = 50;
    private static final int MIN_X = PADDING;
    private static final int MAX_X = 800 - PADDING;
    private static final int MIN_Y = PADDING;
    private static final int MAX_Y = 600 - PADDING;

    public SecondTier(double x, double y, double velocityX, double velocityY, double angle, int health) {
        super(x, y, velocityX, velocityY, angle, health);
        this.bullets = new ArrayList<>();
        this.maxHealth = health;
        this.currentCooldown = (int)(Math.random() * SHOOT_COOLDOWN);

        // เอาการ override ตำแหน่งออก เพื่อให้ใช้ตำแหน่งที่ส่งมาจาก parameter
        this.velocityX = velocityX;
        this.velocityY = velocityY;

        // สุ่มทิศทางเริ่มต้น
        this.moveAngle = Math.random() * 90 - 45; // -45 ถึง 45 องศา

        initializeShipImage();
    }

    private void initializeShipImage() {
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

    @Override
    public void update() {
        stateTimer++;

        // เปลี่ยน state ทุก 3 วินาที (180 frames)
        if (stateTimer >= 180) {
            stateTimer = 0;
            currentState = (currentState + 1) % 3;
            // สุ่มความเร็วใหม่
            moveSpeed = 1.5 + Math.random();
        }

        // แก้ไขให้เช็คว่าอยู่ในช่วง spawn ไหม ถ้าใช่ถึงจะ updateMovement
        if (stateTimer > 60) { // ให้เวลา 1 วินาที (60 frames) ก่อนเริ่มเคลื่อนที่
            updateMovement();
        }

        updateRotation();
        updateShooting();
        updateBullets();
    }

    private void updateMovement() {
        switch (currentState) {
            case 0: // เคลื่อนที่เป็นเส้นตรงพร้อมเปลี่ยนทิศ
                if (stateTimer % 60 == 0) { // เปลี่ยนทิศทางทุก 1 วินาที
                    moveAngle += 45;
                }
                break;

            case 1: // บินวนเป็นวงกว้าง
                moveAngle += 2;
                break;

            case 2: // บินซิกแซก
                if (stateTimer % 30 == 0) { // เปลี่ยนทิศทางทุก 0.5 วินาที
                    moveAngle = -moveAngle;
                }
                break;
        }

        // คำนวณการเคลื่อนที่
        double radians = Math.toRadians(moveAngle);
        double newX = x + Math.cos(radians) * moveSpeed;
        double newY = y + Math.sin(radians) * moveSpeed;

        // ตรวจสอบและปรับการชนขอบ
        if (newX < MIN_X) {
            newX = MIN_X;
            moveAngle = 180 - moveAngle;
        } else if (newX > MAX_X) {
            newX = MAX_X;
            moveAngle = 180 - moveAngle;
        }

        if (newY < MIN_Y) {
            newY = MIN_Y;
            moveAngle = -moveAngle;
        } else if (newY > MAX_Y) {
            newY = MAX_Y;
            moveAngle = -moveAngle;
        }

        x = newX;
        y = newY;
    }

    private void updateRotation() {
        if (target != null) {
            // หันเข้าหาผู้เล่นอย่างนุ่มนวล
            double targetAngle = Math.toDegrees(Math.atan2(target.getY() - y, target.getX() - x));
            double angleDiff = targetAngle - angle;

            // ปรับให้อยู่ในช่วง -180 ถึง 180
            while (angleDiff > 180) angleDiff -= 360;
            while (angleDiff < -180) angleDiff += 360;

            // หมุนด้วยความเร็วที่เหมาะสม
            angle += angleDiff * 0.1;
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
            double[] offsets = {-10, 10};

            for (double offset : offsets) {
                double dx = target.getX() - x;
                double dy = target.getY() - y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                // เพิ่มความแม่นยำ
                double accuracy = 0.95;
                if (Math.random() > accuracy) {
                    dx += (Math.random() - 0.5) * 20;
                    dy += (Math.random() - 0.5) * 20;
                    distance = Math.sqrt(dx * dx + dy * dy);
                }

                dx = (dx / distance) * BULLET_SPEED;
                dy = (dy / distance) * BULLET_SPEED;

                double bulletStartX = x + offset * Math.cos(Math.toRadians(angle + 90));
                double bulletStartY = y + offset * Math.sin(Math.toRadians(angle + 90));

                Bullet bullet = new Bullet(bulletStartX, bulletStartY, angle);
                bullet.setVelocity(dx, dy);
                bullets.add(bullet);
            }
        }
    }

    private void updateBullets() {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.update();
            if (bullet.isOffScreen(800, 600)) {
                bullets.remove(i);
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
        g.fillRect((int)x - healthBarWidth/2, (int)y - 35,
                healthBarWidth, healthBarHeight);

        g.setColor(new Color(0, 255, 0, 192));
        g.fillRect((int)x - healthBarWidth/2, (int)y - 35,
                currentHealthWidth, healthBarHeight);
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