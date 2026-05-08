import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class BedPlacementOverlay extends JDialog {

    private final Rectangle screenBounds;
    private final BufferedImage bedImg;
    private final int bedW, bedH;

    private Point hoverScreen = null;      // raw mouse point (screen coords)
    private Point chosenTopLeft = null;    // finalized top-left (screen coords) after confirm

    private Rectangle previewRect = null;  // top-left derived from hover (screen coords)
    private Rectangle pinnedRect = null;   // rect when user clicks (screen coords)

    private final JButton confirmBtn = new JButton("Confirm");
    private final JButton cancelBtn  = new JButton("Cancel");

    private Point candidateTopLeft = null;

    private BedPlacementOverlay(Window owner, BufferedImage bedImg, int bedW, int bedH) {
        super(owner);
        this.screenBounds = getAllScreenBounds();   // full bounds incl. taskbar
        this.bedImg = bedImg;
        this.bedW = bedW;
        this.bedH = bedH;

        setUndecorated(true);
        setModal(true);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));
        setBounds(screenBounds);

        JPanel root = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();

                // IMPORTANT: do NOT darken the whole screen (user wants to see desktop).
                // We'll only draw a small instruction panel + bed ghost.

                // Instruction panel
                int boxW = Math.min(560, getWidth() - 30);
                int boxH = 92;
                int boxX = 12;
                int boxY = 12;

                g2.setColor(new Color(Theme.BG_MAIN.getRed(), Theme.BG_MAIN.getGreen(), Theme.BG_MAIN.getBlue(), 240));
                g2.fillRoundRect(boxX, boxY, boxW, boxH, 10, 10);

                g2.setColor(Theme.BG_INPUT_BORDER);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(boxX, boxY, boxW, boxH, 10, 10);

                g2.setColor(Theme.TEXT_PRIMARY);
                g2.setFont(Theme.font(Theme.FONT_SIZE_BODY));
                g2.drawString("Place your bed:", boxX + 12, boxY + 28);

                g2.setFont(Theme.font(Theme.FONT_SIZE_SMALL));
                if (pinnedRect == null) {
                    g2.drawString("Move mouse → preview follows. Click to pin. ESC cancels.", boxX + 12, boxY + 50);
                    g2.drawString("The pet will spawn relative to the bed automatically.", boxX + 12, boxY + 70);
                } else {
                    g2.drawString("Pinned. Click Confirm to save (or click elsewhere to reposition).", boxX + 12, boxY + 50);
                    g2.drawString("ESC cancels.", boxX + 12, boxY + 70);
                }

                // Draw preview bed
                Rectangle r = (pinnedRect != null) ? pinnedRect : previewRect;
                if (r != null) {
                    // ghost image
                    Composite old = g2.getComposite();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pinnedRect != null ? 0.95f : 0.60f));

                    if (bedImg != null) {
                        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                        g2.drawImage(bedImg, r.x - screenBounds.x, r.y - screenBounds.y, r.width, r.height, null);
                    } else {
                        // fallback rectangle if image missing
                        g2.setColor(new Color(Theme.BG_INPUT.getRed(), Theme.BG_INPUT.getGreen(), Theme.BG_INPUT.getBlue(), 220));
                        g2.fillRect(r.x - screenBounds.x, r.y - screenBounds.y, r.width, r.height);
                    }

                    g2.setComposite(old);

                    // ink outline around preview
                    g2.setColor(Theme.BG_INPUT_BORDER);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRect(r.x - screenBounds.x, r.y - screenBounds.y, r.width - 1, r.height - 1);
                }

                g2.dispose();
            }
        };
        root.setOpaque(false);
        setContentPane(root);

        // Buttons (only visible after pinning)
        confirmBtn.setVisible(false);
        cancelBtn.setVisible(false);

        styleOverlayButton(confirmBtn);
        styleOverlayButton(cancelBtn);

        confirmBtn.setSize(110, 32);
        cancelBtn.setSize(110, 32);

        root.add(confirmBtn);
        root.add(cancelBtn);

        // Layout buttons bottom-left-ish (but not covering bed)
        root.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                int y = 12 + 92 + 10;
                confirmBtn.setLocation(12, y);
                cancelBtn.setLocation(12 + 110 + 10, y);
            }
        });

        // Mouse move: update preview
        root.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                hoverScreen = e.getLocationOnScreen();
                previewRect = computeRectCentered(hoverScreen);
                repaint();
            }
            @Override public void mouseDragged(java.awt.event.MouseEvent e) {
                mouseMoved(e);
            }
        });

        // Click: pin or reposition
        root.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                hoverScreen = e.getLocationOnScreen();
                Rectangle r = computeRectCentered(hoverScreen);
                pinnedRect = r;
                candidateTopLeft = new Point(r.x, r.y);

                confirmBtn.setVisible(true);
                cancelBtn.setVisible(true);
                repaint();
            }
        });

        confirmBtn.addActionListener(e -> {
            if (candidateTopLeft != null) {
                chosenTopLeft = new Point(candidateTopLeft);
            }
            dispose();
        });

        cancelBtn.addActionListener(e -> {
            chosenTopLeft = null;
            dispose();
        });

        // ESC to cancel
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "esc");
        root.getActionMap().put("esc", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                chosenTopLeft = null;
                dispose();
            }
        });
    }

    private static Rectangle getAllScreenBounds() {
        Rectangle all = new Rectangle();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice gd : ge.getScreenDevices()) {
            all = all.union(gd.getDefaultConfiguration().getBounds());
        }
        return all;
    }

    private void styleOverlayButton(JButton b) {
        b.setBorderPainted(true);
        b.setContentAreaFilled(true);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(Theme.font(Theme.FONT_SIZE_BUTTON));
        b.setForeground(Theme.TEXT_PRIMARY);

        b.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        b.setOpaque(false);

        b.setBackground(Theme.BTN_DEFAULT);

        b.setBorder(BorderFactory.createEmptyBorder());
        b.setRolloverEnabled(true);

        b = b; // (no-op; keeps style intent obvious)
    }

    private Rectangle computeRectCentered(Point mouseScreen) {
        // Convert “mouse position” -> bed top-left, centered on cursor
        int x = mouseScreen.x - bedW / 2;
        int y = mouseScreen.y - bedH / 2;

        // Clamp into usable area so it never ends up off-screen
        x = Math.max(screenBounds.x, Math.min(screenBounds.x + screenBounds.width - bedW, x));
        y = Math.max(screenBounds.y, Math.min(screenBounds.y + screenBounds.height - bedH, y));

        return new Rectangle(x, y, bedW, bedH);
    }

    public static Point pickBedTopLeft(Window owner, BufferedImage bedPreview, int bedW, int bedH) {
        BedPlacementOverlay o = new BedPlacementOverlay(owner, bedPreview, bedW, bedH);
        o.setVisible(true);
        return o.chosenTopLeft; // null if cancelled
    }
}