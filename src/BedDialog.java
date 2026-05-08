import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BedDialog extends JDialog {

    static Toolkit toolkit = Toolkit.getDefaultToolkit();
    static Dimension screenSize = toolkit.getScreenSize();

    public static int imgW = (int) (screenSize.getWidth()  / 5);
    public static int imgH = (int) (screenSize.getHeight() / 5);

    static final int BED_WIDTH  = imgW;
    static final int BED_HEIGHT = imgH;

    private final BedPanel panel;

    public BedDialog() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setFocusable(false);
        setFocusableWindowState(false);

        AWTUtilities_setWindowOpaque(this);

        panel = new BedPanel();
        add(panel);
        pack();
    }

    public void setBedSprite(BufferedImage img) {
        panel.setImage(img);
    }

    public void positionAtBottom() {
        setLocation(getBedScreenPosition());
    }

    public static Point getBedScreenPosition() {
        PetStats s = SaveManager.load();
        if (s != null && s.hasBedPos()) {
            return new Point(s.getBedX(), s.getBedY());
        }

        // fallback: old bottom-right placement
        Rectangle usable = Theme.getUsableScreen();
        int x = usable.x + usable.width  - BED_WIDTH;
        int y = usable.y + usable.height - BED_HEIGHT;
        return new Point(x, y);
    }

    /** Pet spawns relative to bed (centered). */
    public static Point getCatSnapPosition() {
        Point bedPos = getBedScreenPosition();

        int snapX = bedPos.x + (BED_WIDTH - Main.PET_WIDTH) / 2;
        int snapY = bedPos.y - Main.PET_HEIGHT + BED_HEIGHT;

        // small tweak constants if you want later
        int tweakX = 0;
        int tweakY = 0;

        return new Point(snapX + tweakX, snapY + tweakY);
    }

    // ─────────────────────────────────────────────────────────────

    private class BedPanel extends JPanel {
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
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(bedImage, 0, 0, getWidth(), getHeight(), null);
            g2.dispose();
        }
    }

    private static BufferedImage loadImage(String path) {
        try { return ImageIO.read(new File(path)); }
        catch (IOException e) { return null; }
    }

    private static void AWTUtilities_setWindowOpaque(Window w) {
        try {
            Class<?> util = Class.forName("com.sun.awt.AWTUtilities");
            util.getMethod("setWindowOpaque", Window.class, boolean.class).invoke(null, w, false);
        } catch (Exception ignored) {}
    }
}