package se233.asteroid.controller;

import se233.asteroid.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se233.asteroid.util.GameException;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener {
    //Start screen
    private boolean gameStarted = false;
    private boolean showStartMenu = true;
    private Timer timer;
    private PlayerShip player;
    private List<Asteroid> asteroids;
    private List<RegularEnemy> regularEnemies;
    private List<SecondTier> secondTierEnemies;
    private Boss boss;
    private Image backgroundImage;
    private Set<Integer> activeKeys;
    private List<Explosion> explosions;
    // Game state variables
    private int score = 0;
    private int lives = 3;
    private boolean gameOver = false;
    private boolean gameSucceeded = false;
    private String gameOverMessage = "";
    private String gameSucceedMessage = "YOU WIN! Final Score: ";
    private boolean isExploding = false;
    private int explosionTicks = 0;
    private static final int EXPLOSION_DURATION = 60;

    // Boss phase variables
    private boolean bossPhaseStarted = false;
    private boolean bossDefeated = false;
    //On win screen
    private float endingAlpha = 0f;
    private Timer endingTimer;
    // Add new fields to GamePanel class
    private boolean bossSpawning = false;  // Track if we're in spawn countdown
    private int bossSpawnTimer = 180;      // 3 seconds at 60 FPS
    private String bossWarningText = "BOSS INCOMING!";

    private static final Logger logger = LogManager.getLogger(GamePanel.class);

    public GamePanel() {
        this.setPreferredSize(new Dimension(800, 600));
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            handleGlobalException(throwable);
        });

        try {
            setupInitialState();
            timer = new Timer(16, this);
            timer.start();
            // Add input listeners
            addMouseListener(this);
            addMouseMotionListener(this);
            setFocusable(true);
            addKeyListener(this);
        } catch (Exception e) {
            handleGlobalException(e);
        }

        // Add input listeners
        addMouseListener(this);
        addMouseMotionListener(this);
        setFocusable(true);
        addKeyListener(this);


        // Initialize ending timer
        endingTimer = new Timer(16, e -> {
            if (gameSucceeded) {
                endingAlpha = Math.min(1f, endingAlpha + 0.02f);
                repaint();
            }
        });
        endingTimer.start();

        // Mouse controls for rotation
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!gameOver && gameStarted) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        player.rotateLeft();
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        player.rotateRight();
                    }
                }
            }
        });
    }
    private void setupInitialState() {
        // Initialize empty collections
        asteroids = new ArrayList<>();
        regularEnemies = new ArrayList<>();
        secondTierEnemies = new ArrayList<>();
        explosions = new ArrayList<>();
        activeKeys = new HashSet<>();

        // Load background
        backgroundImage = new ImageIcon(getClass().getResource("/assets/bg.gif")).getImage();

        // Set initial game state
        gameStarted = false;
        showStartMenu = true;
        gameOver = false;
        bossPhaseStarted = false;
        bossDefeated = false;
        isExploding = false;
    }
    private void initializeGame() {
        score = 0;
        logger.info("Game started. Score : 0");
        player = new PlayerShip(400, 300);
        player.setInvincible(true);
        asteroids = new ArrayList<>();
        regularEnemies = new ArrayList<>();
        secondTierEnemies = new ArrayList<>();
        explosions = new ArrayList<>();
        boss = null;
        activeKeys = new HashSet<>();
        score = 0;
        lives = 3;
        gameOver = false;
        bossPhaseStarted = false;
        bossDefeated = false;
        isExploding = false;
        gameStarted = true;
        showStartMenu = false;
        // Load background
        backgroundImage = new ImageIcon(getClass().getResource("/assets/bg.gif")).getImage();

        // Spawn initial enemies
        spawnAsteroids();
        spawnRegularEnemies();
        spawnSecondTierEnemies();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (showStartMenu) {
            repaint();
            return;
        }

        if (gameOver || !gameStarted) return;

        if (isExploding) {
            handleExplosion();
            return;
        }

        handlePlayerMovement();
        updateGameObjects();
        checkCollisions();
        checkBossSpawning();

        repaint();
    }

    private void handlePlayerMovement() {
        if (activeKeys.contains(KeyEvent.VK_W)) player.moveUp();
        if (activeKeys.contains(KeyEvent.VK_S)) player.moveDown();
        if (activeKeys.contains(KeyEvent.VK_A)) player.moveLeft();
        if (activeKeys.contains(KeyEvent.VK_D)) player.moveRight();
    }

    private void updateGameObjects() {
        player.update();

        explosions.removeIf(explosion -> {
            explosion.update();
            return explosion.isFinished();
        });
        // Update regular enemies and their bullets
        for (RegularEnemy enemy : regularEnemies) {
            enemy.setTarget(player);
            enemy.update();
            updateEnemyBullets(enemy.getBullets());
        }

        // Update second tier enemies
        for (SecondTier enemy : secondTierEnemies) {
            enemy.setTarget(player);
            enemy.update();
            updateEnemyBullets(enemy.getBullets());
        }

        // Update asteroids
        for (Asteroid asteroid : asteroids) {
            asteroid.update();
        }

        // Update boss if present
        if (boss != null && boss.isAlive()) {
            boss.update();
        }
    }

    private void updateEnemyBullets(List<Bullet> bullets) {
        bullets.removeIf(bullet -> bullet.isOffScreen(getWidth(), getHeight()));
        for (Bullet bullet : bullets) {
            bullet.update();
        }
    }

    private void checkBossSpawning() {
        // Check if it's time to start boss spawn countdown
        if (!bossPhaseStarted && !bossSpawning &&
                asteroids.isEmpty() && regularEnemies.isEmpty() &&
                secondTierEnemies.isEmpty()) {
            bossSpawning = true;
            bossSpawnTimer = 180; // Reset timer
            logger.info("Starting boss spawn countdown");
        }

        // If we're in spawn countdown
        if (bossSpawning) {
            bossSpawnTimer--;
            if (bossSpawnTimer <= 0) {
                startBossPhase();
                bossSpawning = false;
            }
        }
    }

    private void startBossPhase() {
        bossPhaseStarted = true;
        boss = new Boss(400, 100, player); // Spawn boss at top center
        logger.info("Boss phase started");
    }

    private void checkCollisions() {
        if (isExploding) return;

        Rectangle playerBounds = player.getBounds();

        // Check player bullets with enemies and boss
        for (int i = player.getBullets().size() - 1; i >= 0; i--) {
            Bullet bullet = player.getBullets().get(i);
            boolean bulletHit = checkBulletCollisions(bullet);
            if (bulletHit) {
                player.getBullets().remove(i);
            }
        }

        if (!player.isInvincible()) {
            // Check enemy bullets with player
            checkEnemyCollisionsWithPlayer(playerBounds);

            // Check boss bullets with player
            if (boss != null && boss.isAlive()) {
                for (Bullet bullet : boss.getBullets()) {
                    if (bullet.getBounds().intersects(playerBounds)) {
                        startExplosion();
                        return;
                    }
                }
            }
        }if (isExploding) return;

        // Check beam collisions if beam is active
        if (player.isBeamFiring()) {
            PlayerShip.BeamHitbox beamHitbox = player.getBeamHitbox();
            if (beamHitbox != null) {
                // Check beam collision with asteroids
                for (int i = asteroids.size() - 1; i >= 0; i--) {
                    Asteroid asteroid = asteroids.get(i);
                    if (beamHitbox.intersects(asteroid.getBounds())) {
                        explosions.add(new Explosion(asteroid.getX(), asteroid.getY()));
                        asteroid.hit();
                        if (asteroid.isDestroyed()) {
                            // เพิ่มการตรวจสอบว่าเป็น large asteroid ที่ถูกทำลายหรือไม่
                            if (asteroid.wasLargeDestroyed()) {
                                // สร้าง small asteroids 2 อัน
                                asteroids.add(Asteroid.createSmallAsteroid(asteroid.getX(), asteroid.getY()));
                                asteroids.add(Asteroid.createSmallAsteroid(asteroid.getX(), asteroid.getY()));
                            }
                            int points = asteroid.isLarge() ? 2 : 1;
                            score += points;
                            logger.info("Score increased by {} points - Asteroid destroyed by beam. Current score: {}",
                                    points, score);
                            asteroids.remove(i);
                        }
                    }
                }

                // Check beam collision with regular enemies
                for (int i = regularEnemies.size() - 1; i >= 0; i--) {
                    RegularEnemy enemy = regularEnemies.get(i);
                    if (beamHitbox.intersects(enemy.getBounds())) {
                        explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                        enemy.hit();
                        if (enemy.isDestroyed()) {
                            score += 1;
                            logger.info("Score increased by 1 point - Regular enemy destroyed By beam. Current score: {}",
                                    score);
                            regularEnemies.remove(i);
                        }
                    }
                }

                // Check beam collision with second tier enemies
                for (int i = secondTierEnemies.size() - 1; i >= 0; i--) {
                    SecondTier enemy = secondTierEnemies.get(i);
                    if (beamHitbox.intersects(enemy.getBounds())) {
                        explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                        enemy.hit();
                        if (enemy.isDestroyed()) {
                            score += 2;
                            logger.info("Score increased by 2 point - Second-tier enemy destroyed by beam. Current score: {}",
                                    score);
                            secondTierEnemies.remove(i);
                        }
                    }
                }

                // Check beam collision with boss
                if (boss != null && boss.isAlive() && beamHitbox.intersects(boss.getBounds())) {
                    explosions.add(new Explosion(boss.getX(), boss.getY()));
                    boss.hit(5);
                    if (!boss.isAlive()) {
                        score += 50;
                        logger.info("Score increased by 50 points - Boss defeated by beam! Final score: {}",
                                score);
                        bossDefeated = true;
                        gameSucceeded = true;
                        gameOver = true;
                        gameSucceedMessage += score;
                    }
                }
            }
        }
    }

    private boolean checkBulletCollisions(Bullet bullet) {
        // Check asteroid collisions
        for (int j = asteroids.size() - 1; j >= 0; j--) {
            Asteroid asteroid = asteroids.get(j);
            if (bullet.getBounds().intersects(asteroid.getBounds())) {
                explosions.add(new Explosion(bullet.getX(), bullet.getY()));
                asteroid.hit();
                if (asteroid.isDestroyed()) {
                    // เช็คว่าเป็น large asteroid ที่ถูกทำลายหรือไม่
                    if (asteroid.wasLargeDestroyed()) {
                        // สร้าง small asteroids 2 อัน
                        asteroids.add(Asteroid.createSmallAsteroid(asteroid.getX(), asteroid.getY()));
                        asteroids.add(Asteroid.createSmallAsteroid(asteroid.getX(), asteroid.getY()));
                    }
                    int points = asteroid.isLarge() ? 2 : 1;
                    score += points;
                    logger.info("Score increased by {} points - Asteroid destroyed. Current score: {}",
                            points, score);
                    asteroids.remove(j);
                }
                return true;
            }
        }

        // Check regular enemy collisions
        for (int j = regularEnemies.size() - 1; j >= 0; j--) {
            RegularEnemy enemy = regularEnemies.get(j);
            if (bullet.getBounds().intersects(enemy.getBounds())) {
                explosions.add(new Explosion(bullet.getX(), bullet.getY()));
                enemy.hit();
                if (enemy.isDestroyed()) {
                    score += 1;
                    logger.info("Score increased by 1 point - Regular enemy destroyed. Current score: {}",
                            score);
                    regularEnemies.remove(j);
                }
                return true;
            }
        }

        // Check second tier enemy collisions
        for (int j = secondTierEnemies.size() - 1; j >= 0; j--) {
            SecondTier enemy = secondTierEnemies.get(j);
            if (bullet.getBounds().intersects(enemy.getBounds())) {
                explosions.add(new Explosion(bullet.getX(), bullet.getY()));
                enemy.hit();
                if (enemy.isDestroyed()) {
                    score += 2;
                    logger.info("Score increased by 2 points - Second tier enemy destroyed. Current score: {}",
                            score);
                    secondTierEnemies.remove(j);
                }
                return true;
            }
        }

        // Check boss collision
        if (boss != null && boss.isAlive() && bullet.getBounds().intersects(boss.getBounds())) {
            explosions.add(new Explosion(bullet.getX(), bullet.getY()));
            boss.hit(10);
            if (!boss.isAlive()) {
                score += 50;
                logger.info("Score increased by 50 points - Boss defeated! Final score: {}",
                        score);
                bossDefeated = true;
                gameSucceeded = true;
                gameOver=true;
                gameSucceedMessage += score;
            }
            return true;
        }

        return false;
    }

    private void checkEnemyCollisionsWithPlayer(Rectangle playerBounds) {
        // ถ้าผู้เล่นกำลัง invincible ให้ข้ามการเช็ค
        if (player.isInvincible()) return;

        // Check regular enemy bullets
        for (RegularEnemy enemy : regularEnemies) {
            if (checkEnemyBulletsWithPlayer(enemy.getBullets(), playerBounds) ||
                    playerBounds.intersects(enemy.getBounds())) {
                startExplosion();
                return;
            }
        }

        // Check second tier enemy bullets
        for (SecondTier enemy : secondTierEnemies) {
            if (checkEnemyBulletsWithPlayer(enemy.getBullets(), playerBounds) ||
                    playerBounds.intersects(enemy.getBounds())) {
                startExplosion();
                return;
            }
        }

        // Check asteroid collisions
        for (Asteroid asteroid : asteroids) {
            if (playerBounds.intersects(asteroid.getBounds())) {
                startExplosion();
                return;
            }
        }
    }

    private boolean checkEnemyBulletsWithPlayer(List<Bullet> bullets, Rectangle playerBounds) {
        for (Bullet bullet : bullets) {
            if (bullet.getBounds().intersects(playerBounds)) {
                return true;
            }
        }
        return false;
    }

    private void startExplosion() {
        isExploding = true;
        explosionTicks = 0;
    }

    private void handleExplosion() {
        explosionTicks++;
        if (explosionTicks >= EXPLOSION_DURATION) {
            isExploding = false;
            explosionTicks = 0;
            lives--;

            if (lives <= 0) {
                gameOver = true;
                logger.info("Game Over. Final score: {}", score);
                gameOverMessage = "GAME OVER - Final Score: " + score;
            } else {
                logger.info("Player lost a life. Lives remaining: {}", lives);
                player = new PlayerShip(400, 300);
                player.setInvincible(true);
                if (boss != null) {
                    boss.setTarget(player);
                }
            }
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // วาด background
        g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        if (showStartMenu) {
            drawStartMenu(g2d);
            return;
        }

        if (!gameOver && gameStarted) {
            if (!isExploding) {
                player.draw(g2d);
            } else {
                drawExplosion(g2d);
            }

            for (Asteroid asteroid : asteroids) {
                asteroid.draw(g2d);
            }
            for (RegularEnemy enemy : regularEnemies) {
                enemy.draw(g2d);
            }
            for (SecondTier enemy : secondTierEnemies) {
                enemy.draw(g2d);
            }

            if (boss != null && boss.isAlive()) {
                boss.draw(g2d);
            }

            for (Explosion explosion : explosions) {
                explosion.draw(g2d);
            }

            drawHUD(g2d);
        } else if (gameOver) {
            if (bossDefeated) {
                drawGameSucceeded(g2d);
            } else {
                drawGameOver(g2d);
            }
        }
    }
    private void drawStartMenu(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 50));

        String title = "ASTEROID SHOOTER";
        FontMetrics titleMetrics = g2d.getFontMetrics();
        int titleX = (getWidth() - titleMetrics.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, getHeight() / 3);

        g2d.setFont(new Font("Arial", Font.BOLD, 30));
        String startText = "Press 'T' to Start";
        FontMetrics startMetrics = g2d.getFontMetrics();
        int startX = (getWidth() - startMetrics.stringWidth(startText)) / 2;
        g2d.drawString(startText, startX, getHeight() / 2);

        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        String[] controls = {
                "Controls:",
                "WASD - Move",
                "SPACE - Shoot",
                "E - Ultimate Shot",
                "Q - Beam Laser",
                "Mouse - Rotate"
        };

        int yOffset = getHeight() / 2 + 50;
        for (String control : controls) {
            FontMetrics controlMetrics = g2d.getFontMetrics();
            int controlX = (getWidth() - controlMetrics.stringWidth(control)) / 2;
            g2d.drawString(control, controlX, yOffset);
            yOffset += 30;
        }
    }


    private void drawExplosion(Graphics2D g2d) {
        g2d.setColor(Color.ORANGE);
        int size = 40 + (explosionTicks / 2);
        g2d.fillOval((int)player.getX() - size/2, (int)player.getY() - size/2, size, size);
    }

    private void drawHUD(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Score: " + score, 20, 30);
        g2d.drawString("Lives: " + lives, 20, 60);

        // Draw boss warning during spawn countdown
        if (bossSpawning) {
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(bossWarningText);
            int textX = (getWidth() - textWidth) / 2;
            int textY = getHeight() / 2;

            // Make text flash by changing alpha
            float alpha = (float)Math.abs(Math.sin(bossSpawnTimer * 0.05));
            g2d.setColor(new Color(1f, 0f, 0f, alpha));
            g2d.drawString(bossWarningText, textX, textY);
        }

        // Show boss battle text when boss is active
        if (bossPhaseStarted && boss != null && boss.isAlive()) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString("BOSS BATTLE", getWidth()/2 - 60, 30);
        }
    }

    private void drawGameOver(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(gameOverMessage);
        g2d.drawString(gameOverMessage, (getWidth() - textWidth) / 2, getHeight() / 2);
    }


    private void drawGameSucceeded(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(0, 0, 0.5f, 0.8f),
                0, getHeight(), new Color(0, 0, 0.3f, 0.8f)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        Font titleFont = new Font("Arial", Font.BOLD, 50);
        Font scoreFont = new Font("Arial", Font.BOLD, 30);


        String victoryText = "MISSION ACCOMPLISHED";
        String scoreText = "Final Score: " + score;


        drawTextWithShadow(g2d, victoryText, titleFont, 0, getHeight()/3);
        drawTextWithShadow(g2d, scoreText, scoreFont, 0, getHeight()/2);

    }

    private void drawTextWithShadow(Graphics2D g2d, String text, Font font, int offset, int y) {
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;


        g2d.setColor(new Color(0f, 0f, 0f, 0.5f * endingAlpha));
        g2d.drawString(text, x + offset + 2, y + offset + 2);

        g2d.setColor(new Color(1f, 1f, 1f, endingAlpha));
        g2d.drawString(text, x + offset, y + offset);
    }

    // Key Listeners
    @Override
    public void keyPressed(KeyEvent e) {
        if (showStartMenu && e.getKeyCode() == KeyEvent.VK_T) {
            initializeGame();
            return;
        }

        // เพิ่มการตรวจจับปุ่ม SPACE สำหรับเริ่มเกมใหม่
        if (gameSucceeded && e.getKeyCode() == KeyEvent.VK_SPACE) {
            endingAlpha = 0f;
            showStartMenu = true;
            gameSucceeded = false;
            return;
        }

        if (!gameOver && gameStarted) {
            activeKeys.add(e.getKeyCode());
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                player.setShooting(true);
            }
            if (e.getKeyCode() == KeyEvent.VK_E) {
                player.setUltimateShooting(true);
            }
            if (e.getKeyCode() == KeyEvent.VK_Q) {
                player.setBeamFiring(true);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        activeKeys.remove(e.getKeyCode());
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            player.setShooting(false);
        }
        if (e.getKeyCode() == KeyEvent.VK_E) {
            player.setUltimateShooting(false);
        }
        if (e.getKeyCode() == KeyEvent.VK_Q) {
            player.setBeamFiring(false);
        }
    }

    // Required method implementations
    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseDragged(MouseEvent e) {}
    @Override
    public void mouseMoved(MouseEvent e) {}

    private void spawnAsteroids() {
        // spawn เฉพาะ large asteroids
        for (int i = 0; i < 3; i++) { // ปรับจำนวนตามความเหมาะสม
            asteroids.add(new Asteroid(Math.random() * 800, Math.random() * 600, true));
        }
    }

    private void spawnRegularEnemies() {
        for (int i = 0; i < 4; i++) {
            double x = Math.random() * 800;
            double y = Math.random() * 600;
            double velocityX = Math.random() * 2 - 1;
            double velocityY = Math.random() * 2 - 1;
            RegularEnemy enemy = new RegularEnemy(x, y, velocityX, velocityY, 0, 50);
            enemy.setTarget(player);
            regularEnemies.add(enemy);
        }
    }

    private void spawnSecondTierEnemies() {
        for (int i = 0; i < 2; i++) {
            double x = Math.random() * 800;
            double y = Math.random() * 600;
            double velocityX = Math.random() * 2 - 1;
            double velocityY = Math.random() * 2 - 1;
            SecondTier enemy = new SecondTier(x, y, velocityX, velocityY, 0, 75);
            enemy.setTarget(player);
            secondTierEnemies.add(enemy);
        }
    }
    /**
     * centralize Exception
     * catch every Throwable exceotion convert to  GameException
     */
    private void handleGlobalException(Throwable t) {
        try {
            GameException gameException;
            if (t instanceof GameException) {
                gameException = (GameException) t;
            } else {
                // แปลง unknown exception เป็น GameException
                gameException = new GameException(
                        "Unexpected error: " + t.getMessage(),
                        GameException.ErrorType.GENERAL_ERROR,
                        t
                );
            }
            handleGameException(gameException);
        } catch (Exception unexpected) {
            // ถ้าเกิด error ในตัว handler เอง
            logger.fatal("Critical error in exception handler", unexpected);
            showFatalErrorDialog();
            System.exit(1);
        }
    }
    /**
     * จัดการ GameException แต่ละประเภทและทำการกู้คืนที่เหมาะสม
     */
    private void handleGameException(GameException e) {
        switch (e.getErrorType()) {
            case RESOURCE_LOADING_ERROR:
                logger.error("Resource loading error: {}", e.getMessage(), e);
                handleResourceError(e);
                break;

            case SPRITE_PROCESSING_ERROR:
                logger.error("Sprite processing error: {}", e.getMessage(), e);
                handleSpriteError(e);
                break;

            case GENERAL_ERROR:
                logger.error("General game error: {}", e.getMessage(), e);
                handleGeneralError(e);
                break;

            default:
                logger.error("Unknown error type: {}", e.getMessage(), e);
                handleGeneralError(e);
                break;
        }
    }
    /**
     * จัดการ error ที่เกี่ยวกับการโหลดทรัพยากร
     */
    private void handleResourceError(GameException e) {
        showErrorDialog(
                "Resource Error",
                "Failed to load game resources. The game will try to reload."
        );
        try {
            // พยายามโหลดทรัพยากรใหม่
            setupInitialState();
        } catch (Exception retryError) {
            // ถ้ายังไม่สำเร็จ ให้จบการทำงาน
            showFatalErrorDialog();
            System.exit(1);
        }}
    /**
     * จัดการ error ที่เกี่ยวกับการประมวลผล sprite
     */
    private void handleSpriteError(GameException e) {
        showErrorDialog(
                "Graphics Error",
                "Error processing game graphics. The game will try to recover."
        );
        try {
            // พยายามโหลด sprite ใหม่
            if (player != null) {
                player = new PlayerShip(player.getX(), player.getY());
            }
        } catch (Exception retryError) {
            handleGeneralError(retryError);
        }
    }

    /**
     * จัดการ error ทั่วไป
     */
    private void handleGeneralError(Exception e) {
        showErrorDialog(
                "Game Error",
                "An error occurred. The game will restart."
        );
        try {
            initializeGame();
        } catch (Exception retryError) {
            showFatalErrorDialog();
            System.exit(1);
        }
    }
    /**
     * แสดง dialog แจ้ง error แบบทั่วไป
     */
    private void showErrorDialog(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    this,
                    message,
                    title,
                    JOptionPane.ERROR_MESSAGE
            );
        });
    }

    /**
     * แสดง dialog แจ้ง error ร้ายแรง
     */
    private void showFatalErrorDialog() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    this,
                    "A fatal error occurred. The game will close.",
                    "Fatal Error",
                    JOptionPane.ERROR_MESSAGE
            );
        });
    }
}