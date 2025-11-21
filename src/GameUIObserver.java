import java.awt.*;

interface GameUIObserver {
    void updateGridRow(int row, String word, Color[] colors);
    void updateTimerLabel(String text);
    void showMessage(String message);
    void onGameStart();
    void onGameFinished(boolean win, String message, String answer);
    void setInputEnabled(boolean enabled);
    void setStartButtonEnabled(boolean enabled);
}