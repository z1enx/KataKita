import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CooldownPanel extends JPanel {

    private Main mainApp;

    private JLabel cooldownTimerLabel;
    private JLabel infoLabel;

    private final long COOLDOWN_DURATION = 5 * 60 * 1000;

    private long lastGameEndTime;
    private volatile boolean isRunning;
    private Thread cooldownThread;

    public CooldownPanel(Main mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_COLOR);

        initUI();
    }

    private void initUI() {

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        JPanel content = Theme.createRoundedPanel(30);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(45, 70, 45, 70));

        JLabel iconLabel = new JLabel("⏳");
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 80));
        iconLabel.setForeground(Theme.BTN_COLOR);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("COOLDOWN AKTIF");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        titleLabel.setForeground(Theme.BTN_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoLabel = new JLabel("Kamu baru saja menyelesaikan permainan");
        infoLabel.setFont(Theme.FONT_NORMAL);
        infoLabel.setForeground(Theme.FG_TEXT);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel infoLabel2 = new JLabel("Tunggu sebentar untuk bermain lagi");
        infoLabel2.setFont(Theme.FONT_NORMAL);
        infoLabel2.setForeground(Theme.FG_TEXT);
        infoLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel timerText = new JLabel("Waktu tersisa:");
        timerText.setFont(new Font("SansSerif", Font.BOLD, 16));
        timerText.setForeground(Theme.FG_TEXT);
        timerText.setAlignmentX(Component.CENTER_ALIGNMENT);

        cooldownTimerLabel = new JLabel("05:00");
        cooldownTimerLabel.setFont(new Font("SansSerif", Font.BOLD, 72));
        cooldownTimerLabel.setForeground(Theme.COLOR_PRESENT);
        cooldownTimerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        JButton btnView = new JButton("📊 LIHAT HASIL TERAKHIR");
        styleButton(btnView, Theme.COLOR_PRESENT);
        btnView.addActionListener(e -> {
            stopCooldown();
            mainApp.showCooldownPanel(lastGameEndTime);
        });

        JButton btnMenu = new JButton("🏠 KEMBALI KE MENU");
        styleButton(btnMenu, Theme.BTN_COLOR);
        btnMenu.addActionListener(e -> {
            stopCooldown();
            mainApp.showPanel("MAIN_MENU");
        });

        buttonPanel.add(btnView);
        buttonPanel.add(btnMenu);

        content.add(iconLabel);
        content.add(Box.createRigidArea(new Dimension(0, 15)));
        content.add(titleLabel);
        content.add(Box.createRigidArea(new Dimension(0, 25)));
        content.add(infoLabel);
        content.add(infoLabel2);
        content.add(Box.createRigidArea(new Dimension(0, 30)));
        content.add(timerText);
        content.add(Box.createRigidArea(new Dimension(0, 10)));
        content.add(cooldownTimerLabel);
        content.add(Box.createRigidArea(new Dimension(0, 35)));
        content.add(buttonPanel);

        wrapper.add(content);
        add(wrapper, BorderLayout.CENTER);
    }

    private void styleButton(JButton btn, Color color) {
        btn.setPreferredSize(new Dimension(240, 45));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(color.brighter());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(color);
            }
        });
    }

    public void onPanelShown(long gameEndTime) {
        System.out.println("CooldownPanel");
        this.lastGameEndTime = gameEndTime;

        long elapsed = System.currentTimeMillis() - gameEndTime;

        if (elapsed < 60000) {
            infoLabel.setText("Kamu baru saja menyelesaikan permainan");
        } else if (elapsed < 3600000) {
            int m = (int) (elapsed / 60000);
            infoLabel.setText("Game terakhir dimainkan " + m + " menit yang lalu");
        } else {
            int h = (int) (elapsed / 3600000);
            infoLabel.setText("Game terakhir dimainkan " + h + " jam yang lalu");
        }

        startCooldownTimer();
    }

    private void startCooldownTimer() {
        stopCooldown();

        isRunning = true;

        cooldownThread = new Thread(() -> {
            while (isRunning) {
                long now = System.currentTimeMillis();
                long remaining = COOLDOWN_DURATION - (now - lastGameEndTime);

                if (remaining < 0) remaining = 0;

                int m = (int) (remaining / 60000);
                int s = (int) ((remaining % 60000) / 1000);

                SwingUtilities.invokeLater(() -> cooldownTimerLabel.setText(String.format("%02d:%02d", m, s)));

                if (remaining <= 0) {
                    SwingUtilities.invokeLater(() -> {
                        int choice = JOptionPane.showConfirmDialog(
                                this,
                                "⏰ Cooldown selesai! Kamu bisa bermain lagi.\n\nMulai game baru sekarang?",
                                "Cooldown Selesai",
                                JOptionPane.YES_NO_OPTION
                        );

                        if (choice == JOptionPane.YES_OPTION) {
                            mainApp.showPanel("GAME");
                        } else {
                            mainApp.showPanel("MAIN_MENU");
                        }
                    });
                    break;
                }

                try { Thread.sleep(1000); }
                catch (InterruptedException e) { break; }
            }
        });

        cooldownThread.start();
    }

    public void stopCooldown() {
        isRunning = false;
        if (cooldownThread != null) cooldownThread.interrupt();
    }
}
