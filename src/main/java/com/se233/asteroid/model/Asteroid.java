package com.se233.asteroid.model;

import java.awt.*;

public class Asteroid extends Character {
    public Asteroid(double x, double y) {
        super(x, y, Math.random() * 2 - 1, Math.random() * 2 - 1, 0, 50); // Random speed, health=50
    }

    @Override
    public void update() {
        move(); // Simple movement without controls
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.GRAY);
        g.fillOval((int) x - 20, (int) y - 20, 40, 40); // Draws a simple asteroid
    }
}

