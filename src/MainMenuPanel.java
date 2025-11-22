import javax.swing.*;
import java.awt.*;

public class MainMenuPanel extends JPanel {
    private Main mainApp;
    private JLabel welcomeLabel = new JLabel("Welcome, User!");

    public MainMenuPanel(Main mainApp) {
        this.mainApp = mainApp;

        welcomeLabel.setFont(Theme.FONT_TITLE);
        welcomeLabel.setForeground(Theme.BTN_COLOR);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeLabel.setMaximumSize(new Dimension(400, 30));
        welcomeLabel.setPreferredSize(new Dimension(400, 30));

        JButton startGameButton = new JButton("Mulai Main");
        startGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startGameButton.setFont(Theme.FONT_NORMAL);
        startGameButton.setMaximumSize(new Dimension(200, 30));
        startGameButton.setPreferredSize(new Dimension(200, 30));
        startGameButton.setBackground(Theme.BTN_COLOR);
        startGameButton.setForeground(Theme.BTN_TEXT);
        startGameButton.setFocusPainted(false);
        startGameButton.setBorderPainted(false);

        startGameButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                startGameButton.setBackground(Theme.BTN_TEXT); 
                startGameButton.setForeground(Theme.BTN_COLOR); 
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                startGameButton.setBackground(Theme.BTN_COLOR); 
                startGameButton.setForeground(Theme.BTN_TEXT); 
            }
        });

        startGameButton.addActionListener(e -> mainApp.showPanel("GAME"));

        JButton leaderboardButton = new JButton("Lihat Leaderboard");
        leaderboardButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        leaderboardButton.setFont(Theme.FONT_NORMAL);
        leaderboardButton.setMaximumSize(new Dimension(200, 30));
        leaderboardButton.setPreferredSize(new Dimension(200, 30));
        leaderboardButton.setBackground(Theme.BTN_COLOR);
        leaderboardButton.setForeground(Theme.BTN_TEXT);
        leaderboardButton.setFocusPainted(false);
        leaderboardButton.setBorderPainted(false);
        
        leaderboardButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                leaderboardButton.setBackground(Theme.BTN_TEXT); 
                leaderboardButton.setForeground(Theme.BTN_COLOR);
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                leaderboardButton.setBackground(Theme.BTN_COLOR); 
                leaderboardButton.setForeground(Theme.BTN_TEXT); 
            }
        });

        leaderboardButton.addActionListener(e -> mainApp.showPanel("LEADERBOARD"));

        JButton logoutButton = new JButton("Logout");
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.setFont(Theme.FONT_NORMAL);
        logoutButton.setMaximumSize(new Dimension(200, 20));
        logoutButton.setPreferredSize(new Dimension(200, 20));
        logoutButton.setBackground(Theme.BTN_TEXT);
        logoutButton.setForeground(Theme.FG_TEXT);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);

        logoutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(Theme.FG_TEXT); 
                logoutButton.setForeground(Theme.BTN_TEXT); 
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(Theme.BTN_TEXT); 
                logoutButton.setForeground(Theme.FG_TEXT); 
            }
        });

        logoutButton.addActionListener(e -> mainApp.logout());
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(100, 50, 50, 50));
        setOpaque(false);
        add(welcomeLabel);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(startGameButton);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(leaderboardButton);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(logoutButton);

    }

    public void setWelcomeMessage(String usn) {
        welcomeLabel.setText("Selamat datang, " + usn);
    }
}
