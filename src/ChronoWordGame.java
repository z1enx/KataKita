import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class ChronoWordGame extends JFrame implements GameUIObserver {
    
    private JPanel gridPanel;
    private JTextField inputField;
    private JLabel timerLabel;
    private JLabel statusLabel;
    private JButton startButton;
    private JTextField[][] gridFields; // 6 baris, 5 kolom
    
    private ChronoWordLogic gameLogic;

    public ChronoWordGame() {
        setTitle("ChronoWord - Time Attack Wordle");
        setSize(500, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_COLOR);
        setLayout(new BorderLayout());

        // Inisialisasi Logic
        gameLogic = new ChronoWordLogic(this);

        initUI();
    }

    private void initUI() {
        // --- Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.BG_COLOR);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("CHRONOWORD", SwingConstants.CENTER);
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.FG_TEXT);
        
        timerLabel = new JLabel("Tekan Mulai", SwingConstants.CENTER);
        timerLabel.setFont(Theme.FONT_NORMAL);
        timerLabel.setForeground(Theme.FG_TEXT);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(timerLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // --- Grid Panel ---
        gridPanel = new JPanel(new GridLayout(6, 5, 5, 5));
        gridPanel.setBackground(Theme.BG_COLOR);
        gridPanel.setBorder(new EmptyBorder(10, 50, 10, 50));
        
        gridFields = new JTextField[6][5];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                JTextField box = new JTextField();
                box.setEditable(false);
                box.setFont(Theme.FONT_GRID);
                box.setHorizontalAlignment(JTextField.CENTER);
                box.setBackground(Theme.BG_COLOR);
                box.setForeground(Theme.FG_TEXT);
                box.setBorder(BorderFactory.createLineBorder(Theme.COLOR_BORDER, 2));
                
                gridFields[i][j] = box;
                gridPanel.add(box);
            }
        }
        add(gridPanel, BorderLayout.CENTER);

        // --- Input Panel ---
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBackground(Theme.BG_COLOR);
        inputPanel.setBorder(new EmptyBorder(20, 50, 20, 50));

        inputField = new JTextField();
        Theme.styleTextField(inputField);
        inputField.setEnabled(false);

        startButton = new JButton("MAIN GAME");
        Theme.styleButton(startButton);

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(Color.WHITE);

        JPanel bottomContainer = new JPanel(new GridLayout(2, 1));
        bottomContainer.setBackground(Theme.BG_COLOR);
        bottomContainer.add(inputField);
        bottomContainer.add(startButton);
        
        inputPanel.add(statusLabel, BorderLayout.NORTH);
        inputPanel.add(bottomContainer, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        // --- LISTENERS (Event Handling) ---
        
        // Listener Tombol Start
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameLogic.startGame();
            }
        });

        // Listener Input Field (Enter Key)
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameLogic.submitGuess(inputField.getText());
                inputField.setText(""); // Clear input
            }
        });
        
        // Contoh Adapter (WindowListener) - Opsional
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Terima kasih telah bermain.");
            }
        });
    }

    // --- IMPLEMENTASI OBSERVER (Update UI dari Logic) ---

    @Override
    public void onGameStart() {
        // Reset Grid
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                gridFields[i][j].setText("");
                gridFields[i][j].setBackground(Theme.BG_COLOR);
                gridFields[i][j].setBorder(BorderFactory.createLineBorder(Theme.COLOR_BORDER, 2));
            }
        }
        inputField.setEnabled(true);
        inputField.requestFocus();
        startButton.setEnabled(false);
        statusLabel.setText("Tebak kata 5 huruf!");
    }

    @Override
    public void updateGridRow(int row, String word, Color[] colors) {
        for (int i = 0; i < 5; i++) {
            gridFields[row][i].setText(String.valueOf(word.charAt(i)));
            gridFields[row][i].setBackground(colors[i]);
            gridFields[row][i].setBorder(BorderFactory.createLineBorder(colors[i], 2));
        }
    }

    @Override
    public void updateTimerLabel(String text) {
        timerLabel.setText(text);
    }

    @Override
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    @Override
    public void onGameFinished(boolean win, String message, String answer) {
        inputField.setEnabled(false);
        statusLabel.setText(message);
        
        String title = win ? "YOU WIN!" : "GAME OVER";
        JOptionPane.showMessageDialog(this, 
            message + "\nJawaban: " + answer + "\n(Cooldown 5 Menit dimulai)", 
            title, 
            JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void setInputEnabled(boolean enabled) {
        inputField.setEnabled(enabled);
    }

    @Override
    public void setStartButtonEnabled(boolean enabled) {
        startButton.setEnabled(enabled);
    }

    // MAIN METHOD
    
}