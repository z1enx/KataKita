import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.JTextComponent;

public class Theme {

    public static final Color BG_COLOR = new Color(15, 18, 30);
    public static final Color FG_TEXT = new Color(240, 240, 240);
    public static final Color FG_TEXT_SOFT = new Color(200, 200, 200);


    public static final Color BTN_COLOR = new Color(76, 132, 255);
    public static final Color BTN_TEXT  = Color.WHITE;
    public static final Color BTN_HOVER = new Color(105, 158, 255);

    public static final Color PANEL_COLOR = new Color(28, 32, 48);
    public static final Color PANEL_BORDER = new Color(60, 65, 90);
    public static final Color SHADOW_COLOR = new Color(0, 0, 0, 140);

    public static final Color COLOR_CORRECT = new Color(83,141,78);
    public static final Color COLOR_PRESENT = new Color(76, 132, 255);
    public static final Color COLOR_ABSENT  = new Color(58,58,60);
    public static final Color COLOR_DEFAULT = new Color(18,18,19);
    public static final Color COLOR_BORDER  = new Color(58,58,60);

    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_SUBTITLE= new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_NORMAL  = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_GRID   = new Font("SansSerif", Font.BOLD, 32);
    
    public static void styleButton(JButton btn) {
        btn.setBackground(BTN_COLOR);
        btn.setForeground(BTN_TEXT);
        btn.setFont(FONT_NORMAL);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);

        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(110, 140, 255), 2, true),
                new EmptyBorder(10, 15, 10, 15)
        ));
    }

    public static void styleInput(JTextComponent comp) {
        comp.setBackground(new Color(40, 45, 65));
        comp.setForeground(FG_TEXT);
        comp.setCaretColor(FG_TEXT);
        comp.setFont(FONT_NORMAL);

        comp.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(70, 75, 100), 2, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
    }

    public static JPanel createRoundedPanel(int radius) {
        return new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(SHADOW_COLOR);
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, radius, radius);

                g2.setColor(PANEL_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            
                g2.setColor(PANEL_BORDER);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);

                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
    }
}
