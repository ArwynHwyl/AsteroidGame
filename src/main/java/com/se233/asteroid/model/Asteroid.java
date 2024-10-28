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

    // ขนาดต่างๆ คงเดิม
    private static final int LARGE_WIDTH = 120;
    private static final int LARGE_HEIGHT = 70;
    private static final int SMALL_WIDTH = 90;
    private static final int SMALL_HEIGHT = 50;
    private static final int LARGE_HITBOX = 50;
    private static final int SMALL_HITBOX = 25;

    // เพิ่มค่าคงที่สำหรับขอบเขตหน้าจอ
    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;

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

            int width = isLarge ? LARGE_WIDTH : SMALL_WIDTH;
            int height = isLarge ? LARGE_HEIGHT : SMALL_HEIGHT;

            asteroidImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = asteroidImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int drawWidth = width;
            int drawHeight = height;

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

        handleScreenBounce();
    }

    private void handleScreenBounce() {
        int boundWidth = isLarge ? LARGE_WIDTH : SMALL_WIDTH;
        int boundHeight = isLarge ? LARGE_HEIGHT : SMALL_HEIGHT;
        boolean bounced = false;

        if (x - boundWidth/2 <= 0) {
            x = boundWidth/2;
            velocityX = Math.abs(velocityX);
            bounced = true;
        } else if (x + boundWidth/2 >= SCREEN_WIDTH) {
            x = SCREEN_WIDTH - boundWidth/2;
            velocityX = -Math.abs(velocityX);
            bounced = true;
        }
        if (y - boundHeight/2 <= 0) {
            y = boundHeight/2;
            velocityY = Math.abs(velocityY);
            bounced = true;
        } else if (y + boundHeight/2 >= SCREEN_HEIGHT) {
            y = SCREEN_HEIGHT - boundHeight/2;
            velocityY = -Math.abs(velocityY);
            bounced = true;
        }
        if (bounced) {
            rotationSpeed = Math.random() * 2 - 1;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (asteroidImage != null) {
            AffineTransform transform = new AffineTransform();
            transform.translate(x - asteroidImage.getWidth()/2, y - asteroidImage.getHeight()/2);
            transform.rotate(Math.toRadians(rotationAngle), asteroidImage.getWidth()/2, asteroidImage.getHeight()/2);
            g.drawImage(asteroidImage, transform, null);
        }

        drawHealthBar(g);
    }

    private void drawHealthBar(Graphics2D g) {
        int healthBarWidth = isLarge ? 120 : 80;
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