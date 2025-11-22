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
            Connection con =  DriverManager.getConnection(url, usn, pass);
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
        String insertWord = "INSERT INTO word (word_text) VALUES "+ 
                            "('MAKAN'), ('MANDI'), ('KABAR'), ('PILOT'), ('TULIS')" +
                            "('JURUS'), ('TUJUH'), ('HUJAN'), ('SANDI'), ('TIDUR')" +
                            "('TIDAK'), ('SEMUA'), ('AUDIO'), ('ANTAR'), ('BELUM')" +
                            " ;";

        try (Connection con = getConnection();
            java.sql.Statement st = con.createStatement()) {

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
        String sql = "INSERT INTO player(username, password) VALUES(?, ?);";
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
        String sql = "SELECT player_id FROM player WHERE username = ? AND password = ? LIMIT 1;";
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
        String sql = "INSERT INTO gamesession (player_id, word_id, duration_seconds, total_attempts, final_score) VALUES(?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, playerId);
            ps.setInt(2, wordId);
            ps.setInt(3, durations);
            ps.setInt(4, totalAttempts);
            ps.setInt(5, finalScore);
            
            ps.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getSoal() {
        List<String> daftarKata = new ArrayList<>();
        String sql = "SELECT word_text FROM word";
        
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                daftarKata.add(rs.getString("word_text"));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return daftarKata;
    }

    public List<Object[]> getLeaderboard() {
        List<Object[]> leaderboard = new ArrayList<>();
        String sql = "SELECT p.username, gs.total_attempts, gs.duration_seconds, gs.final_score, gs.time " +
                     "FROM gamesession gs " +
                     "JOIN player p ON gs.player_id = p.player_id " +
                     "ORDER BY gs.final_score DESC, gs.duration_seconds ASC " +
                     "LIMIT 10;"; 

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
                    rs.getTimestamp("time")
                });
                peringkat++;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return leaderboard;
    }
}
