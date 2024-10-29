package com.se233.asteroid.test;
import com.se233.asteroid.model.PlayerShip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerShipTest {
    private PlayerShip ship;
    private static final double DELTA = 0.01; // Delta for floating point comparisons
    private static final double MAX_VELOCITY = 5.0; // Maximum velocity constant from PlayerShip
    private static final double ACCELERATION = 0.5; // Acceleration constant from PlayerShip
    private static final double DECELERATION = 0.98; // Deceleration constant from PlayerShip

    @BeforeEach
    void setUp() {
        ship = new PlayerShip(400, 300); // Initialize ship at center of screen
    }

    @Test
    void testInitialPosition() {
        assertEquals(400, ship.getX(), DELTA);
        assertEquals(300, ship.getY(), DELTA);
    }

    @Test
    void testBasicMovement() {
        // Test moving right
        ship = new PlayerShip(400, 300);
        ship.moveRight();
        ship.update();
        assertTrue(ship.getX() > 400);

        // Test moving left
        ship = new PlayerShip(400, 300);
        ship.moveLeft();
        ship.update();
        assertTrue(ship.getX() < 400);

        // Test moving up
        ship = new PlayerShip(400, 300);
        ship.moveUp();
        ship.update();
        assertTrue(ship.getY() < 300);

        // Test moving down
        ship = new PlayerShip(400, 300);
        ship.moveDown();
        ship.update();
        assertTrue(ship.getY() > 300);
    }

    @Test
    void testVelocityLimit() {
        // Test horizontal velocity limit
        for (int i = 0; i < 20; i++) {
            ship.moveRight(); // Apply movement multiple times
            ship.update();
        }
        double totalVelocity = Math.sqrt(Math.pow(ship.getVelocityX(), 2) + Math.pow(ship.getVelocityY(), 2));
        assertTrue(totalVelocity <= MAX_VELOCITY);

        // Test vertical velocity limit
        ship = new PlayerShip(400, 300);
        for (int i = 0; i < 20; i++) {
            ship.moveDown();
            ship.update();
        }
        totalVelocity = Math.sqrt(Math.pow(ship.getVelocityX(), 2) + Math.pow(ship.getVelocityY(), 2));
        assertTrue(totalVelocity <= MAX_VELOCITY);
    }

    @Test
    void testDeceleration() {
        // Test horizontal deceleration
        ship.moveRight();
        ship.update();
        double initialVelocityX = ship.getVelocityX();
        ship.update(); // Update without movement should apply deceleration
        assertTrue(Math.abs(ship.getVelocityX()) < Math.abs(initialVelocityX));

        // Test vertical deceleration
        ship = new PlayerShip(400, 300);
        ship.moveDown();
        ship.update();
        double initialVelocityY = ship.getVelocityY();
        ship.update();
        assertTrue(Math.abs(ship.getVelocityY()) < Math.abs(initialVelocityY));
    }

    @Test
    void testScreenWrap() {
        // Test wrapping at right edge
        ship = new PlayerShip(800, 300);
        for (int i = 0; i < 10; i++) {
            ship.moveRight();
            ship.update();
        }
        assertTrue(ship.getX() < 100); // Should wrap to left side

        // Test wrapping at left edge
        ship = new PlayerShip(10, 300);
        for (int i = 0; i < 10; i++) {
            ship.moveLeft();
            ship.update();
        }
        assertTrue(ship.getX() > 700); // Should wrap to right side

        // Test wrapping at bottom edge
        ship = new PlayerShip(400, 590);
        for (int i = 0; i < 10; i++) {
            ship.moveDown();
            ship.update();
        }
        assertTrue(ship.getY() < 100); // Should wrap to top side

        // Test wrapping at top edge
        ship = new PlayerShip(400, 10);
        for (int i = 0; i < 10; i++) {
            ship.moveUp();
            ship.update();
        }
        assertTrue(ship.getY() > 500); // Should wrap to bottom side
    }

    @Test
    void testDiagonalMovement() {
        ship = new PlayerShip(400, 300);
        // Move diagonally (up-right)
        ship.moveUp();
        ship.moveRight();
        ship.update();

        assertTrue(ship.getX() > 400); // Should move right
        assertTrue(ship.getY() < 300); // Should move up

        // Check that diagonal speed is still limited
        double totalVelocity = Math.sqrt(Math.pow(ship.getVelocityX(), 2) + Math.pow(ship.getVelocityY(), 2));
        assertTrue(totalVelocity <= MAX_VELOCITY);
    }

    @Test
    void testRotation() {
        double initialAngle = ship.getAngle();

        // Test rotate right
        ship.rotateRight();
        assertTrue(ship.getAngle() > initialAngle);

        // Test rotate left
        double currentAngle = ship.getAngle();
        ship.rotateLeft();
        assertTrue(ship.getAngle() < currentAngle);
    }
}