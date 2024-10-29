package se233.asteroid.test;
import se233.asteroid.model.Bullet;
import se233.asteroid.model.PlayerShip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerShipActionTests {
    private PlayerShip ship;
    private static final double DELTA = 0.01; // Delta for floating point comparisons
    private static final int SPRITE_HEIGHT = 80; // From PlayerShip class
    private static final int BEAM_SPAWN_DISTANCE = SPRITE_HEIGHT/2 + 46; // From PlayerShip class

    @BeforeEach
    void setUp() {
        ship = new PlayerShip(400, 300); // Initialize ship at center of screen
    }

    @Test
    void testNormalShoot() {
        // Test single bullet shooting
        ship.shoot();
        assertEquals(1, ship.getBullets().size(), "Should create exactly one bullet");

        // Test bullet initial position using the correct spawn distance (SPRITE_HEIGHT/2)
        Bullet bullet = ship.getBullets().get(0);
        double spawnDistance = SPRITE_HEIGHT / 2;
        double angleInRadians = Math.toRadians(ship.getAngle() - 90);
        double expectedX = ship.getX() + spawnDistance * Math.cos(angleInRadians);
        double expectedY = ship.getY() + spawnDistance * Math.sin(angleInRadians);

        assertEquals(expectedX, bullet.getX(), DELTA, "Bullet X position should match expected spawn point");
        assertEquals(expectedY, bullet.getY(), DELTA, "Bullet Y position should match expected spawn point");
    }

    @Test
    void testBeamHitboxCreation() {
        // Test beam hitbox when not firing
        assertNull(ship.getBeamHitbox(), "Beam hitbox should be null when not firing");

        // Test beam hitbox when firing
        ship.setBeamFiring(true);
        assertNotNull(ship.getBeamHitbox(), "Beam hitbox should exist when firing");

        // Test beam hitbox position using the correct BEAM_SPAWN_DISTANCE
        PlayerShip.BeamHitbox hitbox = ship.getBeamHitbox();
        double angleInRadians = Math.toRadians(ship.getAngle() - 90);
        double expectedStartX = ship.getX() + BEAM_SPAWN_DISTANCE * Math.cos(angleInRadians);
        double expectedStartY = ship.getY() + BEAM_SPAWN_DISTANCE * Math.sin(angleInRadians);

        assertEquals(expectedStartX, hitbox.startX, DELTA, "Beam hitbox start X should match expected position");
        assertEquals(expectedStartY, hitbox.startY, DELTA, "Beam hitbox start Y should match expected position");
    }

    @Test
    void testUltimateShoot() {
        // Test ultimate shot (spreads multiple bullets)
        ship.Ultimateshoot();
        assertEquals(5, ship.getBullets().size(), "Ultimate shot should create 5 bullets");

        // Verify bullets are spread in a cone pattern
        double previousAngle = Double.NEGATIVE_INFINITY;
        for (Bullet bullet : ship.getBullets()) {
            double currentAngle = Math.toDegrees(Math.atan2(bullet.getVelocityY(), bullet.getVelocityX()));
            assertTrue(currentAngle > previousAngle, "Bullets should be spread in increasing angles");
            previousAngle = currentAngle;
        }
    }

    @Test
    void testBeamWeapon() {
        // Test beam activation
        ship.setBeamFiring(true);
        assertTrue(ship.isBeamFiring(), "Beam should be firing");

        // Update multiple times to consume beam charge
        for (int i = 0; i < 25; i++) {
            ship.update();
        }

        // Test beam deactivation when charge depleted
        ship.setBeamFiring(false);
        assertFalse(ship.isBeamFiring(), "Beam should stop firing");
    }

    @Test
    void testInvincibility() {
        // Test initial state
        assertFalse(ship.isInvincible(), "Ship should not be invincible initially");

        // Activate invincibility
        ship.setInvincible(true);
        assertTrue(ship.isInvincible(), "Ship should be invincible after activation");

        // Test invincibility expiration
        for (int i = 0; i < 120; i++) { // INVINCIBLE_DURATION = 120
            ship.update();
        }
        assertFalse(ship.isInvincible(), "Invincibility should expire after duration");
    }

    @Test
    void testShootingWhileMoving() {
        // Move ship diagonally
        ship.moveRight();
        ship.moveUp();
        ship.update();

        // Shoot while moving
        ship.shoot();

        // Verify bullet trajectory matches ship's angle
        Bullet bullet = ship.getBullets().get(0);
        double bulletAngle = Math.toDegrees(Math.atan2(bullet.getVelocityY(), bullet.getVelocityX()));
        assertEquals(ship.getAngle() - 90, bulletAngle, DELTA, "Bullet angle should match ship's angle");
    }

    @Test
    void testBulletCleanup() {
        // Shoot multiple bullets
        for (int i = 0; i < 5; i++) {
            ship.shoot();
            ship.update();
        }

        int initialBulletCount = ship.getBullets().size();

        // Move bullets until they go off screen
        for (int i = 0; i < 100; i++) {
            ship.update();
        }

        assertTrue(ship.getBullets().size() < initialBulletCount,
                "Bullets should be removed when they go off screen");
    }

    @Test
    void testRapidFiring() {
        // Test rapid firing behavior
        for (int i = 0; i < 10; i++) {
            ship.setShooting(true);
            ship.update();
        }

        assertTrue(ship.getBullets().size() >= 10,
                "Should be able to fire multiple bullets in succession");

        // Verify each bullet has unique position
        for (int i = 0; i < ship.getBullets().size() - 1; i++) {
            Bullet bullet1 = ship.getBullets().get(i);
            Bullet bullet2 = ship.getBullets().get(i + 1);
            assertFalse(
                    bullet1.getX() == bullet2.getX() && bullet1.getY() == bullet2.getY(),
                    "Each bullet should have a unique position"
            );
        }
    }
}