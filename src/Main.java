import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    
    private CardLayout cardLayout;
    private JPanel mainPanelContainer;
    
    private LoginPanel loginPanel = new LoginPanel(this);
    private MainMenuPanel mainMenuPanel = new MainMenuPanel(this);
    private GamePanel gamePanel = new GamePanel(this);
    private LeaderboardPanel leaderboardPanel = new LeaderboardPanel(this);
    private CooldownPanel cooldownPanel = new CooldownPanel(this);
    private EndGamePanel endGamePanel = new EndGamePanel(this);
    
    private int currentUserId;
    private String currentUsername;

    public Main() {
        setTitle("KataKita: Tebak kata secepat mungkin!");
        getContentPane().setBackground(Theme.BG_COLOR);
        setSize(700, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        DBCon db = new DBCon();
        // db.initialize();

        cardLayout = new CardLayout();
        mainPanelContainer = new JPanel(cardLayout);
        mainPanelContainer.setOpaque(false);

        JPanel loginWrapper = new JPanel(new GridBagLayout());
        loginWrapper.setOpaque(false);
        loginWrapper.add(loginPanel);
        
        mainPanelContainer.add(loginWrapper, "LOGIN");
        mainPanelContainer.add(mainMenuPanel, "MAIN_MENU");
        mainPanelContainer.add(gamePanel, "GAME");
        mainPanelContainer.add(leaderboardPanel, "LEADERBOARD");
        mainPanelContainer.add(cooldownPanel, "ENDGAME");
        mainPanelContainer.add(endGamePanel, "COOLDOWN");

        add(mainPanelContainer);

        showPanel("LOGIN");
    }

    public void showPanel(String panelName) {
        if (panelName.equals("GAME")) {
            if (gamePanel != null) {
                gamePanel.onPanelShown();
            }
        } else if (panelName.equals("LEADERBOARD")) {
            if (leaderboardPanel != null) {
                leaderboardPanel.onPanelShown();
            }
        }
        cardLayout.show(mainPanelContainer, panelName);
    }
    
    public void showCooldownPanel(long gameEndTime) {
        cardLayout.show(mainPanelContainer, "ENDGAME");
        cooldownPanel.onPanelShown(gameEndTime);
    }
    
    public void showEndGamePanel(long gameEndTime) {
        cardLayout.show(mainPanelContainer, "COOLDOWN");
        endGamePanel.onPanelShown(gameEndTime);
    }

    public void onLoginSuccess(int userId, String username) {
        this.currentUserId = userId;
        this.currentUsername = username;
        mainMenuPanel.setWelcomeMessage(username);
        
        showPanel("MAIN_MENU");
    }

    public void logout() {
        currentUserId = -1;
        currentUsername = null;
        SwingUtilities.invokeLater(() -> showPanel("LOGIN"));
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
             Main app = new Main();
             app.setVisible(true);
         });
    }
}