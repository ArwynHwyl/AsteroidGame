package com.se233.asteroid.model;

import java.awt.*;

public class Bullet {
    private double x, y; // Position of the bullet
    private double angle; // Angle at which the bullet was fired
    private static final double SPEED = 5; // Speed of the bullet

    public Bullet(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }

    public void update() {
        // Move the bullet in the direction it's facing
        x += SPEED * Math.cos(Math.toRadians(angle));
        y += SPEED * Math.sin(Math.toRadians(angle));
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.YELLOW); // Color of the bullet
        g.fillOval((int)x, (int)y, 5, 5); // Draw the bullet as a small circle
    }

    public boolean isOffScreen(int width, int height) {
        return x < 0 || x > width || y < 0 || y > height; // Check if the bullet is off-screen
    }
}