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

    public void positionAtBottom() {
        setLocation(getBedScreenPosition());
    }

    /**
     * Single source of truth for where the bed sits on screen.
     * Uses the usable screen area which already excludes the taskbar.
     * Do NOT subtract insets again.
     */
    public static Point getBedScreenPosition() {
        Rectangle usable = Theme.getUsableScreen();

        int xOffset = 0;
        int yOffset = 0;

        switch (Theme.getScalePercent()) {
            case 100 -> {
                xOffset = -80;
                yOffset = +15;
            }
            case 125 -> {
                xOffset = -88;
                yOffset = -14;
            }
            case 150 -> {
                xOffset = -105;
                yOffset = -18;
            }
            case 175 -> {
                xOffset = -122;
                yOffset = -22;
            }
            default -> {
                xOffset = -70;
                yOffset = -10;
            }
        }

        int x = usable.x + usable.width - BED_WIDTH + xOffset;
        int y = usable.y + usable.height - BED_HEIGHT + yOffset;

        return new Point(x, y);
    }
    /**
     * Returns the screen position the pet dialog should snap to
     * so the cat sprite sits correctly aligned over the bed sprite.
     * Adjust the offsets to fine-tune alignment.
     */
    public static Point getCatSnapPosition() {
        Point bedPos = getBedScreenPosition();

        int snapOffsetX = (BED_WIDTH - Main.PET_WIDTH) / 2;
        int snapOffsetY = -Main.PET_HEIGHT + BED_HEIGHT;

        int extraX = 0;
        int extraY = 0;

        switch (Theme.getScalePercent()) {
            case 100 -> {
                extraX = 0;
                extraY = 5;
            }
            case 125 -> {
                extraX = -6;
                extraY = -119;
            }
            case 150 -> {
                extraX = -8;
                extraY = -142;
            }
            case 175 -> {
                extraX = -10;
                extraY = -166;
            }
            default -> {
                extraX = -5;
                extraY = -95;
            }
        }

        return new Point(
                bedPos.x + snapOffsetX + extraX,
                bedPos.y + snapOffsetY + extraY
        );
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