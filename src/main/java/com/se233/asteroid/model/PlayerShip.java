package com.se233.asteroid.model;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.Level;

public class PlayerShip extends Character {
    private static final double DECELERATION = 0.98;
    private static final double ACCELERATION = 0.5;
    private boolean shooting;
    private List<Bullet> bullets;
    private static final double MAX_VELOCITY = 5.0;
    private static final Logger logger = Logger.getLogger(PlayerShip.class.getName());
    private boolean isMoving = false;
    // Sprite Ship fields
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
    // Screen dimensions
    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;
    // Sprite fields gunflash
    private static BufferedImage[] gunflashSprites;
    private static final int GUNFLASH_FRAMES = 4;
    private int currentGunflashFrame = -1; // -1 means no gunflash
    private static final int GUNFLASH_ANIMATION_DELAY = 4;
    private int gunflashTick = 0;
    private static final int GUNFLASH_WIDTH = 64;
    private static final int GUNFLASH_HEIGHT = 64;
    //Invincible bf spawn
    private boolean isInvincible = false;
    private int invincibleTicks = 0;
    private static final int INVINCIBLE_DURATION = 120;
    private static final float BLINK_RATE = 0.125f;
    private static final float SHIELD_ROTATION = 0.05f;
    private float shieldAngle = 0;
    private MovementDirection currentDirection = MovementDirection.NONE;
    // Beam laser fields
    private boolean firingBeam = false;
    private static final int BEAM_MAX_LENGTH = 72;
    private int beamCharge = 100;
    private static final int BEAM_RECHARGE_RATE = 1;
    private static final int BEAM_CONSUMPTION_RATE = 2;
    private boolean canFireBeam = true;
    private static final int BEAM_SPAWN_DISTANCE = SPRITE_HEIGHT/2 + 46;
    private BufferedImage[][] beamSprites;
    private static final int BEAM_ANIMATION_DELAY = 4; // Added animation delay
    private int beamAnimationTick = 0; // Added animation tick
    private static final int BEAM_WIDTH = 40;
    private int currentBeamRow = 0;
    private int currentBeamCol = 0;
    private static final int BEAM_SPRITE_ROWS = 4;
    private static final int BEAM_SPRITE_COLS = 2;
    private static final int SINGLE_SPRITE_WIDTH = 160;
    private static final int SINGLE_SPRITE_HEIGHT = 72;

    private enum MovementDirection {
        HORIZONTAL,
        VERTICAL,
        NONE
    }

    static {
        loadGunflashSprites();
    }

    private static void loadGunflashSprites() {
        try {
            // เปลี่ยนชื่อไฟล์ให้ตรงกับที่มีในโปรเจค
            BufferedImage spriteSheet = ImageIO.read(PlayerShip.class.getResource("/assets/gunflash.png"));
            gunflashSprites = new BufferedImage[GUNFLASH_FRAMES];

            // ตรวจสอบว่าโหลด sprite สำเร็จ
            if (spriteSheet == null) {
                logger.log(Level.SEVERE, "Failed to load gunflash sprite sheet - file not found");
                return;
            }

            int spriteWidth = spriteSheet.getWidth() / GUNFLASH_FRAMES;
            int spriteHeight = spriteSheet.getHeight();

            for (int i = 0; i < GUNFLASH_FRAMES; i++) {
                gunflashSprites[i] = spriteSheet.getSubimage(
                        i * spriteWidth,
                        0,
                        spriteWidth,
                        spriteHeight
                );
            }
            logger.log(Level.INFO, "Successfully loaded gunflash sprites");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load gunflash sprites", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error loading gunflash sprites", e);
        }
    }

    public PlayerShip(double x, double y) {
        super(x, y, 0, 0, 0, 100);
        bullets = new ArrayList<>();
        loadSpriteSheet();
        loadBeamSprites();
    }

    private void loadSpriteSheet() {
        try {
            spriteSheet = ImageIO.read(getClass().getResource("/assets/ship.png"));
            sprites = new BufferedImage[SPRITE_ROWS][SPRITE_COLS];

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
        // อัพเดท animation ของตัวยาน
        if (isMoving) {
            animationTick++;
            if (animationTick >= animationDelay) {
                animationTick = 0;
                currentFrame--;
                if (currentFrame < 0) {
                    currentFrame = SPRITE_COLS - 1;
                }
            }
        } else {
            currentFrame = SPRITE_COLS - 1;
            animationTick = 0;
            currentDirection = MovementDirection.NONE;
        }

        // อัพเดท gunflash animation
        if (currentGunflashFrame >= 0) {
            gunflashTick++;
            if (gunflashTick >= GUNFLASH_ANIMATION_DELAY) {
                gunflashTick = 0;
                currentGunflashFrame++;
                if (currentGunflashFrame >= GUNFLASH_FRAMES) {
                    currentGunflashFrame = -1; // End gunflash animation
                }
            }
        }
    }

    @Override
    public void update() {
        // อัปเดต invincibility
        if (isInvincible) {
            invincibleTicks--;
            if (invincibleTicks <= 0) {
                isInvincible = false;
            }
            shieldAngle += SHIELD_ROTATION;
        }

        // อัปเดตการเคลื่อนที่ของยานและ screen wrapping
        move(); // This calls the parent's move() which includes screenWrap()

        velocityX *= DECELERATION;
        velocityY *= DECELERATION;
        isMoving = Math.abs(velocityX) > 0.01 || Math.abs(velocityY) > 0.01;

        // อัปเดต animation
        updateAnimation();

        // อัปเดตกระสุน
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.update();
            if (bullet.isOffScreen(SCREEN_WIDTH, SCREEN_HEIGHT)) {
                bullets.remove(i);
            }
        }

        if (!firingBeam && beamCharge < 100) {
            beamCharge += BEAM_RECHARGE_RATE;
            if (beamCharge >= 100) {
                canFireBeam = true;
            }
        }

        if (firingBeam) {
            beamCharge -= BEAM_CONSUMPTION_RATE;
            if (beamCharge <= 0) {
                firingBeam = false;
                canFireBeam = false;
            }
            // Update beam animation
            beamAnimationTick++;
            if (beamAnimationTick >= BEAM_ANIMATION_DELAY) {
                beamAnimationTick = 0;
                currentBeamCol = (currentBeamCol + 1) % BEAM_SPRITE_COLS;
                if (currentBeamCol == 0) {
                    currentBeamRow = (currentBeamRow + 1) % BEAM_SPRITE_ROWS;
                }
            }
        }
    }
    @Override
    public void draw(Graphics2D g) {
        // Draw shield effect when invincible
        if (isInvincible) {
            drawShieldEffect(g);
        }
        if (firingBeam) {
            drawBeam(g);
        }

        // Draw ship with blinking effect when invincible
        if (!isInvincible || (invincibleTicks * BLINK_RATE) % 1 > 0.5) {
            if (sprites != null && sprites[currentRow][currentFrame] != null) {
                AffineTransform old = g.getTransform();

                // เคลื่อนไปที่ตำแหน่งยาน
                g.translate(x, y);

                // หมุนตัวยาน
                g.rotate(Math.toRadians(angle));

                // วาดตัวยาน
                g.drawImage(sprites[currentRow][currentFrame],
                        -SPRITE_WIDTH/2,
                        -SPRITE_HEIGHT/2,
                        SPRITE_WIDTH,
                        SPRITE_HEIGHT,
                        null);

                // วาด gunflash ถ้ากำลังเล่น animation
                if (currentGunflashFrame >= 0 && gunflashSprites != null) {
                    // เก็บ transform ของตัวยานไว้
                    AffineTransform shipTransform = g.getTransform();

                    // หมุน gunflash 90 องศา
                    g.rotate(Math.toRadians(90));

                    // ย้าย gunflash ไปที่ส่วนบนของยาน
                    g.drawImage(gunflashSprites[currentGunflashFrame],
                            SPRITE_HEIGHT/2 - 115,  // ย้ายไปด้านบนของยาน
                            -GUNFLASH_WIDTH/2,  // กึ่งกลางตามแนวแกน x
                            GUNFLASH_WIDTH,
                            GUNFLASH_HEIGHT,
                            null
                    );

                    // กลับไปที่ transform ของตัวยาน
                    g.setTransform(shipTransform);
                }

                g.setTransform(old);
            }
        }

        // Draw all bullets
        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }drawBeamChargeBar(g);
    }
    private void loadBeamSprites() {
        try {
            BufferedImage beamSheet = ImageIO.read(getClass().getResource("/assets/beam.png"));
            beamSprites = new BufferedImage[BEAM_SPRITE_ROWS][BEAM_SPRITE_COLS];

            // Load sprites in a 4x2 grid
            for (int row = 0; row < BEAM_SPRITE_ROWS; row++) {
                for (int col = 0; col < BEAM_SPRITE_COLS; col++) {
                    beamSprites[row][col] = beamSheet.getSubimage(
                            col * SINGLE_SPRITE_WIDTH,   // x coordinate
                            row * SINGLE_SPRITE_HEIGHT,  // y coordinate
                            SINGLE_SPRITE_WIDTH,         // width
                            SINGLE_SPRITE_HEIGHT         // height
                    );
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load beam sprites", e);
        }
    }

    private void drawBeam(Graphics2D g) {
        if (!firingBeam) return;

        double radianAngle = Math.toRadians(angle - 90);
        AffineTransform old = g.getTransform();

        int startX = (int)(x + BEAM_SPAWN_DISTANCE * Math.cos(radianAngle));
        int startY = (int)(y + BEAM_SPAWN_DISTANCE * Math.sin(radianAngle));

        g.translate(startX, startY);
        g.rotate(radianAngle);

        if (beamSprites != null) {
            BufferedImage currentSprite = beamSprites[currentBeamRow][currentBeamCol];
            // วาด sprite ตามขนาดจริง
            g.drawImage(currentSprite,
                    -SINGLE_SPRITE_WIDTH/2,
                    -SINGLE_SPRITE_HEIGHT/2,
                    SINGLE_SPRITE_WIDTH,
                    SINGLE_SPRITE_HEIGHT,
                    null);

            // For debugging: draw hitbox outline
            g.setColor(Color.RED);
            g.drawRect(
                -SINGLE_SPRITE_WIDTH/2,
                -SINGLE_SPRITE_HEIGHT/2,
                SINGLE_SPRITE_WIDTH,
                SINGLE_SPRITE_HEIGHT
            );
        }

        g.setTransform(old);
    }
    private void drawBeamChargeBar(Graphics2D g) {
        int barWidth = 100;
        int barHeight = 10;
        int x = 20;
        int y = 80;

        // Draw background
        g.setColor(new Color(50, 50, 50, 180));
        g.fillRect(x, y, barWidth, barHeight);

        // Draw charge level
        if (beamCharge >= 30) {
            g.setColor(new Color(0, 255, 255, 180));
        } else {
            g.setColor(new Color(255, 0, 0, 180));
        }
        g.fillRect(x, y, (int)((beamCharge/100.0) * barWidth), barHeight);

        // Draw border
        g.setColor(Color.WHITE);
        g.drawRect(x, y, barWidth, barHeight);
    }


    public void setBeamFiring(boolean firing) {
        if (firing && canFireBeam && beamCharge > 0 && !firingBeam) {
            firingBeam = true;
            currentBeamRow = 0;
            currentBeamCol = 0;
            beamAnimationTick = 0;
        } else if (!firing) {
            firingBeam = false;
            currentBeamRow = 0;
            currentBeamCol = 0;
        }
    }

    public boolean isBeamFiring() {
        return firingBeam;
    }
    private void drawShieldEffect(Graphics2D g) {
        int shieldSize = 60;
        int numRings = 3;

        AffineTransform old = g.getTransform();
        g.translate(x, y);
        g.rotate(shieldAngle);

        for (int i = 0; i < numRings; i++) {
            float alpha = (float) (0.15 - (i * 0.04));
            g.setColor(new Color(1f, 1f, 1f, alpha));

            int size = shieldSize + (i * 10);
            g.drawOval(-size/2, -size/2, size, size);

            // วาดเส้นตัดขวาง
            double angle = Math.PI / 3; // 60 degrees
            for (int j = 0; j < 6; j++) {
                int x1 = (int)(Math.cos(angle * j) * (size/2));
                int y1 = (int)(Math.sin(angle * j) * (size/2));
                int x2 = (int)(Math.cos(angle * j + Math.PI) * (size/2));
                int y2 = (int)(Math.sin(angle * j + Math.PI) * (size/2));
                g.drawLine(x1, y1, x2, y2);
            }
        }

        g.setTransform(old);
    }
    private void startGunflashAnimation() {
        currentGunflashFrame = 0;
        gunflashTick = 0;
        logger.log(Level.WARNING, "Starting gunflash animation");
    }

    public void shoot() {
        double radianAngle = Math.toRadians(angle - 90);
        double spawnDistance = SPRITE_HEIGHT / 2;
        double bulletX = x + spawnDistance * Math.cos(radianAngle);
        double bulletY = y + spawnDistance * Math.sin(radianAngle);
        bullets.add(new Bullet(bulletX, bulletY, angle - 90));
        startGunflashAnimation();
        System.out.println("Pew! Pew!");
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
        startGunflashAnimation();
        for (int i = 0; i < numberOfBullets; i++) {
            double currentAngle = startAngle + (i * angleIncrement);
            double radianCurrentAngle = Math.toRadians(currentAngle);
            double bulletSpawnX = x + spawnDistance * Math.cos(radianCurrentAngle);
            double bulletSpawnY = y + spawnDistance * Math.sin(radianCurrentAngle);
            bullets.add(new Bullet(bulletSpawnX, bulletSpawnY, currentAngle));
        }
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
        // กำหนดขนาด hitbox ให้เป็นสี่เหลี่ยมที่เหมาะสมกับรูปยาน
        int hitboxWidth = SPRITE_WIDTH / 4;    // ความกว้างประมาณครึ่งหนึ่งของ sprite
        int hitboxHeight = SPRITE_HEIGHT * 1/3; // ความสูงประมาณ 2/3 ของ sprite เพื่อให้ครอบคลุมส่วนตัวยาน

        return new Rectangle(
                (int)x - hitboxWidth/2,   // จุดเริ่มต้น x (กึ่งกลาง)
                (int)y - hitboxHeight/2,  // จุดเริ่มต้น y (กึ่งกลาง)
                hitboxWidth,              // ความกว้างของ hitbox
                hitboxHeight              // ความสูงของ hitbox
        );
    }
    public void setInvincible(boolean invincible) {
        this.isInvincible = invincible;
        this.invincibleTicks = invincible ? INVINCIBLE_DURATION : 0;
    }

    public boolean isInvincible() {
        return isInvincible;
    }
    @Override
    protected void screenWrap() {
        // Screen wrapping logic with smooth transition
        if (x < -SPRITE_WIDTH/2) {
            x = SCREEN_WIDTH + SPRITE_WIDTH/2;
        } else if (x > SCREEN_WIDTH + SPRITE_WIDTH/2) {
            x = -SPRITE_WIDTH/2;
        }

        if (y < -SPRITE_HEIGHT/2) {
            y = SCREEN_HEIGHT + SPRITE_HEIGHT/2;
        } else if (y > SCREEN_HEIGHT + SPRITE_HEIGHT/2) {
            y = -SPRITE_HEIGHT/2;
        }
    }



    public class BeamHitbox {
        private double startX, startY;
        private double endX, endY;
        private double width;

        public BeamHitbox(double startX, double startY, double endX, double endY, double width) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.width = width;
        }

        public boolean intersects(Rectangle target) {
            // Calculate beam direction vector
            double dx = endX - startX;
            double dy = endY - startY;
            double length = Math.sqrt(dx * dx + dy * dy);

            if (length == 0) return false;

            // Normalize direction vector
            dx /= length;
            dy /= length;

            // Calculate vector to target center
            double targetCenterX = target.getCenterX();
            double targetCenterY = target.getCenterY();

            // Vector from beam start to target center
            double vx = targetCenterX - startX;
            double vy = targetCenterY - startY;

            // Project target center onto beam line
            double projection = vx * dx + vy * dy;

            // Find closest point on beam line to target center
            double closestX = startX + Math.max(0, Math.min(length, projection)) * dx;
            double closestY = startY + Math.max(0, Math.min(length, projection)) * dy;

            // Use beam sprite width for collision detection
            return Math.sqrt(
                    Math.pow(closestX - targetCenterX, 2) +
                            Math.pow(closestY - targetCenterY, 2)
            ) < (width/2 + Math.max(target.width, target.height)/2);
        }
    }

    public BeamHitbox getBeamHitbox() {
        if (!firingBeam) return null;

        double radianAngle = Math.toRadians(angle - 90);

        // ใช้ความกว้างของ sprite beam จริงๆในการคำนวณ hitbox
        double beamHitboxWidth = SINGLE_SPRITE_WIDTH/2; // ปรับขนาด hitbox ให้เท่ากับความกว้างของ sprite

        // ระยะห่างจากตัวยานถึงจุดเริ่มต้นของ beam
        double startX = x + BEAM_SPAWN_DISTANCE * Math.cos(radianAngle);
        double startY = y + BEAM_SPAWN_DISTANCE * Math.sin(radianAngle);

        // ความยาวของ beam ให้เท่ากับความสูงของ sprite
        double beamLength = SINGLE_SPRITE_HEIGHT;

        return new BeamHitbox(
                startX,
                startY,
                startX + Math.cos(radianAngle) * beamLength,
                startY + Math.sin(radianAngle) * beamLength,
                beamHitboxWidth
        );
    }
}