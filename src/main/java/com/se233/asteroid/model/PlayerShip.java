package com.se233.asteroid.model;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class PlayerShip extends Character {
    private static final double DECELERATION = 0.98;
    private static final double ACCELERATION = 0.5;
    private boolean shooting;
    private List<Bullet> bullets;
    private static final double MAX_VELOCITY = 3.0;
    private static final Logger logger = Logger.getLogger(PlayerShip.class.getName());
    private boolean isMoving = false;
    // Sprite animation fields
    private BufferedImage spriteSheet;
    private BufferedImage[][] sprites;
    private int currentRow = 3; // Default to horizontal row
    private int currentFrame = 0;
    private int animationDelay = 5;
    private int animationTick = 0;
    private static final int SPRITE_ROWS = 4;
    private static final int SPRITE_COLS = 4;
    private static final int SPRITE_WIDTH = 80;  // 320/4
    private static final int SPRITE_HEIGHT = 80; // 320/4
    private static final int HORIZONTAL_ROW = 3; // Row for A/D movement
    private static final int VERTICAL_ROW = 2;   // Row for W/S movement

    private MovementDirection currentDirection = MovementDirection.NONE;

    private enum MovementDirection {
        HORIZONTAL,
        VERTICAL,
        NONE
    }

    public PlayerShip(double x, double y) {
        super(x, y, 0, 0, 0, 100);
        bullets = new ArrayList<>();
        loadSpriteSheet();
    }

    private void loadSpriteSheet() {
        try {
            spriteSheet = ImageIO.read(getClass().getResource("/assets/dftibl9-443f0373-e395-47cf-9486-2cbd3c914c55.png"));
            sprites = new BufferedImage[SPRITE_ROWS][SPRITE_COLS];

            // Split sprite sheet into individual frames
            for (int row = 0; row < SPRITE_ROWS; row++) {
                for (int col = 0; col < SPRITE_COLS; col++) {
                    sprites[row][col] = spriteSheet.getSubimage(
                            col * SPRITE_WIDTH,
                            row * SPRITE_HEIGHT,
                            SPRITE_WIDTH,
                            SPRITE_HEIGHT
                    );
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load sprite sheet", e);
        }
    }

    private void updateAnimation() {
        // Only animate if the ship is moving
        if (isMoving) {
            animationTick++;
            if (animationTick >= animationDelay) {
                animationTick = 0;
                // Animation goes from right to left (3 to 0)
                currentFrame--;
                if (currentFrame < 0) {
                    currentFrame = SPRITE_COLS - 1;
                }
            }
        } else {
            // Reset to rightmost frame when not moving
            currentFrame = SPRITE_COLS - 1;
            animationTick = 0;
            currentDirection = MovementDirection.NONE;
        }
    }

    @Override
    public void update() {
        x += velocityX;
        y += velocityY;

        velocityX *= DECELERATION;
        velocityY *= DECELERATION;

        // Check if the ship is moving
        isMoving = Math.abs(velocityX) > 0.01 || Math.abs(velocityY) > 0.01;

        // Reset movement if velocity is very low
        if (!isMoving) {
            currentDirection = MovementDirection.NONE;
        }

        if (x < 0) x = 800;
        if (x > 800) x = 0;
        if (y < 0) y = 600;
        if (y > 600) y = 0;

        updateAnimation();

        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.update();
            if (bullet.isOffScreen(800, 600)) {
                bullets.remove(i);
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (sprites != null && sprites[currentRow][currentFrame] != null) {
            AffineTransform old = g.getTransform();

            // Move to ship position
            g.translate(x, y);

            // Rotate around the center
            g.rotate(Math.toRadians(angle), 0, 0);

            // Draw the sprite centered on the position
            g.drawImage(sprites[currentRow][currentFrame],
                    -SPRITE_WIDTH/2,
                    -SPRITE_HEIGHT/2,
                    SPRITE_WIDTH,
                    SPRITE_HEIGHT,
                    null);

            g.setTransform(old);
        }

        // Draw bullets
        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }
    }

    public void moveLeft() {
        velocityX -= ACCELERATION;
        limitVelocity();
        isMoving = true;
        currentDirection = MovementDirection.HORIZONTAL;
        currentRow = HORIZONTAL_ROW;
        logPosition();
    }

    public void moveRight() {
        velocityX += ACCELERATION;
        limitVelocity();
        isMoving = true;
        currentDirection = MovementDirection.HORIZONTAL;
        currentRow = HORIZONTAL_ROW;
        logPosition();
    }

    public void moveUp() {
        velocityY -= ACCELERATION;
        limitVelocity();
        isMoving = true;
        currentDirection = MovementDirection.VERTICAL;
        currentRow = VERTICAL_ROW;
        logPosition();
    }

    public void moveDown() {
        velocityY += ACCELERATION;
        limitVelocity();
        isMoving = true;
        currentDirection = MovementDirection.VERTICAL;
        currentRow = VERTICAL_ROW;
        logPosition();
    }
    public void shoot() {
        double radianAngle = Math.toRadians(angle - 90);
        double spawnDistance = SPRITE_HEIGHT / 2;
        double bulletX = x + spawnDistance * Math.cos(radianAngle);
        double bulletY = y + spawnDistance * Math.sin(radianAngle);
        bullets.add(new Bullet(bulletX, bulletY, angle - 90));
        System.out.println("Pew! Pew!");
    }

    public void setShooting(boolean shooting) {
        this.shooting = shooting;
        if (shooting) {
            shoot();
        }
    }
    public void setUltimateShooting(boolean shooting) {
        this.shooting = shooting;
        if (shooting) {
            Ultimateshoot();
        }
    }
    public void Ultimateshoot() {
        double radianAngle = Math.toRadians(angle - 90);
        double spawnDistance = SPRITE_HEIGHT / 2;
        double bulletX = x + spawnDistance * Math.cos(radianAngle);
        double bulletY = y + spawnDistance * Math.sin(radianAngle);

        int numberOfBullets = 5; // Number of bullets in the cone
        double spreadAngle = 30; // Total spread angle of the cone in degrees
        double startAngle = angle - 90 - (spreadAngle / 2);
        double angleIncrement = spreadAngle / (numberOfBullets - 1);

        for (int i = 0; i < numberOfBullets; i++) {
            double currentAngle = startAngle + (i * angleIncrement);
            double radianCurrentAngle = Math.toRadians(currentAngle);
            double bulletSpawnX = x + spawnDistance * Math.cos(radianCurrentAngle);
            double bulletSpawnY = y + spawnDistance * Math.sin(radianCurrentAngle);
            bullets.add(new Bullet(bulletSpawnX, bulletSpawnY, currentAngle));
        }

        System.out.println("Pew! Pew!");
    }

    private void limitVelocity() {
        double currentSpeed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        if (currentSpeed > MAX_VELOCITY) {
            double scale = MAX_VELOCITY / currentSpeed;
            velocityX *= scale;
            velocityY *= scale;
        }
    }

    public List<Bullet> getBullets() {
        return bullets;
    }

    public void rotateLeft() {
        angle -= 10;
    }

    public void rotateRight() {
        angle += 10;
    }

    private void logPosition() {
        logger.log(Level.INFO, String.format("PlayerShip Velocity (%.2f, %.2f)", velocityX, velocityY));
    }

    public double getX() {
        return x;
    }


    public double getY() {
        return y;
    }

    public Rectangle getBounds() {
        // ใช้ขนาดที่เล็กกว่า SPRITE_WIDTH/HEIGHT เล็กน้อย เพื่อให้การชนสมจริงมากขึ้น
        int hitboxWidth = SPRITE_WIDTH / 3;  // ประมาณ 1/3 ของความกว้างสไปรต์
        int hitboxHeight = SPRITE_HEIGHT / 3; // ประมาณ 1/3 ของความสูงสไปรต์

        return new Rectangle(
                (int)x - hitboxWidth/2,  // จุดเริ่มต้น x
                (int)y - hitboxHeight/2, // จุดเริ่มต้น y
                hitboxWidth,            // ความกว้างของ hitbox
                hitboxHeight           // ความสูงของ hitbox
        );
    }
}