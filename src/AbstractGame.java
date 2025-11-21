abstract class AbstractGame {
    protected String targetWord;
    protected int attemptsLeft;
    protected int maxAttempts;
    protected boolean isGameActive;
    
    // Encapsulation
    private int score;

    public AbstractGame(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        this.attemptsLeft = maxAttempts;
        this.score = 0;
    }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
    // Abstract method yang harus diimplementasikan anak kelas
    public abstract void loadNewWord();
}