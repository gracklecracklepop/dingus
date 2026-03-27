import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * A transparent, non-interactive window that renders the pet's bed.
 * It is anchored to a fixed screen position and cannot be dragged.
 */
public class BedDialog extends JDialog {

    static final int BED_WIDTH  = 220;
    static final int BED_HEIGHT = 80;

    public BedDialog(JFrame owner) {
        super(owner);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setFocusable(false);
        setFocusableWindowState(false);

        // Let all mouse events fall through to whatever is beneath
        AWTUtilities_setWindowOpaque(this);

        add(new BedPanel());
        pack();
    }

    /** Position the bed so it sits at the bottom-right of the screen, matching the pet. */
    public void positionAtBottom() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screen.width  - Main.PET_WIDTH  + (Main.PET_WIDTH  - BED_WIDTH)  / 2;
        int y = screen.height - BED_HEIGHT - 45;
        setLocation(x, y);
    }

    // ── Inner panel that draws the bed ───────────────────────────

    private static class BedPanel extends JPanel {

        private final BufferedImage bedImage;

        BedPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(BED_WIDTH, BED_HEIGHT));
            bedImage = loadBedImage();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bedImage != null) {
                g.drawImage(bedImage, 0, 0, getWidth(), getHeight(), this);
            }
        }



        private static BufferedImage loadBedImage() {
            try { return ImageIO.read(new File("images/bed.png")); }
            catch (IOException ignored) { return null; }
        }
    }

    /**
     * Makes the window itself click-through on platforms that support it.
     * Falls back silently if unavailable (e.g. some Linux WMs).
     */
    private static void AWTUtilities_setWindowOpaque(Window w) {
        try {
            Class<?> util = Class.forName("com.sun.awt.AWTUtilities");
            util.getMethod("setWindowOpaque", Window.class, boolean.class).invoke(null, w, false);
        } catch (Exception ignored) {
            // Not available on all JVMs — transparency is still handled by the background color
        }
    }
}