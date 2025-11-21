import javax.swing.*;
import java.awt.*;
import java.util.*;

class ChronoWordLogic extends AbstractGame implements IGameMechanics {
    
    private final String[] WORD_DATABASE = {"RUMAH", "WAKTU", "KABAR", "SIANG", "MALAM", "DUNIA", "HEBAT", "KARYA", "BUKTI", "FOKUS"};
    
    // Polymorphism: Menggunakan tipe data interface/parent
    private GameUIObserver uiObserver;
    
    // Timer logic variables
    public static final int GAME_DURATION_SEC = 120; // 2 Menit
    public static final int COOLDOWN_DURATION_SEC = 300; // 5 Menit
    
    private int remainingGameTime;
    private long nextPlayTimeMillis = 0;

    public ChronoWordLogic(GameUIObserver observer) {
        super(6); // Super constructor
        this.uiObserver = observer;
    }

    @Override
    public void loadNewWord() {
        // Pilih kata acak
        this.targetWord = WORD_DATABASE[new Random().nextInt(WORD_DATABASE.length)];
        System.out.println("Cheat (Target): " + targetWord); // Untuk debug
    }

    @Override
    public void startGame() {
        // Cek Cooldown
        long now = System.currentTimeMillis();
        if (now < nextPlayTimeMillis) {
            uiObserver.showMessage("Sedang Cooldown! Tunggu sebentar.");
            return;
        }

        loadNewWord();
        this.attemptsLeft = maxAttempts;
        this.isGameActive = true;
        this.remainingGameTime = GAME_DURATION_SEC;
        
        uiObserver.onGameStart();
        
        // Start Thread untuk Game Timer
        new Thread(new GameTimerRunnable()).start();
    }

    @Override
    public void submitGuess(String guess) {
        if (!isGameActive) return;
        
        guess = guess.toUpperCase();
        if (guess.length() != 5) {
            uiObserver.showMessage("Kata harus 5 huruf!");
            return;
        }

        // Logika Pengecekan Warna (Wordle Logic)
        Color[] resultColors = new Color[5];
        char[] targetChars = targetWord.toCharArray();
        char[] guessChars = guess.toCharArray();
        boolean[] targetUsed = new boolean[5];
        boolean[] guessUsed = new boolean[5];

        // Initialize default
        Arrays.fill(resultColors, Theme.COLOR_ABSENT);

        // 1. Cek HIJAU (Posisi Tepat)
        for (int i = 0; i < 5; i++) {
            if (guessChars[i] == targetChars[i]) {
                resultColors[i] = Theme.COLOR_CORRECT;
                targetUsed[i] = true;
                guessUsed[i] = true;
            }
        }

        // 2. Cek KUNING (Salah Posisi)
        for (int i = 0; i < 5; i++) {
            if (!guessUsed[i]) {
                for (int j = 0; j < 5; j++) {
                    if (!targetUsed[j] && guessChars[i] == targetChars[j]) {
                        resultColors[i] = Theme.COLOR_PRESENT;
                        targetUsed[j] = true;
                        break;
                    }
                }
            }
        }

        // Update UI Grid
        uiObserver.updateGridRow(6 - attemptsLeft, guess, resultColors);
        attemptsLeft--;

        // Cek Menang
        if (guess.equals(targetWord)) {
            handleWin();
        } else if (attemptsLeft == 0) {
            handleLose("Kesempatan Habis!");
        }
    }

    @Override
    public int calculateScore() {
        // Rumus: (Sisa Waktu * 10) + (Sisa Kesempatan * 50)
        int timeBonus = remainingGameTime * 10;
        int attemptBonus = attemptsLeft * 50;
        return timeBonus + attemptBonus;
    }

    private void handleWin() {
        isGameActive = false;
        int finalScore = calculateScore();
        setScore(finalScore);
        uiObserver.onGameFinished(true, "MENANG! Skor: " + finalScore, targetWord);
        startCooldown();
    }

    private void handleLose(String reason) {
        isGameActive = false;
        setScore(0);
        uiObserver.onGameFinished(false, "KALAH! " + reason, targetWord);
        startCooldown();
    }
    
    private void startCooldown() {
        // Set waktu bisa main lagi 5 menit dari sekarang
        nextPlayTimeMillis = System.currentTimeMillis() + (COOLDOWN_DURATION_SEC * 1000);
        
        // Start Thread untuk Cooldown Timer
        new Thread(new CooldownTimerRunnable()).start();
    }

    // ==========================================
    // 4. MULTITHREADING (Inner Classes)
    // ==========================================
    
    // Thread untuk menghitung waktu main (2 menit)
    private class GameTimerRunnable implements Runnable {
        @Override
        public void run() {
            while (isGameActive && remainingGameTime > 0) {
                try {
                    Thread.sleep(1000);
                    remainingGameTime--;
                    
                    // Update UI harus di Event Dispatch Thread
                    SwingUtilities.invokeLater(() -> uiObserver.updateTimerLabel("Sisa Waktu: " + formatTime(remainingGameTime)));
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            if (isGameActive && remainingGameTime <= 0) {
                SwingUtilities.invokeLater(() -> handleLose("Waktu Habis!"));
            }
        }
    }

    // Thread untuk menghitung waktu cooldown (5 menit)
    private class CooldownTimerRunnable implements Runnable {
        @Override
        public void run() {
            long remainingCooldown = COOLDOWN_DURATION_SEC;
            
            while (remainingCooldown > 0) {
                try {
                    // Hitung sisa waktu real-time
                    long now = System.currentTimeMillis();
                    remainingCooldown = (nextPlayTimeMillis - now) / 1000;
                    
                    final long displayTime = remainingCooldown;
                    
                    SwingUtilities.invokeLater(() -> {
                        uiObserver.updateTimerLabel("Cooldown: " + formatTime((int)displayTime));
                        uiObserver.setInputEnabled(false); // Pastikan input mati
                    });
                    
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            // Cooldown Selesai
            SwingUtilities.invokeLater(() -> {
                uiObserver.updateTimerLabel("SIAP MAIN!");
                uiObserver.setStartButtonEnabled(true);
            });
        }
    }
    
    private String formatTime(int totalSeconds) {
        int m = totalSeconds / 60;
        int s = totalSeconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}