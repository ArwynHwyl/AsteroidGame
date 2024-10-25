package com.se233.asteroid.model;

import java.awt.*;

public class Boss extends Character {
    private int attackCooldown;

    public Boss(double x, double y) {
        super(x, y, 0, 0, 0, 500); // Boss with more health and slower speed
        this.attackCooldown = 100; // Example attack cooldown
    }

    @Override
    public void update() {
        move();
        attack();
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.RED);
        g.fillOval((int) x - 50, (int) y - 50, 100, 100); // Bigger size for the boss
    }

    private void attack() {
        if (attackCooldown <= 0) {
            // Boss attack logic (e.g., firing projectiles)
            System.out.println("Boss attack!");
            attackCooldown = 100; // Reset cooldown
        } else {
            attackCooldown--;
        }
    }
}
