package com.se233.asteroid;

import com.se233.asteroid.model.Asteroid;
import com.se233.asteroid.model.Boss;
import com.se233.asteroid.model.PlayerShip;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener {
    private Timer timer;
    private PlayerShip player;
    private List<Asteroid> asteroids;
    private Boss boss;
    private Image backgroundImage;
    public GamePanel() {
        this.setPreferredSize(new Dimension(800, 600));
        this.setBackground(Color.BLACK);

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
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A -> player.moveLeft();
                    case KeyEvent.VK_D -> player.moveRight();
                    case KeyEvent.VK_W -> player.moveUp();
                    case KeyEvent.VK_S -> player.moveDown();
                    case KeyEvent.VK_SPACE -> player.setShooting(true);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    player.setShooting(false);
                }
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        player.update();
        boss.update();
        for (Asteroid asteroid : asteroids) {
            asteroid.update();
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(backgroundImage, 0, 0,getWidth(), getHeight(), this);
        player.draw(g2d);
        boss.draw(g2d);
        for (Asteroid asteroid : asteroids) {
            asteroid.draw(g2d);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Asteroid Game");
        GamePanel gamePanel = new GamePanel();
        frame.add(gamePanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

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