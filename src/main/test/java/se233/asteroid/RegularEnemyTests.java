package com.se233.asteroid.test;

import com.se233.asteroid.model.PlayerShip;
import com.se233.asteroid.model.RegularEnemy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RegularEnemyTests {

    @Test
    void testEnemyMovement() {
        RegularEnemy enemy = new RegularEnemy(100, 100, 1, 1, 0, 100);

        double startX = enemy.getX();
        double startY = enemy.getY();

        enemy.update();

        assertTrue(enemy.getX() != startX || enemy.getY() != startY,
                "Enemy should move from its starting position");
    }

    @Test
    void testEnemyTakeDamage() {
        RegularEnemy enemy = new RegularEnemy(100, 100, 0, 0, 0, 100);

        assertEquals(100, enemy.getHealth(), "Initial health should be 100");

        enemy.hit();

        assertTrue(enemy.getHealth() < 100, "Health should decrease after being hit");
    }

    @Test
    void testEnemyDestruction() {
        // สร้าง enemy ด้วย health น้อยๆ
        RegularEnemy enemy = new RegularEnemy(100, 100, 0, 0, 0, 20);

        // โจมตีจนกว่าจะถูกทำลาย
        while (!enemy.isDestroyed()) {
            enemy.hit();
        }

        // เช็คว่าถูกทำลายแล้ว
        assertTrue(enemy.isDestroyed(), "Enemy should be destroyed when health reaches 0");
    }

    @Test
    void testEnemyRotation() {
        // สร้าง enemy และ target
        RegularEnemy enemy = new RegularEnemy(100, 100, 0, 0, 0, 100);
        PlayerShip target = new PlayerShip(200, 200);

        // กำหนด target ให้ enemy
        enemy.setTarget(target);

        // เก็บมุมเริ่มต้น
        double startAngle = enemy.getAngle();

        // update หลายๆครั้งเพื่อให้หันไปหา target
        for (int i = 0; i < 10; i++) {
            enemy.update();
        }

        // เช็คว่ามุมเปลี่ยนไป
        assertNotEquals(startAngle, enemy.getAngle(),
                "Enemy should rotate to face the target");
    }

    @Test
    void testEnemyBoundaryBehavior() {
        // สร้าง enemy ที่ขอบจอ
        RegularEnemy enemy = new RegularEnemy(0, 0, -1, -1, 0, 100);

        // update การเคลื่อนที่
        enemy.update();

        // เช็คว่าไม่ออกนอกขอบจอ
        assertTrue(enemy.getX() >= 0 && enemy.getX() <= 800 &&
                        enemy.getY() >= 0 && enemy.getY() <= 600,
                "Enemy should stay within screen boundaries");
    }
}