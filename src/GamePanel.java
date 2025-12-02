import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;

public class GamePanel extends JPanel {
    
    private Main mainApp;
    
    private JLabel timerLabel;
    private JTextField[][] gridFields;
    private JButton[] letterButtons = new JButton[26]; 
    private JButton btnEnter;
    private JButton btnBack;
    
    private JPanel gameContentPanel;
    
    private final int MAX_ATTEMPTS = 6;
    private final int WORD_LENGTH = 5;
    private final int GAME_DURATION = 120; 
    private final long COOLDOWN_DURATION = 5 * 60 * 1000;
    
    private String targetWord;
    private int targetWordId;
    private int currentAttempt;
    private int currentLetterVal; 
    private int remainingTime;
    
    private volatile boolean isGameActive; 
    private Thread gameThread;
    
    private StringBuilder currentGuess;

    private Clip musicClip;

    public GamePanel(Main mainApp) {
        this.mainApp = mainApp;
        this.setLayout(new BorderLayout());
        this.setBackground(Theme.BG_COLOR);
        
        initGameUI();
        
        this.setFocusable(true);
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!isGameActive) return;
                
                int code = e.getKeyCode();
                char keyChar = e.getKeyChar();

                if (code == KeyEvent.VK_ENTER) {
                    handleInput("ENTER");
                } else if (code == KeyEvent.VK_BACK_SPACE) {
                    handleInput("BACK");
                } else if (Character.isLetter(keyChar)) {
                    handleInput(String.valueOf(keyChar).toUpperCase());
                }
            }
        });
    }

    private void initGameUI() {
        gameContentPanel = new JPanel(new BorderLayout());
        gameContentPanel.setBackground(Theme.BG_COLOR);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.BG_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JButton btnBackMenu = new JButton("« MENU");
        Theme.styleButton(btnBackMenu);
        btnBackMenu.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnBackMenu.setPreferredSize(new Dimension(80, 30));
        btnBackMenu.addActionListener(e -> stopGameAndReturn());

        timerLabel = new JLabel("02:00", SwingConstants.CENTER);
        timerLabel.setFont(Theme.FONT_TITLE);
        timerLabel.setForeground(Theme.FG_TEXT);

        JLabel dummy = new JLabel(); 
        dummy.setPreferredSize(new Dimension(80, 30));

        headerPanel.add(btnBackMenu, BorderLayout.WEST);
        headerPanel.add(timerLabel, BorderLayout.CENTER);
        headerPanel.add(dummy, BorderLayout.EAST);
        
        gameContentPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel centerContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerContainer.setBackground(Theme.BG_COLOR);
        
        JPanel gridPanel = new JPanel(new GridLayout(MAX_ATTEMPTS, WORD_LENGTH, 5, 5));
        gridPanel.setBackground(Theme.BG_COLOR);
        gridPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        gridFields = new JTextField[MAX_ATTEMPTS][WORD_LENGTH];
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            for (int j = 0; j < WORD_LENGTH; j++) {
                JTextField box = new JTextField();
                box.setPreferredSize(new Dimension(50, 50));
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
        centerContainer.add(gridPanel);
        gameContentPanel.add(centerContainer, BorderLayout.CENTER);

        JPanel keyboardPanel = new JPanel();
        keyboardPanel.setLayout(new BoxLayout(keyboardPanel, BoxLayout.Y_AXIS));
        keyboardPanel.setBackground(Theme.BG_COLOR);
        keyboardPanel.setBorder(new EmptyBorder(10, 10, 20, 10));

        String[] row1 = {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"};
        String[] row2 = {"A", "S", "D", "F", "G", "H", "J", "K", "L"};
        String[] row3 = {"ENTER", "Z", "X", "C", "V", "B", "N", "M", "BACK"};

        keyboardPanel.add(createKeyboardRow(row1));
        keyboardPanel.add(Box.createVerticalStrut(5));
        keyboardPanel.add(createKeyboardRow(row2));
        keyboardPanel.add(Box.createVerticalStrut(5));
        keyboardPanel.add(createKeyboardRow(row3));

        gameContentPanel.add(keyboardPanel, BorderLayout.SOUTH);
        
        add(gameContentPanel, BorderLayout.CENTER);
    }

    private JPanel createKeyboardRow(String[] keys) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        row.setBackground(Theme.BG_COLOR);
        
        for (String key : keys) {
            JButton btn = new JButton(key.equals("BACK") ? "⌫" : key);
            btn.setFont(new Font("SansSerif", Font.BOLD, 14));
            btn.setForeground(Theme.FG_TEXT);
            btn.setBackground(Theme.COLOR_ABSENT); 
            btn.setFocusable(false); 
            btn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            
            if (key.equals("ENTER") || key.equals("BACK")) {
                btn.setPreferredSize(new Dimension(70, 45));
            } else {
                btn.setPreferredSize(new Dimension(45, 45));
            }

            btn.addActionListener(e -> {
                handleInput(key);
                this.requestFocusInWindow();
            });

            if (key.equals("ENTER")) {
                btnEnter = btn;
            } else if (key.equals("BACK")) {
                btnBack = btn;
            } else {
                char c = key.charAt(0);
                int index = c - 'A'; 
                if (index >= 0 && index < 26) {
                    letterButtons[index] = btn;
                }
            }
            
            row.add(btn);
        }
        return row;
    }

    public void onPanelShown() {
        stopCurrentGame();
        
        DBCon db = new DBCon();
        long lastGameTime = db.getLastGameTime(mainApp.getCurrentUserId());
        
        if (lastGameTime > 0) {
            long timeSinceLastGame = System.currentTimeMillis() - lastGameTime;
            
            if (timeSinceLastGame < COOLDOWN_DURATION) {
                final long gameTime = lastGameTime;
                SwingUtilities.invokeLater(() -> {
                    mainApp.showEndGamePanel(gameTime);
                });
                return;
            }
        }
        
        resetGame();
        startTimerThread(); 
        playMusic();
        
        SwingUtilities.invokeLater(() -> {
            this.requestFocusInWindow();
        });
    }
    
    private void stopCurrentGame() {
        isGameActive = false;
        if (gameThread != null && gameThread.isAlive()) {
            gameThread.interrupt();
            try {
                gameThread.join(500); 
            } catch (InterruptedException e) {

            }
        }
        stopMusic();
    }
    
    private void resetGame() {
        currentAttempt = 0;
        currentLetterVal = 0;
        currentGuess = new StringBuilder();
        remainingTime = GAME_DURATION;
        isGameActive = true;
        
        DBCon db = new DBCon();
        Map<String, Integer> words = db.getSoal();
        
        if (words.isEmpty()) {
            words.put("HEBAT", 1); 
        }
        
        ArrayList<String> wordList = new ArrayList<>(words.keySet());
        targetWord = wordList.get(new Random().nextInt(wordList.size())).toUpperCase();
        targetWordId = words.get(targetWord);
        
        System.out.println("Debug - Target Word: " + targetWord + " (ID: " + targetWordId + ")");

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            for (int j = 0; j < WORD_LENGTH; j++) {
                gridFields[i][j].setText("");
                gridFields[i][j].setBackground(Theme.BG_COLOR);
                gridFields[i][j].setBorder(BorderFactory.createLineBorder(Theme.COLOR_BORDER, 2));
            }
        }

        for (int i = 0; i < 26; i++) {
            if (letterButtons[i] != null) {
                letterButtons[i].setBackground(Theme.COLOR_ABSENT);
                letterButtons[i].setForeground(Theme.FG_TEXT);
            }
        }
        if (btnEnter != null) btnEnter.setBackground(Theme.COLOR_ABSENT);
        if (btnBack != null) btnBack.setBackground(Theme.COLOR_ABSENT);
        
        timerLabel.setText("02:00");
        timerLabel.setForeground(Theme.FG_TEXT);
    }

    private void handleInput(String key) {
        if (!isGameActive) return;

        if (key.equals("ENTER")) {
            if (currentGuess.length() == WORD_LENGTH) {
                DBCon db = new DBCon();
                if(db.wordExist(currentGuess.toString())) {
                    checkGuess();
                } else {
                    JOptionPane.showMessageDialog(this, "Kata harus ada di KBBI!", "Info", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Kata harus 5 huruf!", "Info", JOptionPane.WARNING_MESSAGE);
            }
            
        } else if (key.equals("BACK")) {
            if (currentGuess.length() > 0) {
                currentGuess.setLength(currentGuess.length() - 1);
                currentLetterVal--;
                gridFields[currentAttempt][currentLetterVal].setText("");
            }
        } else {
            if (currentGuess.length() < WORD_LENGTH) {
                currentGuess.append(key);
                gridFields[currentAttempt][currentLetterVal].setText(key);
                currentLetterVal++;
            }
        }
    }

    private void checkGuess() {
        String guess = currentGuess.toString();
        
        Color[] resultColors = new Color[WORD_LENGTH];
        char[] targetChars = targetWord.toCharArray();
        char[] guessChars = guess.toCharArray();
        boolean[] isUsed = new boolean[WORD_LENGTH]; 

        for (int i = 0; i < WORD_LENGTH; i++) {
            if (resultColors[i] == null) { 
                boolean found = false;
                for (int j = 0; j < WORD_LENGTH; j++) {
                    if (!isUsed[j] && guessChars[i] == targetChars[j]) {
                        resultColors[i] = Theme.COLOR_PRESENT;
                        isUsed[j] = true;
                        found = true;
                        updateKeyColor(String.valueOf(guessChars[i]), Theme.COLOR_PRESENT);
                        break;
                    }
                }
                if (!found) {
                    resultColors[i] = Theme.COLOR_ABSENT; 
                    updateKeyColor(String.valueOf(guessChars[i]), Theme.COLOR_DEFAULT); 
                }
            }
            if (guessChars[i] == targetChars[i]) {
                resultColors[i] = Theme.COLOR_CORRECT;
                isUsed[i] = true;
                updateKeyColor(String.valueOf(guessChars[i]), Theme.COLOR_CORRECT);
            }
        }

        for (int i = 0; i < WORD_LENGTH; i++) {
            gridFields[currentAttempt][i].setBackground(resultColors[i]);
            gridFields[currentAttempt][i].setBorder(BorderFactory.createLineBorder(resultColors[i], 2));
        }

        if (guess.equals(targetWord)) {
            endGame(true);
        } else {
            currentAttempt++;
            currentLetterVal = 0;
            currentGuess.setLength(0); 

            if (currentAttempt >= MAX_ATTEMPTS) {
                endGame(false);
            }
        }
    }

    private void updateKeyColor(String key, Color newColor) {
        JButton btn = null;

        if (key.equals("ENTER")) {
            btn = btnEnter;
        } else if (key.equals("BACK")) {
            btn = btnBack;
        } else if (key.length() == 1) {
            char c = key.charAt(0);
            int index = c - 'A';
            if (index >= 0 && index < 26) {                    
                btn = letterButtons[index];
            }
        }

        if (btn != null) {
            Color currentColor = btn.getBackground();
            
            if (currentColor.equals(Theme.COLOR_CORRECT)) return; 
            if (currentColor.equals(Theme.COLOR_PRESENT) && !newColor.equals(Theme.COLOR_CORRECT)) return;
            
            if (newColor.equals(Theme.COLOR_DEFAULT)) {
                btn.setBackground(new Color(30, 30, 30)); 
                btn.setForeground(Color.GRAY);
            } else {
                btn.setBackground(newColor);
                btn.setForeground(Color.WHITE);
            }
        }
    }

    private void startTimerThread() {
        if (gameThread != null && gameThread.isAlive()) {
            isGameActive = false; 
            try {
                gameThread.join(); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        isGameActive = true;

        gameThread = new Thread(() -> {
            while (isGameActive && remainingTime > 0) {
                try {
                    Thread.sleep(1000); 
                } catch (InterruptedException e) {
                    break;
                }

                remainingTime--;
                int min = remainingTime / 60;
                int sec = remainingTime % 60;

                SwingUtilities.invokeLater(() -> {
                    timerLabel.setText(String.format("%02d:%02d", min, sec));
                    timerLabel.setForeground(Theme.FG_TEXT);
                });

                if (remainingTime <= 0) {
                    SwingUtilities.invokeLater(() -> endGame(false));
                }
            }
        });

        gameThread.start(); 
    }

    private void playMusic() {
        try {
            File musicFile = new File("assets/backburner.wav");
            if (musicFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
                musicClip = AudioSystem.getClip();
                musicClip.open(audioStream);
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
                musicClip.start();
            }
        } catch (Exception e) {
            System.err.println("Error playing music: " + e.getMessage());
        }
    }

    private void stopMusic() {
        if (musicClip != null) {
            if (musicClip.isRunning()) {
                musicClip.stop();
            }
            musicClip.close();
            musicClip = null;
        }
    }

    private void endGame(boolean isWin) {
        isGameActive = false; 
        if (gameThread != null) {
            gameThread.interrupt(); 
        }
        stopMusic(); 

        int score = 0;
        int actualAttempts = currentAttempt + (isWin ? 1 : 0);
        
        if (isWin) {
            int attemptsLeft = MAX_ATTEMPTS - currentAttempt; 
            score = (remainingTime * 10) + (attemptsLeft * 50);
        }

        int playerId = mainApp.getCurrentUserId();
        int gameDuration = GAME_DURATION - remainingTime;

        String msg = isWin ? 
            "🎉 SELAMAT! Kamu Menang! 🎉\n\nSkor: " + score + "\nWaktu: " + gameDuration + " detik\nPercobaan: " + actualAttempts + " kali" : 
            "😢 YAHHH KALAH!\n\nJawaban: " + targetWord + "\nWaktu bermain: " + gameDuration + " detik\nPercobaan: " + actualAttempts + " kali";
        
        JOptionPane.showMessageDialog(this, 
            msg + "\n\n⏰ Kamu bisa bermain lagi dalam 5 menit.", 
            isWin ? "KAMU MENANG!" : "GAME OVER", 
            isWin ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

        DBCon db = new DBCon();
        
        boolean saved = db.saveResult(playerId, targetWordId, gameDuration, actualAttempts, score);
        
        if (!saved) {
            System.err.println("Gagal menyimpan hasil game ke database!");
        }
        
        long lastGameTime = db.getLastGameTime(playerId);
        mainApp.showCooldownPanel(lastGameTime);
    }

    private void stopGameAndReturn() {
        isGameActive = false;
        if (gameThread != null) {
            gameThread.interrupt();
        }
        stopMusic();
        mainApp.showPanel("MAIN_MENU");
    }
}