package com.se233.asteroid.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Boss extends Character {
    private int attackCooldown;
    private int attackPattern;
    private int patternDuration;
    private List<Bullet> bullets;
    private static final int PATTERN_SWITCH_TIME = 300;
    private static final double MOVEMENT_SPEED = 2.0;
    private static final int INITIAL_HEALTH = 1000;

    // Constants for bullet patterns
    private static final double SPIRAL_SPEED = 6.0;
    private static final double SPREAD_SPEED = 8.0;
    private static final double WAVE_SPEED = 7.0;
    private static final double CROSS_SPEED = 10.0;

    public Boss(double x, double y) {
        super(x, y, 0, 0, 0, INITIAL_HEALTH);
        this.attackCooldown = 0;
        this.attackPattern = 0;
        this.patternDuration = 0;
        this.bullets = new ArrayList<>();
    }

    @Override
    public void update() {
        updateMovement();
        updateAttackPattern();
        updateBullets();
        screenWrap();
    }

    private void updateMovement() {
        patternDuration++;
        double t = patternDuration * 0.02;

        // Complex movement pattern combining circular and figure-8
        velocityX = Math.cos(t) * MOVEMENT_SPEED * Math.sin(t * 0.5);
        velocityY = Math.sin(t) * MOVEMENT_SPEED * Math.cos(t * 0.5);

        move();
        angle = Math.toDegrees(Math.atan2(velocityY, velocityX));
    }

    private void updateAttackPattern() {
        if (patternDuration >= PATTERN_SWITCH_TIME) {
            patternDuration = 0;
            attackPattern = (attackPattern + 1) % 4;
        }

        if (attackCooldown > 0) {
            attackCooldown--;
            return;
        }

        if (isAlive()) {
            executeAttackPattern();
        }
    }

    private void executeAttackPattern() {
        switch (attackPattern) {
            case 0:
                spiralAttack();
                attackCooldown = 8;
                break;
            case 1:
                spreadAttack();
                attackCooldown = 45;
                break;
            case 2:
                waveAttack();
                attackCooldown = 15;
                break;
            case 3:
                crossAttack();
                attackCooldown = 30;
                break;
        }
    }

    private void spiralAttack() {
        if (!isAlive()) return;
        double spiralAngle = angle + patternDuration * 10;
        Bullet bullet = new Bullet(x, y, spiralAngle);
        bullet.setVelocity(
                SPIRAL_SPEED * Math.cos(Math.toRadians(spiralAngle)),
                SPIRAL_SPEED * Math.sin(Math.toRadians(spiralAngle))
        );
        bullets.add(bullet);
    }

    private void spreadAttack() {
        if (!isAlive()) return;
        int numBullets = 12;
        for (int i = 0; i < numBullets; i++) {
            double spreadAngle = angle + (360.0 / numBullets) * i;
            Bullet bullet = new Bullet(x, y, spreadAngle);
            bullet.setVelocity(
                    SPREAD_SPEED * Math.cos(Math.toRadians(spreadAngle)),
                    SPREAD_SPEED * Math.sin(Math.toRadians(spreadAngle))
            );
            bullets.add(bullet);
        }
    }

    private void waveAttack() {
        if (!isAlive()) return;
        double baseAngle = angle + Math.sin(patternDuration * 0.1) * 30;
        for (int i = -3; i <= 3; i++) {
            double waveAngle = baseAngle + i * 10;
            Bullet bullet = new Bullet(x, y, waveAngle);
            bullet.setVelocity(
                    WAVE_SPEED * Math.cos(Math.toRadians(waveAngle)),
                    WAVE_SPEED * Math.sin(Math.toRadians(waveAngle))
            );
            bullets.add(bullet);
        }
    }

    private void crossAttack() {
        if (!isAlive()) return;
        for (int i = 0; i < 4; i++) {
            double crossAngle = angle + i * 90;
            Bullet bullet = new Bullet(x, y, crossAngle);
            bullet.setVelocity(
                    CROSS_SPEED * Math.cos(Math.toRadians(crossAngle)),
                    CROSS_SPEED * Math.sin(Math.toRadians(crossAngle))
            );
            bullets.add(bullet);
        }
    }

    private void updateBullets() {
        bullets.removeIf(bullet -> bullet.isOffScreen(800, 600));
        bullets.forEach(Bullet::update);
    }

    @Override
    public void draw(Graphics2D g) {
        if (!isAlive()) return;

        // Draw boss body
        g.setColor(Color.RED);
        g.fillOval((int) x - 40, (int) y - 40, 80, 80);

        // Draw inner circle with pattern indicator
        Color indicatorColor;
        switch (attackPattern) {
            case 0: indicatorColor = new Color(0, 255, 255, 180); break; // Cyan
            case 1: indicatorColor = new Color(255, 255, 0, 180); break; // Yellow
            case 2: indicatorColor = new Color(0, 255, 0, 180); break;   // Green
            default: indicatorColor = new Color(255, 165, 0, 180);       // Orange
        }
        g.setColor(indicatorColor);
        g.fillOval((int) x - 20, (int) y - 20, 40, 40);

        // Draw direction indicator
        int dirSize = 30;
        int dirX = (int) (x + Math.cos(Math.toRadians(angle)) * 40);
        int dirY = (int) (y + Math.sin(Math.toRadians(angle)) * 40);
        g.setColor(Color.WHITE);
        g.drawLine((int) x, (int) y, dirX, dirY);

        // Draw bullets using their sprite system
        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }

        // Draw health bar
        drawHealthBar(g);
    }

    private void drawHealthBar(Graphics2D g) {
        int barWidth = 100;
        int barHeight = 10;
        int x = (int) this.x - barWidth / 2;
        int y = (int) this.y - 60;

        // Bar background
        g.setColor(new Color(60, 60, 60, 180));
        g.fillRect(x - 1, y - 1, barWidth + 2, barHeight + 2);

        // Health bar
        float healthPercent = (float) health / INITIAL_HEALTH;
        Color healthColor = new Color(
                (int) (255 * (1 - healthPercent)),  // More red as health decreases
                (int) (255 * healthPercent),        // More green as health increases
                0,
                200
        );
        g.setColor(healthColor);
        g.fillRect(x, y, (int)(barWidth * healthPercent), barHeight);
    }

    public List<Bullet> getBullets() {
        return bullets;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x - 35, (int) y - 35, 70, 70);
    }

    public void hit(int damage) {
        health -= damage;
        if (health < 0) health = 0;
    }
}