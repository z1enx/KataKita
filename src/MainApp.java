import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        // Menjalankan GUI di Event Dispatch Thread (Thread Safety)
        SwingUtilities.invokeLater(() -> {
            new ChronoWordGame().setVisible(true);
        });
    }
}
