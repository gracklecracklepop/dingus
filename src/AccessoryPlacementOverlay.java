import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;

public class AccessoryPlacementOverlay extends JDialog {

    public enum Pose { SITTING, BED, DRAG }

    // Must match PetPanel layout
    private static final int BASE_Y = 80;
    private static final int DRAG_EXTRA_Y = 100;

    private final Window targetPetWindow;
    private final String glyph;
    private final Color fill;
    private final Color outline;
    private final Pose pose;

    private final BufferedImage poseSprite;

    private Point hover = null;   // overlay coords
    private Point pinned = null;  // overlay coords

    private Double outXFrac = null;
    private Double outYFrac = null;

    private final JButton confirmBtn = new JButton("Confirm");
    private final JButton cancelBtn  = new JButton("Cancel");

    private AccessoryPlacementOverlay(Window ownerDialog,
                                      Window targetPetWindow,
                                      String spriteColor,
                                      String glyph,
                                      Color fill,
                                      Color outline,
                                      Pose pose) {
        super(ownerDialog);
        this.targetPetWindow = targetPetWindow;
        this.glyph = glyph;
        this.fill = fill;
        this.outline = outline;
        this.pose = pose;

        this.poseSprite = loadPoseSprite(spriteColor, pose);

        setUndecorated(true);
        setModal(true);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));

        // Overlay exactly over the PET window
        setBounds(targetPetWindow.getBounds());

        JPanel root = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // instruction card (no blackout)
                int boxW = Math.min(520, getWidth() - 16);
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
                g2.drawString("Place hat (" + pose.name().toLowerCase() + "): click to pin, then Confirm.", boxX + 12, boxY + 28);

                g2.setFont(Theme.font(Theme.FONT_SIZE_SMALL));
                g2.drawString("ESC cancels. Position saves to stats.", boxX + 12, boxY + 48);

                // sprite box (fractions measured inside this)
                Rectangle spriteBox = getSpriteBox();

                // draw the POSE sprite so placement matches dropdown selection
                if (poseSprite != null) {
                    drawContain(g2, poseSprite, spriteBox);
                }

                // outline sprite box
                g2.setColor(new Color(Theme.BG_INPUT_BORDER.getRed(), Theme.BG_INPUT_BORDER.getGreen(), Theme.BG_INPUT_BORDER.getBlue(), 120));
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(spriteBox.x, spriteBox.y, spriteBox.width - 1, spriteBox.height - 1);

                // hat preview (hover or pinned)
                Point p = (pinned != null) ? pinned : hover;
                if (p != null) {
                    Point clamped = clampPointToRect(p, spriteBox);
                    drawOutlinedGlyph(g2, glyph, clamped.x, clamped.y, 28, fill, outline);
                }

                g2.dispose();
            }
        };
        root.setOpaque(false);
        setContentPane(root);

        confirmBtn.setVisible(false);
        cancelBtn.setVisible(false);

        styleBtn(confirmBtn);
        styleBtn(cancelBtn);

        confirmBtn.setSize(110, 32);
        cancelBtn.setSize(110, 32);
        confirmBtn.setLocation(8, 8 + 64 + 8);
        cancelBtn.setLocation(8 + 110 + 10, 8 + 64 + 8);

        root.add(confirmBtn);
        root.add(cancelBtn);

        root.addMouseMotionListener(new MouseMotionAdapter() {
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
            Rectangle spriteBox = getSpriteBox();
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

    private void styleBtn(JButton b) {
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(Theme.font(Theme.FONT_SIZE_BUTTON));
        b.setForeground(Theme.TEXT_PRIMARY);
        b.setBackground(Theme.BTN_DEFAULT);
    }

    private Rectangle getSpriteBox() {
        int y = BASE_Y;
        if (pose == Pose.DRAG) y += DRAG_EXTRA_Y;
        return new Rectangle(0, y, getWidth(), BedDialog.BED_HEIGHT);
    }

    private static Point clampPointToRect(Point p, Rectangle r) {
        int x = Math.max(r.x, Math.min(r.x + r.width, p.x));
        int y = Math.max(r.y, Math.min(r.y + r.height, p.y));
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

    private static void drawContain(Graphics2D g2, BufferedImage img, Rectangle box) {
        double sx = box.width  / (double) img.getWidth();
        double sy = box.height / (double) img.getHeight();
        double s = Math.min(sx, sy);

        int dw = (int) Math.round(img.getWidth() * s);
        int dh = (int) Math.round(img.getHeight() * s);

        int dx = box.x + (box.width - dw) / 2;
        int dy = box.y + (box.height - dh) / 2;

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(img, dx, dy, dw, dh, null);
    }

    private static BufferedImage loadPoseSprite(String spriteColor, Pose pose) {
        if (spriteColor == null) spriteColor = "Default (Orange)";
        String path;

        switch (spriteColor) {
            case "Void (Black)" -> path = switch (pose) {
                case SITTING -> "dingus - Copy/blacksitting.png";
                case BED     -> "dingus - Copy/blackinbed.png";
                case DRAG    -> "dingus - Copy/blackscruff.png";
            };
            case "Ghost (White)" -> path = switch (pose) {
                case SITTING -> "dingus - Copy/whitesitting.png";
                case BED     -> "dingus - Copy/whiteinbed.png";
                case DRAG    -> "dingus - Copy/whitescruff.png";
            };
            default -> path = switch (pose) {
                case SITTING -> "dingus - Copy/orangesitting.png";
                case BED     -> "dingus - Copy/orangeinbed.png";
                case DRAG    -> "dingus - Copy/orangescruff.png";
            };
        }

        try { return ImageIO.read(new File(path)); }
        catch (Exception e) { return null; }
    }

    /** Returns {xFrac, yFrac} or null if cancelled. */
    public static double[] pick(Window ownerDialog,
                                Window targetPetWindow,
                                String spriteColor,
                                String glyph,
                                Color fill,
                                Color outline,
                                Pose pose) {
        AccessoryPlacementOverlay o =
                new AccessoryPlacementOverlay(ownerDialog, targetPetWindow, spriteColor, glyph, fill, outline, pose);
        o.setVisible(true);
        if (o.outXFrac == null || o.outYFrac == null) return null;
        return new double[]{ o.outXFrac, o.outYFrac };
    }
}