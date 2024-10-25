package com.se233.asteroid.controller;

import com.se233.asteroid.model.Asteroid;
import com.se233.asteroid.model.Boss;
import com.se233.asteroid.model.Bullet;
import com.se233.asteroid.model.PlayerShip;

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
    private Boss boss;
    private Image backgroundImage;
    private Set<Integer> activeKeys;

    public GamePanel() {
        this.setPreferredSize(new Dimension(800, 600));

        player = new PlayerShip(400, 300);
        asteroids = new ArrayList<Asteroid>();
        boss = new Boss(600, 300);
        backgroundImage = new ImageIcon(getClass().getResource("/assets/878d4b7113a683135734352e68e00e58.gif")).getImage();
        if (backgroundImage == null) {
            System.out.println("Background image not found!");
        }
        // Create some asteroids
        for (int i = 0; i < 10; i++) {
            asteroids.add(new Asteroid(Math.random() * 800, Math.random() * 600));
        }
        activeKeys = new HashSet<>();
        timer = new Timer(16, this); // 60 FPS
        timer.start();
        addMouseListener(this);
        addMouseMotionListener(this);
        setFocusable(true);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    player.rotateLeft();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    player.rotateRight();
                }
            }
        });
        addKeyListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(activeKeys.contains(KeyEvent.VK_W)) player.moveUp();
        if(activeKeys.contains(KeyEvent.VK_D)) player.moveRight();
        if(activeKeys.contains(KeyEvent.VK_A)) player.moveLeft();
        if(activeKeys.contains(KeyEvent.VK_S)) player.moveDown();
        player.update();
        boss.update();

        // Update and check for bullet-asteroid collisions
        for (int i = player.getBullets().size() - 1; i >= 0; i--) {
            Bullet bullet = player.getBullets().get(i);
            bullet.update();

            // Remove the bullet if it's off-screen
            if (bullet.isOffScreen(800, 600)) {
                player.getBullets().remove(i);
                continue; // Skip to the next bullet
            }

            // Check collision with asteroids
            for (int j = asteroids.size() - 1; j >= 0; j--) {
                Asteroid asteroid = asteroids.get(j);
                if (bullet.getBounds().intersects(asteroid.getBounds())) {
                    asteroid.hit(); // Reduce asteroid health
                    player.getBullets().remove(i); // Remove bullet

                    if (asteroid.isDestroyed()) {
                        asteroids.remove(j); // Remove asteroid only if destroyed
                    }
                    break; // Exit the loop after collision
                }
            }
        }

        // Update asteroids
        for (Asteroid asteroid : asteroids) {
            asteroid.update();
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        player.draw(g2d);
        boss.draw(g2d);
        for (Asteroid asteroid : asteroids) {
            asteroid.draw(g2d);
        }
    }
    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        activeKeys.add(e.getKeyCode()); // Add the key to the set when pressed
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            player.setShooting(true);
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
}