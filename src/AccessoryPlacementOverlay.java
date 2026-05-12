import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class AccessoryPlacementOverlay extends JDialog {

    public enum Pose { SITTING, BED, DRAG }

    private final String glyph;
    private final Color fill;
    private final Color outline;
    private final Pose pose;
    private final int glyphSizePx;

    // Provided by PetPanel (window coords)
    private final Rectangle spriteBox;

    private Point hover = null;   // overlay coords
    private Point pinned = null;  // overlay coords

    private Double outXFrac = null;
    private Double outYFrac = null;

    private final JButton confirmBtn = makeMenuStyleButton("Confirm");
    private final JButton cancelBtn  = makeMenuStyleButton("Cancel");

    private AccessoryPlacementOverlay(
            Window ownerDialog,
            Window targetPetWindow,
            String glyph,
            Color fill,
            Color outline,
            Pose pose,
            int glyphSizePx,
            Rectangle spriteBox
    ) {
        super(ownerDialog);
        this.glyph = glyph;
        this.fill = fill;
        this.outline = outline;
        this.pose = pose;
        this.glyphSizePx = glyphSizePx;
        this.spriteBox = spriteBox;

        setUndecorated(true);
        setModal(true);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));

        // Overlay exactly over the PET window bounds
        setBounds(targetPetWindow.getBounds());

        JPanel root = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // instruction card
                int boxW = Math.min(560, getWidth() - 16);
                int boxH = 64;
                int boxX = 8;
                int boxY = 8;

                g2.setColor(new Color(Theme.BG_MAIN.getRed(), Theme.BG_MAIN.getGreen(), Theme.BG_MAIN.getBlue(), 235));
                g2.fillRoundRect(boxX, boxY, boxW, boxH, 10, 10);

                g2.setColor(Theme.BG_INPUT_BORDER);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(boxX, boxY, boxW, boxH, 10, 10);

                g2.setColor(Theme.TEXT_PRIMARY);
                g2.setFont(Theme.font(Theme.FONT_SIZE_BODY));
                g2.drawString("Place hat (" + pose.name().toLowerCase() + "): click to pin, then Confirm.",
                        boxX + 12, boxY + 28);

                g2.setFont(Theme.font(Theme.FONT_SIZE_SMALL));
                g2.drawString("ESC cancels. Click directly on the sprite.",
                        boxX + 12, boxY + 48);

                // outline spriteBox (so you can see the saved coordinate area)
                g2.setColor(new Color(Theme.BG_INPUT_BORDER.getRed(), Theme.BG_INPUT_BORDER.getGreen(), Theme.BG_INPUT_BORDER.getBlue(), 120));
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(spriteBox.x, spriteBox.y, spriteBox.width - 1, spriteBox.height - 1);

                // hat preview
                Point p = (pinned != null) ? pinned : hover;
                if (p != null) {
                    Point clamped = clampPointToRect(p, spriteBox);
                    drawOutlinedGlyph(g2, glyph, clamped.x, clamped.y, glyphSizePx, fill, outline);
                }

                g2.dispose();
            }
        };
        root.setOpaque(false);
        setContentPane(root);

        confirmBtn.setVisible(false);
        cancelBtn.setVisible(false);

        confirmBtn.setSize(110, 32);
        cancelBtn.setSize(110, 32);
        confirmBtn.setLocation(8, 8 + 64 + 8);
        cancelBtn.setLocation(8 + 110 + 10, 8 + 64 + 8);

        root.add(confirmBtn);
        root.add(cancelBtn);

        root.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                hover = e.getPoint();
                root.repaint();
            }
            @Override public void mouseDragged(java.awt.event.MouseEvent e) {
                mouseMoved(e);
            }
        });

        root.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                pinned = e.getPoint();
                confirmBtn.setVisible(true);
                cancelBtn.setVisible(true);
                root.repaint();
            }
        });

        confirmBtn.addActionListener(e -> {
            Point clamped = clampPointToRect(pinned, spriteBox);

            outXFrac = (clamped.x - spriteBox.x) / (double) spriteBox.width;
            outYFrac = (clamped.y - spriteBox.y) / (double) spriteBox.height;

            dispose();
        });

        cancelBtn.addActionListener(e -> {
            outXFrac = null;
            outYFrac = null;
            dispose();
        });

        // ESC to cancel
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "esc");
        root.getActionMap().put("esc", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                outXFrac = null;
                outYFrac = null;
                dispose();
            }
        });
    }

    private JButton makeMenuStyleButton(String text) {
        int btnHeight = 22;
        int totalHeight = 32;

        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color bg = getBackground();
                if (!isEnabled())                 bg = Theme.BTN_DISABLED;
                else if (getModel().isPressed())  bg = Theme.BTN_PRESSED;
                else if (getModel().isRollover()) bg = Theme.BTN_HOVER;

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), btnHeight,
                        Theme.BUTTON_CORNER_RADIUS, Theme.BUTTON_CORNER_RADIUS + 1);

                g2.setColor(Theme.BTN_ACCENT);
                g2.drawRoundRect(0, 0, getWidth() - 1, btnHeight - 1,
                        Theme.BUTTON_CORNER_RADIUS, Theme.BUTTON_CORNER_RADIUS + 1);

                g2.setColor(isEnabled() ? Theme.TEXT_PRIMARY : Theme.TEXT_DISABLED);

                int textWidth = Theme.mixedStringWidth(g2, getText(), Theme.FONT_SIZE_BUTTON);
                g2.setFont(Theme.font(Theme.FONT_SIZE_BUTTON));
                FontMetrics fm = g2.getFontMetrics();

                int x = (getWidth() - textWidth) / 2;
                int y = (btnHeight + fm.getAscent() - fm.getDescent()) / 2;

                Theme.drawMixedString(g2, getText(), x, y, Theme.FONT_SIZE_BUTTON);

                g2.dispose();
            }
        };

        btn.setBackground(Theme.BTN_DEFAULT);
        btn.setForeground(Theme.TEXT_PRIMARY);
        btn.setPreferredSize(new Dimension(110, totalHeight));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setRolloverEnabled(true);
        return btn;
    }

    private static Point clampPointToRect(Point p, Rectangle r) {
        int maxX = r.x + r.width - 1;
        int maxY = r.y + r.height - 1;
        int x = Math.max(r.x, Math.min(maxX, p.x));
        int y = Math.max(r.y, Math.min(maxY, p.y));
        return new Point(x, y);
    }

    private static void drawOutlinedGlyph(Graphics2D g2, String glyph, int cx, int cy, int size,
                                          Color fill, Color outline) {
        Font symbol = new Font("Segoe UI Symbol", Font.PLAIN, size);
        Font emoji  = Theme.emojiFont(size);
        Font f = (symbol.canDisplayUpTo(glyph) == -1) ? symbol : emoji;

        g2.setFont(f);
        FontMetrics fm = g2.getFontMetrics();
        int x = cx - fm.stringWidth(glyph) / 2;
        int y = cy + (fm.getAscent() - fm.getDescent()) / 2;

        g2.setColor(outline);
        for (int ox = -1; ox <= 1; ox++) {
            for (int oy = -1; oy <= 1; oy++) {
                if (ox == 0 && oy == 0) continue;
                g2.drawString(glyph, x + ox, y + oy);
            }
        }

        g2.setColor(fill);
        g2.drawString(glyph, x, y);
    }

    /** Returns {xFrac, yFrac} or null if cancelled. */
    public static double[] pickOnPetWindow(
            Window ownerDialog,
            Window targetPetWindow,
            String glyph,
            Color fill,
            Color outline,
            Pose pose,
            int glyphSizePx,
            Rectangle spriteBoxInWindowCoords
    ) {
        AccessoryPlacementOverlay o = new AccessoryPlacementOverlay(
                ownerDialog, targetPetWindow, glyph, fill, outline, pose, glyphSizePx, spriteBoxInWindowCoords
        );
        o.setVisible(true);
        if (o.outXFrac == null || o.outYFrac == null) return null;
        return new double[]{ o.outXFrac, o.outYFrac };
    }
}