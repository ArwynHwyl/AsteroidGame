package com.se233.asteroid.model;

import java.awt.*;

public class Bullet {
    private double x;
    private double y;
    private double angle;
    private double velocityX;
    private double velocityY;
    private double speed = 2; // Speed of the bullet

    public Bullet(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        // Set initial velocity based on angle
        this.velocityX = speed * Math.cos(Math.toRadians(angle));
        this.velocityY = speed * Math.sin(Math.toRadians(angle));
    }

    public void setVelocity(double velocityX, double velocityY) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    public void update() {
        // Update bullet position based on velocity
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
        g.setColor(Color.YELLOW);
        g.fillOval((int) x - 2, (int) y - 2, 4, 4); // Draws a simple bullet
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x - 2, (int) y - 2, 4, 4); // Bullet bounds for collision detection
    }
}