package se233.asteroid.model;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Asteroid extends Character {
    private int maxHealth;
    private boolean isLarge;
    public double rotationAngle;
    private double rotationSpeed;
    private BufferedImage asteroidImage;

    // Sizes with correct hitboxes
    private static final int LARGE_DIAMETER = 110;
    private static final int SMALL_DIAMETER = 50;
    private static final int LARGE_HITBOX_SIZE = 100;  // ปรับให้เล็กกว่าภาพเล็กน้อย
    private static final int SMALL_HITBOX_SIZE = 45;   // ปรับให้เล็กกว่าภาพเล็กน้อย

    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;
    private boolean wasLargeDestroyed = false;

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

        loadImage();
    }

    private void loadImage() {
        try {
            // Load the dark asteroid image
            ImageIcon icon = new ImageIcon(getClass().getResource("/assets/realasteroid.png"));
            Image originalImage = icon.getImage();

            int diameter = isLarge ? LARGE_DIAMETER : SMALL_DIAMETER;
            asteroidImage = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = asteroidImage.createGraphics();

            // Minimal rendering hints for better performance
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

            g2d.drawImage(originalImage, 0, 0, diameter, diameter, null);
            g2d.dispose();
        } catch (Exception e) {
            System.err.println("Error loading asteroid image: " + e.getMessage());
            // Fallback to simple filled circle if image fails to load
            asteroidImage = new BufferedImage(
                    isLarge ? LARGE_DIAMETER : SMALL_DIAMETER,
                    isLarge ? LARGE_DIAMETER : SMALL_DIAMETER,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = asteroidImage.createGraphics();
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillOval(0, 0, asteroidImage.getWidth(), asteroidImage.getHeight());
            g2d.dispose();
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

        // Debug: แสดง hitbox (ถ้าต้องการ)
        /*
        Rectangle bounds = getBounds();
        g.setColor(new Color(255, 0, 0, 100));
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        */

        drawHealthBar(g);
    }

    private void drawHealthBar(Graphics2D g) {
        int healthBarWidth = isLarge ? LARGE_DIAMETER : SMALL_DIAMETER;
        int healthBarHeight = 4;
        int currentHealthWidth = (int)((health / (double)maxHealth) * healthBarWidth);
        int yOffset = (isLarge ? LARGE_DIAMETER : SMALL_DIAMETER) / 2 + 10;

        // Simplified health bar colors with less transparency
        g.setColor(new Color(255, 0, 0));
        g.fillRect((int)x - healthBarWidth/2, (int)y - yOffset,
                healthBarWidth, healthBarHeight);

        g.setColor(new Color(0, 255, 0));
        g.fillRect((int)x - healthBarWidth/2, (int)y - yOffset,
                currentHealthWidth, healthBarHeight);
    }

    public Rectangle getBounds() {
        int size = isLarge ? LARGE_HITBOX_SIZE : SMALL_HITBOX_SIZE;
        return new Rectangle(
                (int)(x - size/2),
                (int)(y - size/2),
                size,
                size
        );
    }

    public void hit() {
        health -= 10;
        if (health <= 0 && isLarge) {
            wasLargeDestroyed = true;
        }
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public boolean isLarge() {
        return isLarge;
    }

    public boolean wasLargeDestroyed() {
        return wasLargeDestroyed;
    }

    @Override
    public void update() {
        x += velocityX;
        y += velocityY;
        rotationAngle += rotationSpeed;
        handleScreenBounce();
    }

    private void handleScreenBounce() {
        int size = isLarge ? LARGE_HITBOX_SIZE : SMALL_HITBOX_SIZE;
        int radius = size / 2;
        boolean bounced = false;

        if (x - radius <= 0) {
            x = radius;
            velocityX = Math.abs(velocityX);
            bounced = true;
        } else if (x + radius >= SCREEN_WIDTH) {
            x = SCREEN_WIDTH - radius;
            velocityX = -Math.abs(velocityX);
            bounced = true;
        }

        if (y - radius <= 0) {
            y = radius;
            velocityY = Math.abs(velocityY);
            bounced = true;
        } else if (y + radius >= SCREEN_HEIGHT) {
            y = SCREEN_HEIGHT - radius;
            velocityY = -Math.abs(velocityY);
            bounced = true;
        }

        if (bounced) {
            rotationSpeed = Math.random() * 2 - 1;
        }
    }

    public static Asteroid createSmallAsteroid(double x, double y) {
        double angle = Math.random() * Math.PI * 2;
        double speed = 1.0 + Math.random();
        double vx = Math.cos(angle) * speed;
        double vy = Math.sin(angle) * speed;

        Asteroid smallAsteroid = new Asteroid(x, y, false);
        smallAsteroid.velocityX = vx;
        smallAsteroid.velocityY = vy;

        return smallAsteroid;
    }
}