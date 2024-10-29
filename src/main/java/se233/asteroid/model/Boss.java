package se233.asteroid.model;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.image.RasterFormatException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Boss extends Character {
    private int attackCooldown;
    private int attackPattern;
    private int patternDuration;
    private List<Bullet> bullets;
    private static final int PATTERN_SWITCH_TIME = 300;
    private static final int INITIAL_HEALTH = 100;
    private static final Logger logger = Logger.getLogger(Boss.class.getName());

    // Sprite-related constants
    private static final int SPRITE_ROWS = 2;
    private static final int SPRITE_COLS = 4;

    // Animation fields
    private BufferedImage spriteSheet;
    private BufferedImage[][] sprites;
    private int currentRow = 0;
    private int currentFrame = 0;
    private int animationDelay = 8;
    private int animationTick = 0;
    private boolean isMoving = true;

    // Constants for bullet patterns
    private static final double SPIRAL_SPEED = 8.0;
    private static final double SPREAD_SPEED = 8.0;
    private static final double WAVE_SPEED = 7.0;
    private static final double CROSS_SPEED = 10.0;
    //orbital move
    private double orbitAngle = 0;                    // มุมปัจจุบันในการวน
    private PlayerShip target;                        // อ้างอิงถึงผู้เล่น
    private double currentDistance = ORBIT_RADIUS;
    private static final double ORBIT_RADIUS = 250.0; // ปรับระยะห่างเริ่มต้นให้อยู่กึ่งกลางระหว่าง MIN และ MAX
    private static final double ORBIT_SPEED = 0.015;  // ลดความเร็วในการวนลงเล็กน้อย
    private static final double MOVEMENT_SPEED = 2; // เพิ่มความเร็วในการเคลื่อนที่
    private static final double MIN_DISTANCE = 200.0;
    private static final double MAX_DISTANCE = 300.0;
    private static final double DISTANCE_ADJUSTMENT_SPEED = 1.5; // ความเร็วในการปรับระยะห่าง
    public Boss(double x, double y,PlayerShip player) {
        super(x, y, 0, 0, 0, INITIAL_HEALTH);
        this.attackCooldown = 0;
        this.attackPattern = 0;
        this.patternDuration = 0;
        this.bullets = new ArrayList<>();
        this.target = player;
        loadSpriteSheet();
    }

    private void loadSpriteSheet() {
        try {
            spriteSheet = ImageIO.read(getClass().getResource("/assets/boss.png"));
            if (spriteSheet == null) {
                logger.log(Level.SEVERE, "Failed to load boss sprite sheet - file not found");
                return;
            }
            // Log the actual dimensions of the loaded sprite sheet
            logger.log(Level.INFO, "Loaded sprite sheet dimensions: " +
                    spriteSheet.getWidth() + "x" + spriteSheet.getHeight());
            // Calculate sprite dimensions based on the actual sprite sheet size
            int actualSpriteWidth = spriteSheet.getWidth() / SPRITE_COLS;
            int actualSpriteHeight = spriteSheet.getHeight() / SPRITE_ROWS;
            logger.log(Level.INFO, "Individual sprite dimensions: " +
                    actualSpriteWidth + "x" + actualSpriteHeight);

            sprites = new BufferedImage[SPRITE_ROWS][SPRITE_COLS];

            // Safely extract subimages
            for (int row = 0; row < SPRITE_ROWS; row++) {
                for (int col = 0; col < SPRITE_COLS; col++) {
                    if ((col * actualSpriteWidth + actualSpriteWidth) <= spriteSheet.getWidth() &&
                            (row * actualSpriteHeight + actualSpriteHeight) <= spriteSheet.getHeight()) {

                        sprites[row][col] = spriteSheet.getSubimage(
                                col * actualSpriteWidth,
                                row * actualSpriteHeight,
                                actualSpriteWidth,
                                actualSpriteHeight
                        );
                    } else {
                        logger.log(Level.WARNING, "Sprite at position [" + row + "][" + col +
                                "] is outside sprite sheet bounds");
                    }
                }
            }
            logger.log(Level.INFO, "Successfully loaded boss sprites");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load boss sprite sheet", e);
        } catch (RasterFormatException e) {
            logger.log(Level.SEVERE, "Error cutting sprite sheet: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error loading sprite sheet: " + e.getMessage(), e);
        }
    }

    private void updateAnimation() {
        if (isMoving) {
            animationTick++;
            if (animationTick >= animationDelay) {
                animationTick = 0;
                currentFrame = (currentFrame + 1) % SPRITE_COLS;

                // Switch rows based on attack pattern
                currentRow = attackPattern % SPRITE_ROWS;
            }
        }
    }

    @Override
    public void update() {
        updateMovement();
        updateAttackPattern();
        updateBullets();
        updateAnimation();
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
        // ปรับรูปแบบการโจมตีตามระยะห่าง
        double distanceToPlayer = getDistanceToPlayer();

        if (distanceToPlayer < MIN_DISTANCE) {
            // ถ้าอยู่ใกล้ ใช้การโจมตีแบบ spread
            spreadAttack();
            attackCooldown = 45;
        } else if (distanceToPlayer > MAX_DISTANCE) {
            // ถ้าอยู่ไกล ใช้การโจมตีแบบ wave
            waveAttack();
            attackCooldown = 15;
        } else {
            // ถ้าอยู่ในระยะกลาง สุ่มใช้การโจมตีทั้งหมด
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

    private void updateMovement() {
        if (target == null || !isAlive()) return;

        // คำนวณระยะห่างปัจจุบันจากผู้เล่น
        double dx = x - target.getX();
        double dy = y - target.getY();
        double currentDistanceToPlayer = Math.sqrt(dx * dx + dy * dy);

        // ปรับระยะห่างแบบค่อยเป็นค่อยไป
        if (currentDistanceToPlayer < MIN_DISTANCE) {
            currentDistance += DISTANCE_ADJUSTMENT_SPEED;
            if (currentDistance > MAX_DISTANCE) {
                currentDistance = MAX_DISTANCE;
            }
        } else if (currentDistanceToPlayer > MAX_DISTANCE) {
            currentDistance -= DISTANCE_ADJUSTMENT_SPEED;
            if (currentDistance < MIN_DISTANCE) {
                currentDistance = MIN_DISTANCE;
            }
        }

        // อัปเดตมุมการวน
        orbitAngle += ORBIT_SPEED;
        if (orbitAngle >= Math.PI * 2) {
            orbitAngle -= Math.PI * 2;
        }

        // คำนวณตำแหน่งเป้าหมาย
        double targetX = target.getX() + Math.cos(orbitAngle) * currentDistance;
        double targetY = target.getY() + Math.sin(orbitAngle) * currentDistance;

        // คำนวณเวกเตอร์การเคลื่อนที่
        double moveAngle = Math.atan2(targetY - y, targetX - x);

        // ปรับความเร็วตามระยะห่างจากตำแหน่งเป้าหมาย
        double distanceToTarget = Math.sqrt(Math.pow(targetX - x, 2) + Math.pow(targetY - y, 2));
        double speedMultiplier = Math.min(distanceToTarget / 50.0, 1.0); // ปรับความเร็วตามระยะห่าง

        // อัปเดตความเร็ว
        velocityX = Math.cos(moveAngle) * MOVEMENT_SPEED * speedMultiplier;
        velocityY = Math.sin(moveAngle) * MOVEMENT_SPEED * speedMultiplier;

        // อัปเดตตำแหน่ง
        x += velocityX;
        y += velocityY;

        // หันหน้าไปทางผู้เล่น
        angle = Math.toDegrees(Math.atan2(target.getY() - y, target.getX() - x));

        // ตรวจสอบขอบจอ
        x = Math.max(50, Math.min(x, 750)); // เพิ่มระยะห่างจากขอบ
        y = Math.max(50, Math.min(y, 550)); // เพิ่มระยะห่างจากขอบ

        patternDuration++;
    }

    @Override
    public void draw(Graphics2D g) {
        if (!isAlive()) return;

        if (sprites != null && sprites[0][currentFrame] != null) {
            AffineTransform old = g.getTransform();

            // Move to boss position
            g.translate(x, y);

            // Rotate boss sprite
            g.rotate(Math.toRadians(angle));

            // Get the current sprite dimensions
            int spriteWidth = sprites[0][currentFrame].getWidth();
            int spriteHeight = sprites[0][currentFrame].getHeight();

            // Draw the current sprite frame
            g.drawImage(sprites[0][currentFrame],
                    -spriteWidth/2,
                    -spriteHeight/2,
                    spriteWidth,
                    spriteHeight,
                    null);

            g.setTransform(old);

            // Draw bullets
            for (Bullet bullet : bullets) {
                bullet.draw(g);
            }

            // Draw health bar
            drawHealthBar(g);
        } else {
            // Fallback drawing if sprites aren't loaded
            g.setColor(Color.RED);
            g.fillOval((int)x - 40, (int)y - 40, 80, 80);
        }
    }

    private void drawHealthBar(Graphics2D g) {
        int barWidth = 100;
        int barHeight = 10;
        int spriteHeight = (sprites != null && sprites[0][0] != null) ?
                sprites[0][0].getHeight() : 80;
        int barX = (int) this.x - barWidth / 2;
        int barY = (int) this.y - spriteHeight/2 - 20;

        // Bar background
        g.setColor(new Color(60, 60, 60, 180));
        g.fillRect(barX - 1, barY - 1, barWidth + 2, barHeight + 2);

        // Health bar
        float healthPercent = (float) health / INITIAL_HEALTH;
        Color healthColor = new Color(
                (int) (255 * (1 - healthPercent)),
                (int) (255 * healthPercent),
                0,
                200
        );
        g.setColor(healthColor);
        g.fillRect(barX, barY, (int)(barWidth * healthPercent), barHeight);
    }



    public Rectangle getBounds() {
        int spriteWidth = (sprites != null && sprites[0][0] != null) ?
                sprites[0][0].getWidth() : 80;
        int spriteHeight = (sprites != null && sprites[0][0] != null) ?
                sprites[0][0].getHeight() : 80;
        return new Rectangle(
                (int)x - spriteWidth/3,
                (int)y - spriteHeight/3,
                spriteWidth*2/3,
                spriteHeight*2/3
        );
    }

    public List<Bullet> getBullets() {
        return bullets;
    }

    public void hit(int damage) {
        health -= damage;
        if (health < 0) health = 0;
    }
    private double getDistanceToPlayer() {
        if (target == null) return 0;
        double dx = x - target.getX();
        double dy = y - target.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    public void setTarget(PlayerShip newTarget) {
        this.target = newTarget;
        logger.log(Level.INFO, "Boss target updated to new player ship");
    }
}