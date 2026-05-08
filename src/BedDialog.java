import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BedDialog extends JDialog {

    // Match the "bed area" in PetPanel (baseY = 80)
    public static final int BED_WIDTH  = Main.PET_WIDTH;
    public static final int BED_HEIGHT = Main.PET_HEIGHT - 80;

    private final BedPanel panel;

    public BedDialog() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        // overlay sticker behavior
        setType(Window.Type.POPUP);
        setAlwaysOnTop(true);
        setFocusableWindowState(false);
        setAutoRequestFocus(false);

        AWTUtilities_setWindowOpaque(this);

        panel = new BedPanel();
        add(panel);
        pack();
    }

    public void setBedSprite(BufferedImage img) {
        panel.setImage(img);
    }

    /** Call AFTER setVisible(true) to bump above taskbar/topmost layers. */
    public void bumpTopmost() {
        try {
            setAlwaysOnTop(false);
            setAlwaysOnTop(true);
        } catch (Exception ignored) {}
        toFront();
        repaint();
    }

    public void positionAtBottom() {
        setLocation(getBedScreenPosition());
    }

    public static Point getBedScreenPosition() {
        PetStats s = SaveManager.load();
        if (s != null && s.hasBedPos()) {
            return new Point(s.getBedX(), s.getBedY());
        }

        // bottom-right of FULL screen (can overlap taskbar)
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        Rectangle screen = gd.getDefaultConfiguration().getBounds();

        int x = screen.x + screen.width  - BED_WIDTH;
        int y = screen.y + screen.height - BED_HEIGHT;
        return new Point(x, y);
    }

    /** Pet snaps relative to bed. */
    public static Point getCatSnapPosition() {
        Point bedPos = getBedScreenPosition();

        int snapX = bedPos.x; // since BED_WIDTH == PET_WIDTH
        int snapY = bedPos.y - Main.PET_HEIGHT + BED_HEIGHT; // aligns bed-area in pet window

        return new Point(snapX, snapY);
    }

    // ─────────────────────────────────────────────────────────────

    private static class BedPanel extends JPanel {
        private BufferedImage bedImage;

        BedPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(BED_WIDTH, BED_HEIGHT));
            bedImage = loadImage("dingus - Copy/orangebed.png");
        }

        void setImage(BufferedImage img) {
            bedImage = (img != null) ? img : loadImage("dingus - Copy/orangebed.png");
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bedImage == null) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            // NO STRETCH: draw contain (aspect preserved)
            drawContain(g2, bedImage, new Rectangle(0, 0, getWidth(), getHeight()));

            g2.dispose();
        }

        private static BufferedImage loadImage(String path) {
            try { return ImageIO.read(new File(path)); }
            catch (IOException e) { return null; }
        }
    }

    private static void drawContain(Graphics2D g2, BufferedImage img, Rectangle box) {
        int iw = img.getWidth();
        int ih = img.getHeight();

        double sx = box.width  / (double) iw;
        double sy = box.height / (double) ih;
        double s = Math.min(sx, sy);

        int dw = (int) Math.round(iw * s);
        int dh = (int) Math.round(ih * s);

        int dx = box.x + (box.width  - dw) / 2;
        int dy = box.y + (box.height - dh) / 2;

        g2.drawImage(img, dx, dy, dw, dh, null);
    }

    private static void AWTUtilities_setWindowOpaque(Window w) {
        try {
            Class<?> util = Class.forName("com.sun.awt.AWTUtilities");
            util.getMethod("setWindowOpaque", Window.class, boolean.class).invoke(null, w, false);
        } catch (Exception ignored) {}
    }
}