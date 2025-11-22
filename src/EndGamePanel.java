import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EndGamePanel extends JPanel {
    
    private Main mainApp;
    
    private JLabel cooldownTimerLabel;
    private JLabel infoLabel;
    
    private final long COOLDOWN_DURATION = 5 * 60 * 1000; // 5 menit
    
    private long lastGameEndTime;
    private volatile boolean isRunning;
    private Thread cooldownThread;
    
    public EndGamePanel(Main mainApp) {
        this.mainApp = mainApp;
        this.setLayout(new BorderLayout());
        this.setBackground(Theme.BG_COLOR);
        
        initUI();
    }
    
    private void initUI() {
        // Main container
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
        
        // Icon
        JLabel iconLabel = new JLabel("⏰");
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 72));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Title
        JLabel titleLabel = new JLabel("COOLDOWN AKTIF");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        titleLabel.setForeground(Theme.BTN_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Info text
        infoLabel = new JLabel("Kamu baru saja menyelesaikan permainan");
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        infoLabel.setForeground(Theme.FG_TEXT);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel infoLabel2 = new JLabel("Tunggu sebentar untuk bermain lagi");
        infoLabel2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        infoLabel2.setForeground(Theme.FG_TEXT);
        infoLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Cooldown timer
        JLabel timerTextLabel = new JLabel("Waktu tersisa:");
        timerTextLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        timerTextLabel.setForeground(Theme.FG_TEXT);
        timerTextLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        cooldownTimerLabel = new JLabel("05:00");
        cooldownTimerLabel.setFont(new Font("SansSerif", Font.BOLD, 64));
        cooldownTimerLabel.setForeground(Theme.COLOR_PRESENT); // Kuning
        cooldownTimerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton btnViewResult = new JButton("📊 LIHAT HASIL TERAKHIR");
        styleButton(btnViewResult, Theme.COLOR_PRESENT);
        btnViewResult.addActionListener(e -> {
            stopCooldown();
            mainApp.showCooldownPanel(lastGameEndTime);
        });
        
        JButton btnBackToMenu = new JButton("🏠 KEMBALI KE MENU");
        styleButton(btnBackToMenu, Theme.BTN_COLOR);
        btnBackToMenu.addActionListener(e -> {
            stopCooldown();
            mainApp.showPanel("MAIN_MENU");
        });
        
        buttonsPanel.add(btnViewResult);
        buttonsPanel.add(btnBackToMenu);
        
        // Add all components
        contentPanel.add(iconLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(infoLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(infoLabel2);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        contentPanel.add(timerTextLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(cooldownTimerLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 35)));
        contentPanel.add(buttonsPanel);
        
        mainContainer.add(contentPanel);
        
        add(mainContainer, BorderLayout.CENTER);
    }
    
    private void styleButton(JButton btn, Color bgColor) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(220, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });
    }
    
    public void onPanelShown(long gameEndTime) {
        this.lastGameEndTime = gameEndTime;
        
        // Update info text
        long timeSinceGame = System.currentTimeMillis() - gameEndTime;
        if (timeSinceGame < 60000) { // Kurang dari 1 menit
            infoLabel.setText("Kamu baru saja menyelesaikan permainan");
        } else if (timeSinceGame < 3600000) { // Kurang dari 1 jam
            int minutes = (int) (timeSinceGame / 60000);
            infoLabel.setText("Game terakhir dimainkan " + minutes + " menit yang lalu");
        } else {
            int hours = (int) (timeSinceGame / 3600000);
            infoLabel.setText("Game terakhir dimainkan " + hours + " jam yang lalu");
        }
        
        // Start cooldown timer
        startCooldownTimer();
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
                        int choice = JOptionPane.showConfirmDialog(this, 
                            "⏰ Cooldown selesai! Kamu bisa bermain lagi.\n\nMulai game baru sekarang?", 
                            "Cooldown Selesai", 
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        if (choice == JOptionPane.YES_OPTION) {
                            mainApp.showPanel("GAME");
                        } else {
                            mainApp.showPanel("MAIN_MENU");
                        }
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
