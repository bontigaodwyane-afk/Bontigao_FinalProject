package View;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        // Cross-platform look so the button and header colors show correctly
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // the default look is fine too
        }
        SwingUtilities.invokeLater(() -> new MainForm().setVisible(true));
    }
}