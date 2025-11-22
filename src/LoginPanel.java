import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private Main mainApp;
    private JTextField usnField = new JTextField(15);
    private JPasswordField passField = new JPasswordField(15);

    public LoginPanel(Main mainApp) {
        this.mainApp = mainApp;
        
        JLabel title = new JLabel("KataKita");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.FG_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setMaximumSize(new Dimension(300, 30));
        title.setPreferredSize(new Dimension(300, 30));

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(Theme.FG_TEXT);
        usnField.setMaximumSize(new Dimension(220, 28));
        usnField.setBackground(Theme.BG_COLOR);
        usnField.setForeground(Theme.FG_TEXT);
        usnField.setCaretColor(Theme.BTN_COLOR);
        usnField.setBorder(BorderFactory.createLineBorder(Theme.BTN_COLOR));

        JPanel username = new JPanel();
        username.setOpaque(false);
        username.setLayout(new BoxLayout(username, BoxLayout.X_AXIS));
        username.add(usernameLabel);
        username.add(Box.createRigidArea(new Dimension(8,0)));
        username.add(usnField);
        username.setMaximumSize(new Dimension(300, 20));
        username.setPreferredSize(new Dimension(300, 20));
        
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Theme.FG_TEXT);
        passField.setMaximumSize(new Dimension(220, 28));
        passField.setBackground(Theme.BG_COLOR);
        passField.setForeground(Theme.FG_TEXT);
        passField.setCaretColor(Theme.BTN_COLOR);
        passField.setBorder(BorderFactory.createLineBorder(Theme.BTN_COLOR));
        JPanel password = new JPanel();
        password.setOpaque(false);
        password.setLayout(new BoxLayout(password, BoxLayout.X_AXIS));
        password.add(passwordLabel);
        password.add(Box.createRigidArea(new Dimension(8,0)));
        password.add(passField);
        password.setMaximumSize(new Dimension(300, 20));
        password.setPreferredSize(new Dimension(300, 20));
        
        JButton loginButton = new JButton("Login");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(300, 20));
        loginButton.setPreferredSize(new Dimension(300, 20));
        loginButton.setBackground(Theme.BTN_COLOR);
        loginButton.setForeground(Theme.BTN_TEXT);
        loginButton.setBorderPainted(false);

        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(Theme.BTN_TEXT);
                loginButton.setForeground(Theme.BTN_COLOR); 
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(Theme.BTN_COLOR);
                loginButton.setForeground(Theme.BTN_TEXT);
            }
        });

        JButton registerButton = new JButton("Register");
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setMaximumSize(new Dimension(300, 20));
        registerButton.setPreferredSize(new Dimension(300, 20));
        registerButton.setBackground(Theme.BTN_COLOR);
        registerButton.setForeground(Theme.BTN_TEXT);
        registerButton.setBorderPainted(false);

        registerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                registerButton.setBackground(Theme.BTN_TEXT);
                registerButton.setForeground(Theme.BTN_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                registerButton.setBackground(Theme.BTN_COLOR); 
                registerButton.setForeground(Theme.BTN_TEXT);
            }
        });

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(320, 240));
        setMaximumSize(new Dimension(320, 240));
        setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        setOpaque(false);

        add(title);
        add(Box.createRigidArea(new Dimension(0,12)));
        add(username);
        add(Box.createRigidArea(new Dimension(0,8)));
        add(password);
        add(Box.createRigidArea(new Dimension(0,12)));
        add(loginButton);
        add(Box.createRigidArea(new Dimension(0,8)));
        add(registerButton);

        loginButton.addActionListener(e -> processLogin());
        registerButton.addActionListener(e -> processRegister());
    }

    private void processLogin() {
        DBCon db = new DBCon();
        String username = usnField.getText().trim();
        String password = new String(passField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi username dan password terlebih dahulu.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int userId = db.loginPlayer(username, password);
        if (userId > 0) {
            resetField();
            JOptionPane.showMessageDialog(this, "Login berhasil.", "Message", JOptionPane.INFORMATION_MESSAGE);
            mainApp.onLoginSuccess(userId, username);
        } else {
            JOptionPane.showMessageDialog(this, "Username atau password salah.", "Login Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processRegister() {
        DBCon db = new DBCon();
        String username = usnField.getText().trim();
        String password = new String(passField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi username dan password untuk registrasi.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean ok = db.registerPlayer(username, password);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Registrasi berhasil! Silakan login.", "Message", JOptionPane.INFORMATION_MESSAGE);
            resetField();
        } else {
            JOptionPane.showMessageDialog(this, "Registrasi gagal. Username mungkin sudah digunakan.", "Registrasi Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void resetField() {
        usnField.setText("");
        passField.setText("");
    }
}
