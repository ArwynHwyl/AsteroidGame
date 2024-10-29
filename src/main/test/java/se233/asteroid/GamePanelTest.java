package com.se233.asteroid;
import com.se233.asteroid.controller.GamePanel;
import com.se233.asteroid.model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;


import java.util.List;

public class GamePanelTest {
    private GamePanel gamePanel;
    private PlayerShip player;
    private static final int TEST_X = 400;
    private static final int TEST_Y = 300;

    @BeforeEach
    void setUp() {
        gamePanel = new GamePanel();
        try {
            java.lang.reflect.Method initMethod = GamePanel.class.getDeclaredMethod("initializeGame");
            initMethod.setAccessible(true);
            initMethod.invoke(gamePanel);

            java.lang.reflect.Field playerField = GamePanel.class.getDeclaredField("player");
            playerField.setAccessible(true);
            player = (PlayerShip) playerField.get(gamePanel);
        } catch (Exception e) {
            fail("Failed to setup test: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test scoring for destroying a small asteroid")
    void testSmallAsteroidScore() {
        try {
            java.lang.reflect.Field scoreField = GamePanel.class.getDeclaredField("score");
            java.lang.reflect.Field asteroidsField = GamePanel.class.getDeclaredField("asteroids");
            scoreField.setAccessible(true);
            asteroidsField.setAccessible(true);

            scoreField.set(gamePanel, 0);

            List<Asteroid> asteroids = (List<Asteroid>) asteroidsField.get(gamePanel);
            asteroids.clear();
            Asteroid smallAsteroid = new Asteroid(TEST_X, TEST_Y, false); // Small asteroid has 50 health, takes 5 hits
            asteroids.add(smallAsteroid);

            java.lang.reflect.Method checkBulletCollisions = GamePanel.class.getDeclaredMethod("checkBulletCollisions", Bullet.class);
            checkBulletCollisions.setAccessible(true);

            // Hit asteroid multiple times until destroyed
            for (int i = 0; i < 5; i++) {
                Bullet bullet = new Bullet(TEST_X, TEST_Y, 0);
                checkBulletCollisions.invoke(gamePanel, bullet);
            }

            int score = (int) scoreField.get(gamePanel);
            assertEquals(1, score, "Small asteroid should be worth 1 point");
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test scoring for destroying a large asteroid")
    void testLargeAsteroidScore() {
        try {
            java.lang.reflect.Field scoreField = GamePanel.class.getDeclaredField("score");
            java.lang.reflect.Field asteroidsField = GamePanel.class.getDeclaredField("asteroids");
            scoreField.setAccessible(true);
            asteroidsField.setAccessible(true);

            scoreField.set(gamePanel, 0);

            List<Asteroid> asteroids = (List<Asteroid>) asteroidsField.get(gamePanel);
            asteroids.clear();
            Asteroid largeAsteroid = new Asteroid(TEST_X, TEST_Y, true); // Large asteroid has 100 health, takes 10 hits
            asteroids.add(largeAsteroid);

            java.lang.reflect.Method checkBulletCollisions = GamePanel.class.getDeclaredMethod("checkBulletCollisions", Bullet.class);
            checkBulletCollisions.setAccessible(true);

            // Hit asteroid multiple times until destroyed
            for (int i = 0; i < 10; i++) {
                Bullet bullet = new Bullet(TEST_X, TEST_Y, 0);
                checkBulletCollisions.invoke(gamePanel, bullet);
            }

            int score = (int) scoreField.get(gamePanel);
            assertEquals(2, score, "Large asteroid should be worth 2 points");
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test scoring for destroying a regular enemy")
    void testRegularEnemyScore() {
        try {
            java.lang.reflect.Field scoreField = GamePanel.class.getDeclaredField("score");
            java.lang.reflect.Field enemiesField = GamePanel.class.getDeclaredField("regularEnemies");
            scoreField.setAccessible(true);
            enemiesField.setAccessible(true);

            scoreField.set(gamePanel, 0);

            List<RegularEnemy> enemies = (List<RegularEnemy>) enemiesField.get(gamePanel);
            enemies.clear();
            RegularEnemy enemy = new RegularEnemy(TEST_X, TEST_Y, 0, 0, 0, 50); // 50 health, takes 2 hits
            enemies.add(enemy);

            java.lang.reflect.Method checkBulletCollisions = GamePanel.class.getDeclaredMethod("checkBulletCollisions", Bullet.class);
            checkBulletCollisions.setAccessible(true);

            // Hit enemy multiple times until destroyed
            for (int i = 0; i < 2; i++) {
                Bullet bullet = new Bullet(TEST_X, TEST_Y, 0);
                checkBulletCollisions.invoke(gamePanel, bullet);
            }

            int score = (int) scoreField.get(gamePanel);
            assertEquals(1, score, "Regular enemy should be worth 1 point");
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test scoring for destroying a second tier enemy")
    void testSecondTierEnemyScore() {
        try {
            java.lang.reflect.Field scoreField = GamePanel.class.getDeclaredField("score");
            java.lang.reflect.Field enemiesField = GamePanel.class.getDeclaredField("secondTierEnemies");
            scoreField.setAccessible(true);
            enemiesField.setAccessible(true);

            // Reset score and clear enemies
            scoreField.set(gamePanel, 0);
            List<SecondTier> enemies = (List<SecondTier>) enemiesField.get(gamePanel);
            enemies.clear();

            // Add test enemy with debug output
            SecondTier enemy = new SecondTier(TEST_X, TEST_Y, 0, 0, 0, 75);
            enemies.add(enemy);

            System.out.println("Initial enemy position: " + enemy.getX() + ", " + enemy.getY());
            System.out.println("Initial enemy health: " + enemy.getHealth());

            java.lang.reflect.Method checkBulletCollisions = GamePanel.class.getDeclaredMethod("checkBulletCollisions", Bullet.class);
            checkBulletCollisions.setAccessible(true);

            // Hit enemy with debug output
            for (int i = 0; i < 3; i++) {
                Bullet bullet = new Bullet(TEST_X, TEST_Y, 0);
                System.out.println("Bullet position: " + bullet.getX() + ", " + bullet.getY());
                System.out.println("Before hit " + (i+1) + " - Enemy health: " + enemy.getHealth());

                boolean hit = (boolean) checkBulletCollisions.invoke(gamePanel, bullet);
                System.out.println("Hit registered: " + hit);

                if (enemies.size() > 0) {
                    System.out.println("After hit " + (i+1) + " - Enemy health: " + enemy.getHealth());
                } else {
                    System.out.println("Enemy destroyed");
                }
                System.out.println("Current score: " + scoreField.get(gamePanel));
            }

            // Final verification
            int score = (int) scoreField.get(gamePanel);
            assertEquals(2, score, "Second tier enemy should be worth 2 points");
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test cumulative scoring")
    void testCumulativeScoring() {
        try {
            // Get all required fields
            java.lang.reflect.Field scoreField = GamePanel.class.getDeclaredField("score");
            java.lang.reflect.Field asteroidsField = GamePanel.class.getDeclaredField("asteroids");
            java.lang.reflect.Field regularEnemiesField = GamePanel.class.getDeclaredField("regularEnemies");
            java.lang.reflect.Field secondTierField = GamePanel.class.getDeclaredField("secondTierEnemies");
            scoreField.setAccessible(true);
            asteroidsField.setAccessible(true);
            regularEnemiesField.setAccessible(true);
            secondTierField.setAccessible(true);

            // Reset score and clear all collections
            scoreField.set(gamePanel, 0);
            List<Asteroid> asteroids = (List<Asteroid>) asteroidsField.get(gamePanel);
            List<RegularEnemy> regularEnemies = (List<RegularEnemy>) regularEnemiesField.get(gamePanel);
            List<SecondTier> secondTierEnemies = (List<SecondTier>) secondTierField.get(gamePanel);
            asteroids.clear();
            regularEnemies.clear();
            secondTierEnemies.clear();

            java.lang.reflect.Method checkBulletCollisions = GamePanel.class.getDeclaredMethod("checkBulletCollisions", Bullet.class);
            checkBulletCollisions.setAccessible(true);

            // 1. First destroy large asteroid (2 points)
            System.out.println("Testing large asteroid - Initial score: " + scoreField.get(gamePanel));
            Asteroid largeAsteroid = new Asteroid(TEST_X, TEST_Y, true);
            asteroids.add(largeAsteroid);
            int largeAsteroidHits = 0;
            while (!largeAsteroid.isDestroyed() && largeAsteroidHits < 10) {
                checkBulletCollisions.invoke(gamePanel, new Bullet(TEST_X, TEST_Y, 0));
                largeAsteroidHits++;
                System.out.println("Large asteroid hit " + largeAsteroidHits + ", Score: " + scoreField.get(gamePanel));
            }
            asteroids.clear();
            int scoreAfterLarge = (int)scoreField.get(gamePanel);
            assertEquals(2, scoreAfterLarge, "Score should be 2 after destroying large asteroid");

            // 2. Then destroy regular enemy (1 point)
            System.out.println("\nTesting regular enemy - Score before: " + scoreField.get(gamePanel));
            RegularEnemy regularEnemy = new RegularEnemy(TEST_X, TEST_Y, 0, 0, 0, 50);
            regularEnemies.add(regularEnemy);
            int regularEnemyHits = 0;
            while (!regularEnemy.isDestroyed() && regularEnemyHits < 2) {
                checkBulletCollisions.invoke(gamePanel, new Bullet(TEST_X, TEST_Y, 0));
                regularEnemyHits++;
                System.out.println("Regular enemy hit " + regularEnemyHits + ", Score: " + scoreField.get(gamePanel));
            }
            regularEnemies.clear();
            int scoreAfterRegular = (int)scoreField.get(gamePanel);
            assertEquals(3, scoreAfterRegular, "Score should be 3 after destroying regular enemy");

            // 3. Finally destroy second tier enemy (2 points)
            System.out.println("\nTesting second tier enemy - Score before: " + scoreField.get(gamePanel));
            SecondTier secondTier = new SecondTier(TEST_X, TEST_Y, 0, 0, 0, 75);
            secondTierEnemies.add(secondTier);
            int secondTierHits = 0;
            while (!secondTier.isDestroyed() && secondTierHits < 3) {
                checkBulletCollisions.invoke(gamePanel, new Bullet(TEST_X, TEST_Y, 0));
                secondTierHits++;
                System.out.println("Second tier hit " + secondTierHits + ", Score: " + scoreField.get(gamePanel));
            }

            // Final score verification
            int finalScore = (int)scoreField.get(gamePanel);
            System.out.println("\nFinal score: " + finalScore);
            assertEquals(5, finalScore, "Final score should be 5 (2 + 1 + 2)");

            // Additional verification steps
            assertTrue(largeAsteroidHits <= 10, "Large asteroid should be destroyed within 10 hits");
            assertTrue(regularEnemyHits <= 2, "Regular enemy should be destroyed within 2 hits");
            assertTrue(secondTierHits <= 3, "Second tier enemy should be destroyed within 3 hits");

        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }
}