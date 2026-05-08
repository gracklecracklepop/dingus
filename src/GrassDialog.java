import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GrassDialog extends JDialog {

    private static final List<GrassDialog> OPEN = new CopyOnWriteArrayList<>();

    // Must match Theme.paintMacWindow() traffic light geometry
    private static final int TL_X0  = 12;
    private static final int TL_Y0  = 9;
    private static final int TL_DOT = 10;
    private static final int TL_GAP = 6;

    private final BufferedImage img;
    private boolean hidden = false;

    public GrassDialog(Window owner, String url) throws Exception {
        super(owner);

        setModal(false);
        setAlwaysOnTop(false);          // never above Dingus
        setFocusableWindowState(false); // don't take focus
        setAutoRequestFocus(false);     // don't request focus when shown
        setUndecorated(true);
        setType(Type.UTILITY);
        setBackground(new Color(0, 0, 0, 0));

        img = ImageIO.read(new URL(url));

        int w = 380, h = 260;
        setSize(w, h);
        setLocation(centerSpawnPoint(w, h));

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.paintMacWindow(g2, getWidth(), getHeight(), "GRASS");
                g2.dispose();
            }
        };
        root.setOpaque(false);

        // titlebar overlay: hitboxes + drag region
        root.add(buildTitlebarOverlay(), BorderLayout.NORTH);

        // image content area
        root.add(new ImagePane(), BorderLayout.CENTER);

        setContentPane(root);

        OPEN.add(this);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { OPEN.remove(GrassDialog.this); }
            @Override public void windowClosing(java.awt.event.WindowEvent e) { OPEN.remove(GrassDialog.this); }
        });
    }

    /** Open one grass window near the center of the screen. */
    public static void openRandom(Window owner, String[] urls) {
        try {
            String url = urls[(int)(Math.random() * urls.length)];
            GrassDialog d = new GrassDialog(owner, url);

            d.setAlwaysOnTop(false);
            d.setVisible(true);

            // Force grass behind
            d.toBack();

            // Force Dingus above
            if (owner != null) {
                owner.setAlwaysOnTop(true);
                owner.toFront();
                owner.repaint();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * If pet bounds intersect any open grass dialog, close ONE and return true.
     * Called from PetMenu.checkGrassDrop(...) on drag release.
     */
    public static boolean consumeIfIntersect(Rectangle petBoundsOnScreen) {
        for (GrassDialog d : OPEN) {
            if (!d.isShowing()) continue;
            if (d.hidden) continue;

            Rectangle r = d.getBounds(); // screen coords
            if (r.intersects(petBoundsOnScreen)) {
                SwingUtilities.invokeLater(d::dispose);
                return true;
            }
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────────
    // UI parts
    // ─────────────────────────────────────────────────────────────

    private JLayeredPane buildTitlebarOverlay() {
        JLayeredPane layer = new JLayeredPane() {
            @Override public Dimension getPreferredSize() {
                return new Dimension(10, Theme.TITLEBAR_HEIGHT);
            }

            @Override public void doLayout() {
                Component red = find("tl_red");
                Component org = find("tl_orange");
                Component grn = find("tl_green");

                if (red != null) red.setBounds(TL_X0, TL_Y0, TL_DOT, TL_DOT);
                if (org != null) org.setBounds(TL_X0 + (TL_DOT + TL_GAP), TL_Y0, TL_DOT, TL_DOT);
                if (grn != null) grn.setBounds(TL_X0 + 2 * (TL_DOT + TL_GAP), TL_Y0, TL_DOT, TL_DOT);
            }

            private Component find(String name) {
                for (Component c : getComponents()) {
                    if (name.equals(c.getName())) return c;
                }
                return null;
            }
        };
        layer.setOpaque(false);

        JButton red = makeTrafficHitbox(this::dispose, "Close");
        red.setName("tl_red");

        JButton orange = makeTrafficHitbox(this::toggleHidden, "Hide");
        orange.setName("tl_orange");

        JButton green = makeTrafficHitbox(null, null);
        green.setName("tl_green");
        green.setEnabled(false);

        layer.add(red, Integer.valueOf(2));
        layer.add(orange, Integer.valueOf(2));
        layer.add(green, Integer.valueOf(2));

        // drag the window by the titlebar
        Point[] offset = {null};
        layer.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                offset[0] = e.getPoint();
            }
        });
        layer.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseDragged(java.awt.event.MouseEvent e) {
                if (offset[0] == null) return;
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - offset[0].x,
                        loc.y + e.getY() - offset[0].y);
            }
        });

        return layer;
    }

    private JButton makeTrafficHitbox(Runnable action, String tooltip) {
        JButton b = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                // No paint: Theme.paintMacWindow draws the circles
            }
        };
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (tooltip != null) b.setToolTipText(tooltip);
        if (action != null) b.addActionListener(e -> action.run());
        return b;
    }

    private void toggleHidden() {
        hidden = !hidden;
        // “Hide” behavior: collapse to a tiny strip under the titlebar
        if (hidden) {
            setSize(getWidth(), Theme.TITLEBAR_HEIGHT + 6);
        } else {
            setSize(380, 260);
        }
        repaint();
    }

    private class ImagePane extends JComponent {
        @Override public Dimension getPreferredSize() {
            return new Dimension(380, 260 - Theme.TITLEBAR_HEIGHT);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            int pad = 12;
            int top = Theme.TITLEBAR_HEIGHT + 8;

            // content bounds inside the window frame
            Rectangle box = new Rectangle(
                    pad,
                    0, // we are already inside CENTER (below titlebar), so just pad within this component
                    getWidth() - pad * 2,
                    getHeight() - pad
            );

            // background paper
            g2.setColor(Theme.BG_MAIN);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // draw the image inside the content box, aspect-fit (contain)
            drawImageContain(g2, img, box);

            // ink border around image area
            g2.setColor(Theme.BG_INPUT_BORDER);
            ((Graphics2D) g2).setStroke(new BasicStroke(2));
            g2.drawRect(box.x, box.y, box.width - 1, box.height - 1);

            g2.dispose();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    private static Point centerSpawnPoint(int w, int h) {
        Rectangle usable = Theme.getUsableScreen();
        int cx = usable.x + (usable.width  - w) / 2;
        int cy = usable.y + (usable.height - h) / 2;

        // small jitter so multiple opens are not identical
        int jx = (int) (Math.random() * 120) - 60;
        int jy = (int) (Math.random() * 120) - 60;

        return new Point(cx + jx, cy + jy);
    }

    private static void drawImageContain(Graphics2D g2, BufferedImage img, Rectangle box) {
        if (img == null) return;

        double sx = box.width  / (double) img.getWidth();
        double sy = box.height / (double) img.getHeight();
        double s = Math.min(sx, sy);

        int dw = (int) Math.round(img.getWidth() * s);
        int dh = (int) Math.round(img.getHeight() * s);

        int dx = box.x + (box.width  - dw) / 2;
        int dy = box.y + (box.height - dh) / 2;

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(img, dx, dy, dw, dh, null);
    }
}