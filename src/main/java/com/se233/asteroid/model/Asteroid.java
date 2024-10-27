package com.se233.asteroid.model;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class Asteroid extends Character {
    private int maxHealth;
    private boolean isLarge;
    private double rotationAngle;
    private double rotationSpeed;

    public Asteroid(double x, double y, boolean isLarge) {
        // Large asteroids have more health and are slower
        super(x, y,
                Math.random() * (isLarge ? 1.0 : 2.0) - (isLarge ? 0.5 : 1.0),  // velocity X
                Math.random() * (isLarge ? 1.0 : 2.0) - (isLarge ? 0.5 : 1.0),  // velocity Y
                0,  // initial angle
                isLarge ? 100 : 50);  // health

        this.isLarge = isLarge;
        this.maxHealth = health;
        this.rotationAngle = Math.random() * 360;
        this.rotationSpeed = Math.random() * 2 - 1; // Random rotation speed
    }

    @Override
    public void update() {
        // Update position
        x += velocityX;
        y += velocityY;

        // Rotate asteroid
        rotationAngle += rotationSpeed;

        // Wrap around screen
        if (x < -40) x = 840;
        if (x > 840) x = -40;
        if (y < -40) y = 640;
        if (y > 640) y = -40;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.GRAY);
        g.fillOval((int) x - 20, (int) y - 20, 40, 40);

        // Draw health bar
        g.setColor(Color.RED);
        int healthBarWidth = 40;
        int healthBarHeight = 5;
        int currentHealthWidth = (int)((health / (double)maxHealth) * healthBarWidth);
        g.fillRect((int)x - 20, (int)y - 30, currentHealthWidth, healthBarHeight);
    }

    private Polygon createAsteroidShape(int size) {
        Polygon shape = new Polygon();
        int vertices = 8; // Number of vertices for the asteroid

        for (int i = 0; i < vertices; i++) {
            double angle = 2 * Math.PI * i / vertices;
            // Add some randomness to the radius
            double radius = size * (0.8 + Math.random() * 0.4);
            int px = (int)(radius * Math.cos(angle));
            int py = (int)(radius * Math.sin(angle));
            shape.addPoint(px, py);
        }

        return shape;
    }

    private void drawHealthBar(Graphics2D g) {
        int healthBarWidth = isLarge ? 60 : 30;
        int healthBarHeight = 4;
        int currentHealthWidth = (int)((health / (double)maxHealth) * healthBarWidth);

        // Background of health bar
        g.setColor(new Color(255, 0, 0, 128));
        g.fillRect((int)x - healthBarWidth/2, (int)y - (isLarge ? 30 : 20),
                healthBarWidth, healthBarHeight);

        // Current health
        g.setColor(new Color(0, 255, 0, 192));
        g.fillRect((int)x - healthBarWidth/2, (int)y - (isLarge ? 30 : 20),
                currentHealthWidth, healthBarHeight);
    }

    public Rectangle getBounds() {
        int size = isLarge ? 40 : 20;
        return new Rectangle((int)x - size, (int)y - size, size * 2, size * 2);
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