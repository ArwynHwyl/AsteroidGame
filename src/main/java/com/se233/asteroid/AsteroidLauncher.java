import com.se233.asteroid.controller.GamePanel;
import javax.swing.*;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Asteroid Game");
        GamePanel gamePanel = new GamePanel();
        frame.add(gamePanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }