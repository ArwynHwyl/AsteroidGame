package com.se233.asteroid.model;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

public class RegularEnemy extends Character {
    private static final int SHOOT_COOLDOWN = 60; // 1 second at 60 FPS
    private int currentCooldown = 0;
    private List<Bullet> bullets;
    private PlayerShip target;
    private int maxHealth;

    public RegularEnemy(double x, double y, double velocityX, double velocityY, double angle, int health) {
        super(x, y, velocityX, velocityY, angle, health);
        this.bullets = new ArrayList<>();
        this.maxHealth = health;
    }

    public void setTarget(PlayerShip target) {
        this.target = target;
    }

    @Override
    public void update() {
        // Update position
        x += velocityX;
        y += velocityY;

        // Wrap around screen edges
        if (x < -20) x = 820;
        if (x > 820) x = -20;
        if (y < -20) y = 620;
        if (y > 620) y = -20;

        // Update shooting cooldown
        if (currentCooldown > 0) {
            currentCooldown--;
        }

        // If we have a target, try to shoot at it
        if (target != null && currentCooldown <= 0) {
            shoot();
            currentCooldown = SHOOT_COOLDOWN;
        }

        // Update bullets
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.update();
            if (bullet.isOffScreen(800, 600)) {
                bullets.remove(i);
            }
        }

        // Update angle to face target
        if (target != null) {
            double dx = target.getX() - x;
            double dy = target.getY() - y;
            angle = Math.toDegrees(Math.atan2(dy, dx));
        }
    }

    @Override
    public void draw(Graphics2D g) {
        // Store the current transform
        AffineTransform oldTransform = g.getTransform();

        // Move to enemy position
        g.translate(x, y);
        g.rotate(Math.toRadians(angle));

        // Draw the enemy ship
        drawShip(g);

        // Restore the original transform
        g.setTransform(oldTransform);

        // Draw health bar
        drawHealthBar(g);

        // Draw bullets
        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }
    }

    private void drawShip(Graphics2D g) {
        // Main body
        g.setColor(Color.RED);
        int[] xPoints = {15, -15, -15};
        int[] yPoints = {0, -10, 10};
        g.fillPolygon(xPoints, yPoints, 3);

        // Wing details
        g.setColor(Color.ORANGE);
        g.fillRect(-10, -12, 5, 24);
    }

    private void drawHealthBar(Graphics2D g) {
        int healthBarWidth = 40;
        int healthBarHeight = 4;
        int currentHealthWidth = (int)((health / (double)maxHealth) * healthBarWidth);

        // Background of health bar
        g.setColor(new Color(255, 0, 0, 128));
        g.fillRect((int)x - healthBarWidth/2, (int)y - 25,
                healthBarWidth, healthBarHeight);

        // Current health
        g.setColor(new Color(0, 255, 0, 192));
        g.fillRect((int)x - healthBarWidth/2, (int)y - 25,
                currentHealthWidth, healthBarHeight);
    }

    private void shoot() {
        if (target != null) {
            double bulletSpeed = 5.0;

            // Calculate direction to target
            double dx = target.getX() - x;
            double dy = target.getY() - y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            // Normalize direction and multiply by bullet speed
            dx = (dx / distance) * bulletSpeed;
            dy = (dy / distance) * bulletSpeed;

            // Create bullet with calculated velocity
            Bullet bullet = new Bullet(x, y, angle);
            bullet.setVelocity(dx, dy);
            bullets.add(bullet);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x - 15, (int)y - 10, 30, 20);
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
}