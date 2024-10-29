package se233.asteroid.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ResourceLoader {
    private static final Logger logger = LogManager.getLogger(ResourceLoader.class);

    public static BufferedImage loadImage(String path, String resourceName) {
        try {
            BufferedImage image = ImageIO.read(ResourceLoader.class.getResource(path));
            if (image == null) {
                throw new GameException(
                        String.format("Failed to load %s: Resource not found at %s", resourceName, path),
                        GameException.ErrorType.RESOURCE_LOADING_ERROR
                );
            }
            return image;
        } catch (IOException e) {
            throw new GameException(
                    String.format("Failed to load %s from %s", resourceName, path),
                    GameException.ErrorType.RESOURCE_LOADING_ERROR,
                    e
            );
        }
    }

    public static BufferedImage[][] loadSpriteGrid(BufferedImage spriteSheet,
                                                   int rows, int cols,
                                                   int spriteWidth, int spriteHeight,
                                                   String spriteName) {
        BufferedImage[][] sprites = new BufferedImage[rows][cols];
        try {
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    sprites[row][col] = spriteSheet.getSubimage(
                            col * spriteWidth,
                            row * spriteHeight,
                            spriteWidth,
                            spriteHeight
                    );
                }
            }
            return sprites;
        } catch (Exception e) {
            throw new GameException(
                    String.format("Error processing %s sprite grid", spriteName),
                    GameException.ErrorType.SPRITE_PROCESSING_ERROR,
                    e
            );
        }
    }

    public static BufferedImage[] loadSpriteStrip(BufferedImage spriteSheet,
                                                  int frames,
                                                  String spriteName) {
        BufferedImage[] sprites = new BufferedImage[frames];
        try {
            int spriteWidth = spriteSheet.getWidth() / frames;
            int spriteHeight = spriteSheet.getHeight();

            for (int i = 0; i < frames; i++) {
                sprites[i] = spriteSheet.getSubimage(
                        i * spriteWidth,
                        0,
                        spriteWidth,
                        spriteHeight
                );
            }
            return sprites;
        } catch (Exception e) {
            throw new GameException(
                    String.format("Error processing %s sprite strip", spriteName),
                    GameException.ErrorType.SPRITE_PROCESSING_ERROR,
                    e
            );
        }
    }
}