import java.awt.*;
import javax.swing.*;

class Theme {
    // Encapsulation: Fields are private, accessed via methods/constants
    public static final Color BG_COLOR = new Color(18, 18, 19);
    public static final Color FG_TEXT = new Color(255, 255, 255);
    public static final Color BTN_COLOR = new Color(235, 179, 89);
    public static final Color BTN_TEXT = new Color(31, 31, 30);
    
    // Warna Status (Wordle Style)
    public static final Color COLOR_CORRECT = new Color(83, 141, 78);   // Hijau
    public static final Color COLOR_PRESENT = new Color(181, 159, 59);  // Kuning
    public static final Color COLOR_ABSENT = new Color(58, 58, 60);     // Abu-abu
    public static final Color COLOR_DEFAULT = new Color(18, 18, 19);    // Kosong
    public static final Color COLOR_BORDER = new Color(58, 58, 60);

    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 24);
    public static final Font FONT_GRID = new Font("SansSerif", Font.BOLD, 32);
    public static final Font FONT_NORMAL = new Font("SansSerif", Font.PLAIN, 14);

    // Method utility untuk styling komponen
    public static void styleButton(JButton btn) {
        btn.setBackground(COLOR_PRESENT);
        btn.setForeground(FG_TEXT);
        btn.setFont(FONT_NORMAL);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    }
    
    public static void styleTextField(JTextField tf) {
        tf.setBackground(COLOR_ABSENT);
        tf.setForeground(FG_TEXT);
        tf.setFont(FONT_TITLE);
        tf.setCaretColor(FG_TEXT);
        tf.setHorizontalAlignment(JTextField.CENTER);
    }
}