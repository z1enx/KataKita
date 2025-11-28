import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class LoginPanel extends JPanel {

    private Main mainApp;
    private JTextField usnField = new JTextField(18);
    private JPasswordField passField = new JPasswordField(18);

    public LoginPanel(Main mainApp) {
        this.mainApp = mainApp;

        setLayout(new GridBagLayout());
        setBackground(Theme.BG_COLOR);

        // PANEL UTAMA
        JPanel container = Theme.createRoundedPanel(25);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(40, 55, 45, 55)); // padding diperhalus

        // TITLE
        JLabel title = new JLabel("KataKita");
        title.setFont(Theme.FONT_TITLE.deriveFont(34f));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // FIELD USERNAME
        Theme.styleInput(usnField);
        usnField.setMaximumSize(new Dimension(300, 42)); // biar rapi & konsisten
        addPlaceholder(usnField, "Masukkan Username");

        // FIELD PASSWORD
        Theme.styleInput(passField);
        passField.setMaximumSize(new Dimension(300, 42));
        passField.setEchoChar((char) 0);
        addPlaceholder(passField, "Masukkan Password");

        // BUTTON LOGIN
        JButton loginButton = new JButton("Login");
        Theme.styleButton(loginButton);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(160, 45));

        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { loginButton.setBackground(Theme.BTN_HOVER); }
            public void mouseExited (java.awt.event.MouseEvent evt) { loginButton.setBackground(Theme.BTN_COLOR); }
        });
        loginButton.addActionListener(e -> processLogin());

        // BUTTON REGISTER
        JButton registerButton = new JButton("Register");
        Theme.styleButton(registerButton);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setMaximumSize(new Dimension(160, 45));

        registerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { registerButton.setBackground(Theme.BTN_HOVER); }
            public void mouseExited (java.awt.event.MouseEvent evt) { registerButton.setBackground(Theme.BTN_COLOR); }
        });
        registerButton.addActionListener(e -> processRegister());


        // === SUSUN TATA LETAK ===
        container.add(title);
        container.add(Box.createRigidArea(new Dimension(0, 35)));

        container.add(usnField);
        container.add(Box.createRigidArea(new Dimension(0, 18)));

        container.add(passField);
        container.add(Box.createRigidArea(new Dimension(0, 28)));

        container.add(loginButton);
        container.add(Box.createRigidArea(new Dimension(0, 14)));

        container.add(registerButton);

        add(container, new GridBagConstraints());
    }


    // ============================
    // PLACEHOLDER LOGIC (TIDAK DIUBAH)
    // ============================
    private void addPlaceholder(JTextComponent field, String text) {
        field.setForeground(new Color(170, 170, 170));
        field.setText(text);

        field.addFocusListener(new FocusAdapter() {
            boolean isPassword = field instanceof JPasswordField;

            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(text)) {
                    field.setText("");
                    field.setForeground(Color.WHITE);

                    if (isPassword) {
                        ((JPasswordField) field).setEchoChar('•');
                    }
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setForeground(new Color(170, 170, 170));
                    field.setText(text);

                    if (isPassword) {
                        ((JPasswordField) field).setEchoChar((char) 0);
                    }
                }
            }
        });
    }


    // ======================================
    // LOGIN LOGIC
    // ======================================
    private void processLogin() {
        DBCon db = new DBCon();
        String username = usnField.getText().trim();
        String password = new String(passField.getPassword());

        if (username.isEmpty() || username.equals("Masukkan Username")
                || password.isEmpty() || password.equals("Masukkan Password")) {

            JOptionPane.showMessageDialog(this, "Isi username dan password terlebih dahulu.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int userId = db.loginPlayer(username, password);

        if (userId > 0) {
            resetField();
            JOptionPane.showMessageDialog(this, "Login berhasil.", "Message", JOptionPane.INFORMATION_MESSAGE);
            mainApp.onLoginSuccess(userId, username);
        } else {
            JOptionPane.showMessageDialog(this, "Username atau password salah.",
                    "Login Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processRegister() {
        DBCon db = new DBCon();
        String username = usnField.getText().trim();
        String password = new String(passField.getPassword());

        if (username.isEmpty() || username.equals("Masukkan Username")
                || password.isEmpty() || password.equals("Masukkan Password")) {

            JOptionPane.showMessageDialog(this, "Isi username dan password untuk registrasi.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean ok = db.registerPlayer(username, password);

        if (ok) {
            JOptionPane.showMessageDialog(this, "Registrasi berhasil! Silakan login.",
                    "Message", JOptionPane.INFORMATION_MESSAGE);
            resetField();
        } else {
            JOptionPane.showMessageDialog(this, "Registrasi gagal. Username sudah digunakan.",
                    "Registrasi Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void resetField() {
        usnField.setText("");
        passField.setText("");
    }
}
