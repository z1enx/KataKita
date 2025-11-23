import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;

public class CooldownPanel extends JPanel {
    
    private Main mainApp;
    
    private JLabel cooldownTimerLabel;
    private JLabel lastResultLabel;
    
    private final long COOLDOWN_DURATION = 5 * 60 * 1000; // 5 menit
    
    private long lastGameEndTime;
    private volatile boolean isRunning;
    private Thread cooldownThread;
    
    public CooldownPanel(Main mainApp) {
        this.mainApp = mainApp;
        this.setLayout(new BorderLayout());
        this.setBackground(Theme.BG_COLOR);
        
        initUI();
    }
    
    private void initUI() {
        // Main container dengan GridBagLayout untuk centering
        JPanel mainContainer = new JPanel(new GridBagLayout());
        mainContainer.setBackground(Theme.BG_COLOR);
        
        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Theme.COLOR_ABSENT);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BTN_COLOR, 3),
            new EmptyBorder(40, 50, 40, 50)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("🎮 HASIL GAME TERAKHIR 🎮");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        titleLabel.setForeground(Theme.BTN_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Last result info
        lastResultLabel = new JLabel();
        lastResultLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lastResultLabel.setForeground(Theme.FG_TEXT);
        lastResultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Cooldown info
        JLabel cooldownInfoLabel = new JLabel("⏰ Kamu bisa bermain lagi dalam:");
        cooldownInfoLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        cooldownInfoLabel.setForeground(Theme.FG_TEXT);
        cooldownInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Cooldown timer
        cooldownTimerLabel = new JLabel("05:00");
        cooldownTimerLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
        cooldownTimerLabel.setForeground(Theme.COLOR_PRESENT); // Kuning
        cooldownTimerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Back button
        JButton btnBackToEndGame = new JButton("⬅️ KEMBALI");
        btnBackToEndGame.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBackToEndGame.setFont(Theme.FONT_NORMAL);
        btnBackToEndGame.setBackground(Theme.COLOR_PRESENT);
        btnBackToEndGame.setForeground(Color.WHITE);
        btnBackToEndGame.setFocusPainted(false);
        btnBackToEndGame.setBorderPainted(false);
        btnBackToEndGame.setPreferredSize(new Dimension(200, 40));
        btnBackToEndGame.setMaximumSize(new Dimension(200, 40));
        btnBackToEndGame.addActionListener(e -> {
            stopCooldown();
            mainApp.showEndGamePanel(lastGameEndTime);
        });
        
        JButton btnBackToMenu = new JButton("🏠 MENU UTAMA");
        btnBackToMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBackToMenu.setFont(Theme.FONT_NORMAL);
        btnBackToMenu.setBackground(Theme.BTN_COLOR);
        btnBackToMenu.setForeground(Theme.BTN_TEXT);
        btnBackToMenu.setFocusPainted(false);
        btnBackToMenu.setBorderPainted(false);
        btnBackToMenu.setPreferredSize(new Dimension(200, 40));
        btnBackToMenu.setMaximumSize(new Dimension(200, 40));
        btnBackToMenu.addActionListener(e -> {
            stopCooldown();
            mainApp.showPanel("MAIN_MENU");
        });
        
        // Add components
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        contentPanel.add(lastResultLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        contentPanel.add(cooldownInfoLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(cooldownTimerLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        contentPanel.add(btnBackToMenu);
        
        mainContainer.add(contentPanel);
        
        add(mainContainer, BorderLayout.CENTER);
    }
    
    public void onPanelShown(long gameEndTime) {
    this.lastGameEndTime = System.currentTimeMillis(); // FIX: start timer now
    loadLastGameResult();
    startCooldownTimer();
}

    
    private void loadLastGameResult() {
        DBCon db = new DBCon();
        Object[] lastResult = db.getLastGameResult(mainApp.getCurrentUserId());
        
        if (lastResult != null) {
            String wordText = (String) lastResult[0];
            int duration = (int) lastResult[1];
            int attempts = (int) lastResult[2];
            int score = (int) lastResult[3];
            String status = (String) lastResult[4];
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String dateStr = sdf.format(lastResult[5]);
            
            String resultHtml = "<html><div style='text-align: center;'>" +
                "<p style='font-size: 18px; margin: 10px;'><b>Status:</b> " + 
                (status.equals("WIN") ? "<span style='color: #538D4E;'>MENANG! 🎉</span>" : "<span style='color: #B59F3B;'>KALAH 😢</span>") + "</p>" +
                "<p style='margin: 6px;'><b>Kata:</b> " + wordText + "</p>" +
                "<p style='margin: 6px;'><b>Percobaan:</b> " + attempts + " kali</p>" +
                "<p style='margin: 6px;'><b>Durasi:</b> " + duration + " detik</p>" +
                "<p style='margin: 6px;'><b>Skor:</b> " + score + "</p>" +
                "<p style='margin: 6px; color: #888;'><i>" + dateStr + "</i></p>" +
                "</div></html>";
            
            lastResultLabel.setText(resultHtml);
        }
    }
    
    private void startCooldownTimer() {
        stopCooldown(); // Stop any existing thread
        
        isRunning = true;
        
        cooldownThread = new Thread(() -> {
            while (isRunning) {
                long currentTime = System.currentTimeMillis();
                long elapsed = currentTime - lastGameEndTime;
                long remaining = COOLDOWN_DURATION - elapsed;
                
                if (remaining <= 0) {
                    SwingUtilities.invokeLater(() -> {
                        isRunning = false;
                        JOptionPane.showMessageDialog(this, 
                            "⏰ Cooldown selesai! Kamu bisa bermain lagi.", 
                            "Info", 
                            JOptionPane.INFORMATION_MESSAGE);
                        mainApp.showPanel("GAME");
                    });
                    break;
                }
                
                int minutes = (int) (remaining / 60000);
                int seconds = (int) ((remaining % 60000) / 1000);
                
                SwingUtilities.invokeLater(() -> {
                    cooldownTimerLabel.setText(String.format("%02d:%02d", minutes, seconds));
                });
                
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        
        cooldownThread.start();
    }
    
    public void stopCooldown() {
        isRunning = false;
        if (cooldownThread != null && cooldownThread.isAlive()) {
            cooldownThread.interrupt();
            try {
                cooldownThread.join(100); // Wait max 100ms
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}