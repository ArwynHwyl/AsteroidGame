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
    public Rectangle getBounds() {
        // ใช้ขนาดที่เล็กกว่า SPRITE_WIDTH/HEIGHT เล็กน้อย เพื่อให้การชนสมจริงมากขึ้น
        int hitboxWidth = 0;  // ประมาณ 1/3 ของความกว้างสไปรต์
        int hitboxHeight = 0; // ประมาณ 1/3 ของความสูงสไปรต์

        return new Rectangle(
                (int) x - hitboxWidth / 2,  // จุดเริ่มต้น x
                (int) y - hitboxHeight / 2, // จุดเริ่มต้น y
                hitboxWidth,            // ความกว้างของ hitbox
                hitboxHeight           // ความสูงของ hitbox
        );
    }
}
