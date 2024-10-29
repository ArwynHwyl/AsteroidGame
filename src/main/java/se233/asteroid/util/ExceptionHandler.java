package se233.asteroid.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ExceptionHandler {
    private static final Logger logger = LogManager.getLogger(ExceptionHandler.class);

    public static void handleException(GameException e) {
        switch (e.getErrorType()) {
            case RESOURCE_LOADING_ERROR:
                logger.error("Resource loading error: {}", e.getMessage(), e);
                // Handle resource loading errors (e.g., show error dialog, load fallback resources)
                break;
            case SPRITE_PROCESSING_ERROR:
                logger.error("Sprite processing error: {}", e.getMessage(), e);
                // Handle sprite processing errors
                break;
            case GENERAL_ERROR:
                logger.error("General error: {}", e.getMessage(), e);
                // Handle general errors
                break;
            default:
                logger.error("Unhandled error type: {}", e.getMessage(), e);
                break;
        }
    }

}