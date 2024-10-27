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
    private Boss boss;
    private Image backgroundImage;
    private Set<Integer> activeKeys;

    // Game state variables
    private int score = 0;
    private int lives = 3;
    private boolean gameOver = false;
    private String gameOverMessage = "";
    private boolean isExploding = false;
    private int explosionTicks = 0;
    private static final int EXPLOSION_DURATION = 60;
    public GamePanel() {
        this.setPreferredSize(new Dimension(800, 600));

        initializeGame();

        timer = new Timer(16, this); // 60 FPS
        timer.start();
        addMouseListener(this);
        addMouseMotionListener(this);
        setFocusable(true);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (!gameOver) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        player.rotateLeft();
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        player.rotateRight();
                    }
                }
            }
        });
        addKeyListener(this);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) {
            return;
        }

        if (isExploding) {
            handleExplosion();
            return;
        }

        // Handle player movement
        handlePlayerMovement();

        // Update all game objects
        updateGameObjects();

        // Check all collisions
        checkCollisions();

        // Check if we need to spawn more enemies
        checkEnemySpawning();

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw background
        g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        // Draw game objects if not game over
        if (!gameOver) {
            // Draw all game objects
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

            // Draw HUD
            drawHUD(g2d);
        } else {
            // Draw game over screen
            drawGameOver(g2d);
        }
    }
    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameOver) {
            activeKeys.add(e.getKeyCode());
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                player.setShooting(true);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        activeKeys.remove(e.getKeyCode()); // Remove the key from the set when released
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            player.setShooting(false);
        }
    }

    @Override
    public void mouseClicked(java.awt.event.MouseEvent e) {

    }

    @Override
    public void mousePressed(java.awt.event.MouseEvent e) {

    }

    @Override
    public void mouseReleased(java.awt.event.MouseEvent e) {

    }

    @Override
    public void mouseEntered(java.awt.event.MouseEvent e) {

    }

    @Override
    public void mouseExited(java.awt.event.MouseEvent e) {

    }

    @Override
    public void mouseDragged(java.awt.event.MouseEvent e) {

    }

    @Override
    public void mouseMoved(java.awt.event.MouseEvent e) {
    }
    private void initializeGame() {
        player = new PlayerShip(400, 300);
        asteroids = new ArrayList<>();
        regularEnemies = new ArrayList<>();
        boss = new Boss(600, 300);
        activeKeys = new HashSet<>();
        score = 0;
        lives = 3;
        gameOver = false;
        isExploding = false;

        // Load background
        backgroundImage = new ImageIcon(getClass().getResource("/assets/878d4b7113a683135734352e68e00e58.gif")).getImage();

        // Create initial asteroids
        spawnAsteroids();

        // Create some regular enemies
        spawnRegularEnemies();
    }
    private void spawnAsteroids() {
        for (int i = 0; i < 5; i++) {
            // Spawn large asteroids
            asteroids.add(new Asteroid(Math.random() * 800, Math.random() * 600, true));
        }
        for (int i = 0; i < 8; i++) {
            // Spawn small asteroids
            asteroids.add(new Asteroid(Math.random() * 800, Math.random() * 600, false));
        }
    }
    private void spawnRegularEnemies() {
        for (int i = 0; i < 3; i++) {
            double x = Math.random() * 800;
            double y = Math.random() * 600;
            double velocityX = Math.random() * 2 - 1;
            double velocityY = Math.random() * 2 - 1;
            regularEnemies.add(new RegularEnemy(x, y, velocityX, velocityY, 0, 50));
        }
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
                // Respawn player
                player = new PlayerShip(400, 300);
            }
        }
    }
    private void handlePlayerMovement() {
        if (activeKeys.contains(KeyEvent.VK_W)) player.moveUp();
        if (activeKeys.contains(KeyEvent.VK_D)) player.moveRight();
        if (activeKeys.contains(KeyEvent.VK_A)) player.moveLeft();
        if (activeKeys.contains(KeyEvent.VK_S)) player.moveDown();
    }
    private void updateGameObjects() {
        player.update();
        boss.update();

        // Update bullets and check for off-screen
        for (int i = player.getBullets().size() - 1; i >= 0; i--) {
            Bullet bullet = player.getBullets().get(i);
            bullet.update();
            if (bullet.isOffScreen(800, 600)) {
                player.getBullets().remove(i);
            }
        }

        // Update asteroids
        for (Asteroid asteroid : asteroids) {
            asteroid.update();
        }

        // Update regular enemies
        for (RegularEnemy enemy : regularEnemies) {
            enemy.update();
        }
    }
    private void checkCollisions() {
        // Check bullet collisions with asteroids and enemies
        for (int i = player.getBullets().size() - 1; i >= 0; i--) {
            Bullet bullet = player.getBullets().get(i);
            boolean bulletHit = false;

            // Check asteroid collisions
            for (int j = asteroids.size() - 1; j >= 0; j--) {
                Asteroid asteroid = asteroids.get(j);
                if (bullet.getBounds().intersects(asteroid.getBounds())) {
                    asteroid.hit();
                    bulletHit = true;

                    if (asteroid.isDestroyed()) {
                        score += asteroid.isLarge() ? 2 : 1;
                        asteroids.remove(j);
                    }
                    break;
                }
            }

            // Check regular enemy collisions
            if (!bulletHit) {
                for (int j = regularEnemies.size() - 1; j >= 0; j--) {
                    RegularEnemy enemy = regularEnemies.get(j);
                    if (bullet.getBounds().intersects(enemy.getBounds())) {
                        enemy.hit();
                        bulletHit = true;

                        if (enemy.isDestroyed()) {
                            score += 1;
                            regularEnemies.remove(j);
                        }
                        break;
                    }
                }
            }

            // Remove bullet if it hit something
            if (bulletHit) {
                player.getBullets().remove(i);
            }
        }

        // Check player collision with asteroids and enemies
        if (!isExploding) {
            Rectangle playerBounds = player.getBounds();

            // Check asteroid collisions
            for (Asteroid asteroid : asteroids) {
                if (playerBounds.intersects(asteroid.getBounds())) {
                    startExplosion();
                    return;
                }
            }

            // Check regular enemy collisions
            for (RegularEnemy enemy : regularEnemies) {
                if (playerBounds.intersects(enemy.getBounds())) {
                    startExplosion();
                    return;
                }
            }

            // Check boss collision
            if (playerBounds.intersects(boss.getBounds())) {
                startExplosion();
            }
        }
    }
    private void startExplosion() {
        isExploding = true;
        explosionTicks = 0;
    }

    private void checkEnemySpawning() {
        // Spawn new asteroids if there are too few
        if (asteroids.size() < 5) {
            spawnAsteroids();
        }

        // Spawn new regular enemies if there are too few
        if (regularEnemies.size() < 2) {
            spawnRegularEnemies();
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
    }

    private void drawGameOver(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(gameOverMessage);
        g2d.drawString(gameOverMessage, (getWidth() - textWidth) / 2, getHeight() / 2);
    }

}