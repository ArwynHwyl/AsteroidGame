package se233.asteroid.model;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Explosion {
    private double x, y;
    private int currentFrame;
    private static final int TOTAL_FRAMES = 8;
    private static final int FRAME_DELAY = 2;
    private int frameTimer;
    private boolean isFinished;
    private static BufferedImage[] explosionFrames;
    private static final int SPRITE_SIZE = 32; // ปรับขนาดตามต้องการ
    private static final Logger logger = Logger.getLogger(Explosion.class.getName());

    static {
        try {
            BufferedImage spriteSheet = ImageIO.read(Explosion.class.getResource("/assets/hit.png"));
            explosionFrames = new BufferedImage[TOTAL_FRAMES];

            for (int i = 0; i < TOTAL_FRAMES; i++) {
                explosionFrames[i] = spriteSheet.getSubimage(i * SPRITE_SIZE, 0, SPRITE_SIZE, SPRITE_SIZE);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load explosion sprite sheet", e);
        }
    }

    public Explosion(double x, double y) {
        this.x = x;
        this.y = y;
        this.currentFrame = 0;
        this.frameTimer = 0;
        this.isFinished = false;
    }

    public void update() {
        if (isFinished) return;

        frameTimer++;
        if (frameTimer >= FRAME_DELAY) {
            frameTimer = 0;
            currentFrame++;
            if (currentFrame >= TOTAL_FRAMES) {
                isFinished = true;
            }
        }
    }

    public void draw(Graphics2D g) {
        if (isFinished || explosionFrames == null) return;

        BufferedImage currentSprite = explosionFrames[currentFrame];
        if (currentSprite != null) {
            g.drawImage(currentSprite,
                    (int)(x - SPRITE_SIZE/2),
                    (int)(y - SPRITE_SIZE/2),
                    SPRITE_SIZE,
                    SPRITE_SIZE,
                    null);
        }
    }

    public boolean isFinished() {
        return isFinished;
    }
}