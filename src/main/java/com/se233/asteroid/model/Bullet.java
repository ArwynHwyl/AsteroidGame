package com.se233.asteroid.model;

import java.awt.*;

public class Bullet {
    private double x;
    private double y;
    private double angle;
    private double speed = 2; // Speed of the bullet

    public Bullet(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }

    public void update() {
        // Update bullet position based on angle
        x += speed * Math.cos(Math.toRadians(angle));
        y += speed * Math.sin(Math.toRadians(angle));
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