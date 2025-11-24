import java.sql.*;
import java.util.*;

public class DBCon {
    private String dbName = "katakita";
    private String dbUrl = "jdbc:mysql://127.0.0.1:3306/" + dbName;
    private String usn = "root";
    private String pass = "";

    public Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // String url = dbUrl + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            String url = dbUrl + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Makassar";
            Connection con = DriverManager.getConnection(url, usn, pass);
            return con;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void initialize() {
        String checkWord = "SELECT COUNT(*) FROM word";
        String insertWord = "INSERT INTO word (word_text) VALUES " +
                            "('MAKAN'), ('MANDI'), ('KABAR'), ('PILOT'), ('TULIS')," +
                            "('JURUS'), ('TUJUH'), ('HUJAN'), ('SANDI'), ('TIDUR')," +
                            "('TIDAK'), ('SEMUA'), ('AUDIO'), ('ANTAR'), ('BELUM')";

        try (Connection con = getConnection();
            Statement st = con.createStatement()) {

            try (ResultSet rs = st.executeQuery(checkWord)) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    if (count == 0) {
                        st.executeUpdate(insertWord);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean registerPlayer(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return false;
        }
        String sql = "INSERT INTO player(username, password) VALUES(?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int loginPlayer(String username, String password) {
        if (username == null || username.isBlank() || password == null) return -1;
        String sql = "SELECT player_id FROM player WHERE username = ? AND password = ? LIMIT 1";
        try (Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("player_id");
                } else {
                    return -1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean saveResult(int playerId, int wordId, int durations, int totalAttempts, int finalScore) {

        String sql = "INSERT INTO gamesession (player_id, word_id, start_time, duration_seconds, total_attempts, final_score, end_time) VALUES(?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            // Hitung waktu start dan end di Java
            long endTimeMillis = System.currentTimeMillis();
            long startTimeMillis = endTimeMillis - (durations * 1000); // Mundurkan waktu sesuai durasi

            ps.setInt(1, playerId);
            ps.setInt(2, wordId);
            
            // --- BAGIAN PENTING: Konversi long ke Timestamp untuk DATETIME ---
            ps.setTimestamp(3, new java.sql.Timestamp(startTimeMillis)); // start_time
            // -----------------------------------------------------------------
            
            ps.setInt(4, durations);
            ps.setInt(5, totalAttempts);
            ps.setInt(6, finalScore);
            
            // --- BAGIAN PENTING: Konversi long ke Timestamp untuk DATETIME ---
            ps.setTimestamp(7, new java.sql.Timestamp(endTimeMillis));   // end_time

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public Map<String, Integer> getSoal() {
        Map<String, Integer> wordMap = new HashMap<>();
        String sql = "SELECT word_id, word_text FROM word WHERE is_active = 1";
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                wordMap.put(rs.getString("word_text"), rs.getInt("word_id"));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wordMap;
    }

    public boolean wordExist(String wordSearch) {
        // Cek jika input kosong
        if (wordSearch == null || wordSearch.trim().isEmpty()) {
            return false;
        }

        // Menggunakan SELECT COUNT agar lebih ringan daripada mengambil datanya
        String sql = "SELECT COUNT(*) FROM word WHERE word_text = ?";

        try (Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            // Set parameter ? dengan kata yang dicari (dijadikan uppercase agar konsisten)
            ps.setString(1, wordSearch.toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Jika hasil hitungan > 0, berarti kata ada
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // Jika terjadi error atau kata tidak ditemukan
    }

    // PERUBAHAN: Leaderboard hanya menampilkan HIGHEST SCORE per pemain
    public List<Object[]> getLeaderboard() {
        List<Object[]> leaderboard = new ArrayList<>();
        
        // Query untuk mendapatkan score tertinggi per pemain
        String sql = "SELECT p.username, " +
                     "       MAX(gs.final_score) as highest_score, " +
                     "       (SELECT gs2.total_attempts FROM gamesession gs2 " +
                     "        WHERE gs2.player_id = p.player_id AND gs2.final_score = MAX(gs.final_score) " +
                     "        ORDER BY gs2.start_time DESC LIMIT 1) as attempts, " +
                     "       (SELECT gs2.duration_seconds FROM gamesession gs2 " +
                     "        WHERE gs2.player_id = p.player_id AND gs2.final_score = MAX(gs.final_score) " +
                     "        ORDER BY gs2.start_time DESC LIMIT 1) as duration, " +
                     "       (SELECT gs2.start_time FROM gamesession gs2 " +
                     "        WHERE gs2.player_id = p.player_id AND gs2.final_score = MAX(gs.final_score) " +
                     "        ORDER BY gs2.start_time DESC LIMIT 1) as play_time " +
                     "FROM gamesession gs " +
                     "JOIN player p ON gs.player_id = p.player_id " +
                     "GROUP BY p.player_id, p.username " +
                     "ORDER BY highest_score DESC, duration ASC " +
                     "LIMIT 10";

        try (Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            
            int peringkat = 1;
            while (rs.next()) {
                leaderboard.add(new Object[]{
                    peringkat,
                    rs.getString("username"),
                    rs.getInt("attempts"),
                    rs.getInt("duration"),
                    rs.getInt("highest_score"),
                    rs.getTimestamp("play_time")
                });
                peringkat++;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return leaderboard;
    }

    // Method baru: Cek apakah player masih dalam cooldown
    public long getLastGameTime(int playerId) {
    String sql = "SELECT MAX(end_time) as last_time FROM gamesession WHERE player_id = ?";
        try (Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    java.sql.Timestamp ts = rs.getTimestamp("last_time");
                    if(ts != null) {
                        return ts.getTime();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


     // Method: Ambil hasil game terakhir
    public Object[] getLastGameResult(int playerId) {
        String sql = "SELECT w.word_text, gs.duration_seconds, gs.total_attempts, gs.final_score, gs.start_time " +
                     "FROM gamesession gs " +
                     "JOIN word w ON gs.word_id = w.word_id " +
                     "WHERE gs.player_id = ? " +
                     "ORDER BY gs.start_time DESC LIMIT 1";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getString("word_text"),
                        rs.getInt("duration_seconds"),
                        rs.getInt("total_attempts"),
                        rs.getInt("final_score"),
                        rs.getTimestamp("start_time")
                    };
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}