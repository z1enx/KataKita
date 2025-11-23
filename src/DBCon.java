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
            String url = dbUrl + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
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

    public boolean saveResult(int playerId, int wordId, int durations, int totalAttempts, int finalScore, String status) {

        String sql = "INSERT INTO gamesession (player_id, word_id, start_time, duration_seconds, total_attempts, final_score, status, end_time) VALUES(?, ?, NOW(), ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, playerId);
            ps.setInt(2, wordId);
            ps.setInt(3, durations);
            ps.setInt(4, totalAttempts);
            ps.setInt(5, finalScore);
            ps.setString(6, status);
            ps.setLong(7, System.currentTimeMillis());   // <- waktu yang benar

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

    public List<Object[]> getLeaderboard() {
        List<Object[]> leaderboard = new ArrayList<>();
        String sql = "SELECT p.username, gs.total_attempts, gs.duration_seconds, gs.final_score, gs.start_time " +
                     "FROM gamesession gs " +
                     "JOIN player p ON gs.player_id = p.player_id " +
                     "ORDER BY gs.final_score DESC, gs.duration_seconds ASC " +
                     "LIMIT 10"; 

        try (Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            
            int peringkat = 1;
            while (rs.next()) {
                leaderboard.add(new Object[]{
                    peringkat,
                    rs.getString("username"),
                    rs.getInt("total_attempts"),
                    rs.getInt("duration_seconds"),
                    rs.getInt("final_score"),
                    rs.getTimestamp("start_time")
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
                    return rs.getLong("last_time");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    // Method baru: Ambil hasil game terakhir
    public Object[] getLastGameResult(int playerId) {
        String sql = "SELECT w.word_text, gs.duration_seconds, gs.total_attempts, gs.final_score, gs.status, gs.start_time " +
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
                        rs.getString("status"),
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