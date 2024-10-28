package com.se233.asteroid.controller;

import com.se233.asteroid.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener {
    private Timer timer;
    private PlayerShip player;
    private List<Asteroid> asteroids;
    private List<RegularEnemy> regularEnemies;
    private List<SecondTier> secondTierEnemies;
    private Boss boss;
    private Image backgroundImage;
    private Set<Integer> activeKeys;

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

    public GamePanel() {
        this.setPreferredSize(new Dimension(800, 600));
        initializeGame();
        timer = new Timer(16, this); // 60 FPS
        timer.start();

        // Add input listeners
        addMouseListener(this);
        addMouseMotionListener(this);
        setFocusable(true);
        addKeyListener(this);

        // Mouse controls for rotation
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!gameOver) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        player.rotateLeft();
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        player.rotateRight();
                    }
                }
            }
        });
    }

    private void initializeGame() {
        player = new PlayerShip(400, 300);
        asteroids = new ArrayList<>();
        regularEnemies = new ArrayList<>();
        secondTierEnemies = new ArrayList<>();
        boss = null;
        activeKeys = new HashSet<>();
        score = 0;
        lives = 3;
        gameOver = false;
        bossPhaseStarted = false;
        bossDefeated = false;
        isExploding = false;

        // Load background
        backgroundImage = new ImageIcon(getClass().getResource("/assets/bg.gif")).getImage();

        // Spawn initial enemies
        spawnAsteroids();
        spawnRegularEnemies();
        spawnSecondTierEnemies();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        if (isExploding) {
            handleExplosion();
            return;
        }

        // Update game objects
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
        if (!bossPhaseStarted && asteroids.isEmpty() && regularEnemies.isEmpty() &&
                secondTierEnemies.isEmpty()) {
            startBossPhase();
        }
    }

    private void startBossPhase() {
        bossPhaseStarted = true;
        boss = new Boss(400, 300);
        // You could add dramatic effects or messages here
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
    }

    private boolean checkBulletCollisions(Bullet bullet) {
        // Check asteroid collisions
        for (int j = asteroids.size() - 1; j >= 0; j--) {
            Asteroid asteroid = asteroids.get(j);
            if (bullet.getBounds().intersects(asteroid.getBounds())) {
                asteroid.hit();
                if (asteroid.isDestroyed()) {
                    score += asteroid.isLarge() ? 2 : 1;
                    asteroids.remove(j);
                }
                return true;
            }
        }

        // Check regular enemy collisions
        for (int j = regularEnemies.size() - 1; j >= 0; j--) {
            RegularEnemy enemy = regularEnemies.get(j);
            if (bullet.getBounds().intersects(enemy.getBounds())) {
                enemy.hit();
                if (enemy.isDestroyed()) {
                    score += 1;
                    regularEnemies.remove(j);
                }
                return true;
            }
        }

        // Check second tier enemy collisions
        for (int j = secondTierEnemies.size() - 1; j >= 0; j--) {
            SecondTier enemy = secondTierEnemies.get(j);
            if (bullet.getBounds().intersects(enemy.getBounds())) {
                enemy.hit();
                if (enemy.isDestroyed()) {
                    score += 2;
                    secondTierEnemies.remove(j);
                }
                return true;
            }
        }

        // Check boss collision
        if (boss != null && boss.isAlive() && bullet.getBounds().intersects(boss.getBounds())) {
            boss.hit(10);
            if (!boss.isAlive()) {
                score += 50;
                bossDefeated = true;
                gameSucceeded = true;
                gameSucceedMessage += score;
            }
            return true;
        }

        return false;
    }

    private void checkEnemyCollisionsWithPlayer(Rectangle playerBounds) {
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
                gameOverMessage = "GAME OVER - Final Score: " + score;
            } else {
                player = new PlayerShip(400, 300);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw background
        g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        if (!gameOver) {
            // Draw game objects
            if (!isExploding) {
                player.draw(g2d);
            } else {
                drawExplosion(g2d);
            }

            // Draw enemies and asteroids
            for (Asteroid asteroid : asteroids) {
                asteroid.draw(g2d);
            }
            for (RegularEnemy enemy : regularEnemies) {
                enemy.draw(g2d);
            }
            for (SecondTier enemy : secondTierEnemies) {
                enemy.draw(g2d);
            }

            // Draw boss if active
            if (boss != null && boss.isAlive()) {
                boss.draw(g2d);
            }

            drawHUD(g2d);
        } else {
            if (bossDefeated) {
                drawGameSucceeded(g2d);
            } else {
                drawGameOver(g2d);
            }
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

        if (bossPhaseStarted && boss != null && boss.isAlive()) {
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
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(gameSucceedMessage);
        g2d.drawString(gameSucceedMessage, (getWidth() - textWidth) / 2, getHeight() / 2);
    }

    // Key Listeners
    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameOver) {
            activeKeys.add(e.getKeyCode());
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                player.setShooting(true);
            }
            if (e.getKeyCode() == KeyEvent.VK_E) {
                player.setUltimateShooting(true);
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
        for (int i = 0; i < 5; i++) {
            asteroids.add(new Asteroid(Math.random() * 800, Math.random() * 600, true));
        }
        for (int i = 0; i < 8; i++) {
            asteroids.add(new Asteroid(Math.random() * 800, Math.random() * 600, false));
        }
    }

    private void spawnRegularEnemies() {
        for (int i = 0; i < 3; i++) {
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
}