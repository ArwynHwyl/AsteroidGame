package com.se233.asteroid.model;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class PlayerShip extends Character {
    private boolean shooting;
    private  Point mouseStart;
    public PlayerShip(double x, double y) {
        super(x, y, 0, 0, 0, 100); // Initialize with health=100, no speed at start
    }

    @Override
    public void update() {
        move();
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
    }

    public void shoot() {
        // Bullet shooting logic (not fully implemented here)
        System.out.println("Pew! Pew!"); // Debug print for shooting
    }
    public void setShooting(boolean shooting) {
        this.shooting = shooting;
    }
    public void rotateLeft() {
        angle -= 10;
    }

    public void rotateRight() {
        angle += 10;
    }

    public void moveUp() {
        velocityY -= 0.5;
    }


    public void moveLeft() {
        velocityX -= 0.5;
    }
    public void moveRight() {
        velocityX += 0.5;
    }
    public void moveDown() {
        velocityY += 0.5;
    }
}