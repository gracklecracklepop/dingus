import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GrassDialog extends JDialog {

    private static final List<GrassDialog> OPEN = new CopyOnWriteArrayList<>();

    // Must match Theme.paintMacWindow()
    private static final int TL_X0  = 12;
    private static final int TL_Y0  = 9;
    private static final int TL_DOT = 10;
    private static final int TL_GAP = 6;

    private final BufferedImage img;
    private final Runnable onClosed;
    private boolean closedCallbackFired = false;

    public GrassDialog(Window owner, String url, Runnable onClosed) throws Exception {
        super(owner);
        this.onClosed = onClosed;

        setModal(false);
        setUndecorated(true);
        setType(Type.UTILITY);
        setBackground(new Color(0, 0, 0, 0));

        // Keep tabs behind Dingus
        setAlwaysOnTop(false);
        setFocusableWindowState(false);
        setAutoRequestFocus(false);

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

        root.add(buildTitlebarOverlay(), BorderLayout.NORTH);
        root.add(new ImagePane(), BorderLayout.CENTER);

        setContentPane(root);

        OPEN.add(this);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { cleanupAndCallback(); }
            @Override public void windowClosing(java.awt.event.WindowEvent e) { cleanupAndCallback(); }
        });
    }

    private void cleanupAndCallback() {
        OPEN.remove(this);
        if (!closedCallbackFired) {
            closedCallbackFired = true;
            if (onClosed != null) SwingUtilities.invokeLater(onClosed);
        }
    }

    public static GrassDialog findIntersecting(Rectangle screenRect) {
        for (GrassDialog d : OPEN) {
            if (!d.isShowing()) continue;
            if (d.getBounds().intersects(screenRect)) return d;
        }
        return null;
    }

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
                for (Component c : getComponents()) if (name.equals(c.getName())) return c;
                return null;
            }
        };
        layer.setOpaque(false);

        JButton red = makeTrafficHitbox(this::dispose, "Close");
        red.setName("tl_red");

        JButton orange = makeTrafficHitbox(this::dispose, "Back");
        orange.setName("tl_orange");

        JButton green = makeTrafficHitbox(null, null);
        green.setName("tl_green");
        green.setEnabled(false);

        layer.add(red, Integer.valueOf(2));
        layer.add(orange, Integer.valueOf(2));
        layer.add(green, Integer.valueOf(2));

        // draggable titlebar
        Point[] offset = {null};
        layer.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) { offset[0] = e.getPoint(); }
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
            @Override protected void paintComponent(Graphics g) { }
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

    private class ImagePane extends JComponent {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Theme.BG_MAIN);
            g2.fillRect(0, 0, getWidth(), getHeight());

            int pad = 12;
            Rectangle box = new Rectangle(pad, pad, getWidth() - pad * 2, getHeight() - pad * 2);
            drawContain(g2, img, box);

            g2.setColor(Theme.BG_INPUT_BORDER);
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(box.x, box.y, box.width - 1, box.height - 1);

            g2.dispose();
        }
    }

    private static Point centerSpawnPoint(int w, int h) {
        Rectangle usable = Theme.getUsableScreen();
        int cx = usable.x + (usable.width  - w) / 2;
        int cy = usable.y + (usable.height - h) / 2;

        int jx = (int) (Math.random() * 140) - 70;
        int jy = (int) (Math.random() * 140) - 70;

        return new Point(cx + jx, cy + jy);
    }

    private static void drawContain(Graphics2D g2, BufferedImage img, Rectangle box) {
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