import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class LeaderboardPanel extends JPanel {
    private Main mainApp;
    private JTable leaderboardTable;
    private DefaultTableModel modelTable;

    public LeaderboardPanel(Main mainApp) {
        this.mainApp = mainApp;

        // 1. Setup Panel Utama
        this.setBackground(Theme.BG_COLOR);
        this.setLayout(new BorderLayout(0, 20));
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 2. Title Label
        JLabel titleLabel = new JLabel("🏆 PERINGKAT TERTINGGI 🏆");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.BTN_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(titleLabel, BorderLayout.NORTH);

        // 3. Setup Kolom Tabel
        String[] columnNames = {"Rank", "Username", "Percobaan", "Durasi (dtk)", "Skor Akhir", "Waktu Main"};
        
        modelTable = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // 4. Setup JTable
        leaderboardTable = new JTable(modelTable);
        leaderboardTable.setRowHeight(35);
        leaderboardTable.setFont(Theme.FONT_NORMAL);
        leaderboardTable.setGridColor(Theme.COLOR_BORDER);
        leaderboardTable.setShowGrid(true);
        leaderboardTable.setFillsViewportHeight(true);
        leaderboardTable.setSelectionBackground(Theme.COLOR_PRESENT);
        leaderboardTable.setSelectionForeground(Theme.FG_TEXT);

        // 5. Styling Header Tabel
        JTableHeader header = leaderboardTable.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBackground(Theme.BTN_COLOR);
        header.setForeground(Theme.BTN_TEXT);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.COLOR_BORDER));

        // 6. Custom Renderer
        DefaultTableCellRenderer darkRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Theme.BG_COLOR : Theme.COLOR_ABSENT);
                    c.setForeground(Theme.FG_TEXT);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                
                // Highlight Top 3
                if (column == 0 && !isSelected && value != null && !value.toString().equals("—")) {
                    try {
                        int rank = Integer.parseInt(value.toString());
                        if (rank == 1) {
                            c.setForeground(new Color(255, 215, 0)); // Gold
                        } else if (rank == 2) {
                            c.setForeground(new Color(192, 192, 192)); // Silver
                        } else if (rank == 3) {
                            c.setForeground(new Color(205, 127, 50)); // Bronze
                        }
                    } catch (NumberFormatException ex) {
                        // Ignore if not a number
                    }
                }
                
                return c;
            }
        };

        // Terapkan renderer ke semua kolom
        for (int i = 0; i < leaderboardTable.getColumnCount(); i++) {
            leaderboardTable.getColumnModel().getColumn(i).setCellRenderer(darkRenderer);
        }
        
        // Atur lebar kolom
        leaderboardTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // Rank
        leaderboardTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Username
        leaderboardTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Percobaan
        leaderboardTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Durasi
        leaderboardTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Skor
        leaderboardTable.getColumnModel().getColumn(5).setPreferredWidth(180); // Waktu

        // 7. ScrollPane
        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.COLOR_BORDER));
        scrollPane.getViewport().setBackground(Theme.BG_COLOR);
        this.add(scrollPane, BorderLayout.CENTER);

        // 8. Tombol Kembali
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setBackground(Theme.BG_COLOR);

        JButton backButton = new JButton("« KEMBALI KE MENU");
        Theme.styleButton(backButton); 
        backButton.setBackground(Theme.BTN_COLOR);
        backButton.setForeground(Theme.BTN_TEXT);
        backButton.addActionListener(e -> mainApp.showPanel("MAIN_MENU"));
        
        southPanel.add(backButton);
        this.add(southPanel, BorderLayout.SOUTH);
    }

    public void onPanelShown() {
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<List<Object[]>, Void>() {
            
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                DBCon db = new DBCon(); 
                return db.getLeaderboard(); 
            }

            @Override
            protected void done() {
                try {
                    modelTable.setRowCount(0);
                    
                    List<Object[]> leaderboardData = get();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    
                    if (leaderboardData.isEmpty()) {
                        Object[] emptyRow = {"—", "Belum ada data", "—", "—", "—", "—"};
                        modelTable.addRow(emptyRow);
                    } else {
                        for (Object[] row : leaderboardData) {
                            // row[0] = rank, row[1] = username, row[2] = attempts, 
                            // row[3] = duration, row[4] = score, row[5] = timestamp
                            
                            String formattedDate = "";
                            if (row[5] != null) {
                                formattedDate = sdf.format(row[5]);
                            }
                            
                            Object[] formattedRow = {
                                row[0],                    // Rank
                                row[1],                    // Username
                                row[2] + " kali",          // Total Attempts
                                row[3] + " dtk",           // Duration
                                row[4],                    // Final Score
                                formattedDate              // Waktu Main
                            };
                            modelTable.addRow(formattedRow);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                        LeaderboardPanel.this, 
                        "Gagal memuat data: " + e.getMessage(), 
                        "Error Database", 
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        worker.execute(); 
    }
}