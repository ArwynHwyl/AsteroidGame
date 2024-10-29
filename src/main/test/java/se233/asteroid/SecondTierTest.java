package com.se233.asteroid.test;
import com.se233.asteroid.model.Bullet;
import com.se233.asteroid.model.PlayerShip;
import com.se233.asteroid.model.SecondTier;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class SecondTierTest {

    private SecondTier enemy;
    private PlayerShip target;
    private static final double INITIAL_X = 400;
    private static final double INITIAL_Y = 300;

    @BeforeEach
    void setUp() {
        // Create new enemy and target before each test
        enemy = new SecondTier(INITIAL_X, INITIAL_Y, 0, 0, 0, 75);
        target = new PlayerShip(500, 300);
        enemy.setTarget(target);
    }

    @Test
    @DisplayName("Test initial enemy state")
    void testInitialState() {
        // Check if enemy is created with correct initial values
        assertEquals(INITIAL_X, enemy.getX(), "Initial X position should match");
        assertEquals(INITIAL_Y, enemy.getY(), "Initial Y position should match");
        assertEquals(75, enemy.getHealth(), "Initial health should be 75");
        assertFalse(enemy.isDestroyed(), "Enemy should not be destroyed initially");
    }

    @Test
    @DisplayName("Test enemy damage and destruction")
    void testDamageAndDestruction() {
        // Initial health check
        assertEquals(75, enemy.getHealth(), "Should start with 75 health");

        // Hit enemy twice
        enemy.hit();
        assertEquals(50, enemy.getHealth(), "Health should be 50 after one hit");

        enemy.hit();
        assertEquals(25, enemy.getHealth(), "Health should be 25 after two hits");

        // Final hit to destroy
        enemy.hit();
        assertEquals(0, enemy.getHealth(), "Health should be 0 after three hits");
        assertTrue(enemy.isDestroyed(), "Enemy should be destroyed after three hits");
    }

    @Test
    @DisplayName("Test enemy movement boundaries")
    void testMovementBoundaries() {
        // Create enemy near the boundary with velocity towards it
        SecondTier edgeEnemy = new SecondTier(750, 550, 5, 5, 0, 75);

        // Let it move for a while
        for(int i = 0; i < 120; i++) {
            edgeEnemy.update();

            // Check boundaries after initial spawn delay (60 frames)
            if (i > 60) {
                // The actual boundary values from SecondTier class
                assertTrue(edgeEnemy.getX() >= 50 && edgeEnemy.getX() <= 750,
                        "X position should stay within boundaries: " + edgeEnemy.getX());
                assertTrue(edgeEnemy.getY() >= 50 && edgeEnemy.getY() <= 550,
                        "Y position should stay within boundaries: " + edgeEnemy.getY());
            }
        }
    }

    @Test
    @DisplayName("Test enemy shooting mechanism")
    void testShooting() {
        // Get initial bullet count
        int initialBullets = enemy.getBullets().size();

        // Force several updates to trigger shooting (after spawn delay)
        for(int i = 0; i < 180; i++) {
            enemy.update();
        }

        // Check if bullets were created (enemy shoots 2 bullets at a time)
        assertTrue(enemy.getBullets().size() > initialBullets,
                "Enemy should have created bullets after updates");
    }

    @Test
    @DisplayName("Test enemy rotation towards target")
    void testRotationTowardsTarget() {
        // Store initial angle
        double initialAngle = enemy.getAngle();

        // Move target to new position
        target = new PlayerShip(600, 400);
        enemy.setTarget(target);

        // Update several times to allow rotation (after spawn delay)
        for(int i = 0; i < 120; i++) {
            enemy.update();
        }

        // Check if angle changed
        assertNotEquals(initialAngle, enemy.getAngle(),
                "Enemy should rotate to face target");
    }

    @Test
    @DisplayName("Test enemy bullet cleaning")
    void testBulletCleaning() {
        // Create enemy and set target to make it shoot
        SecondTier shootingEnemy = new SecondTier(400, 300, 0, 0, 0, 75);
        shootingEnemy.setTarget(new PlayerShip(500, 300));

        // Wait for spawn delay and force enemy to shoot
        for(int i = 0; i < 180; i++) {
            shootingEnemy.update();
        }

        // Store initial bullet count
        int initialBullets = shootingEnemy.getBullets().size();
        assertTrue(initialBullets > 0, "Enemy should have created bullets");

        // Move all bullets off screen by updating many times
        for(Bullet bullet : shootingEnemy.getBullets()) {
            // Force bullets off screen
            bullet.setVelocity(100, 100); // High velocity to ensure going off screen
        }

        // Update to clean bullets
        for(int i = 0; i < 10; i++) {
            shootingEnemy.update();
        }

        // Verify bullets were cleaned
        assertTrue(shootingEnemy.getBullets().size() < initialBullets,
                "Off-screen bullets should be removed");
    }
}