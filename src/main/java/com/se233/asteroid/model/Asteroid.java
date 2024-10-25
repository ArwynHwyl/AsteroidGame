package com.se233.asteroid.model;

import java.awt.*;

public class Asteroid extends Character {
    private int maxHealth;

    public Asteroid(double x, double y) {
        int randomHealth = 30 + (int)(Math.random() * 71);
        super(x, y, Math.random() * 2 - 1, Math.random() * 2 - 1, 0, randomHealth);
        this.maxHealth = randomHealth; // Random speed, health=50
    }

    @Override
    public void update() {
        move(); // Simple movement without controls
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

    public Rectangle getBounds() {
        return new Rectangle((int) x - 20, (int) y - 20, 40, 40); // Asteroid bounds for collision detection
    }

    public void hit() {
        // Logic when hit by a bullet (you may want to add effects or sounds)
        health -= 10;
        if (health <= 0) {
            System.out.println("Asteroid destroyed!");
        }
    }
    public boolean isDestroyed() {
        return health <= 0;
    }
}
