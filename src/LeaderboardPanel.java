import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class LeaderboardPanel extends JPanel {
    private Main mainApp; // Sesuaikan dengan nama class Main Frame kamu
    private JTable leaderboardTable;
    private DefaultTableModel modelTable;

    public LeaderboardPanel(Main mainApp) {
        this.mainApp = mainApp;

        // 1. Setup Panel Utama
        this.setBackground(Theme.BG_COLOR);
        this.setLayout(new BorderLayout(0, 20));
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 2. Title Label
        JLabel titleLabel = new JLabel("PERINGKAT TERTINGGI");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.BTN_COLOR); // Warna Aksen
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(titleLabel, BorderLayout.NORTH);

        // 3. Setup Kolom Tabel
        String[] columnNames = {"Rank", "Username", "Percobaan", "Durasi (dtk)", "Skor Akhir", "Waktu Main"};
        
        modelTable = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabel tidak bisa diedit
            }
        };

        // 4. Setup JTable
        leaderboardTable = new JTable(modelTable);
        leaderboardTable.setRowHeight(35); // Baris lebih tinggi agar rapi
        leaderboardTable.setFont(Theme.FONT_NORMAL);
        leaderboardTable.setGridColor(Theme.COLOR_BORDER);
        leaderboardTable.setShowGrid(true);
        leaderboardTable.setFillsViewportHeight(true);
        leaderboardTable.setSelectionBackground(Theme.COLOR_PRESENT); // Warna saat baris dipilih
        leaderboardTable.setSelectionForeground(Theme.FG_TEXT);

        // 5. Styling Header Tabel
        JTableHeader header = leaderboardTable.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBackground(Theme.BTN_COLOR); // Header warna oranye/aksen
        header.setForeground(Theme.BTN_TEXT);  // Teks header gelap
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.COLOR_BORDER));

        // 6. Custom Renderer (Agar cell tabel backgroundnya gelap sesuai tema)
        DefaultTableCellRenderer darkRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    // Warna belang-belang (Zebra striping) agar mudah dibaca
                    c.setBackground(row % 2 == 0 ? Theme.BG_COLOR : Theme.COLOR_ABSENT);
                    c.setForeground(Theme.FG_TEXT);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };

        // Terapkan renderer ke semua kolom
        for (int i = 0; i < leaderboardTable.getColumnCount(); i++) {
            leaderboardTable.getColumnModel().getColumn(i).setCellRenderer(darkRenderer);
        }
        
        // Atur lebar kolom (Opsional, biar rapi)
        leaderboardTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // Rank
        leaderboardTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Username
        leaderboardTable.getColumnModel().getColumn(5).setPreferredWidth(150); // Waktu

        // 7. ScrollPane
        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.COLOR_BORDER));
        scrollPane.getViewport().setBackground(Theme.BG_COLOR);
        this.add(scrollPane, BorderLayout.CENTER);

        // 8. Tombol Kembali
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setBackground(Theme.BG_COLOR); // Pastikan panel tombol backgroundnya gelap

        JButton backButton = new JButton("« KEMBALI KE MENU");
        
        // Menggunakan style dari Theme tapi override warna agar sesuai aksen leaderboard
        Theme.styleButton(backButton); 
        backButton.setBackground(Theme.BTN_COLOR);
        backButton.setForeground(Theme.BTN_TEXT);
        
        backButton.addActionListener(e -> mainApp.showPanel("MAIN_MENU"));
        
        southPanel.add(backButton);
        this.add(southPanel, BorderLayout.SOUTH);
    }

    // Method untuk memuat data saat panel ditampilkan
    public void onPanelShown() {
        // Multithreading menggunakan SwingWorker
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<List<Object[]>, Void>() {
            
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                DBCon db = new DBCon(); 
                // Pastikan query di DBCon mengambil kolom urut: 
                // username, total_attempts, duration_seconds, final_score, created_at
                return db.getLeaderboard(); 
            }

            @Override
            protected void done() {
                try {
                    modelTable.setRowCount(0); // Bersihkan data lama
                    
                    List<Object[]> leaderboardData = get();
                    
                    for (Object[] row : leaderboardData) {
                        // Mapping data dari Database ke Tabel
                        // Asumsi urutan dari query: [0]Username, [1]Attempts, [2]Duration, [3]Score, [4]Date
                        
                        Object[] formattedRow = {
                            row[0],                                         // Username
                            row[1],                                         // Total Attempts
                            row[2],                                // Duration
                            row[3] + " dtk",                                         // Final Score
                            row[4],                                         // Waktu Main
                            row[5]
                        };
                        modelTable.addRow(formattedRow);
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