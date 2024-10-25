package com.se233.asteroid.model;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
public class PlayerShip extends Character {
    private boolean shooting;
    private List<Bullet> bullets; // List to hold bullets
    private static final double MAX_VELOCITY = 3.0;
    public PlayerShip(double x, double y) {
        super(x, y, 0, 0, 0, 100); // Initialize with health=100, no speed at start
        bullets = new ArrayList<>(); // Initialize the bullet list
    }

    @Override
    public void update() {
        move();
        // Update each bullet
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.update();
            // Remove the bullet if it's off-screen
            if (bullet.isOffScreen(800, 600)) {
                bullets.remove(i);
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        AffineTransform old = g.getTransform();
        g.translate(x, y);
        g.rotate(Math.toRadians(angle));
        g.setColor(Color.WHITE);
        int[] xPoints = {-15, 15, -15};
        int[] yPoints = {-10, 0, 10};
        g.fillPolygon(xPoints, yPoints, 3); // Simple triangle spaceship
        g.setTransform(old);

        // Draw each bullet
        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }
    }

    public void shoot() {
        // Create a new bullet at the ship's position and add it to the list
        bullets.add(new Bullet(x, y, angle));
        System.out.println("Pew! Pew!"); // Debug print for shooting
    }

    public void setShooting(boolean shooting) {
        this.shooting = shooting;
        if (shooting) {
            shoot(); // Shoot immediately when the key is pressed
        }
    }
    private void limitVelocity() {
        // Calculate current speed
        double currentSpeed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);

        // If current speed exceeds maximum velocity, scale it down
        if (currentSpeed > MAX_VELOCITY) {
            double scale = MAX_VELOCITY / currentSpeed;
            velocityX *= scale;
            velocityY *= scale;
        }
    }
    public void rotateLeft() {
        angle -= 10;
    }

    public void rotateRight() {
        angle += 10;
    }

    public void moveUp() {
        velocityY -= 0.5;
        limitVelocity();
    }


    public void moveLeft() {
        velocityX -= 0.5;
        limitVelocity();
    }
    public void moveRight() {
        velocityX += 0.5;
        limitVelocity();
    }
    public void moveDown() {
        velocityY += 0.5;
        limitVelocity();
    }
}