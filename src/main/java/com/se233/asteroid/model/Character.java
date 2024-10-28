package com.se233.asteroid.model;

import java.awt.*;

abstract class Character {
    protected double x, y;        // Position
    protected double velocityX, velocityY; // Speed
    protected double angle;       // Rotation angle
    protected int health;         // Health points

    public Character(double x, double y, double velocityX, double velocityY, double angle, int health) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.angle = angle;
        this.health = health;
    }
    public void move() {
        x += velocityX;
        y += velocityY;
        screenWrap();
    }
    protected void screenWrap() {
        // Screen wrapping logic
        if (x < 0) x = 800; // Assuming 800x600 screen size
        if (x > 800) x = 0;
        if (y < 0) y = 600;
        if (y > 600) y = 0;
    }
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public abstract void draw(Graphics2D g);

    public abstract void update(); // Abstract update logic specific to each character type

    public boolean isAlive() {
        return health > 0;
    }
}