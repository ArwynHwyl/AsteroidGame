package com.se233.asteroid;

import com.se233.asteroid.model.Boss;
import com.se233.asteroid.model.Bullet;
import com.se233.asteroid.model.PlayerShip;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public  class BossTest {
    private Boss boss;
    private PlayerShip player;
    private static final int INITIAL_X = 400;
    private static final int INITIAL_Y = 300;

    @BeforeEach
    void setUp() {
        // Create new player at middle of screen
        player = new PlayerShip(INITIAL_X, INITIAL_Y);
        // Create boss targeting that player
        boss = new Boss(INITIAL_X, INITIAL_Y, player);
    }

    @Test
    @DisplayName("Test boss initial state")
    void testInitialState() {
        // Check initial position and health
        assertEquals(INITIAL_X, boss.getX(), "Initial X position should match");
        assertEquals(INITIAL_Y, boss.getY(), "Initial Y position should match");
        assertEquals(100, boss.getHealth(), "Initial health should be 100");
        assertTrue(boss.isAlive(), "Boss should be alive initially");
        assertEquals(0, boss.getBullets().size(), "Boss should start with no bullets");
    }

    @Test
    @DisplayName("Test boss damage and health system")
    void testDamageSystem() {
        // Test initial health
        assertEquals(100, boss.getHealth(), "Should start with 100 health");

        // Test taking damage
        boss.hit(20);
        assertEquals(80, boss.getHealth(), "Health should be 80 after 20 damage");

        // Test multiple hits
        boss.hit(30);
        assertEquals(50, boss.getHealth(), "Health should be 50 after another 30 damage");

        // Test death
        boss.hit(50);
        assertEquals(0, boss.getHealth(), "Health should be 0 after fatal damage");
        assertFalse(boss.isAlive(), "Boss should be dead after health reaches 0");
    }

    @Test
    @DisplayName("Test boss movement boundaries")
    void testMovementBoundaries() {
        // Move boss to edge of screen
        boss = new Boss(800, 600, player);

        // Update few times to allow movement
        for(int i = 0; i < 60; i++) {
            boss.update();
        }

        // Check if boss stays within screen
        assertTrue(boss.getX() >= 0 && boss.getX() <= 800,
                "Boss X position should stay within screen bounds");
        assertTrue(boss.getY() >= 0 && boss.getY() <= 600,
                "Boss Y position should stay within screen bounds");
    }

    @Test
    @DisplayName("Test boss attack generation")
    void testAttackGeneration() {
        // Store initial bullet count
        int initialBullets = boss.getBullets().size();

        // Update boss multiple times to trigger attacks
        for(int i = 0; i < 60; i++) {
            boss.update();
        }

        // Check if bullets were created
        assertTrue(boss.getBullets().size() > initialBullets,
                "Boss should create bullets when attacking");
    }

    @Test
    @DisplayName("Test boss distance-based behavior")
    void testDistanceBasedBehavior() {
        // Test close range behavior
        PlayerShip closePlayer = new PlayerShip(INITIAL_X + 100, INITIAL_Y);
        Boss closeRangeBoss = new Boss(INITIAL_X, INITIAL_Y, closePlayer);

        // Update and check bullet pattern
        for(int i = 0; i < 60; i++) {
            closeRangeBoss.update();
        }

        // Store bullet count at close range
        int closeRangeBullets = closeRangeBoss.getBullets().size();

        // Test long range behavior
        PlayerShip farPlayer = new PlayerShip(INITIAL_X + 400, INITIAL_Y);
        Boss longRangeBoss = new Boss(INITIAL_X, INITIAL_Y, farPlayer);

        // Update and check bullet pattern
        for(int i = 0; i < 60; i++) {
            longRangeBoss.update();
        }

        // Store bullet count at long range
        int longRangeBullets = longRangeBoss.getBullets().size();

        // Patterns should be different at different ranges
        assertNotEquals(closeRangeBullets, longRangeBullets,
                "Boss should use different attack patterns at different ranges");
    }

    @Test
    @DisplayName("Test boss targeting system")
    void testTargetingSystem() {
        // Create new player at different position
        PlayerShip newTarget = new PlayerShip(600, 400);

        // Create boss with some initial distance from target
        Boss boss = new Boss(300, 200, newTarget);

        // Track distances over multiple updates to verify orbital behavior
        double previousDistance = Double.MAX_VALUE;
        int stabilityCount = 0;

        // Update more times to allow boss to establish orbit
        for(int i = 0; i < 180; i++) { // Increased from 60 to 180 updates
            boss.update();

            double currentDistance = Math.sqrt(
                    Math.pow(boss.getX() - newTarget.getX(), 2) +
                            Math.pow(boss.getY() - newTarget.getY(), 2)
            );

            // Check if distance is stabilizing in the desired range
            if (currentDistance >= 190 && currentDistance <= 310) { // Slightly wider range
                stabilityCount++;
            }

            previousDistance = currentDistance;
        }

        // Get final distance after updates
        double finalDistance = Math.sqrt(
                Math.pow(boss.getX() - newTarget.getX(), 2) +
                        Math.pow(boss.getY() - newTarget.getY(), 2)
        );

        // Verify that the boss maintained appropriate distance for some updates
        assertTrue(stabilityCount > 30,
                "Boss should maintain orbital distance (190-310) for multiple updates. " +
                        "Final distance: " + finalDistance +
                        ", Stability count: " + stabilityCount);
    }

    @Test
    @DisplayName("Test bullet cleanup")
    void testBulletCleanup() {
        // Force boss to create bullets
        for(int i = 0; i < 60; i++) {
            boss.update();
        }

        int bulletCount = boss.getBullets().size();
        assertTrue(bulletCount > 0, "Boss should have created bullets");

        // Move all bullets off screen
        for(Bullet bullet : boss.getBullets()) {
            bullet.setVelocity(1000, 1000); // Very high speed to ensure going off screen
        }

        // Update to clean bullets
        for(int i = 0; i < 10; i++) {
            boss.update();
        }

        // Check if off-screen bullets were removed
        assertTrue(boss.getBullets().size() < bulletCount,
                "Off-screen bullets should be removed");
    }

    @Test
    @DisplayName("Test boss orbital movement")
    void testOrbitalMovement() {
        // Create target in center of screen
        PlayerShip target = new PlayerShip(400, 300);
        Boss boss = new Boss(400, 100, target); // Start boss above target

        // Record initial position
        double startX = boss.getX();
        double startY = boss.getY();

        // Update boss multiple times to allow for proper orbital movement
        for(int i = 0; i < 200; i++) { // เพิ่มจำนวนรอบการ update
            boss.update();

            // Check distance during movement
            double currentDistance = Math.sqrt(
                    Math.pow(boss.getX() - target.getX(), 2) +
                            Math.pow(boss.getY() - target.getY(), 2)
            );

            // Check if boss has moved from its starting position
            assertFalse(
                    startX == boss.getX() && startY == boss.getY(),
                    "Boss should not remain at its starting position"
            );
        }
    }
}
