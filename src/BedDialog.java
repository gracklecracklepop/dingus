import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BedDialog extends JDialog {

    static final int BED_WIDTH  = 220;
    static final int BED_HEIGHT = 80;

    public BedDialog(JFrame owner) {
        super(owner);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setFocusable(false);
        setFocusableWindowState(false);

        AWTUtilities_setWindowOpaque(this);

        add(new BedPanel());
        pack();
    }

    // ── Positioning ──────────────────────────────────────────────

    /** Position the bed at the bottom-right of the screen. */
    public void positionAtBottom() {
        Point pos = getBedScreenPosition();
        setLocation(pos.x, pos.y+105);
    }

    /**
     * Single source of truth for where the bed sits on screen.
     * Both BedDialog and PetPanel use this so they are always in sync.
     */
    public static Point getBedScreenPosition() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screen.width  - BED_WIDTH  - 70;   // 20px from right edge
        int y = screen.height - BED_HEIGHT - 132;    // 45px from bottom (taskbar)
        return new Point(x, y);
    }

    /**
     * Returns the screen position the pet dialog should snap to
     * so the cat sprite sits correctly aligned over the bed sprite.
     * Adjust snapOffsetX / snapOffsetY to fine-tune the alignment.
     */
    public static Point getCatSnapPosition() {
        Point bedPos = getBedScreenPosition();

        // How many pixels to shift the cat dialog relative to the bed
        // Positive X = right, Positive Y = down
        int snapOffsetX = (BED_WIDTH  - Main.PET_WIDTH)  / 2; // Center cat over bed horizontally
        int snapOffsetY = (BED_HEIGHT - Main.PET_HEIGHT) / 2; // Center cat over bed vertically

        return new Point(bedPos.x + snapOffsetX, bedPos.y + snapOffsetY);
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

    private static void AWTUtilities_setWindowOpaque(Window w) {
        try {
            Class<?> util = Class.forName("com.sun.awt.AWTUtilities");
            util.getMethod("setWindowOpaque", Window.class, boolean.class).invoke(null, w, false);
        } catch (Exception ignored) {}
    }
}